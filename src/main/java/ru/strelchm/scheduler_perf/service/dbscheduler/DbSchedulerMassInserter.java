package ru.strelchm.scheduler_perf.service.dbscheduler;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import io.micrometer.core.instrument.MeterRegistry;
import ru.strelchm.scheduler_perf.service.AbstractMassInserter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static ru.strelchm.scheduler_perf.config.dbscheduler.TasksConfiguration.TASK_NAME;

public class DbSchedulerMassInserter extends AbstractMassInserter {

    private final SchedulerClient schedulerClient;

    public DbSchedulerMassInserter(SchedulerClient schedulerClient,
                                  MeterRegistry meterRegistry,
                                  boolean enabled,
                                  int count,
                                  int batchSize,
                                  long delayMs) {
        super(meterRegistry, enabled, count, batchSize, delayMs);
        this.schedulerClient = schedulerClient;
    }

    @Override
    protected void insertBatch(int startIndex, int batchCount) {
        Instant executionTime = Instant.now();
        List<TaskInstance<?>> taskInstances = new ArrayList<>(batchCount);

        for (int i = 0; i < batchCount; i++) {
            String taskId = String.valueOf(startIndex + i);
            taskInstances.add(new TaskInstance<>(TASK_NAME, taskId));
        }

        schedulerClient.scheduleBatch(taskInstances, executionTime);
    }

    @Override
    protected String getSchedulerName() {
        return "DbScheduler";
    }
}
