package ru.strelchm.scheduler_perf.core.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpService {
    public void noop(NoOpDto noOpDto) {
        log.info("noop executed for {}", noOpDto.getIteration());
    }
}
