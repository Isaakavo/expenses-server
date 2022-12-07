FROM openjdk:17-jdk-slim

WORKDIR /app
COPY gradle/ gradle
COPY gradlew build.gradle.kts ./

RUN ls -al
RUN ./gradlew build -x test

COPY src ./src

CMD ["./gradlew", "bootRun"]

#ARG JAR_FILE=build/libs/*.jar
#COPY ${JAR_FILE} application.jar
#ENTRYPOINT ["java", "-jar", "application.jar"]