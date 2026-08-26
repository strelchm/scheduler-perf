package ru.strelchm.scheduler_perf.core.jobrunr;

import io.micrometer.core.instrument.MeterRegistry;
import org.jobrunr.scheduling.BackgroundJob;
import ru.strelchm.scheduler_perf.core.AbstractMassInserter;
import ru.strelchm.scheduler_perf.core.service.NoOpDto;
import ru.strelchm.scheduler_perf.core.service.NoOpService;

public class JobrunrMassInserter extends AbstractMassInserter {

    private final NoOpService noopService;

    public JobrunrMassInserter(NoOpService noopService,
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
            String iteration = JOB_ID_GENERATOR.apply(startIndex, i);
            BackgroundJob.enqueue(() -> noopService.noop(new NoOpDto(iteration)));
        }
    }

    @Override
    protected String getSchedulerName() {
        return "JobRunr";
    }
}
