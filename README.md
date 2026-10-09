# TP2 · PostgreSQL + JPA + Flyway + Listas (evolución del TP1)

Catálogo de productos (solo lectura, espejo de DummyJSON, sin persistencia) + Favoritos y Listas (recursos propios persistidos en PostgreSQL con Spring Data JPA/Hibernate y versionado de esquema con Flyway).

Arquitectura: `Controller → Service → Repository (puerto) → Adapter JPA → PostgreSQL`, con DTOs desacoplados, Bean Validation, manejo uniforme de errores (`ProblemDetail`) y Swagger/OpenAPI.

## Cómo levantar el proyecto

Requiere Java 25 y PostgreSQL 16.

### 1. Levantar PostgreSQL con Docker (recomendado)

En la raíz hay un `docker-compose.yml` con la imagen oficial `postgres:16` (base `demo`, usuario `demo`, clave `demo`):

```bash
docker compose up -d
docker compose ps
```

Si no podés usar Docker, instalá PostgreSQL local y creá los mismos datos de conexión:

```sql
CREATE DATABASE demo;
CREATE USER demo WITH PASSWORD 'demo';
GRANT ALL PRIVILEGES ON DATABASE demo TO demo;
```

Conexión configurada en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/demo
spring.datasource.username=demo
spring.datasource.password=demo
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

> `ddl-auto=validate`: el esquema lo maneja Flyway, no Hibernate. Con `update`/`create` habría dos herramientas compitiendo por el mismo esquema.

### 2. Migraciones Flyway

Al arrancar, Flyway aplica en orden `src/main/resources/db/migration/V*__*.sql`:

| Migración | Qué hace |
|---|---|
| `V1__create_favoritos.sql` | Crea `favoritos (id, producto_id, nota, fecha_alta)` |
| `V2__create_listas.sql` | Crea `listas (id, nombre)` |
| `V3__add_lista_id_a_favoritos.sql` | Agrega `favoritos.lista_id → listas(id)` + índice |
| `V4__lista_id_obligatorio.sql` | Backfill a lista “Sin clasificar” y `SET NOT NULL` |

Arrancar:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

Confirmar que migró:

- En el log: `Flyway ... Successfully applied 4 migrations` y `FlywaySchemaHistory` creada.
- En la base:

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT * FROM favoritos LIMIT 5;
SELECT * FROM listas LIMIT 5;
```

Compilar / tests:

```bash
.\mvnw.cmd test
```

## Endpoints

| Método | Path | Éxito | Qué hace |
|---|---|---|---|
| GET | `/health` | 200 | Chequeo de salud |
| GET | `/ping` | 200 | Devuelve `pong` |
| GET | `/api/productos?limit=10&skip=0` | 200 | Lista productos (DummyJSON) |
| GET | `/api/productos/{id}` | 200 / 404 | Un producto por id |
| POST | `/api/favoritos` | 201 | Crea favorito `{productoId, nota, listaId}` |
| GET | `/api/favoritos` | 200 | Lista favoritos |
| GET | `/api/favoritos/{id}` | 200 / 404 | Un favorito |
| PUT | `/api/favoritos/{id}` | 200 / 400 / 404 | Actualiza favorito (incluye `listaId`) |
| DELETE | `/api/favoritos/{id}` | 204 / 404 | Elimina favorito |
| POST | `/api/listas` | 201 | Crea lista `{nombre}` |
| GET | `/api/listas` | 200 | Lista listas |
| GET | `/api/listas/{id}` | 200 / 404 | Una lista |
| GET | `/api/listas/{id}/favoritos` | 200 / 404 | Favoritos de una lista |
| DELETE | `/api/listas/{id}` | 204 / 404 / 409 | Elimina lista vacía; 409 si tiene favoritos |
| POST | `/api/listas/{origenId}/mover-favoritos` | 204 / 404 | Mueve todos los favoritos a `{destinoId}` y elimina la origen (atómico) |

Swagger UI: `http://localhost:8080/swagger-ui.html` (grupos **Productos**, **Favoritos** y **Listas**, cada endpoint con `@Operation`).

## Probar

Archivo `requests.http` (VS Code + REST Client o IntelliJ): incluye 1 éxito + 1 error por recurso, más mover-favoritos y el 409 al borrar lista con favoritos.

Ejemplo mínimo:

```bash
curl -X POST http://localhost:8080/api/listas -H "Content-Type: application/json" -d "{\"nombre\":\"Regalos\"}" -i  # 201 -> id 1
curl -X POST http://localhost:8080/api/favoritos -H "Content-Type: application/json" -d "{\"productoId\":1,\"nota\":\"Regalo\",\"listaId\":1}" -i  # 201
curl http://localhost:8080/api/listas/1/favoritos -i          # 200
curl -X DELETE http://localhost:8080/api/listas/1 -i          # 409 (tiene favoritos)
curl -X POST http://localhost:8080/api/listas/1/mover-favoritos -H "Content-Type: application/json" -d "{\"destinoId\":2}" -i  # 204
```

## Errores (`ProblemDetail`)

* Recurso inexistente → `404` (`RecursoNoEncontradoException`).
* Validación fallida → `400` con `errores: {campo: motivo}`.
* Borrar lista con favoritos → `409 Conflict` (`ListaConFavoritosException`, título "La lista todavía tiene favoritos").
* Caída/timeout de DummyJSON → `502` (`ServicioExternoException`).

## Qué cambió respecto del TP1 (puertos y adaptadores)

**Quedaron exactamente iguales:** `FavoritoService` (lógica), `FavoritoController` (salvo descripción Swagger), DTOs de forma (solo sumaron `listaId`), `ProductoService/Controller`, `GlobalExceptionHandler` (solo sumó el caso 409), dominio `Favorito` como concepto (solo sumó `listaId`).

**Se tocó / agregó en infraestructura:**

* Eliminado: `repository/InMemoryFavoritoRepository.java` (el `Map` en memoria).
* Nuevo: `persistence/entity/FavoritoEntity.java`, `persistence/entity/ListaEntity.java` (JPA, constructor vacío + mutable; el dominio no se toca con anotaciones).
* Nuevo: `persistence/FavoritoJpaRepository.java`, `persistence/ListaJpaRepository.java` (Spring Data las implementa solas; `findByListaId` resuelve el lado inverso sin `@OneToMany`).
* Nuevo: `repository/FavoritoRepositoryAdapter.java`, `repository/ListaRepositoryAdapter.java` (convierten Entity ↔ dominio y delegan en el Jpa).
* Nuevo: `model/Lista.java`, `repository/ListaRepository.java`, `dto/lista/*`, `service/ListaService.java`, `controller/ListaController.java`, `exception/ListaConFavoritosException.java`.
* Modificado: `model/Favorito.java` + `FavoritoRequestDTO/ResponseDTO/Mapper` (suman `listaId` referenciado por id, validado con `@NotNull`; el dominio no navega objetos).
* Modificado: `FavoritoRepository.java` (suma `findByListaId`), `FavoritoService.java` (inyecta `ListaRepository` solo para validar 404), `pom.xml`, `application.properties`, `docker-compose.yml`, `db/migration/V1..V4`.

**Por qué fue posible:** `FavoritoRepository` es un **puerto** (un contrato: `save`, `findAll`, `findById`, `deleteById`...). Tanto el `InMemoryFavoritoRepository` del TP1 como el `FavoritoRepositoryAdapter` JPA del TP2 son **adapters** intercambiables detrás de la misma interfaz. El `Service` y el `Controller` dependen del puerto, no de la implementación concreta, así que migrar de memoria a PostgreSQL no los obliga a cambiar (inversión de dependencias / arquitectura hexagonal).

## Evolución del esquema (por qué V4 y no editar V1–V3)

Las filas de favoritos creadas antes del punto 5 no tienen `lista_id`. Se resuelve con una migración **nueva** (`V4__lista_id_obligatorio.sql`: crea “Sin clasificar” si no existe → backfill de nulos → `SET NOT NULL`), nunca editando `V1/V2/V3` ya aplicadas, porque Flyway registra el checksum de cada migración en `flyway_schema_history` y rechaza arrancar si una ya aplicada cambió (garantía de historia inmutable y reproducible entre entornos).

## Transacciones (`POST /api/listas/{origenId}/mover-favoritos`)

`ListaService.moverFavoritos` está marcado con `@Transactional`: valida origen y destino (404), reasigna todos los favoritos origen → destino y elimina la origen en una sola transacción (atomicidad ACID: todo o nada).

Sin `@Transactional`, cada `save`/ `delete` confirmaría en su propia transacción: si una escritura fallara después de otra ya confirmada, quedaría la base inconsistente (p. ej. la mitad de los favoritos movidos y la otra mitad en la origen, o todos movidos pero la origen sin borrar → duplicación lógica / lista fantasma sin atomicidad ni rollback).

## Estructura

```
controller/  ProductoController, FavoritoController, ListaController, HealthController, PingController
service/     ProductoService, FavoritoService, ListaService (moverFavoritos @Transactional)
repository/  FavoritoRepository + FavoritoRepositoryAdapter, ListaRepository + ListaRepositoryAdapter (puertos + adapters)
persistence/ FavoritoJpaRepository, ListaJpaRepository (Spring Data) 
persistence/entity/ FavoritoEntity (@ManyToOne ListaEntity), ListaEntity
model/       Favorito (id, productoId, nota, fechaAgregado, listaId), Lista (id, nombre)
dto/favorito/ FavoritoRequestDTO (listaId @NotNull) + FavoritoResponseDTO + FavoritoMapper
dto/lista/   ListaRequestDTO + ListaResponseDTO + ListaMapper + MoverFavoritosRequestDTO
db/migration/ V1..V4
exception/   GlobalExceptionHandler + RecursoNoEncontradoException + ListaConFavoritosException (409) + ServicioExternoException
config/      RestClientConfig + OpenApiConfig
```

## Dependencias

* `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui`.
* `spring-boot-starter-data-jpa` — JPA/Hibernate.
* `org.postgresql:postgresql` (runtime) — driver.
* `spring-boot-starter-flyway` + `flyway-database-postgresql` — versionado (en Boot 4.x `flyway-core` solo no autoconfigura).
