FROM azul/zulu-openjdk:17-latest

RUN apt-get update && apt-get install -y curl

WORKDIR /app

RUN curl -L -o app.war "https://www.dropbox.com/scl/fi/1mt2b67lyjyscmlm5yt0n/rutifyApi-0.0.1-SNAPSHOT.war?rlkey=f24vaq5gd22xb0nigzlptqxz5&st=4glgzdqf&dl=1"

CMD ["java", "-jar", "app.war"]