package ru.strelchm.scheduler_perf.comparison;

import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import ru.strelchm.scheduler_perf.comparison.config.AppConfig;
import ru.strelchm.scheduler_perf.comparison.config.AppConfig.SchedulerType;
import ru.strelchm.scheduler_perf.comparison.metrics.MetricsServer;
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
import java.sql.Connection;
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

        runLiquibaseMigrations(dataSource, config.getSchedulerType());
        cleanJobs(config.getSchedulerType(), dataSource);
        SchedulerRunner runner = getSchedulerRunner(config, dataSource, meterRegistry, noopService);
        runner.initialize();
        insertJobs(config, dataSource, noopService, meterRegistry);
        runner.startBackgroundServer();

        new CountDownLatch(1).await();
    }

    private static void runLiquibaseMigrations(DataSource dataSource, SchedulerType schedulerType) throws LiquibaseException {
        String changeLog = switch (schedulerType) {
            case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> "db/changelog/db-scheduler/db.changelog-master.xml";
            case JOB_RUNR -> "db/changelog/jobrunr/db.changelog-master.xml";
        };

        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDatabaseChangeLogTableName(switch (schedulerType) {
                case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> "db_scheduler_change_log";
                case JOB_RUNR -> "jobrunr_change_log";
            });
            database.setDatabaseChangeLogLockTableName(switch (schedulerType) {
                case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> "db_scheduler_change_log_lock";
                case JOB_RUNR -> "jobrunr_change_log_lock";
            });

            try (Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update();
            }
        } catch (Exception e) {
            throw new LiquibaseException("Failed to apply database migrations for scheduler type " + schedulerType, e);
        }
    }

    private static void cleanJobs(SchedulerType schedulerType, DataSource dataSource) {
        DbCleaner dbCleaner = switch (schedulerType) {
            case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> new DbSchedulerCleaner(dataSource);
            case JOB_RUNR -> new JobrunrCleaner(dataSource);
        };
        dbCleaner.cleanOldJobs();
    }

    private static void insertJobs(AppConfig config, DataSource dataSource, NoOpService noopService, PrometheusMeterRegistry meterRegistry) {
        SchedulerType schedulerType = config.getSchedulerType();
        MassInserter massInserter = switch (schedulerType) {
            case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC -> new DbSchedulerMassInserter(
                    dataSource,
                    List.of(noOpTask(noopService)),
                    meterRegistry,
                    config.isMassInsertEnabled(),
                    config.getMassInsertCount(),
                    config.getSleepingJobsCount(),
                    config.getMassInsertBatchSize(),
                    config.getMassInsertDelayMs()

            );
            case JOB_RUNR -> new JobrunrMassInserter(
                    noopService,
                    meterRegistry,
                    config.isMassInsertEnabled(),
                    config.getMassInsertCount(),
                    config.getSleepingJobsCount(),
                    config.getMassInsertBatchSize(),
                    config.getMassInsertDelayMs()
            );
            case null, default -> throw new IllegalArgumentException("Unknown scheduler type: " + schedulerType);
        };

        massInserter.batchInsert();
        log.info("{} mass insert completed", schedulerType);
    }

    private static SchedulerRunner getSchedulerRunner(AppConfig config, DataSource dataSource, PrometheusMeterRegistry meterRegistry, NoOpService noopService) {
        return switch (config.getSchedulerType()) {
            case DB_SCHEDULLER, DB_SCHEDULLER_GENERIC ->
                    new DbSchedulerRunner(dataSource, meterRegistry, config, List.of(noOpTask(noopService)));
            case JOB_RUNR ->
                    new JobRunrRunner(dataSource, meterRegistry, config);
        };
    }

    private static OneTimeTask<NoOpDto> noOpTask(NoOpService noopService) {
        return Tasks.oneTime(TASK_NAME, NoOpDto.class)
                .execute((taskInstance, _) -> noopService.noop(taskInstance.getData()));
    }
}
