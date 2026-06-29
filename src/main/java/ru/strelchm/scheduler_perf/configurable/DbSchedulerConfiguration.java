package ru.strelchm.scheduler_perf.configurable;

import com.github.kagkarlsson.scheduler.CurrentlyExecuting;
import com.github.kagkarlsson.scheduler.boot.autoconfigure.Jackson3Serializer;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.event.AbstractSchedulerListener;
import com.github.kagkarlsson.scheduler.event.SchedulerListener;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import com.github.kagkarlsson.scheduler.task.ExecutionComplete;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Configuration
@Profile("db-scheduler")
public class DbSchedulerConfiguration {

    private static final String MDC_TASK_NAME = "task-name";
    private static final String MDC_TASK_INSTANCE_ID = "task-instance-id";

    @Bean
    DbSchedulerCustomizer customizer() {
        return new DbSchedulerCustomizer() {
            @Override
            public Optional<Serializer> serializer() {
                return Optional.of(new Jackson3Serializer());
            }
        };
    }

    @Bean
    SchedulerListener mdcSchedulerListener() {
        return new AbstractSchedulerListener() {
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
        };
    }
}
