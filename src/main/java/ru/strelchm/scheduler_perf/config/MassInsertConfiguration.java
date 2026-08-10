package ru.strelchm.scheduler_perf.config;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.strelchm.scheduler_perf.service.MassInserter;
import ru.strelchm.scheduler_perf.service.dbscheduler.DbSchedulerMassInserter;
import ru.strelchm.scheduler_perf.service.jobrunr.JobrunrMassInserter;


@Configuration
public class MassInsertConfiguration {

    @Bean
    @Profile("jobrunr")
    public MassInserter jobrunrMassInserter(NoopService noopService) {
        return new JobrunrMassInserter(noopService);
    }

    @Bean
    @Profile("db-scheduler")
    public MassInserter dbSchedulerMassInserter(SchedulerClient schedulerClient) {
        return new DbSchedulerMassInserter(schedulerClient);
    }

    @Bean
    public CommandLineRunner massInsertCLR(MassInserter massInserter) {
        return _ -> massInserter.batchInsert();
    }
}
