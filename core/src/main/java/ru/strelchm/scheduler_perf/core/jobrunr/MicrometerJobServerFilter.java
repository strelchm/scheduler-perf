package ru.strelchm.scheduler_perf.core.jobrunr;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.filters.JobServerFilter;

@RequiredArgsConstructor
public class MicrometerJobServerFilter implements JobServerFilter {
    private final Counter schedulerStartCounter;
    private final Counter schedulerCompletedCounter;

    public MicrometerJobServerFilter(MeterRegistry meterRegistry) {
        this.schedulerStartCounter = meterRegistry.counter("scheduler_start", "lib", "jobrunr");
        this.schedulerCompletedCounter = meterRegistry.counter("scheduler_completed", "lib", "jobrunr");
    }

    @Override
    public void onProcessing(Job job) {
        schedulerStartCounter.increment();
    }

    @Override
    public void onProcessingSucceeded(Job job) {
        schedulerCompletedCounter.increment();
    }
}
