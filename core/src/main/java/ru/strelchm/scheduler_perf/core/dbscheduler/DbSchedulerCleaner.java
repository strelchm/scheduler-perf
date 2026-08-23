package ru.strelchm.scheduler_perf.core.dbscheduler;

import com.github.kagkarlsson.shaded.jdbc.JdbcRunner;
import com.github.kagkarlsson.shaded.jdbc.PreparedStatementSetter;
import lombok.RequiredArgsConstructor;
import ru.strelchm.scheduler_perf.core.DbCleaner;

@RequiredArgsConstructor
public class DbSchedulerCleaner implements DbCleaner {
    private final JdbcRunner jdbcRunner;

    @Override
    public void cleanOldJobs() {
        jdbcRunner.execute("TRUNCATE TABLE scheduled_tasks", PreparedStatementSetter.NOOP);
    }
}
