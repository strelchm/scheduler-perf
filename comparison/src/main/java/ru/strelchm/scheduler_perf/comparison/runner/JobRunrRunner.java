package ru.strelchm.scheduler_perf.comparison.runner;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrMicroMeterIntegration;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.configuration.BackgroundJobServerThreadType;
import org.jobrunr.server.configuration.DefaultBackgroundJobServerWorkerPolicy;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import ru.strelchm.scheduler_perf.comparison.AppConfig;
import ru.strelchm.scheduler_perf.core.jobrunr.MicrometerJobServerFilter;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class JobRunrRunner implements SchedulerRunner {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    private final AppConfig config;

    @Override
    public void run() {
        int jobrunrWorkerCount = config.getJobrunrWorkerCount();
        int pollIntervalInSeconds = config.getPollIntervalInSeconds();
        log.info("Starting JobRunr with worker count: {}", jobrunrWorkerCount);

        JobRunrMicroMeterIntegration jobRunrMicroMeterIntegration = new JobRunrMicroMeterIntegration(meterRegistry);
        PostgresStorageProvider storageProvider = new PostgresStorageProvider(dataSource, "");

        JobRunr.configure()
                .useStorageProvider(storageProvider)
                .useBackgroundJobServer(jobrunrWorkerCount)
                .useMetrics(jobRunrMicroMeterIntegration)
                .withJobFilter(new MicrometerJobServerFilter(meterRegistry))
                .useBackgroundJobServer(
                        BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration()
                                .andPollIntervalInSeconds(pollIntervalInSeconds)
                                .andBackgroundJobServerWorkerPolicy(new DefaultBackgroundJobServerWorkerPolicy(
                                        jobrunrWorkerCount,
                                        BackgroundJobServerThreadType.PlatformThreads
                                ))
                )
                .initialize();
    }
}
