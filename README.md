# 🌹 AgroSmart — Plataforma de Comercialización Agrícola

Backend desarrollado para el Examen Final Práctico de Programación Avanzada de
la Universidad de las Fuerzas Armadas ESPE.

AgroSmart persiste productos agrícolas mediante JPA/Hibernate, publica los
productos comercializables mediante Spring WebFlux y genera publicidad usando
LangChain4j.

---

## Datos del estudiante

- **Estudiante:** Javier Neicer Bravo Macias
- **NN:** 77
- **Tabla:** `tbl_productos_base_77`
- **Puerto:** `8177`
- **Categoría:** Flores
- **Base de datos:** `agrosmart_db`
- **Nonce:** `AGS-2026`

La semilla se obtuvo usando los dos últimos dígitos de mi cédula. Como termina
en `77`, la tabla usa el sufijo `77`, el puerto es `8177` y el último dígito
`7` corresponde a la categoría Flores.

---

## Tecnologías

- Java 21
- Maven Wrapper
- Spring Boot 4.1.0
- Spring WebFlux con Netty
- Spring Data JPA / Hibernate
- PostgreSQL
- Docker Compose
- Project Reactor
- LangChain4j
- JUnit 5
- Mockito
- Reactor Test / StepVerifier

---

## Arquitectura

```text
HTTP / Netty
    |
    v
AgroSmartController
    |
    +--> ProductoService
    |        |
    |        +--> ProductoRepository --> Hibernate --> PostgreSQL
    |
    +--> PublicidadService
             |
             +--> AgroSmartAIService --> LangChain4j
```

Las operaciones de JPA y LangChain4j son bloqueantes. Por esa razón se aíslan
mediante:

```java
Mono.fromCallable(...)
        .subscribeOn(Schedulers.boundedElastic())
```

El event loop de Netty no ejecuta directamente esas operaciones.

---

## Estructura principal

```text
src/main/java/ec/edu/espe/agrosmart/
├── AgrosmartApplication.java
├── controller/
│   └── AgroSmartController.java
├── service/
│   ├── ProductoService.java
│   ├── AgroSmartAIService.java
│   └── PublicidadService.java
├── repository/
│   └── ProductoRepository.java
├── entity/
│   └── ProductoEntity.java
├── domain/
│   ├── Producto.java
│   └── ProductoFilters.java
├── mapper/
│   └── ProductoMapper.java
└── exception/
    └── ProductoNoEncontradoException.java
```

---

## Semilla de datos

La aplicación siembra exactamente cinco productos de la categoría Flores:

- Tres productos válidos: precio mayor que cero y al menos un correo.
- Un producto inválido con precio igual a cero.
- Un producto inválido con correos vacíos.

La siembra es idempotente:

```java
if (repository.count() == 0)
```

Por ello, reiniciar la aplicación no duplica los registros.

---

## Requisitos previos

- Java 21.
- Docker Desktop.
- Git.
- Puerto `8177` disponible.
- Puerto `5432` disponible para PostgreSQL.

En Windows, si PostgreSQL 17 local ocupa el puerto `5432`, se puede detener
temporalmente desde PowerShell como administrador:

```powershell
Stop-Service -Name "postgresql-x64-17" -Force
```

---

## Ejecución

### 1. Configurar Java 21 en PowerShell

```powershell
$env:JAVA_HOME="$env:USERPROFILE\.jdks\ms-21.0.11"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

java -version
.\mvnw.cmd -v
```

### 2. Levantar PostgreSQL

```powershell
docker compose up -d
docker compose ps
```

### 3. Ejecutar las pruebas

```powershell
.\mvnw.cmd clean test
```

Resultado obtenido:

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 4. Ejecutar AgroSmart

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en:

```text
http://localhost:8177
```

---

## Endpoints

| Método | Endpoint | Descripción | Retorno |
|---|---|---|---|
| GET | `/api/productos` | Productos comercializables | `Flux<Producto>` |
| GET | `/api/productos/{id}` | Producto por identificador | `Mono<Producto>` |
| GET | `/api/agrosmart/publicidad` | Genera publicidad | `Mono<String>` |

---

## Pruebas con curl

### Consultar productos comercializables

```powershell
curl.exe http://localhost:8177/api/productos
```

La respuesta contiene los tres productos válidos y sus nombres transformados a
mayúsculas.

### Consultar un producto existente

```powershell
curl.exe http://localhost:8177/api/productos/1
```

### Consultar un producto inexistente

```powershell
curl.exe -i http://localhost:8177/api/productos/9999
```

Respuesta comprobada:

```text
HTTP/1.1 404 Not Found
```

### Generar publicidad

```powershell
curl.exe "http://localhost:8177/api/agrosmart/publicidad?producto=Rosas%20Premium&audiencia=florister%C3%ADas%20premium"
```

El endpoint produce texto plano. Cuando el proveedor de IA falla, el flujo se
recupera mediante `onErrorResume` y devuelve el mensaje de respaldo configurado.

Las salidas completas y reales de los cuatro comandos están registradas en
`DECISIONES.md` y en `docs/evidencias/`.

---

## Operadores reactivos utilizados

### `Mono.fromCallable`

Difiere la ejecución de `repository.findAll()`, `repository.findById(...)` y la
llamada de LangChain4j hasta que exista una suscripción.

No se utilizó:

```java
Mono.just(repository.findAll())
```

porque eso ejecutaría la consulta antes de que Reactor pueda cambiarla de hilo.

### `subscribeOn(Schedulers.boundedElastic())`

Traslada JPA y LangChain4j fuera del event loop de Netty, ya que ambas son
operaciones bloqueantes.

### `flatMapMany`

Convierte la lista obtenida mediante `JpaRepository.findAll()` en un
`Flux<ProductoEntity>`.

### `map`

Se utiliza para:

1. Convertir `ProductoEntity` en `Producto`.
2. Crear un nuevo producto con el nombre en mayúsculas.

### `filter`

Aplica `ProductoFilters.IS_VALID` y conserva solamente los productos cuyo precio
es mayor que cero y que tienen al menos un correo.

### `doOnNext`

Ejecuta `ProductoFilters.LOG_PRODUCTO` como efecto de trazabilidad. No transforma
el elemento.

### `defaultIfEmpty`

En `obtenerProductosComercializables()` emite `PRODUCTO_GENERICO` cuando todos
los registros fueron descartados por el filtro.

### `switchIfEmpty`

En `buscarPorId(...)` cambia un resultado vacío por
`ProductoNoEncontradoException`, que WebFlux traduce a HTTP 404.

### `timeout`

Limita a treinta segundos la espera de la llamada al proveedor de IA.

### `onErrorResume`

Recupera el flujo cuando LangChain4j genera un error y emite un mensaje de
respaldo.

---

## Modelo inmutable

`ProductoEntity` es mutable porque Hibernate requiere constructor vacío y acceso
a sus propiedades.

`Producto` es el modelo de dominio inmutable:

- Clase `final`.
- Atributos `private final`.
- Sin setters.
- Copia defensiva en el constructor.
- Copia defensiva y lista no modificable en el getter.

La copia del constructor evita que la lista original modifique el estado interno.
La copia del getter evita exponer la colección almacenada por el objeto.

---

## Pruebas unitarias

Las pruebas se encuentran en:

```text
src/test/java/ec/edu/espe/agrosmart/
├── domain/
│   ├── ProductoTest.java
│   └── ProductoFiltersTest.java
└── service/
    ├── ProductoServiceTest.java
    └── PublicidadServiceTest.java
```

Cobertura funcional:

- Getters y ambas copias defensivas.
- Producto válido.
- Producto inválido por precio cero.
- Producto inválido por correos vacíos.
- Tres productos comercializables.
- Producto genérico cuando todos son inválidos.
- Error al buscar un ID inexistente.
- IA en camino exitoso.
- IA en camino de fallo.

Las pruebas utilizan Mockito y StepVerifier. No dependen de PostgreSQL ni de
internet.

---

## Evidencias

Las evidencias están en:

```text
docs/evidencias/
```

Incluyen:

- Arranque con el perfil `prod`.
- Netty en el puerto `8177`.
- Estructura de `tbl_productos_base_77`.
- Cinco productos sembrados.
- Cuatro comandos `curl`.
- Doce pruebas en verde.
- Historial de Git con ramas y commits.

---

## Flujo de Git

Cada fase se desarrolló en su rama:

```text
feature/config-perfiles
feature/persistencia-jpa
feature/modelo-inmutable
feature/servicio-reactivo
feature/ia-langchain4j
feature/api-reactiva
feature/pruebas
feature/documentacion
```

Las ramas fueron integradas mediante Pull Request y merge commit, sin squash,
sin rebase del historial y sin `push --force`.

---

## Defensa oral

El enlace del video se encuentra en `IDENTIDAD.md`.

---

## Autor

**Javier Neicer Bravo Macias**  
Universidad de las Fuerzas Armadas ESPE  
Programación Avanzada — NRC 30405