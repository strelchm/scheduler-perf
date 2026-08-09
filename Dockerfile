FROM openjdk:27-ea-29-jdk-slim-trixie
COPY build/libs/scheduler-perf-0.0.1-SNAPSHOT.jar /app/
VOLUME /app/logs
ARG deployenv
RUN echo $deployenv
ENV SPRING_PROFILES_ACTIVE $deployenv
ENTRYPOINT ["bin/sh", "-c", "java -jar /app/scheduler-perf-0.0.1-SNAPSHOT.jar"]