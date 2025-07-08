# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY pom.xml .
COPY Accounts/pom.xml Accounts/
COPY Accounts/src Accounts/src

# Собираем с указанием относительного пути к родительскому POM
RUN mvn -f Accounts/pom.xml clean package -DskipTests

# Финальный образ
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/Accounts/target/Accounts-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]