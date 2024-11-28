FROM amazoncorretto:17-alpine3.18

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar" ]