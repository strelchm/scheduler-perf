package ru.strelchm.scheduler_perf.service.jobrunr;

import org.jobrunr.scheduling.BackgroundJob;
import ru.strelchm.scheduler_perf.config.NoopService;
import ru.strelchm.scheduler_perf.service.AbstractMassInserter;

public class JobrunrMassInserter extends AbstractMassInserter {

    private final NoopService noopService;

    public JobrunrMassInserter(NoopService noopService) {
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
