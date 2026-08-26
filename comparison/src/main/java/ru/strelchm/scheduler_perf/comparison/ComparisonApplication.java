package ru.strelchm.scheduler_perf.comparison;

import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import ru.strelchm.scheduler_perf.comparison.AppConfig.SchedulerType;
import ru.strelchm.scheduler_perf.comparison.runner.DbSchedulerRunner;
import ru.strelchm.scheduler_perf.comparison.runner.JobRunrRunner;
import ru.strelchm.scheduler_perf.comparison.runner.SchedulerRunner;
import ru.strelchm.scheduler_perf.core.DbCleaner;
import ru.strelchm.scheduler_perf.core.MassInserter;
import ru.strelchm.scheduler_perf.core.dbscheduler.DbSchedulerCleaner;
import ru.strelchm.scheduler_perf.core.dbscheduler.DbSchedulerMassInserter;
import ru.strelchm.scheduler_perf.core.jobrunr.JobrunrCleaner;
import ru.strelchm.scheduler_perf.core.jobrunr.JobrunrMassInserter;
import ru.strelchm.scheduler_perf.core.service.NoOpDto;
import ru.strelchm.scheduler_perf.core.service.NoOpService;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static ru.strelchm.scheduler_perf.core.dbscheduler.DbSchedulerMassInserter.TASK_NAME;

@Slf4j
public class ComparisonApplication {

    public static void main(String[] args) throws Exception {
        AppConfig config = new AppConfig();
        log.info("Starting comparison application with scheduler type: {} and config {}", config.getSchedulerType(), config);

        final DataSource dataSource = DataSourceFactory.createDataSource(
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword()
        );

        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        MetricsServer metricsServer = new MetricsServer(meterRegistry);
        metricsServer.start();

        NoOpService noopService = new NoOpService();

        cleanJobs(config.getSchedulerType(), dataSource);
        runScheduler(config, dataSource, meterRegistry, noopService);
        insertJobs(config, dataSource, noopService, meterRegistry);

        new CountDownLatch(1).await();
    }

    private static void cleanJobs(SchedulerType schedulerType, DataSource dataSource) {
        DbCleaner dbCleaner = switch (schedulerType) {
            case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> new DbSchedulerCleaner(dataSource);
            case JOB_RUNR -> new JobrunrCleaner(dataSource);
        };
        dbCleaner.cleanOldJobs();
    }

    private static void insertJobs(AppConfig config, DataSource dataSource, NoOpService noopService, PrometheusMeterRegistry meterRegistry) {
        MassInserter massInserter;
        SchedulerType schedulerType = config.getSchedulerType();
        if (schedulerType == SchedulerType.DB_SCHEDULLER) {
            massInserter = new DbSchedulerMassInserter(
                    dataSource,
                    List.of(noOpTask(noopService)),
                    meterRegistry,
                    config.isMassInsertEnabled(),
                    config.getMassInsertCount(),
                    config.getSleepingJobsCount(),
                    config.getMassInsertBatchSize(),
                    config.getMassInsertDelayMs()

            );
        } else if (schedulerType == SchedulerType.JOB_RUNR) {
            massInserter = new JobrunrMassInserter(
                    noopService,
                    meterRegistry,
                    config.isMassInsertEnabled(),
                    config.getMassInsertCount(),
                    config.getSleepingJobsCount(),
                    config.getMassInsertBatchSize(),
                    config.getMassInsertDelayMs()
            );
        } else {
            throw new IllegalArgumentException("Unknown scheduler type: " + schedulerType);
        }

        massInserter.batchInsert();
        log.info("{} mass insert completed", schedulerType);
    }

    private static void runScheduler(AppConfig config, DataSource dataSource, PrometheusMeterRegistry meterRegistry, NoOpService noopService) {
        SchedulerRunner schedulerRunner = switch (config.getSchedulerType()) {
            case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> new DbSchedulerRunner(dataSource, meterRegistry, config, List.of(noOpTask(noopService)));
            case JOB_RUNR -> new JobRunrRunner(dataSource, meterRegistry, config);
        };
        schedulerRunner.run();
    }

    private static OneTimeTask<NoOpDto> noOpTask(NoOpService noopService) {
        return Tasks.oneTime(TASK_NAME, NoOpDto.class)
                .execute((taskInstance, _) -> noopService.noop(taskInstance.getData()));
    }
}
