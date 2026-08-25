package ru.strelchm.scheduler_perf.core.dbscheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.strelchm.scheduler_perf.core.DbCleaner;
import ru.strelchm.scheduler_perf.core.utils.TableExistenceChecker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@RequiredArgsConstructor
public class DbSchedulerCleaner implements DbCleaner {
    private final DataSource dataSource;

    @Override
    public void cleanOldJobs() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            if (!TableExistenceChecker.tableExists(conn, "jobrunr_jobs")) {
                log.info("Skip truncating tables cause db does not contain them");
                return;
            }
            stmt.execute("TRUNCATE TABLE scheduled_tasks");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean scheduled_tasks", e);
        }
    }
}
