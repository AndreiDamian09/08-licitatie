FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY src ./src

RUN javac src/*.java

CMD ["java", "-cp", "src", "MainClient", "auction-server", "5000"]
