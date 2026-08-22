package ru.strelchm.scheduler_perf.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopService {
    public void noop(String taskName) {
        log.info("noop executed for {}", taskName);
    }
}
