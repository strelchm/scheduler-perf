package ru.strelchm.scheduler_perf.comparison;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceFactory {

    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_IDLE = 2;
    private static final int CONNECTION_TIMEOUT_MS = 30000;
    private static final int IDLE_TIMEOUT_MS = 600000;
    private static final int MAX_LIFETIME_MS = 1800000;
    private static final long LEAK_DETECTION_THRESHOLD = 0;

    public static HikariDataSource createDataSource(String dbUrl, String dbUsername, String dbPassword) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);

        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);
        config.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

        return new HikariDataSource(config);
    }
}
