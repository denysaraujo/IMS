# Etapa 1: Construção do JAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar arquivos necessários para o build
COPY pom.xml .
COPY src ./src

# Construir o JAR
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final para executar a aplicação
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiar o JAR da etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expor a porta padrão
EXPOSE 8080

# Comando para executar o app
ENTRYPOINT ["java", "-jar", "app.jar"]

