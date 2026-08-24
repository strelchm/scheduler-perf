FROM openjdk:27-ea-29-jdk-slim-trixie
COPY comparison/build/libs/comparison.jar /app/
COPY comparison/src/main/resources/application-*.properties /app/
WORKDIR /app
ARG scheduler_type=db-scheduler
ENV SCHEDULER_TYPE $scheduler_type
ENV JVM_ARGS=""
CMD if [ "$SCHEDULER_TYPE" = "jobrunr" ]; then \
    cp /app/application-jobrunr.properties /app/application.properties; \
    else \
    cp /app/application-dbscheduler.properties /app/application.properties; \
    fi && \
    exec java $JVM_ARGS -jar comparison.jar
