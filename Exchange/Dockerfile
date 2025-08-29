# Стадия для загрузки зависимостей (кешируемый слой)
FROM maven:3.9.6-eclipse-temurin-21 as dependencies
WORKDIR /app

# Копируем ТОЛЬКО POM файлы сначала
COPY pom.xml .
COPY Exchange/pom.xml Exchange/pom.xml

# Скачиваем зависимости (этот слой будет кешироваться)
RUN mvn dependency:go-offline -B -pl Exchange

# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем кешированные зависимости
COPY --from=dependencies /root/.m2 /root/.m2

# Копируем исходный код
COPY . .

# Собираем проект (используем кешированные зависимости)
RUN mvn clean package -pl Exchange -am -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/Exchange/target/Exchange-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]