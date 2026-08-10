package ru.strelchm.scheduler_perf.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopService {
    public void noop(String taskName) {
        log.info("noop executed for {}", taskName);
    }
}
