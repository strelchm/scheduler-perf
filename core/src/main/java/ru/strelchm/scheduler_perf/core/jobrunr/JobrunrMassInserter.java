package ru.strelchm.scheduler_perf.core.jobrunr;

import io.micrometer.core.instrument.MeterRegistry;
import org.jobrunr.scheduling.BackgroundJob;
import ru.strelchm.scheduler_perf.core.AbstractMassInserter;
import ru.strelchm.scheduler_perf.core.service.NoOpDto;
import ru.strelchm.scheduler_perf.core.service.NoOpService;

import java.time.Instant;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JobrunrMassInserter extends AbstractMassInserter {

    private final NoOpService noopService;

    public JobrunrMassInserter(NoOpService noopService,
                               MeterRegistry meterRegistry,
                               boolean enabled,
                               int count,
                               Integer sleepingJobsCount,
                               int batchSize,
                               long delayMs) {
        super(meterRegistry, enabled, count, sleepingJobsCount, batchSize, delayMs);
        this.noopService = noopService;
    }

    @Override
    protected void insertBatch(int startIndex, int batchCount) {
        Stream<NoOpDto> taskInstances = IntStream.range(0, batchCount)
                .mapToObj(iteration -> JOB_ID_GENERATOR.apply(startIndex, iteration))
                .map(NoOpDto::new);
        BackgroundJob.enqueue(taskInstances, noopService::noop);
    }

    @Override
    protected void insertSleepingJobs(int startIndex, int batchCount, Instant from, Instant to) {
        for (int i = 0; i < batchCount; i++) {
            String iteration = JOB_ID_GENERATOR.apply(startIndex, i);
            BackgroundJob.schedule(getInstantBetween(from, to), () -> noopService.noop(new NoOpDto(iteration)));
        }
    }

    @Override
    protected String getSchedulerName() {
        return "JobRunr";
    }
}
