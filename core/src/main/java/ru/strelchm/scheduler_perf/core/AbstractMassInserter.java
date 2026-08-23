package ru.strelchm.scheduler_perf.core;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMassInserter implements MassInserter {

    private static final Logger log = LoggerFactory.getLogger(AbstractMassInserter.class);
    private static final String METRIC_NAME = "scheduler.mass.insert.duration";

    private final MeterRegistry meterRegistry;
    private final boolean enabled;
    private final int count;
    private final int batchSize;
    private final long delayMs;
    private final Timer timer;

    protected AbstractMassInserter(MeterRegistry meterRegistry, boolean enabled, int count, int batchSize, long delayMs) {
        this.meterRegistry = meterRegistry;
        this.enabled = enabled;
        this.count = count;
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

        int inserted = 0;
        try {
            while (inserted < count) {
                int currentBatchSize = Math.min(batchSize, count - inserted);
                insertBatch(inserted, currentBatchSize);
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
                    log.info("inserted {} / {} {} tasks", inserted, count, getSchedulerName());
                }
            }
        } finally {
            sample.stop(timer);
        }

        log.info("finished inserting {} {} tasks", inserted, getSchedulerName());
    }

    protected abstract void insertBatch(int startIndex, int batchCount);

    protected abstract String getSchedulerName();
}
