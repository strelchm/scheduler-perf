package ru.strelchm.scheduler_perf.comparison;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
public class AppConfig {

    private SchedulerType schedulerType;
    private boolean massInsertEnabled;
    private int massInsertCount;
    private int massInsertBatchSize;
    private long massInsertDelayMs;
    private int jobrunrWorkerCount;
    private int dbSchedulerThreads;
    private int pollIntervalInSeconds;
    private Integer sleepingJobsCount;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    public AppConfig() {
        fillSchedulerTypeFromEnvironment();
        fillDbPropsFromEnvironment();
        fillSchedulerPropsFromEnvironment();
    }

    private void fillSchedulerTypeFromEnvironment() {
        final SchedulerType schedulerType;
        String schedulerTypeEnv = System.getenv("SCHEDULER_TYPE");
        schedulerType = switch (schedulerTypeEnv) {
            case "db-scheduler-generic" -> SchedulerType.DB_SCHEDULLER_GENERIC;
            case "db-scheduler" -> SchedulerType.DB_SCHEDULLER;
            case "jobrunr" -> SchedulerType.JOB_RUNR;
            case null, default -> throw new IllegalArgumentException("Unknown scheduler type: " + schedulerTypeEnv);
        };
        this.schedulerType = schedulerType;
    }

    private void fillDbPropsFromEnvironment() {
        dbUrl = System.getenv("DB_URL");
        dbUsername = System.getenv("DB_USERNAME");
        dbPassword = System.getenv("DB_PASSWORD");
    }

    private void fillSchedulerPropsFromEnvironment() {
        final String envMassInsertEnabled = System.getenv("MASS_INSERT_ENABLED");
        final String envCount = System.getenv("MASS_INSERT_COUNT");
        final String envSleepingCount = System.getenv("MASS_INSERT_SLEEPING_COUNT");
        final String envBatchSize = System.getenv("MASS_INSERT_BATCH_SIZE");
        final String envDelayMs = System.getenv("MASS_INSERT_DELAY_MS");

        massInsertCount = Integer.parseInt(envCount);
        massInsertBatchSize = Integer.parseInt(envBatchSize);
        massInsertDelayMs = Long.parseLong(envDelayMs);
        massInsertEnabled = Boolean.parseBoolean(envMassInsertEnabled);

        final String envJobrunrWorkerCount = System.getenv("JOB_RUNR_WORKER_COUNT");
        final String envDbSchedulerThreads = System.getenv("DB_SCHEDULER_THREADS");
        final String envPollIntervalInSeconds = System.getenv("POLL_INTERVAL_IN_SECONDS");
        log.info("config {}, {}, {}", envJobrunrWorkerCount, envDbSchedulerThreads, envPollIntervalInSeconds);

        jobrunrWorkerCount = Integer.parseInt(envJobrunrWorkerCount);
        dbSchedulerThreads = Integer.parseInt(envDbSchedulerThreads);
        pollIntervalInSeconds = Integer.parseInt(envPollIntervalInSeconds);
        sleepingJobsCount = envSleepingCount == null || envSleepingCount.isEmpty() ? null : Integer.parseInt(envSleepingCount);
    }

    public enum SchedulerType {
        JOB_RUNR,
        DB_SCHEDULLER,
        DB_SCHEDULLER_GENERIC
    }
}
