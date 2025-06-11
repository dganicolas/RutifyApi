FROM azul/zulu-openjdk:17-latest

RUN apt-get update && apt-get install -y curl

WORKDIR /app

RUN curl -L -o app.war "https://www.dropbox.com/scl/fi/al8llx0w9j5vdfzonjo03/rutifyApi-0.0.1-SNAPSHOT.war?rlkey=ucjp9l8xd3gr1haqpw01zwy8l&st=8f48qfyc&dl=1"

CMD ["java", "-jar", "app.war"]