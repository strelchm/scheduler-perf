package ru.strelchm.scheduler_perf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.strelchm.scheduler_perf.core.NoopService;

@Configuration
public class NoopServiceConfig {

    @Bean
    public NoopService noopService() {
        return new NoopService();
    }
}
