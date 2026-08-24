# Scheduler Performance Comparison

Comparison of persistent job scheduling libraries performance (db-scheduler vs JobRunr).

## 📋 Overview

This project compares the performance of two popular Java job scheduling libraries:

- **[db-scheduler](https://github.com/kagkarlsson/db-scheduler)** — lightweight library focused on persistence and reliability
- **[JobRunr](https://www.jobrunr.io/)** — full-featured scheduler with dashboard and advanced capabilities

Both libraries use PostgreSQL for task persistence, ensuring reliability and scalability.

## 🏗️ Project Architecture

```
scheduler-perf/
├── core/                    # Core module (interfaces, base classes)
│   └── src/main/java/
│       └── ru/strelchm/scheduler_perf/core/
│           ├── AbstractMassInserter.java
│           ├── MassInserter.java
│           ├── DbCleaner.java
│           ├── NoopService.java
│           ├── dbscheduler/
│           │   ├── DbSchedulerMassInserter.java
│           │   └── DbSchedulerCleaner.java
│           └── jobrunr/
│               └── JobrunrMassInserter.java
│
├── spring-comparison/       # Spring Boot application
│   └── src/main/java/
│       └── ru/strelchm/scheduler_perf/
│           ├── SchedulerPerfApplication.java
│           └── config/
│               ├── DataSourceConfiguration.java
│               ├── MassInsertConfiguration.java
│               ├── NoopServiceConfig.java
│               ├── dbscheduler/
│               │   ├── DbSchedulerConfiguration.java
│               │   └── TasksConfiguration.java
│               └── jobrunr/
│                   └── JobrunrConfiguration.java
│
├── comparison/              # Console application (no Spring)
│   └── src/main/java/
│       └── ru/strelchm/scheduler_perf/comparison/
│           ├── ComparisonApplication.java      # Main entry point
│           ├── AppConfig.java                  # Configuration loader
│           ├── DataSourceFactory.java          # HikariCP DataSource creator
│           ├── MetricsServer.java              # HTTP server for Prometheus metrics
│           ├── DbSchedulerRunner.java          # db-scheduler execution logic
│           └── JobRunrRunner.java              # JobRunr execution logic
│
├── docker/                  # Docker configuration
│   ├── postgres/
│   │   └── init/
│   │       └── 01-init.sql
│   ├── prometheus/
│   │   └── prometheus.yml
│   └── grafana/
│       └── provisioning/
│
├── docker-compose-infra.yml         # Infrastructure (DB, Prometheus, Postgres Exporter)
├── docker-compose-grafana.yml       # Grafana (optional, depends on Prometheus)
├── docker-compose-spring.yml        # Spring Boot application
├── docker-compose-console.yml       # Console application
├── .env.example                     # Environment variables template
└── README.md                        # This file
```

## 🚀 Quick Start

### 1. Copy Environment File (Optional)

```bash
cp .env.example .env
```

Edit `.env` to customize settings.

### 2. Start Infrastructure

```bash
docker-compose -f docker-compose-infra.yml up -d
```

**Ports:**
- PostgreSQL: `6433` (external), `5432` (internal)
- Prometheus: `9091`
- Postgres Exporter: `9187`

**Credentials:**
- PostgreSQL: `myuser` / `mypassword`

### 2b. Start Grafana (Optional)

Grafana is separated into its own compose file to allow running infrastructure without visualization.

```bash
docker-compose -f docker-compose-infra.yml -f docker-compose-grafana.yml up -d
```

**Ports:**
- Grafana: `3001`

**Credentials:** `admin` / `admin`

### 3. Start Applications

#### Spring Boot Application

```bash
# db-scheduler (default)
docker-compose -f docker-compose-infra.yml -f docker-compose-spring.yml up -d

# JobRunr
SPRING_SCHEDULER_TYPE=jobrunr docker-compose -f docker-compose-infra.yml -f docker-compose-spring.yml up -d
```

**Ports:**
- HTTP: `8080`
- Metrics: `8086`

#### Console Application (No Spring)

```bash
# db-scheduler (default)
docker-compose -f docker-compose-infra.yml -f docker-compose-console.yml up -d

# JobRunr
CONSOLE_SCHEDULER_TYPE=jobrunr docker-compose -f docker-compose-infra.yml -f docker-compose-console.yml up -d
```

**Ports:**
- HTTP: `8081`
- Metrics: `8087`

### 4. Override Settings via Environment Variables

```bash
# Run Spring with JobRunr and custom settings
JOB_RUNR_WORKER_COUNT=8 MASS_INSERT_COUNT=5000 \
  docker-compose -f docker-compose-infra.yml -f docker-compose-spring.yml up -d

# Run Console with custom settings
CONSOLE_SCHEDULER_TYPE=jobrunr DB_SCHEDULER_THREADS=20 \
  docker-compose -f docker-compose-infra.yml -f docker-compose-console.yml up -d
```

### 5. Run Both Applications (not recommended)

```bash
# Both with db-scheduler
docker-compose -f docker-compose-infra.yml \
  -f docker-compose-spring.yml \
  -f docker-compose-console.yml up -d

# Spring with JobRunr, Console with db-scheduler
SPRING_SCHEDULER_TYPE=jobrunr \
  docker-compose -f docker-compose-infra.yml \
  -f docker-compose-spring.yml \
  -f docker-compose-console.yml up -d
```

### 6. Stop Applications

```bash
# Stop Spring only
docker-compose -f docker-compose-spring.yml down

# Stop Console only
docker-compose -f docker-compose-console.yml down

# Stop all (including infrastructure)
docker-compose -f docker-compose-infra.yml \
  -f docker-compose-spring.yml \
  -f docker-compose-console.yml down

# Stop all with data removal
docker-compose -f docker-compose-infra.yml \
  -f docker-compose-spring.yml \
  -f docker-compose-console.yml down -v
```

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_SCHEDULER_TYPE` | `db-scheduler-generic` | Spring scheduler: `db-scheduler-generic` or `jobrunr` |
| `CONSOLE_SCHEDULER_TYPE` | `db-scheduler` | Console scheduler: `db-scheduler` or `jobrunr` |
| `MASS_INSERT_COUNT` | `1000` | Number of tasks to insert |
| `MASS_INSERT_BATCH_SIZE` | `1000` | Batch size for mass insert |
| `MASS_INSERT_DELAY_MS` | `0` | Delay between batches (ms) |
| `JOB_RUNR_WORKER_COUNT` | `4` | Number of JobRunr workers |
| `DB_SCHEDULER_THREADS` | `10` | Number of db-scheduler threads |
| `JVM_ARGS` | `-Xms256m -Xmx512m -XX:+UseG1GC` | JVM memory and GC settings |

### Mass Insert Parameters

All applications use identical settings for fair comparison:

| Parameter | Value | Description |
|-----------|-------|-------------|
| `mass.insert.enabled` | `true` | Enable mass insert |
| `mass.insert.count` | `1000` | Total number of tasks |
| `mass.insert.batch-size` | `1000` | Batch size |
| `mass.insert.delayMs` | `0` | Delay between batches (ms) |
| `jobrunr.worker-count` | `4` | Number of JobRunr workers |
| `db-scheduler.threads` | `10` | Number of db-scheduler threads |

### Identical Configuration for Fair Comparison

To ensure unbiased performance comparison, both applications use identical infrastructure settings:

| Component | Setting | Value | Description |
|-----------|---------|-------|-------------|
| **Connection Pool** | HikariCP `maximum-pool-size` | 10 | Max DB connections |
| | HikariCP `minimum-idle` | 2 | Min idle connections |
| | HikariCP `connection-timeout` | 30000ms | Connection timeout |
| | HikariCP `idle-timeout` | 600000ms | Idle connection timeout |
| | HikariCP `max-lifetime` | 1800000ms | Max connection lifetime |
| | HikariCP `leak-detection-threshold` | 0 | Disabled |
| **Thread Pools** | Executor core pool size | 10 | Core thread count |
| | Executor max pool size | 10 | Max thread count |
| | Executor queue capacity | 1000 | Task queue size |
| **Jackson** | Serialization features | Default | No special features enabled |
| | Deserialization features | Default | No special features enabled |
| | Date format | ISO-8601 | Standard date format |
| **JVM** | Initial heap size | 256MB | `-Xms256m` |
| | Max heap size | 512MB | `-Xmx512m` |
| | GC | G1GC | Default for Java 25 |
| **Database** | PostgreSQL version | 15 | Same container for both |
| | Connection URL | `jdbc:postgresql://postgres:5432/schedulers` | Same DB |
| **Scheduler** | Poll interval | 10s | Same for both libraries |

**Why this matters:**

- Both schedulers compete under identical conditions
- Differences in performance come from the libraries themselves, not configuration
- Connection pool settings prevent DB contention from skewing results
- Same JVM settings ensure fair memory and GC behavior
- Same poll interval ensures equal polling frequency

### Best Practices for Accurate Comparison

Follow these guidelines to ensure fair and reproducible benchmark results:

1. **Run multiple iterations** — Execute each scenario 3-5 times and calculate average values to account for JVM warmup and system noise

2. **Monitor system resources** — Ensure no other heavy processes are running on the host during benchmarks

3. Both applications have CPU/memory limits set to prevent resource starvation:
   - CPU: 1.0-2.0 cores
   - Memory: 512MB-768MB
 
4. Identical noop tasks are used for both libraries

6. **Database proximity** — Both applications connect to the same PostgreSQL container to eliminate network variance

7. **Serializer consistency** — Both libraries use Jackson for serialization (configured in `DbSchedulerConfiguration.java`)

8. **Lock-and-fetch mode** — db-scheduler uses `generic-lock-and-fetch: true` for PostgreSQL, matching JobRunr's locking strategy

6. **Check for GC pressure** — Monitor `jvm_gc_pause_seconds` metric; high GC can skew results

### Configuration Files

**Console (no Spring):**
- `comparison/src/main/resources/application-dbscheduler.properties`
- `comparison/src/main/resources/application-jobrunr.properties`

**Spring Boot:**
- `spring-comparison/src/main/resources/application.yaml` (base)
- `spring-comparison/src/main/resources/application-db-scheduler-generic.yaml`
- `spring-comparison/src/main/resources/application-jobrunr.yaml`

## 📊 Monitoring & Metrics

### Prometheus

Prometheus automatically collects metrics from all applications.

**UI:** http://localhost:9091

### Grafana (Optional)

Grafana is pre-configured with Prometheus datasource but runs in a separate compose file.

**Start Grafana:**
```bash
docker-compose -f docker-compose-infra.yml -f docker-compose-grafana.yml up -d
```

**UI:** http://localhost:3001

**Credentials:** `admin` / `admin`

### Application Metrics

**Spring Boot:**
```bash
curl http://localhost:8086/actuator/metrics
```

**Console (no Spring):**
```bash
curl http://localhost:8087/actuator/metrics
```

## 🔧 Local Development

### Requirements

- Java 25
- Gradle 9.5.1+
- Docker & Docker Compose
- PostgreSQL 15 (local, optional)

### Build

```bash
# Build core module
./gradlew :core:jar

# Build console module
./gradlew :comparison:jar

# Build spring-comparison module
./gradlew :spring-comparison:jar
```

### Run Locally

```bash
# Start infrastructure
docker-compose -f docker-compose-infra.yml up -d

# Run console (no Spring)
java -jar comparison/build/libs/comparison.jar

# Run spring-comparison
java -jar spring-comparison/build/libs/spring-comparison.jar
```

### Debugging

**db-scheduler logging:**
```yaml
logging:
  level:
    com.github.kagkarlsson.scheduler: DEBUG
```

**JobRunr logging:**
```yaml
logging:
  level:
    org.jobrunr: DEBUG
```

**HikariCP logging:**
```yaml
logging:
  level:
    com.zaxxer.hikari: DEBUG
```

## 🧪 Test Scenarios

### 1. Basic Performance Comparison

```bash
# Start infrastructure
docker-compose -f docker-compose-infra.yml up -d

# Run Spring with db-scheduler
docker-compose -f docker-compose-infra.yml -f docker-compose-spring.yml up -d

# Observe metrics in Grafana/Prometheus
# After completion, stop
docker-compose -f docker-compose-spring.yml down

# Run Spring with JobRunr
SPRING_SCHEDULER_TYPE=jobrunr docker-compose -f docker-compose-infra.yml -f docker-compose-spring.yml up -d
```

### 2. Load Testing

Increase mass insert parameters:

```bash
MASS_INSERT_COUNT=100000 MASS_INSERT_BATCH_SIZE=1000 \
  docker-compose -f docker-compose-infra.yml \
  -f docker-compose-spring.yml \
  -f docker-compose-console.yml up -d
```

## 🔍 Results Analysis

### What to Look For

1. **Mass Insert Time**
   - Metric: `scheduler_mass_insert_duration_seconds`
   - Lower = better

2. **Resource Usage**
   - CPU, memory, DB connections
   - Lower = better

3. **Reliability**
   - Lost tasks
   - Duplicate executions
   - Recovery after failures

4. **Scalability**
   - Behavior with increased workers
   - DB connection contention

### Typical Results

| Scenario | db-scheduler | JobRunr |
|----------|--------------|---------|
| Simple tasks | ⚡ Faster | 🐢 Slower |
| Complex tasks | 🐢 Slower | ⚡ Faster |
| Memory usage | 📉 Low | 📈 High |
| Features | 🔧 Basic | 🎛️ Advanced |

## 🛠️ Troubleshooting

### DB Connection Errors

```bash
# Check if DB is running
docker ps | grep postgres

# Check logs
docker logs postgres-scheduler-perf

# Recreate DB
docker-compose -f docker-compose-infra.yml down -v
docker-compose -f docker-compose-infra.yml up -d
```

### Build Errors

```bash
# Clear Gradle cache
./gradlew clean --no-daemon

# Rebuild
./gradlew :comparison:jar :spring-comparison:jar --no-daemon --rerun-tasks
```

### Port Conflicts

```bash
# Check port usage (Windows)
netstat -ano | findstr :6433
netstat -ano | findstr :8086
netstat -ano | findstr :8087
netstat -ano | findstr :9091
netstat -ano | findstr :3001

# Stop containers
docker-compose -f docker-compose-infra.yml \
  -f docker-compose-grafana.yml \
  -f docker-compose-spring.yml \
  -f docker-compose-console.yml down
```

### Grafana Not Starting

```bash
# Make sure Prometheus is running first
docker ps | grep prometheus

# Start Grafana with infrastructure
docker-compose -f docker-compose-infra.yml -f docker-compose-grafana.yml up -d

# Check logs
docker-compose logs grafana
```

### Application Won't Start

```bash
# Check logs
docker-compose -f docker-compose-spring.yml logs spring-app
docker-compose -f docker-compose-console.yml logs console-app

# Rebuild
docker-compose -f docker-compose-spring.yml build --no-cache
docker-compose -f docker-compose-console.yml build --no-cache
```

## 📚 Additional Resources

- [db-scheduler Documentation](https://github.com/kagkarlsson/db-scheduler)
- [JobRunr Documentation](https://www.jobrunr.io/en/documentation/)
- [Micrometer Documentation](https://micrometer.io/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

## 📝 License

This project is created for educational and research purposes.

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

---
