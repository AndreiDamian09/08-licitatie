FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY src ./src

RUN javac src/*.java

EXPOSE 5000

CMD ["java", "-cp", "src", "MainServer"]
