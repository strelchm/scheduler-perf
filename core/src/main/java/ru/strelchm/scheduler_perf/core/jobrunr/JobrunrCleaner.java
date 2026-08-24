package ru.strelchm.scheduler_perf.core.jobrunr;

import lombok.RequiredArgsConstructor;
import ru.strelchm.scheduler_perf.core.DbCleaner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@RequiredArgsConstructor
public class JobrunrCleaner implements DbCleaner {
    private final DataSource dataSource;

    @Override
    public void cleanOldJobs() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE jobrunr_jobs");
            stmt.execute("TRUNCATE TABLE jobrunr_job_states");
            stmt.execute("TRUNCATE TABLE jobrunr_background_job_servers");
            stmt.execute("TRUNCATE TABLE jobrunr_recurring_jobs");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean jobrunr tables", e);
        }
    }
}
