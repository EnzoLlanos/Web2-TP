# TP3 · Autenticación, autorización, pruebas e integración continua

Evolución del TP2 sobre el mismo repositorio y los mismos paquetes. Catálogo de productos (solo lectura, espejo de DummyJSON) + Favoritos y Listas persistidos en PostgreSQL, ahora con:

- registro y login con contraseña hasheada (BCrypt) y JWT;
- autorización por rol (`USER` / `ADMIN`) y por propiedad de recursos;
- pruebas unitarias (`*Test`) y de integración con Testcontainers (`*IT`);
- CI con GitHub Actions ejecutando `./mvnw verify`;
- despliegue con configuración por variables de entorno y `GET /health`.

Arquitectura: `Controller → Service → Repository (puerto) → Adapter JPA → PostgreSQL`, con DTOs desacoplados, Bean Validation, manejo uniforme de errores (`ProblemDetail`), Swagger/OpenAPI y Spring Security stateless con JWT.

## Historial de tags

| Tag | Contenido |
|---|---|
| `tp1` | Primera entrega (favoritos en memoria + catálogo de productos). |
| `tp2` | Persistencia en PostgreSQL con JPA/Flyway, listas y favoritos. |
| `main` | TP3: seguridad JWT, autorización, tests, CI y despliegue. |

## Cómo levantar el proyecto

Requiere Java 25. Para los tests de integración y para la base local, PostgreSQL 16 (Docker o instalación local).

### 1. Levantar PostgreSQL con Docker (recomendado)

```bash
docker compose up -d
docker compose ps
```

Si no usás Docker, instalá PostgreSQL local y creá los mismos datos de conexión:

```sql
CREATE DATABASE demo;
CREATE USER demo WITH PASSWORD 'demo';
GRANT ALL PRIVILEGES ON DATABASE demo TO demo;
```

### 2. Variables de entorno

`application.properties` usa `${VARIABLE:valorPorDefecto}`, así que en local funciona sin configurar nada. En CI/Render se pisan:

| Variable | Default local | Para qué |
|---|---|---|
| `DB_HOST` | `localhost` | Host de PostgreSQL (Render lo provee) |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `demo` | Nombre de la base |
| `DB_USER` | `demo` | Usuario de la base |
| `DB_PASSWORD` | `demo` | Clave de la base |
| `DB_JDBC_URL` | *(vacío → se arma con `DB_HOST/PORT/NAME`)* | Override opcional: URL **JDBC** completa (no usar `postgres://`) |
| `JWT_SECRET` | secreto de desarrollo (32+ bytes) | Firma HS256 de los JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Vigencia del token (24 h) |
| `PORT` | `8080` | Puerto HTTP (Render lo inyecta) |

> En producción `JWT_SECRET` debe ser un valor aleatorio de al menos 32 bytes y **no** el default del repositorio.

### 3. Migraciones Flyway

Al arrancar, Flyway aplica en orden `src/main/resources/db/migration/V*__*.sql`:

| Migración | Qué hace |
|---|---|
| `V1__create_favoritos.sql` | Crea `favoritos (id, producto_id, nota, fecha_alta)` |
| `V2__create_listas.sql` | Crea `listas (id, nombre)` |
| `V3__add_lista_id_a_favoritos.sql` | Agrega `favoritos.lista_id → listas(id)` + índice |
| `V4__lista_id_obligatorio.sql` | Backfill a lista “Sin clasificar” y `SET NOT NULL` |
| `V5__create_usuarios.sql` | Crea `usuarios (id, email único, password_hash, rol)` |
| `V6__add_usuario_id_a_listas.sql` | Agrega `listas.usuario_id → usuarios(id)`; las listas viejas quedan asignadas a un usuario legado ADMIN sin login |

Arrancar:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

Confirmar que migró:

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

## Autenticación y uso de la API

### Registro

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@demo.com","password":"secreta123"}' -i
# 201 Created  -> { "id": 1, "email": "ana@demo.com", "rol": "USER" }
# 409 Conflict si el email ya existe
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@demo.com","password":"secreta123"}' -i
# 200 OK -> { "token": "eyJ...", "tipo": "Bearer", "expiraEnMs": 86400000 }
# 401 Unauthorized si las credenciales son inválidas
```

### Request protegida

```bash
TOKEN="eyJ..."
curl http://localhost:8080/api/listas -H "Authorization: Bearer $TOKEN" -i
# 200 con las listas propias
# 401 si falta el token o es inválido/vencido
```

### Endpoints

| Método | Path | Auth | Éxito | Errores relevantes |
|---|---|---|---|---|
| GET | `/health` | público | 200 | — |
| GET | `/ping` | público | 200 | — |
| GET | `/api/productos?limit=10&skip=0` | público | 200 | 502 si DummyJSON falla |
| GET | `/api/productos/{id}` | público | 200 | 404 |
| POST | `/api/auth/registro` | público | 201 | 400, 409 |
| POST | `/api/auth/login` | público | 200 | 400, 401 |
| POST | `/api/favoritos` | Bearer | 201 | 400, 403, 404 |
| GET | `/api/favoritos` | Bearer | 200 | 401 |
| GET | `/api/favoritos/{id}` | Bearer | 200 | 401, 403, 404 |
| PUT | `/api/favoritos/{id}` | Bearer | 200 | 400, 401, 403, 404 |
| DELETE | `/api/favoritos/{id}` | Bearer | 204 | 401, 403, 404 |
| POST | `/api/listas` | Bearer | 201 | 400, 401 |
| GET | `/api/listas` | Bearer | 200 | 401 |
| GET | `/api/listas/{id}` | Bearer | 200 | 401, 403, 404 |
| GET | `/api/listas/{id}/favoritos` | Bearer | 200 | 401, 403, 404 |
| DELETE | `/api/listas/{id}` | Bearer | 204 | 401, 403, 404, 409 |
| POST | `/api/listas/{origenId}/mover-favoritos` | Bearer | 204 | 401, 403, 404 |

Swagger UI: `http://localhost:8080/swagger-ui.html` (grupos **Autenticación**, **Productos**, **Favoritos** y **Listas**). Los endpoints protegidos muestran el candado `bearerAuth`: pegá el token obtenido en `/api/auth/login`.

## Dónde vive la autorización y por qué

La regla de propiedad vive en la **capa Service** (`ListaService` y `FavoritoService`), no en el Controller ni en un filtro HTTP ni en anotaciones:

- `ListaService.buscarPropia(id)` busca la lista (404 si no existe) y compara `lista.usuarioId` con el id del usuario autenticado; si no coincide y el rol no es `ADMIN`, lanza `AccesoDenegadoException` → **403**.
- `ListaService.crear` toma el dueño de `UsuarioActual` (la identidad que dejó el filtro JWT), nunca de un campo del body.
- `ListaService.listar` devuelve solo `findByUsuarioId(...)`, salvo que el rol sea `ADMIN`, que ve todas.
- `FavoritoService` valida la lista contenedora en cada operación: un favorito hereda el dueño de su lista.
- El **filtro JWT** solo autentica (¿quién sos?); no decide permisos sobre recursos concretos.

Por qué ahí y no en el Controller:

1. Todo acceso a listas/favoritos pasa por el Service, así que no hay forma de que un endpoint nuevo se olvide de validar.
2. El Controller queda enfocado en HTTP/DTOs y el Service en reglas de negocio.
3. Se prueba con tests unitarios sin levantar Spring MVC (`ListaServiceImplTest`).

Por qué no en un filtro o con anotaciones: un filtro tendría que inspeccionar ids de path y consultar la base, duplicando lógica de negocio; las anotaciones de método no expresan “el dueño del recurso es el usuario del token” sin escribir un evaluador custom. La regla es del dominio y se testea mejor como código Java normal.

> Para listas ajenas se responde **403**, no 404: “no existe” y “no es tuya” son casos distintos y el TP lo exige explícitamente.

## Pruebas

### Unitarias (`mvn test` → Surefire)

No levantan Spring ni necesitan base de datos:

```bash
# Windows
.\mvnw.cmd test

# macOS/Linux
./mvnw test
```

| Test | Qué verifica |
|---|---|
| `ListaServiceImplTest.crear_asociaListaAlUsuarioAutenticado` | La lista creada queda con el `usuarioId` del usuario autenticado. |
| `ListaServiceImplTest.obtener_listaAjena_lanzaAccesoDenegado` | Una lista de otro usuario produce `AccesoDenegadoException` (403). |
| `UsuarioServiceImplTest.registrar_nuncaGuardaLaPasswordEnTextoPlano` | Se persiste el hash de `PasswordEncoder`, nunca la contraseña original. |
| `UsuarioServiceImplTest.login_passwordIncorrecta_lanzaCredencialesInvalidas` | Contraseña incorrecta → 401. |

Los nombres siguen `operación_escenario_resultado`. Las unitarias terminan en `Test` y las de integración en `IT`: eso decide qué plugin las ejecuta.

### Integración (`mvn verify` → Failsafe + Testcontainers)

```bash
# Requiere Docker corriendo (Testcontainers levanta PostgreSQL 16)
.\mvnw.cmd verify    # Windows
./mvnw verify        # macOS/Linux
```

`AbstractIntegrationTest` levanta un contenedor `postgres:16-alpine`, apunta Spring a esa base limpia y Flyway aplica **V1..V6** en cada ejecución. `ListaControllerIT` valida:

- login con credenciales válidas → 200 + token;
- login con credenciales inválidas → 401;
- acceso a lista propia → 200;
- acceso a lista ajena → 403;
- `GET /api/listas` sin token → 401;
- ADMIN accediendo a lista ajena → 200.

Reportes:

- Surefire: `target/surefire-reports/`
- Failsafe: `target/failsafe-reports/`

Un `BUILD SUCCESS` de `verify` significa que pasaron unitarias, integración y migraciones contra base limpia.

## Integración continua (GitHub Actions)

Workflow: [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

En cada push a `main` y en pull requests:

1. checkout del repo;
2. JDK 25 (Temurin) con caché de Maven;
3. `chmod +x mvnw` (por si se perdió el bit de ejecución);
4. `./mvnw -DskipTests compile`;
5. `./mvnw verify` → Surefire (`*Test`) + Failsafe (`*IT` + Testcontainers);
6. publica `target/surefire-reports/**` y `target/failsafe-reports/**` como artefacto, incluso si falla.

Cómo leer el resultado: en GitHub → pestaña **Actions** → workflow **CI** → run más reciente. Verde = todo pasó. Si falla, entrar al job y mirar el paso “Pruebas unitarias + integración”; el log muestra el test y el stacktrace. Los reportes completos quedan en **Artifacts → reportes-surefire-failsafe**.

El workflow corre en un runner limpio sin PostgreSQL local: toda prueba que levante Spring usa su propia base vía Testcontainers. Si Maven falla con `Permission denied`, ejecutar `git update-index --chmod=+x mvnw` y commitear el cambio (ya está aplicado en este repo).

## Despliegue

Se incluyen `Dockerfile` (build multi-stage con Java 25) y `render.yaml` (blueprint de Render con PostgreSQL y variables conectadas).

### Pasos seguidos

1. Subir el repo a GitHub.
2. En Render: **New → Blueprint** y seleccionar este repositorio (detecta `render.yaml`). Alternativa: **New → Web Service → Docker**.
3. Render crea la base PostgreSQL `web2-tp3-db` y define `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`; además genera `JWT_SECRET` y setea `PORT`.
   - Si se configura el servicio a mano, usar los valores de la base en las variables `DB_*` y un `JWT_SECRET` aleatorio de 32+ bytes.
4. Deploy. Render construye con el `Dockerfile` y arranca el jar.
5. Verificar en los logs:
   - `Flyway ... Successfully applied 6 migrations` (V1..V6 sobre la base remota);
   - `Tomcat started` / `Started DemoApplication` sin errores.
6. Verificar los endpoints públicos y protegidos:

```bash
# Health público
curl https://<servicio>.onrender.com/health
# {"status":"ok","service":"demo"}

# Sin token -> 401
curl -i https://<servicio>.onrender.com/api/listas

# Con token -> 200
TOKEN=$(curl -s -X POST https://<servicio>.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@demo.com","password":"secreta123"}' | jq -r .token)
curl -i https://<servicio>.onrender.com/api/listas -H "Authorization: Bearer $TOKEN"
```

### Evidencia

- Workflow CI con corrida exitosa: pestaña **Actions** del repositorio.
- `GET /health` desde la URL pública: captura de pantalla.
- Endpoint protegido sin token → 401 y con token → 200: capturas de pantalla.
- Migraciones en la base remota: `SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;`

> El plan free de Render puede dormir el servicio; la primera request después de inactividad tarda unos segundos. No hace falta mantenerlo activo después de verificar.

## Errores (`ProblemDetail`)

* Recurso inexistente → `404` (`RecursoNoEncontradoException`).
* Validación fallida → `400` con `errores: {campo: motivo}`.
* Sin token / token inválido o vencido → `401` (entry point de Spring Security).
* Recurso ajeno sin ser ADMIN → `403` (`AccesoDenegadoException`).
* Borrar lista con favoritos → `409 Conflict` (`ListaConFavoritosException`).
* Email ya registrado → `409 Conflict` (`EmailDuplicadoException`).
* Credenciales inválidas → `401` (`CredencialesInvalidasException`).
* Caída/timeout de DummyJSON → `502` (`ServicioExternoException`).

## Estructura

```
controller/  AuthController, ProductoController, FavoritoController, ListaController, HealthController, PingController
service/     UsuarioService, ProductoService, FavoritoService, ListaService (autorización + moverFavoritos @Transactional)
security/    SecurityConfig, JwtService, JwtAuthenticationFilter, UsuarioActual, UsuarioPrincipal, TokenPayload
repository/  puertos + adapters: Usuario, Lista, Favorito
persistence/ JpaRepositories + entity/UsuarioEntity, ListaEntity (@ManyToOne UsuarioEntity), FavoritoEntity (@ManyToOne ListaEntity)
model/       Usuario (id, email, passwordHash, rol), Rol (USER/ADMIN), Lista (id, nombre, usuarioId), Favorito
dto/auth/    RegistroRequestDTO, LoginRequestDTO, AuthResponseDTO, UsuarioResponseDTO
dto/...      DTOs y mappers de listas/favoritos/productos
db/migration/ V1..V6
exception/   GlobalExceptionHandler + excepciones 401/403/404/409/502
config/      RestClientConfig + OpenApiConfig (Bearer JWT en Swagger)
```

## Dependencias principales

* `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui`.
* `spring-boot-starter-security` — autenticación, BCrypt y filtros.
* `io.jsonwebtoken:jjwt-api` + `jjwt-impl` + `jjwt-jackson` — emisión/validación de JWT.
* `spring-boot-starter-data-jpa`, `org.postgresql:postgresql` (runtime).
* `spring-boot-starter-flyway` + `flyway-database-postgresql` — versionado (en Boot 4.x `flyway-core` solo no autoconfigura).
* `spring-boot-starter-test` + `spring-boot-starter-webmvc-test` — JUnit 6, Mockito, MockMvc.
* `org.testcontainers:testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-postgresql` — PostgreSQL real para `*IT`.
* `maven-failsafe-plugin` — ejecuta `*IT` durante `verify`.
