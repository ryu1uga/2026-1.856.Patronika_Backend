# Patronika Backend

Backend de **Patronika**, una API REST desarrollada con **Kotlin**, **Spring Boot**, **Spring MVC**, **Spring Data JPA** y **PostgreSQL**.

El backend permite manejar usuarios, autenticación, patrones, publicaciones, comentarios, tutoriales y progreso de tutoriales.

---

## Tecnologías principales

- Kotlin
- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- JWT
- Cloudinary
- SMTP para envío de correos
- Docker

---

## Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- Java 21
- Docker
- Gradle o usar el wrapper incluido:
  - Linux/macOS: `./gradlew`
  - Windows: `gradlew.bat`
- Un cliente HTTP como:
  - Postman
  - Insomnia
  - IntelliJ HTTP Client
  - curl

---

## Base de datos local con Docker

Utilice el siguiente código para crear una base de datos local para probar el modo desarrollador:

```bash
docker run -d --name patronika-db -e POSTGRES_DB=patronika_db -e POSTGRES_USER=patronika -e POSTGRES_PASSWORD=patronika -p 5433:5432 -v patronika_pgdata:/var/lib/postgresql/data postgres:16
```

Esto crea una base de datos PostgreSQL local con los siguientes datos:

| Campo       | Valor          |
|-------------|----------------|
| Host        | `127.0.0.1`    |
| Puerto local| `5433`         |
| Base de datos | `patronika_db` |
| Usuario     | `patronika`    |
| Contraseña  | `patronika`    |

---

## Configuración del ambiente local

El proyecto tiene configuración para el perfil `dev`.

Para ejecutar localmente se debe activar el perfil:

```bash
SPRING_PROFILES_ACTIVE=dev
```

También se necesitan variables de entorno para JWT, correo y Cloudinary.

Ejemplo de variables necesarias:

```bash
JWT_SECRET_BASE64=valor_base64_para_firmar_tokens

SMTP_HOST=smtp.example.com
SMTP_PORT=2525
SMTP_USERNAME=usuario_smtp
SMTP_PASSWORD=password_smtp

CLOUDINARY_CLOUD_NAME=nombre_cloudinary
CLOUDINARY_API_KEY=api_key
CLOUDINARY_API_SECRET=api_secret
```

> **Importante:** no subir valores reales de estas variables al repositorio.

---

## Migraciones de base de datos

El proyecto usa **Flyway** para crear y versionar el esquema de base de datos.

Las migraciones están en:

```
src/main/resources/db.migration
```

Al iniciar la aplicación, Flyway ejecuta automáticamente las migraciones pendientes.

---

## Documentación Swagger / OpenAPI

Cuando el proyecto se ejecuta con el perfil `dev`, Swagger está habilitado.

Puedes abrir la documentación interactiva en:

```
http://localhost:8080/swagger-ui/index.html
```

También puedes consultar el JSON de OpenAPI en:

```
http://localhost:8080/v3/api-docs
```

Swagger es la forma recomendada de explorar los endpoints, probar requests y revisar los modelos disponibles.

---

## Formato general de respuestas

La API responde usando una estructura común:

```json
{
  "success": true,
  "data": {}
}
```

En errores, la API puede responder con mensajes relacionados a validación, autenticación, recursos no encontrados o conflictos.

Ejemplos comunes de códigos HTTP:

| Código | Significado |
|--------|-------------|
| `200 OK` | Operación exitosa |
| `201 Created` | Recurso creado |
| `400 Bad Request` | Datos inválidos |
| `401 Unauthorized` | Token inválido, expirado o ausente |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Conflicto con datos existentes |

---

## Autenticación

La autenticación se maneja con JWT.

El flujo general es:

1. Solicitar código de verificación al correo.
2. Verificar el código recibido.
3. Registrar usuario usando el token de verificación.
4. Iniciar sesión.
5. Usar el access token para consumir endpoints protegidos.
6. Renovar sesión con refresh token cuando sea necesario.
7. Cerrar sesión con logout.

---

## Endpoints de autenticación

Base path:

```
/api/auth
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "usuario",
  "password": "password"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": {
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token"
  }
}
```

---

### Refresh token

```http
POST /api/auth/refresh
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "jwt_refresh_token"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": {
    "accessToken": "nuevo_access_token",
    "refreshToken": "nuevo_refresh_token"
  }
}
```

---

### Logout

```http
POST /api/auth/logout/{userId}
Authorization: Bearer jwt_access_token
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "jwt_refresh_token"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": "Cerró sesión exitosamente"
}
```

---

### Solicitar código de verificación

```http
POST /api/auth/register/request-code
Content-Type: application/json
```

Body:

```json
{
  "email": "usuario@correo.com"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": "Código enviado al correo"
}
```

---

### Verificar código

```http
POST /api/auth/register/verify-code
Content-Type: application/json
```

Body:

```json
{
  "email": "usuario@correo.com",
  "code": "123456"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": {
    "verificationToken": "token_de_verificacion"
  }
}
```

---

### Registrar usuario

```http
POST /api/auth/register
Content-Type: application/json
```

Body:

```json
{
  "verificationToken": "token_de_verificacion",
  "user": {
    "username": "usuario",
    "email": "usuario@correo.com",
    "password": "password",
    "isAdmin": false,
    "status": 0,
    "activateNotification": true,
    "suspensionEndDate": null
  }
}
```

Respuesta esperada:

```json
{
  "success": true,
  "data": {
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token"
  }
}
```

---

## Uso de token JWT

Para consumir endpoints protegidos, enviar el access token en el header:

```http
Authorization: Bearer jwt_access_token
```

Algunos endpoints también usan el header `UserId` para identificar al usuario que realiza la operación:

```http
UserId: uuid-del-usuario
```

---

## Endpoints de usuarios

Base path:

```
/api/users
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/users` | Lista todos los usuarios |
| `GET` | `/api/users/{id}` | Obtiene un usuario por ID |
| `POST` | `/api/users` | Crea un usuario |
| `PUT` | `/api/users/{id}` | Actualiza un usuario |
| `DELETE` | `/api/users/{id}/{username}` | Elimina un usuario |

> Todos los endpoints requieren `Authorization: Bearer jwt_access_token`.

Ejemplo de creación/actualización:

```json
{
  "username": "usuario",
  "email": "usuario@correo.com",
  "password": "password",
  "isAdmin": false,
  "status": 0,
  "activateNotification": true,
  "suspensionEndDate": null
}
```

---

## Endpoints de patrones

Base path:

```
/api/patterns
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/patterns` | Lista todos los patrones |
| `GET` | `/api/patterns/{id}` | Obtiene un patrón por ID |
| `GET` | `/api/patterns/user/{userId}` | Lista patrones de un usuario |
| `POST` | `/api/patterns` | Crea un patrón |
| `PUT` | `/api/patterns/{id}` | Actualiza un patrón |
| `DELETE` | `/api/patterns/{id}` | Elimina un patrón |

> Todos los endpoints requieren `Authorization: Bearer jwt_access_token`.

### Crear patrón

Este endpoint consume `multipart/form-data`.

```http
POST /api/patterns
Authorization: Bearer jwt_access_token
UserId: uuid-del-usuario
Content-Type: multipart/form-data
```

Campos:

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `name` | text | Sí | Nombre del patrón |
| `size` | text/number | Sí | Tamaño del patrón. Debe estar entre `1` y `100` |
| `image` | file | No | Imagen opcional del patrón |

Ejemplo usando curl:

```bash
curl -X POST http://localhost:8080/api/patterns \
  -H "Authorization: Bearer jwt_access_token" \
  -H "UserId: uuid-del-usuario" \
  -F "name=Patrón ejemplo" \
  -F "size=20" \
  -F "image=@/ruta/imagen.png"
```

---

## Endpoints de publicaciones

Base path:

```
/api/publications
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/publications` | Lista todas las publicaciones |
| `GET` | `/api/publications/{id}` | Obtiene una publicación por ID |
| `POST` | `/api/publications` | Crea una publicación |
| `PUT` | `/api/publications/{id}` | Actualiza una publicación |
| `DELETE` | `/api/publications/{id}` | Elimina una publicación |

> Todos los endpoints requieren `Authorization: Bearer jwt_access_token`.

### Crear publicación

Este endpoint consume `multipart/form-data`.

```http
POST /api/publications
Authorization: Bearer jwt_access_token
Content-Type: multipart/form-data
```

Parámetros:

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `userId` | UUID | Sí | ID del usuario |
| `patternId` | UUID | Sí | ID del patrón asociado |
| `publication` | JSON string | Sí | Datos de la publicación |
| `file` | file | No | Imagen o archivo asociado |