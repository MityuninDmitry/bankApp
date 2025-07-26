# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем ВЕСЬ проект
COPY . .

# Собираем весь проект (из корня)
# RUN mvn clean install -DskipTests
RUN mvn clean package -pl ExchangeGenerator -am -DskipTests


# Финальный образ
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/ExchangeGenerator/target/ExchangeGenerator-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]