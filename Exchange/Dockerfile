# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY pom.xml .
COPY Exchange/pom.xml Exchange/
COPY Exchange/src Exchange/src

# Собираем с указанием относительного пути к родительскому POM
RUN mvn -f Exchange/pom.xml clean package -DskipTests

# Финальный образ
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/Exchange/target/Exchange-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]