package ru.strelchm.scheduler_perf.core.dbscheduler;

import com.github.kagkarlsson.scheduler.CurrentlyExecuting;
import com.github.kagkarlsson.scheduler.event.AbstractSchedulerListener;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import org.slf4j.MDC;


public class MdcSchedulerListener extends AbstractSchedulerListener {
    private static final String MDC_TASK_NAME = "task-name";
    private static final String MDC_TASK_INSTANCE_ID = "task-instance-id";

    @Override
    public void onExecutionStart(CurrentlyExecuting executing) {
        TaskInstance<?> taskInstance = executing.getTaskInstance();
        MDC.put(MDC_TASK_NAME, taskInstance.getTaskName());
        MDC.put(MDC_TASK_INSTANCE_ID, taskInstance.getId());
    }

    @Override
    public void onExecutionComplete(ExecutionComplete executionComplete) {
        MDC.remove(MDC_TASK_NAME);
        MDC.remove(MDC_TASK_INSTANCE_ID);
    }
}
