FROM eclipse-temurin:17-jdk

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} productservice.jar

ENTRYPOINT ["java", "-jar", "/productservice.jar"]

EXPOSE 9296

