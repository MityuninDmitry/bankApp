# Стадия сборки с кешированием зависимостей
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем только файлы, необходимые для разрешения зависимостей
COPY pom.xml .
COPY Accounts/pom.xml Accounts/
COPY Cash/pom.xml Cash/
COPY Common/pom.xml Common/
COPY Transfer/pom.xml Transfer/
COPY Exchange/pom.xml Exchange/
COPY ExchangeGenerator/pom.xml ExchangeGenerator/
COPY Blocker/pom.xml Blocker/
COPY Notifications/pom.xml Notifications/
COPY Gateway/pom.xml Gateway/
COPY FrontUI/pom.xml FrontUI/

# Скачиваем зависимости (кешируем этот слой)
RUN mvn dependency:go-offline -B

# Копируем весь исходный код
COPY . .

# Аргумент для выбора сервиса для сборки
ARG SERVICE_NAME

# Собираем конкретный сервис
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# Финальный образ
FROM openjdk:21-jdk-slim
WORKDIR /app

# Аргумент для пути к jar файлу
ARG JAR_PATH

COPY --from=builder /app/${JAR_PATH} app.jar
EXPOSE ${SERVER_PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.jar"]