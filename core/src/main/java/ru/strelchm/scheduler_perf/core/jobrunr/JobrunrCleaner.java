package ru.strelchm.scheduler_perf.core.jobrunr;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.strelchm.scheduler_perf.core.DbCleaner;
import ru.strelchm.scheduler_perf.core.utils.TableExistenceChecker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@RequiredArgsConstructor
public class JobrunrCleaner implements DbCleaner {
    private static final Logger log = LoggerFactory.getLogger(JobrunrCleaner.class);
    private final DataSource dataSource;

    @Override
    public void cleanOldJobs() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            if (!TableExistenceChecker.tableExists(conn, "jobrunr_jobs")||
                    !TableExistenceChecker.tableExists(conn, "jobrunr_job_states")) {
                log.info("Skip truncating tables cause db does not contain them");
                return;
            }
            stmt.execute("TRUNCATE TABLE jobrunr_jobs");
            stmt.execute("TRUNCATE TABLE jobrunr_job_states");
            stmt.execute("TRUNCATE TABLE jobrunr_background_job_servers");
            stmt.execute("TRUNCATE TABLE jobrunr_recurring_jobs");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean jobrunr tables", e);
        }
    }
}
