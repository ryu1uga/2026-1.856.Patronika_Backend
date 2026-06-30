# Patronika Backend

Backend de **Patronika**, una API REST desarrollada con **Kotlin**, **Spring Boot**, **Spring MVC**, **Spring Data JPA** y **PostgreSQL**.

El backend permite manejar usuarios, autenticación, patrones, publicaciones, comentarios, tutoriales, progreso de tutoriales, biblioteca de patrones y patrones publicados.

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

| Campo         | Valor          |
|---------------|----------------|
| Host          | `127.0.0.1`    |
| Puerto local  | `5433`         |
| Base de datos | `patronika_db` |
| Usuario       | `patronika`    |
| Contraseña    | `patronika`    |

---

## Configuración del ambiente local

El proyecto tiene configuración para el perfil `dev`.

Para ejecutar localmente se debe activar el perfil:

```bash
SPRING_PROFILES_ACTIVE=dev
```

También se necesitan variables de entorno para JWT, correo y Cloudinary:

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
src/main/resources/db/migration
```

| Versión | Descripción |
|---------|-------------|
| V1 | Esquema inicial (users, patterns, publications, comments, tutorials, tutorial_progresses, refresh_tokens, email_verification_codes) |
| V2 | Tabla `pattern_library` |
| V3 | Columna `report_count` en `comments` + tabla `published_patterns` |

Al iniciar la aplicación, Flyway ejecuta automáticamente las migraciones pendientes.

---

## Documentación Swagger / OpenAPI

Cuando el proyecto se ejecuta con el perfil `dev`, Swagger está habilitado.

```
http://localhost:8080/swagger-ui/index.html
```

También puedes consultar el JSON de OpenAPI en:

```
http://localhost:8080/v3/api-docs
```

---

## Formato general de respuestas

```json
{
  "success": true,
  "data": {}
}
```

Códigos HTTP comunes:

| Código | Significado |
|--------|-------------|
| `200 OK` | Operación exitosa |
| `201 Created` | Recurso creado |
| `400 Bad Request` | Datos inválidos |
| `401 Unauthorized` | Token inválido, expirado o ausente |
| `404 Not Found` | Recurso no encontrado |
| `409 Conflict` | Conflicto con datos existentes |
| `415 Unsupported Media Type` | Content-Type no soportado |

---

## Autenticación

La autenticación se maneja con JWT. El flujo general es:

1. Solicitar código de verificación al correo (`/register/request-code`).
2. Verificar el código recibido (`/verify-code`).
3. Registrar usuario (`/register`).
4. Iniciar sesión (`/login`).
5. Usar el access token para consumir endpoints protegidos.
6. Renovar sesión con refresh token cuando sea necesario (`/refresh`).
7. Cerrar sesión (`/logout/{id}`).

---

## Endpoints de autenticación

Base path: `/api/auth`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | Iniciar sesión |
| `POST` | `/api/auth/refresh` | Renovar access token |
| `POST` | `/api/auth/logout/{id}` | Cerrar sesión |
| `POST` | `/api/auth/register/request-code` | Solicitar código de verificación (registro) |
| `POST` | `/api/auth/verify-code` | Verificar código |
| `POST` | `/api/auth/register` | Registrar usuario |
| `POST` | `/api/auth/change-password/request-code` | Solicitar código para cambio de contraseña |
| `POST` | `/api/auth/change-password` | Cambiar contraseña |

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "usuario",
  "password": "password"
}
```

Respuesta:

```json
{
  "success": true,
  "data": {
    "userId": "uuid",
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token",
    "status": 0,
    "suspensionEndDate": null,
    "suspensionDaysRemaining": null
  }
}
```

### Refresh token

```http
POST /api/auth/refresh
Content-Type: application/json
```

```json
{ "refreshToken": "jwt_refresh_token" }
```

### Logout

```http
POST /api/auth/logout/{userId}
Authorization: Bearer jwt_access_token
Content-Type: application/json
```

```json
{ "refreshToken": "jwt_refresh_token" }
```

### Solicitar código de verificación (registro)

```http
POST /api/auth/register/request-code
Content-Type: application/json
```

```json
{ "email": "usuario@correo.com" }
```

El código enviado es de **6 dígitos**.

### Solicitar código para cambio de contraseña

```http
POST /api/auth/change-password/request-code
Content-Type: application/json
```

```json
{ "email": "usuario@correo.com" }
```

### Verificar código

```http
POST /api/auth/verify-code
Content-Type: application/json
```

```json
{
  "email": "usuario@correo.com",
  "code": "123456"
}
```

### Registrar usuario

Este endpoint consume `multipart/form-data`.

```http
POST /api/auth/register
Content-Type: multipart/form-data
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `userRequest` | JSON string | Sí | Datos del usuario como JSON en texto plano |
| `file` | file | No | Foto de perfil |

Ejemplo con curl:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -F 'userRequest={"username":"juan","email":"juan@mail.com","password":"1234"}' \
  -F 'file=@foto.jpg'
```

### Cambiar contraseña

```http
POST /api/auth/change-password
Content-Type: application/json
```

```json
{
  "email": "usuario@correo.com",
  "password": "nueva_password"
}
```

---

## Uso de token JWT

Para consumir endpoints protegidos, enviar el access token en el header:

```http
Authorization: Bearer jwt_access_token
```

Algunos endpoints usan el header `UserId` para identificar al usuario que realiza la operación:

```http
UserId: uuid-del-usuario
```

---

## Endpoints de usuarios

Base path: `/api/users`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/users` | Lista todos los usuarios |
| `GET` | `/api/users/{id}` | Obtiene un usuario por ID |
| `POST` | `/api/users` | Crea un usuario |
| `PUT` | `/api/users/{id}` | Actualiza un usuario |
| `PUT` | `/api/users/{id}/profile-image` | Actualiza foto de perfil |
| `DELETE` | `/api/users/{id}/{username}` | Elimina un usuario |

### Crear / actualizar usuario

```http
POST /api/users
Content-Type: multipart/form-data
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `userRequest` | JSON string | Sí | Datos del usuario como JSON en texto plano |
| `file` | file | No | Foto de perfil |

### Actualizar foto de perfil

```http
PUT /api/users/{id}/profile-image
Content-Type: multipart/form-data
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `file` | file | Sí | Nueva foto de perfil |

---

## Endpoints de patrones

Base path: `/api/patterns`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/patterns` | Lista todos los patrones |
| `GET` | `/api/patterns/{id}` | Obtiene un patrón por ID |
| `GET` | `/api/patterns/user/{userId}` | Lista patrones de un usuario |
| `POST` | `/api/patterns` | Crea un patrón |
| `PUT` | `/api/patterns/{id}` | Actualiza un patrón |
| `DELETE` | `/api/patterns/{id}` | Elimina un patrón |

### Crear / actualizar patrón

```http
POST /api/patterns
Content-Type: application/json
Authorization: Bearer jwt_access_token
```

```json
{
  "name": "Mi patrón",
  "width": 20,
  "height": 30
}
```

---

## Endpoints de publicaciones

Base path: `/api/publications`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/publications` | Lista todas las publicaciones (más recientes primero) |
| `GET` | `/api/publications/{id}` | Obtiene una publicación por ID |
| `POST` | `/api/publications` | Crea una publicación |
| `PUT` | `/api/publications/{id}` | Actualiza una publicación |
| `DELETE` | `/api/publications/{id}` | Elimina una publicación |

### Crear publicación

Este endpoint consume `multipart/form-data`.

```http
POST /api/publications
Content-Type: multipart/form-data
Authorization: Bearer jwt_access_token
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `publication` | JSON string | Sí | Datos de la publicación como JSON en texto plano |
| `file` | file | No | Imagen de la publicación |

El campo `publication` debe contener `userId`, `patternId`, `description` y `technique`.

Ejemplo con curl:

```bash
curl -X POST http://localhost:8080/api/publications \
  -H "Authorization: Bearer jwt_access_token" \
  -F 'publication={"userId":"uuid-usuario","patternId":"uuid-patron","description":"Mi publicación","technique":0}' \
  -F 'file=@imagen.jpg'
```

> Al crear una publicación, se registra automáticamente una entrada en `published_patterns` para la combinación usuario-patrón (si no existe ya).

---

## Endpoints de comentarios

Base path: `/api/comments`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/comments` | Lista todos los comentarios |
| `GET` | `/api/comments/{id}` | Obtiene un comentario por ID |
| `POST` | `/api/comments` | Crea un comentario |
| `PUT` | `/api/comments/{id}` | Actualiza un comentario |
| `POST` | `/api/comments/{id}/report` | Reporta un comentario (incrementa contador) |
| `DELETE` | `/api/comments/{id}` | Elimina un comentario |

### Crear comentario

```http
POST /api/comments
Authorization: Bearer jwt_access_token
UserId: uuid-del-usuario
Content-Type: application/json
```

```json
{
  "publicationId": "uuid-de-publicacion",
  "content": "Texto del comentario"
}
```

### Reportar comentario

```http
POST /api/comments/{id}/report
Authorization: Bearer jwt_access_token
```

Incrementa el campo `reportCount` del comentario en 1. Retorna el comentario actualizado.

---

## Endpoints de tutoriales

Base path: `/api/tutorials`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/tutorials` | Lista todos los tutoriales |
| `GET` | `/api/tutorials/{id}` | Obtiene un tutorial por ID |
| `POST` | `/api/tutorials` | Crea un tutorial |
| `PUT` | `/api/tutorials/{id}` | Actualiza un tutorial |
| `DELETE` | `/api/tutorials/{id}` | Elimina un tutorial |

---

## Endpoints de progreso de tutoriales

Base path: `/api/tutorial-progresses`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/tutorial-progresses` | Lista todos los progresos |
| `GET` | `/api/tutorial-progresses/{id}` | Obtiene un progreso por ID |
| `POST` | `/api/tutorial-progresses` | Crea un progreso |
| `PUT` | `/api/tutorial-progresses/{id}` | Actualiza un progreso |
| `DELETE` | `/api/tutorial-progresses/{id}` | Elimina un progreso |

---

## Endpoints de biblioteca de patrones

Base path: `/api/pattern-library`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/pattern-library` | Guarda un patrón en la biblioteca del usuario |
| `DELETE` | `/api/pattern-library` | Elimina un patrón de la biblioteca |
| `GET` | `/api/pattern-library/user/{userId}` | Lista patrones guardados por el usuario |
| `GET` | `/api/pattern-library/user/{userId}/all` | Lista todos los patrones del usuario (propios + guardados) |

```json
{
  "userId": "uuid-usuario",
  "patternId": "uuid-patron"
}
```

---

## Endpoints de patrones publicados

Base path: `/api/published-patterns`

Registra qué usuarios han publicado qué patrones. Se crea automáticamente al publicar. La combinación `userId + patternId` es única.

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/published-patterns` | Lista todos los registros |
| `GET` | `/api/published-patterns/user/{userId}` | Patrones publicados por un usuario |
| `GET` | `/api/published-patterns/pattern/{patternId}` | Usuarios que publicaron un patrón |
