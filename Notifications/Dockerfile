# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY . .

# Собираем с указанием относительного пути к родительскому POM
RUN mvn clean package -pl Notifications -am -DskipTests

# Финальный образ
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/Notifications/target/Notifications-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]