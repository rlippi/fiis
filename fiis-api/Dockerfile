# syntax=docker/dockerfile:1

# ==========================================================
# Build stage — compila o jar com Maven + JDK 21
# ==========================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom primeiro para aproveitar o cache do Docker nas dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código e gera o jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================================
# Runtime stage — imagem pequena só com JRE 21 e o jar final
# ==========================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Porta padrão em dev; em produção o Render injeta a variável PORT
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
