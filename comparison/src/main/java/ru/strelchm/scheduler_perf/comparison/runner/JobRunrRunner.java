package ru.strelchm.scheduler_perf.comparison.runner;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.configuration.JobRunrMicroMeterIntegration;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.configuration.BackgroundJobServerThreadType;
import org.jobrunr.server.configuration.DefaultBackgroundJobServerWorkerPolicy;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import ru.strelchm.scheduler_perf.comparison.config.AppConfig;
import ru.strelchm.scheduler_perf.core.jobrunr.MicrometerJobServerFilter;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class JobRunrRunner implements SchedulerRunner {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    private final AppConfig config;

    private JobRunrConfiguration.JobRunrConfigurationResult jobRunrConfigurationResult;

    @Override
    public void initialize() {
        int jobrunrWorkerCount = config.getJobrunrWorkerCount();
        int pollIntervalInSeconds = config.getPollIntervalInSeconds();
        log.info("Starting JobRunr with worker count: {}", jobrunrWorkerCount);

        JobRunrMicroMeterIntegration jobRunrMicroMeterIntegration = new JobRunrMicroMeterIntegration(meterRegistry);
        PostgresStorageProvider storageProvider = new PostgresStorageProvider(dataSource, "");

        jobRunrConfigurationResult = JobRunr.configure()
                .useStorageProvider(storageProvider)
                .useBackgroundJobServer(jobrunrWorkerCount)
                .useMetrics(jobRunrMicroMeterIntegration)
                .withJobFilter(new MicrometerJobServerFilter(meterRegistry))
                .withJobFilter(new RetryFilter(RetryFilter.DEFAULT_NBR_OF_RETRIES, RetryFilter.DEFAULT_BACKOFF_POLICY_TIME_SEED))
                .useBackgroundJobServerIf(
                        true,
                        BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration()
                                .andPollIntervalInSeconds(pollIntervalInSeconds)
                                .andBackgroundJobServerWorkerPolicy(new DefaultBackgroundJobServerWorkerPolicy(
                                        jobrunrWorkerCount,
                                        BackgroundJobServerThreadType.PlatformThreads
                                )),
                        false
                )
                .initialize();
    }

    @Override
    public void startBackgroundServer() {
        jobRunrConfigurationResult.getBackgroundJobServer().start();
    }
}
