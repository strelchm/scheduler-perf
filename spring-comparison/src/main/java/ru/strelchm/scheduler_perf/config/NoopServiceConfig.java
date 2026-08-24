package ru.strelchm.scheduler_perf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.strelchm.scheduler_perf.core.service.NoOpService;

@Configuration
public class NoopServiceConfig {

    @Bean
    public NoOpService noopService() {
        return new NoOpService();
    }
}
