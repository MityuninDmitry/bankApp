# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY pom.xml .
COPY Blocker/pom.xml Blocker/
COPY Blocker/src Blocker/src

# Собираем с указанием относительного пути к родительскому POM
RUN mvn -f Blocker/pom.xml clean package -DskipTests

# Финальный образ
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/Blocker/target/Blocker-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]