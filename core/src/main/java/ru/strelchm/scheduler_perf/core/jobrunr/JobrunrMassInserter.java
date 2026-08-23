package ru.strelchm.scheduler_perf.core.jobrunr;

import io.micrometer.core.instrument.MeterRegistry;
import org.jobrunr.scheduling.BackgroundJob;
import ru.strelchm.scheduler_perf.core.AbstractMassInserter;
import ru.strelchm.scheduler_perf.core.NoopService;

public class JobrunrMassInserter extends AbstractMassInserter {

    private final NoopService noopService;

    public JobrunrMassInserter(NoopService noopService,
                              MeterRegistry meterRegistry,
                              boolean enabled,
                              int count,
                              int batchSize,
                              long delayMs) {
        super(meterRegistry, enabled, count, batchSize, delayMs);
        this.noopService = noopService;
    }

    @Override
    protected void insertBatch(int startIndex, int batchCount) {
        for (int i = 0; i < batchCount; i++) {
            String taskName = "perf_task_" + (startIndex + i);
            BackgroundJob.enqueue(() -> noopService.noop(taskName));
        }
    }

    @Override
    protected String getSchedulerName() {
        return "JobRunr";
    }
}
