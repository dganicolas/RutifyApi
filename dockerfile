FROM azul/zulu-openjdk:17-latest

# Instala curl
RUN apt-get update && apt-get install -y curl

# Crea el directorio de trabajo
WORKDIR /app

# Descarga directa desde Dropbox (con dl=1)
RUN curl -L -o app.war "https://www.dropbox.com/scl/fi/jaxtzbq8demq22l7v9gbj/rutifyApi-0.0.1-SNAPSHOT-plain.war?rlkey=r1tgzhcny6us2ylicngtn99f7&st=16ehqk4w&dl=1"

# Ejecuta el .war
CMD ["java", "-jar", "app.war"]