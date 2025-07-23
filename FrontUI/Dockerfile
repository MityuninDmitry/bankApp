FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Копируем ВЕСЬ проект (чтобы Maven видел все модули)
COPY . .

# Собираем FrontUI и его зависимости (включая Common)
RUN mvn clean package -pl FrontUI -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/FrontUI/target/FrontUI-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]