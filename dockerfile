FROM azul/zulu-openjdk:17-latest

# Instala curl
RUN apt-get update && apt-get install -y curl

# Crea el directorio de trabajo
WORKDIR /app

# Descarga directa desde Dropbox (con dl=1)
RUN curl -L -o app.war "https://www.dropbox.com/scl/fi/zusep9fp0fk0oxt99y2sh/rutify.war?rlkey=44uscw5me4hs8s0mozel3xu5l&st=sn280w6j&dl=1"

# Ejecuta el .war
CMD ["java", "-jar", "app.war"]