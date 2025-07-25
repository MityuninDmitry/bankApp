# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем сначала родительский POM, затем модуль
COPY . .

# Собираем с указанием относительного пути к родительскому POM
# RUN mvn -f Blocker/pom.xml clean package -DskipTests
RUN mvn clean package -pl Blocker -am -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/Blocker/target/Blocker-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]