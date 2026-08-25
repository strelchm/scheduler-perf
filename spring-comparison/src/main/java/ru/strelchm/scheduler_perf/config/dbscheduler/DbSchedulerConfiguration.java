package ru.strelchm.scheduler_perf.config.dbscheduler;

import com.github.kagkarlsson.scheduler.boot.autoconfigure.Jackson3Serializer;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.event.SchedulerListener;
import com.github.kagkarlsson.scheduler.jdbc.JdbcCustomization;
import com.github.kagkarlsson.scheduler.jdbc.PostgreSqlJdbcCustomization;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.strelchm.scheduler_perf.core.dbscheduler.MdcSchedulerListener;

import java.util.Optional;

@Slf4j
@Configuration
@Profile("db-scheduler")
public class DbSchedulerConfiguration {

    @Bean
    DbSchedulerCustomizer customizer(@Value("${db-scheduler.generic-lock-and-fetch:false}") Boolean genericLockAndFetch) {
        return new DbSchedulerCustomizer() {
            @Override
            public Optional<Serializer> serializer() {
                return Optional.of(new Jackson3Serializer());
            }

            @Override
            public Optional<JdbcCustomization> jdbcCustomization() {
                // different modes, see https://github.com/kagkarlsson/db-scheduler/issues/823
                log.info("LockAndFetch is {}", (genericLockAndFetch ? "generic" : "single statement"));
                return Optional.of(new PostgreSqlJdbcCustomization(genericLockAndFetch, false));
            }
        };
    }

    @Bean
    SchedulerListener mdcSchedulerListener() {
        return new MdcSchedulerListener();
    }
}
