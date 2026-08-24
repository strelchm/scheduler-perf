package ru.strelchm.scheduler_perf.comparison.runner;

import io.micrometer.core.instrument.MeterRegistry;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.strelchm.scheduler_perf.comparison.AppConfig;
import ru.strelchm.scheduler_perf.core.service.NoOpService;

import javax.sql.DataSource;

public class JobRunrRunner implements SchedulerRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunrRunner.class);

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    private final AppConfig config;
    private final NoOpService noopService;

    public JobRunrRunner(DataSource dataSource, MeterRegistry meterRegistry,
                         AppConfig config, NoOpService noopService) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
        this.config = config;
        this.noopService = noopService;
    }

    @Override
    public void run() {
        log.info("Starting JobRunr with worker count: {}", config.getJobRunrWorkerCount());

        JobRunr.configure()
                .useStorageProvider(new PostgresStorageProvider(dataSource, "jobrunr"))
                .useBackgroundJobServer(config.getJobRunrWorkerCount())
                .initialize();
    }
}
