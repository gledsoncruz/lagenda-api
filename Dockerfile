# Etapa 1: Compilação com Maven e OpenJDK 21 (Temurin)
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Execução com JRE leve
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
#EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
#ENTRYPOINT ["java", \
#            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
#            "-jar", "app.jar"]