FROM openjdk:27-ea-29-jdk-slim-trixie
COPY spring-comparison/build/libs/spring-comparison.jar /app/
VOLUME /app/logs
ARG deployenv
RUN echo $deployenv
ENV SPRING_PROFILES_ACTIVE $deployenv
ENTRYPOINT ["bin/sh", "-c", "java -jar /app/spring-comparison.jar"]