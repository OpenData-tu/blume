FROM openjdk:8-jre-alpine

ADD "target/blume-jar-with-dependencies.jar" "app/blume-jar-with-dependencies.jar"

ENTRYPOINT ["java", "-jar", "app/blume-jar-with-dependencies.jar"]