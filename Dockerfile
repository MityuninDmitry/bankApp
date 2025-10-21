# syntax=docker/dockerfile:1.4
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем только файлы, необходимые для разрешения зависимостей
COPY pom.xml .
COPY Accounts/pom.xml Accounts/
COPY Cash/pom.xml Cash/
COPY Common/pom.xml Common/
COPY Transfer/pom.xml Transfer/
COPY Notifications/pom.xml Notifications/
COPY Gateway/pom.xml Gateway/
COPY FrontUI/pom.xml FrontUI/
COPY Auth/pom.xml Auth/

# Скачиваем зависимости (кешируем этот слой)
RUN mvn dependency:go-offline -B

# Копируем весь исходный код
COPY . .

# Создаем отдельные стадии для каждого сервиса
FROM builder as auth-builder
RUN mvn clean package -pl Auth -am -DskipTests

FROM builder as accounts-builder
RUN mvn clean package -pl Accounts -am -DskipTests

FROM builder as cash-builder
RUN mvn clean package -pl Cash -am -DskipTests

FROM builder as transfer-builder
RUN mvn clean package -pl Transfer -am -DskipTests

FROM builder as notifications-builder
RUN mvn clean package -pl Notifications -am -DskipTests

FROM builder as gateway-builder
RUN mvn clean package -pl Gateway -am -DskipTests

FROM builder as frontui-builder
RUN mvn clean package -pl FrontUI -am -DskipTests

# Базовый финальный образ
FROM openjdk:21-jdk-slim as base
WORKDIR /app
EXPOSE ${SERVER_PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.jar"]

# Финальные образы для каждого сервиса
FROM base as auth-service
COPY --from=auth-builder /app/Auth/target/Auth-*.jar app.jar

FROM base as accounts-service
COPY --from=accounts-builder /app/Accounts/target/Accounts-*.jar app.jar

FROM base as cash-service
COPY --from=cash-builder /app/Cash/target/Cash-*.jar app.jar

FROM base as transfer-service
COPY --from=transfer-builder /app/Transfer/target/Transfer-*.jar app.jar

FROM base as notifications-service
COPY --from=notifications-builder /app/Notifications/target/Notifications-*.jar app.jar

FROM base as gateway-service
COPY --from=gateway-builder /app/Gateway/target/Gateway-*.jar app.jar

FROM base as frontui-service
COPY --from=frontui-builder /app/FrontUI/target/FrontUI-*.jar app.jar