package ru.strelchm.scheduler_perf.core.dbscheduler;

import com.github.kagkarlsson.scheduler.CurrentlyExecuting;
import com.github.kagkarlsson.scheduler.event.AbstractSchedulerListener;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerSchedulerListener extends AbstractSchedulerListener {
    private final Counter schedulerStartCounter;
    private final Counter schedulerCompletedCounter;

    public MicrometerSchedulerListener(MeterRegistry meterRegistry) {
        this.schedulerStartCounter = meterRegistry.counter("scheduler_start", "lib", "db-scheduler");
        this.schedulerCompletedCounter = meterRegistry.counter("scheduler_completed", "lib", "db-scheduler");
    }

    @Override
    public void onExecutionStart(CurrentlyExecuting executing) {
        schedulerStartCounter.increment();
    }

    @Override
    public void onExecutionComplete(ExecutionComplete executionComplete) {
        schedulerCompletedCounter.increment();
    }
}
