package ru.strelchm.scheduler_perf.config;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.shaded.jdbc.JdbcRunner;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.strelchm.scheduler_perf.core.DbCleaner;
import ru.strelchm.scheduler_perf.core.MassInserter;
import ru.strelchm.scheduler_perf.core.NoopService;
import ru.strelchm.scheduler_perf.core.dbscheduler.DbSchedulerCleaner;
import ru.strelchm.scheduler_perf.core.dbscheduler.DbSchedulerMassInserter;
import ru.strelchm.scheduler_perf.core.jobrunr.JobrunrMassInserter;


@Configuration
public class MassInsertConfiguration {

    @Bean
    @Profile("jobrunr")
    public MassInserter jobrunrMassInserter(NoopService noopService,
                                            MeterRegistry meterRegistry,
                                            @Value("${mass.insert.enabled:true}") boolean enabled,
                                            @Value("${mass.insert.count:1000}") int count,
                                            @Value("${mass.insert.batch-size:1000}") int batchSize,
                                            @Value("${mass.insert.delayMs:0}") long delayMs) {
        return new JobrunrMassInserter(noopService, meterRegistry, enabled, count, batchSize, delayMs);
    }

    @Bean
    @Profile("db-scheduler")
    public MassInserter dbSchedulerMassInserter(SchedulerClient schedulerClient,
                                              MeterRegistry meterRegistry,
                                              @Value("${mass.insert.enabled:true}") boolean enabled,
                                              @Value("${mass.insert.count:1000}") int count,
                                              @Value("${mass.insert.batch-size:1000}") int batchSize,
                                              @Value("${mass.insert.delayMs:0}") long delayMs) {
        return new DbSchedulerMassInserter(schedulerClient, meterRegistry, enabled, count, batchSize, delayMs);
    }

    @Bean
    public DbCleaner dbCleaner(JdbcRunner jdbcRunner) {
        return new DbSchedulerCleaner(jdbcRunner);
    }

    @Bean
    public CommandLineRunner massInsertCLR(MassInserter massInserter, DbCleaner dbCleaner) {
        return _ -> {
            dbCleaner.cleanOldJobs();
            massInserter.batchInsert();
        };
    }
}
