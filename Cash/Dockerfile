# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY pom.xml .
COPY Cash/pom.xml Cash/
COPY Cash/src Cash/src

# Собираем с указанием относительного пути к родительскому POM
RUN mvn -f Cash/pom.xml clean package -DskipTests

# Финальный образ
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/Cash/target/Cash-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]