package ru.strelchm.scheduler_perf.config.jobrunr;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.filters.JobServerFilter;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.JobActivator;
import org.jobrunr.server.configuration.BackgroundJobServerThreadType;
import org.jobrunr.server.configuration.BackgroundJobServerWorkerPolicy;
import org.jobrunr.server.configuration.DefaultBackgroundJobServerWorkerPolicy;
import org.jobrunr.spring.autoconfigure.JobRunrProperties;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.utils.mapper.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.strelchm.scheduler_perf.core.jobrunr.MicrometerJobServerFilter;

import java.util.List;

@Slf4j
@Configuration
@Profile("jobrunr")
public class JobrunrConfiguration {

    @Bean
    public BackgroundJobServer backgroundJobServer(
            StorageProvider storageProvider, JsonMapper jobRunrJsonMapper,
            JobActivator jobActivator, BackgroundJobServerConfiguration backgroundJobServerConfiguration,
            JobRunrProperties properties, JobServerFilter micrometerJobServerFilter
    ) {
        final BackgroundJobServer backgroundJobServer = new BackgroundJobServer(storageProvider, jobRunrJsonMapper, jobActivator, backgroundJobServerConfiguration);
        backgroundJobServer.setJobFilters(List.of(micrometerJobServerFilter, new RetryFilter(properties.getJobs().getDefaultNumberOfRetries(), properties.getJobs().getRetryBackOffTimeSeed())));
        return backgroundJobServer;
    }

    @Bean
    public JobServerFilter micrometerJobServerFilter(MeterRegistry meterRegistry) {
        return new MicrometerJobServerFilter(meterRegistry);
    }

    @Bean
    public BackgroundJobServerWorkerPolicy platformThreadsWorkerPolicy(
            @Value("${jobrunr.background-job-server.worker-count:#{null}}") Integer workerCount
    ) {
        int finalWorkerCount = (workerCount != null) ? workerCount :
                BackgroundJobServerThreadType.PlatformThreads.getDefaultWorkerCount();

        log.info("Jobrunr configured with PlatformThreads: workerCount={}", finalWorkerCount);

        return new DefaultBackgroundJobServerWorkerPolicy(
                finalWorkerCount,
                BackgroundJobServerThreadType.PlatformThreads
        );
    }
}
