FROM openjdk:27-ea-29-jdk-slim-trixie
COPY comparison/build/libs/comparison.jar /app/
WORKDIR /app
ARG scheduler_type=db-scheduler
ENV SCHEDULER_TYPE $scheduler_type
ENV JVM_ARGS=""
CMD exec java $JVM_ARGS -jar comparison.jar
