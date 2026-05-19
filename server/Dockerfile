FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/server-1.0-SNAPSHOT.jar server.jar
COPY target/lib/ lib/

EXPOSE 1337/udp

CMD ["java", "-cp", "server.jar:lib/*", "server.ServerApp"]
