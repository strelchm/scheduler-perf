package ru.strelchm.scheduler_perf.config.dbscheduler;

import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.strelchm.scheduler_perf.core.NoopService;

@Slf4j
@Configuration
@Profile("db-scheduler")
public class TasksConfiguration {
    public static final String TASK_NAME = "perf_task";

    @Bean
        public OneTimeTask<Void> oneTimeTask(NoopService noopService) {
            return Tasks.oneTime(TASK_NAME)
                    .execute((taskInstance, executionContext) -> {
                        noopService.noop(taskInstance.getTaskName());
                    });
        }
}
