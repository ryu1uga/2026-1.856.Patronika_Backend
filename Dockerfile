# ──────────────────────────────────────────────
# Stage 1 – Build
# ──────────────────────────────────────────────
FROM gradle:8.10-jdk21 AS build

WORKDIR /home/gradle/src

# 1. Copiar solo los archivos de configuración primero
#    para aprovechar la caché de capas de Docker.
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./

# 2. Permisos antes de ejecutar
RUN chmod +x gradlew

# 3. Descargar dependencias sin compilar el código fuente.
#    Esta capa se invalida solo si build.gradle.kts cambia.
RUN ./gradlew dependencies --no-daemon \
      --console=plain \
      -Dorg.gradle.jvmargs="-Xmx512m -Dfile.encoding=UTF-8"

# 4. Copiar el resto del código fuente
COPY src/ src/

# 5. Compilar el fat-jar ignorando tests
RUN ./gradlew bootJar -x test --no-daemon \
      --console=plain \
      -Dorg.gradle.jvmargs="-Xmx512m -Dfile.encoding=UTF-8"

# ──────────────────────────────────────────────
# Stage 2 – Runtime
# ──────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiar el jar desde el stage de build.
# El wildcard excluye el "-plain.jar" que Gradle genera como artefacto secundario.
COPY --from=build /home/gradle/src/build/libs/*-[0-9]*.jar app.jar

# Puerto por defecto; Render lo sobreescribe con su propia variable PORT.
ENV PORT=8080

# Activar el perfil staging + opciones JVM recomendadas para contenedores pequeños:
#   -XX:+UseContainerSupport   → respeta los límites de memoria del contenedor
#   -XX:MaxRAMPercentage=75    → usa hasta el 75% de la RAM disponible
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dspring.profiles.active=staging", \
  "-Dserver.port=${PORT}", \
  "-jar", "/app/app.jar"]
