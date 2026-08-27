package ru.strelchm.scheduler_perf.core;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

@Slf4j
public abstract class AbstractMassInserter implements MassInserter {

    private static final String METRIC_NAME = "scheduler.mass.insert.duration";
    protected static final BiFunction<Integer, Integer, String> JOB_ID_GENERATOR = "%d___%d"::formatted;
    protected static final BiFunction<Integer, Integer, String> SLEEPING_JOB_ID_GENERATOR = "sleeping_%d___%d"::formatted;
    public static final long MONTH_SECONDS = 2_678_400L;
    public static final long WEEK_SECONDS = 604_800L;

    private final MeterRegistry meterRegistry;
    private final boolean enabled;
    private final int count;
    private final Integer sleepingJobsCount;
    private final int batchSize;
    private final long delayMs;
    private final Timer timer;

    protected AbstractMassInserter(MeterRegistry meterRegistry, boolean enabled, int count, Integer sleepingJobsCount, int batchSize, long delayMs) {
        this.meterRegistry = meterRegistry;
        this.enabled = enabled;
        this.count = count;
        this.sleepingJobsCount = sleepingJobsCount;
        this.batchSize = batchSize;
        this.delayMs = delayMs;
        this.timer = Timer.builder(METRIC_NAME)
                .description("Duration of processing all records for a single library")
                .tag("library", getSchedulerName())
                .register(meterRegistry);
    }

    public final void batchInsert() {
        if (!enabled) {
            log.info("mass.insert.enabled=false — skipping {} mass insert", getSchedulerName());
            return;
        }

        if (count <= 0) {
            log.info("mass.insert.count={} — nothing to insert for {}", count, getSchedulerName());
            return;
        }

        log.info("mass.insert.enabled=true — inserting {} {} tasks (batchSize={}, delay={} ms)", count, getSchedulerName(), batchSize, delayMs);

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            insertJobs(count, batchSize, false);
        } finally {
            sample.stop(timer);
        }

        if (sleepingJobsCount != null) {
            log.info("mass.insert.enabled=true and sleepingJobsCount is set — inserting {} {} tasks (batchSize={}, delay={} ms)", sleepingJobsCount, getSchedulerName(), batchSize, delayMs);
            insertJobs(sleepingJobsCount, batchSize, true);
        }

        log.info("Massive tasks inserting is finished");
    }

    private void insertJobs(int count, int batchSize, boolean sleepingJobs) {
        int inserted = 0;
            while (inserted < count) {
                int currentBatchSize = Math.min(batchSize, count - inserted);
                if (sleepingJobs) {
                    Instant now = Instant.now();
                    insertSleepingJobs(inserted, currentBatchSize, now.plusSeconds(WEEK_SECONDS), now.plusSeconds(MONTH_SECONDS));
                } else {
                    insertBatch(inserted, currentBatchSize);
                }
                inserted += currentBatchSize;

                if (delayMs > 0 && inserted < count) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (inserted % Math.max(1, batchSize * 10) == 0 || inserted == count) {
                    log.info("inserted {} / {} {}{}tasks", inserted, count, getSchedulerName(), (sleepingJobs ? " sleeping " : ""));
                }
            }

        log.info("finished inserting {} {} tasks", inserted, getSchedulerName());
    }

    protected abstract void insertBatch(int startIndex, int batchCount);

    protected abstract void insertSleepingJobs(int startIndex, int batchCount, Instant from, Instant to);

    protected abstract String getSchedulerName();

    protected Instant getInstantBetween(Instant startInclusive, Instant endExclusive) {
        long startSeconds = startInclusive.getEpochSecond();
        long endSeconds = endExclusive.getEpochSecond();
        long random = ThreadLocalRandom
                .current()
                .nextLong(startSeconds, endSeconds);

        return Instant.ofEpochSecond(random);
    }
}
