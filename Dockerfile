# Build stage
FROM gradle:8.10-jdk21 AS build
WORKDIR /home/gradle/src

# 1. Copia primero los archivos de configuración y el wrapper
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# 2. CORRECCIÓN: Dar permisos de ejecución ANTES de usar el gradlew
RUN chmod +x gradlew

# 3. Descargar y cachear dependencias de forma eficiente
RUN ./gradlew build -x test --dry-run --no-daemon

# Copia todo el resto del código fuente
COPY . .

# Limita memoria para evitar que Render mate el proceso por falta de RAM (OOM)
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx512m -Dfile.encoding=UTF-8"

# Compilar el archivo ejecutable ignorando los tests
RUN ./gradlew bootJar -x test --no-daemon --info

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# 4. CORRECCIÓN: Filtrar para copiar SOLO el jar ejecutable y evitar conflictos con el plain.jar
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar
# NOTA: Si tu proyecto no tiene la versión "-SNAPSHOT" en el build.gradle,
# puedes usar: COPY --from=build /home/gradle/src/build/libs/*[0-9].jar app.jar

ENV PORT=8080
ENTRYPOINT ["java","-jar","/app/app.jar"]