FROM amazoncorretto:17-alpine-jdk
VOLUME /tmp
COPY build/libs/*.war rutifyApi-0.0.1-SNAPSHOT-plain.war
CMD ["java", "-jar", "/rutifyApi-0.0.1-SNAPSHOT-plain.war"]