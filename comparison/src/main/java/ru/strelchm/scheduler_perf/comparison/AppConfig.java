package ru.strelchm.scheduler_perf.comparison;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class AppConfig {

    private final Properties properties;
    private final SchedulerType schedulerType;


    public AppConfig() {
        String schedulerTypeEnv = System.getenv("SCHEDULER_TYPE");
        schedulerType = switch (schedulerTypeEnv) {
            case "db-scheduler" -> SchedulerType.DB_SCHEDULLER;
            case "jobrunr" -> SchedulerType.JOB_RUNR;
            case null, default -> throw new IllegalArgumentException("Unknown scheduler type: " + schedulerTypeEnv);
        };
        this.properties = loadProperties();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        loadFromEnvironment(props);
        loadFromFile(props);
        applyDefaults(props);
        return props;
    }

    private void loadFromEnvironment(Properties props) {
        String envCount = System.getenv("MASS_INSERT_COUNT");
        String envBatchSize = System.getenv("MASS_INSERT_BATCH_SIZE");
        String envDelayMs = System.getenv("MASS_INSERT_DELAY_MS");
        String envWorkerCount = System.getenv("JOB_RUNR_WORKER_COUNT");

        if (envCount != null) {
            props.setProperty("mass.insert.count", envCount);
        }
        if (envBatchSize != null) {
            props.setProperty("mass.insert.batch-size", envBatchSize);
        }
        if (envDelayMs != null) {
            props.setProperty("mass.insert.delayMs", envDelayMs);
        }
        if (envWorkerCount != null) {
            props.setProperty("jobrunr.background-job-server.worker-count", envWorkerCount);
        }
    }

    private void loadFromFile(Properties props) {
        Path configPath = Path.of("application.properties");
        if (Files.exists(configPath)) {
            try (InputStream is = Files.newInputStream(configPath)) {
                props.load(is);
                log.info("Loaded configuration from application.properties");
            } catch (IOException e) {
                log.warn("Failed to load application.properties: {}", e.getMessage());
            }
        }
    }

    private void applyDefaults(Properties props) {
        if (!props.containsKey("db.url"))
            props.setProperty("db.url", "jdbc:postgresql://postgres:5432/schedulers");
        if (!props.containsKey("db.username"))
            props.setProperty("db.username", "myuser");
        if (!props.containsKey("db.password"))
            props.setProperty("db.password", "mypassword");
        if (!props.containsKey("scheduler.type"))
            props.setProperty("scheduler.type", "db-scheduler");
        if (!props.containsKey("mass.insert.count"))
            props.setProperty("mass.insert.count", "1000");
        if (!props.containsKey("mass.insert.batch-size"))
            props.setProperty("mass.insert.batch-size", "1000");
        if (!props.containsKey("mass.insert.delayMs"))
            props.setProperty("mass.insert.delayMs", "0");
        if (!props.containsKey("jobrunr.background-job-server.worker-count"))
            props.setProperty("jobrunr.background-job-server.worker-count", "4");
    }

    public SchedulerType getSchedulerType() {
        return schedulerType;
    }

    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public int getMassInsertCount() {
        return Integer.parseInt(properties.getProperty("mass.insert.count"));
    }

    public int getMassInsertBatchSize() {
        return Integer.parseInt(properties.getProperty("mass.insert.batch-size"));
    }

    public long getMassInsertDelayMs() {
        return Long.parseLong(properties.getProperty("mass.insert.delayMs"));
    }

    public int getJobRunrWorkerCount() {
        return Integer.parseInt(properties.getProperty("jobrunr.background-job-server.worker-count"));
    }

    public boolean isMassInsertEnabled() {
        return Boolean.parseBoolean(properties.getProperty("mass.insert.enabled", "true"));
    }

    public Properties getProperties() {
        return properties;
    }

    public enum SchedulerType {
        JOB_RUNR,
        DB_SCHEDULLER
    }
}
