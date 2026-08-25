package ru.strelchm.scheduler_perf.comparison.runner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.jdbc.PostgreSqlJdbcCustomization;
import com.github.kagkarlsson.scheduler.serializer.JacksonSerializer;
import com.github.kagkarlsson.scheduler.serializer.jackson.InstantDeserializer;
import com.github.kagkarlsson.scheduler.serializer.jackson.InstantSerializer;
import com.github.kagkarlsson.scheduler.serializer.jackson.ScheduleMixin;
import com.github.kagkarlsson.scheduler.stats.MicrometerStatsRegistry;
import com.github.kagkarlsson.scheduler.stats.StatsRegistryAdapter;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.strelchm.scheduler_perf.comparison.AppConfig;
import ru.strelchm.scheduler_perf.core.dbscheduler.MdcSchedulerListener;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DbSchedulerRunner implements SchedulerRunner {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    private final AppConfig config;
    private final List<Task<?>> knownTasks;

    @Override
    public void run() {
        boolean genericLockAndFetch = config.getSchedulerType() == AppConfig.SchedulerType.DB_SCHEDULLER_GENERIC;
        int pollIntervalInSeconds = config.getPollIntervalInSeconds();

        log.info(
                "Starting db-scheduler with poll interval: {}. LockAndFetch is {}",
                pollIntervalInSeconds,
                (genericLockAndFetch ? "generic" : "single statement")
        );

        Scheduler scheduler = Scheduler.create(dataSource, knownTasks)
                .pollingInterval(Duration.ofSeconds(pollIntervalInSeconds))
                .pollUsingLockAndFetch(1.0, 2.0) // todo config
                .serializer(new JacksonSerializer(getObjectMapper()))
                .addSchedulerListener(new MdcSchedulerListener())
                .addSchedulerListener(new StatsRegistryAdapter(new MicrometerStatsRegistry(meterRegistry, knownTasks)))
                .threads(config.getDbSchedulerThreads())
                .jdbcCustomization(new PostgreSqlJdbcCustomization(genericLockAndFetch, false))
                .build();
        scheduler.start();
    }

    private static ObjectMapper getObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantSerializer());
        module.addDeserializer(Instant.class, new InstantDeserializer());

        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addMixIn(Schedule.class, ScheduleMixin.class)
                .registerModule(module)
                .registerModule(new JavaTimeModule());
    }
}
