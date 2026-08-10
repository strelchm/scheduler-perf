package ru.strelchm.scheduler_perf.config.jobrunr;

import lombok.extern.slf4j.Slf4j;
import org.jobrunr.server.configuration.BackgroundJobServerThreadType;
import org.jobrunr.server.configuration.BackgroundJobServerWorkerPolicy;
import org.jobrunr.server.configuration.DefaultBackgroundJobServerWorkerPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("jobrunr")
public class JobrunrConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BackgroundJobServerWorkerPolicy platformThreadsWorkerPolicy(
            @Value("${jobrunr.worker-count:#{null}}") Integer workerCount
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
