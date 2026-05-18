Utilice el siguiente código para crear una base de datos local para probar el modo desarrollador:

```
docker run -d --name patronika-db -e POSTGRES_DB=patronika_db -e POSTGRES_USER=patronika -e POSTGRES_PASSWORD=patronika -p 5433:5432 -v patronika_pgdata:/var/lib/postgresql/data postgres:16
```