package ru.strelchm.scheduler_perf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractMassInserter implements MassInserter {

    private static final Logger log = LoggerFactory.getLogger(AbstractMassInserter.class);

    @Value("${mass.insert.enabled:true}")
    private boolean enabled;

    @Value("${mass.insert.count:1000}")
    private int count;

    @Value("${mass.insert.batch-size:1000}")
    private int batchSize;

    @Value("${mass.insert.delayMs:0}")
    private long delayMs;

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

        int inserted = 0;
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

        log.info("finished inserting {} {} tasks", inserted, getSchedulerName());
    }

    protected abstract void insertBatch(int startIndex, int batchCount);

    protected abstract String getSchedulerName();
}
