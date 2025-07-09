# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY pom.xml .
COPY ExchangeGenerator/pom.xml ExchangeGenerator/
COPY ExchangeGenerator/src ExchangeGenerator/src

# Собираем с указанием относительного пути к родительскому POM
RUN mvn -f ExchangeGenerator/pom.xml clean package -DskipTests

# Финальный образ
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/ExchangeGenerator/target/ExchangeGenerator-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]