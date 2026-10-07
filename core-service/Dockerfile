# Build trong container nên máy không cần cài Maven.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Tải thư viện trước, tách khỏi source để lần build sau dùng lại cache
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Unit test chạy riêng bằng mvn test; ở đây chỉ đóng gói
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/core-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
