# API de Gestión de Pólizas — Prueba Técnica

API REST para la gestión de pólizas de arrendamiento (individuales y colectivas) y sus riesgos, desarrollada con Spring Boot. Corresponde al Módulo 2 (Code Challenge) de la prueba técnica.

## Stack

- **Java 21**
- **Spring Boot 3.3.5** (Web, Data JPA, Validation)
- **H2** (base de datos en memoria)
- **Gradle** (con wrapper incluido)
- **springdoc-openapi** (Swagger UI / OpenAPI)
- **Lombok** (reduce boilerplate)
- **JaCoCo** (cobertura) y **SonarQube** (calidad de código)

## Arquitectura

Proyecto organizado en capas:

```
src/main/java/com/bolivar/polizas/
├── PolizasApplication.java      # Clase principal
├── config/
│   ├── ApiKeyFilter.java        # Seguridad por header api-key
│   └── OpenApiConfig.java       # Configuración de Swagger / OpenAPI
├── controller/                  # Capa de presentación (REST)
│   ├── PolizaController.java
│   ├── RiesgoController.java
│   └── CoreMockController.java
├── service/                     # Lógica de negocio
│   ├── PolizaService.java
│   ├── RiesgoService.java
│   └── CoreMockService.java     # Mock del CORE (registra en logs)
├── repository/                  # Acceso a datos (Spring Data JPA)
│   ├── PolizaRepository.java
│   └── RiesgoRepository.java
├── model/                       # Entidades y enums
│   ├── Poliza.java
│   ├── Riesgo.java
│   ├── TipoPoliza.java
│   ├── EstadoPoliza.java
│   └── EstadoRiesgo.java
├── dto/                         # Objetos de entrada
│   ├── CrearRiesgoRequest.java
│   └── EventoCoreRequest.java
└── exception/                   # Manejo de errores y reglas de negocio
    ├── RecursoNoEncontradoException.java
    ├── ReglaNegocioException.java
    └── ManejadorGlobalExcepciones.java
```

## Cómo ejecutar

Requisitos: tener instalado **JDK 21**. No se necesita instalar Gradle (se usa el wrapper) ni ninguna base de datos (H2 es en memoria).

Desde la raíz del proyecto:

```bash
# Windows
.\gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```

La aplicación queda disponible en: `http://localhost:8080`

Los datos de ejemplo se cargan automáticamente al iniciar (ver `src/main/resources/data.sql`).

### Documentación de la API (Swagger / OpenAPI)

Con la aplicación levantada:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificación OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Ambas rutas están exceptuadas del filtro de `api-key` para facilitar la exploración.

### Consola H2 (opcional)

Disponible en `http://localhost:8080/h2-console` con:
- JDBC URL: `jdbc:h2:mem:polizasdb`
- Usuario: `sa`
- Contraseña: *(vacía)*

## Seguridad

Todos los endpoints de negocio exigen el header:

```
api-key: 123456
```

Si falta o es incorrecto, la API responde `401 Unauthorized`. (La consola H2 queda exceptuada solo para facilitar la revisión.)

## Endpoints

Prefijo base: `/api/v1`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/polizas` | Lista pólizas; filtros opcionales `?tipo=` y `?estado=` |
| GET | `/api/v1/polizas/{id}/riesgos` | Lista los riesgos de una póliza |
| POST | `/api/v1/polizas/{id}/renovar` | Renueva: ajusta canon y prima por IPC; estado → RENOVADA |
| POST | `/api/v1/polizas/{id}/cancelar` | Cancela la póliza y, en cascada, todos sus riesgos |
| POST | `/api/v1/polizas/{id}/riesgos` | Agrega un riesgo (solo si la póliza es COLECTIVA) |
| POST | `/api/v1/riesgos/{id}/cancelar` | Cancela un riesgo específico |
| POST | `/api/v1/core-mock/evento` | Mock del CORE: registra en logs el intento de envío |

> Nota: las rutas usan el prefijo de versión `/api/v1/` por buenas prácticas de versionamiento de APIs. La funcionalidad corresponde a la solicitada en el enunciado (GET /polizas, POST /polizas/{id}/renovar, etc.).

### Valores de los enums

- `tipo`: `INDIVIDUAL`, `COLECTIVA`
- `estado` (póliza): `ACTIVA`, `RENOVADA`, `CANCELADA`
- `estado` (riesgo): `ACTIVO`, `CANCELADO`

## Reglas de negocio implementadas

1. Una póliza **individual** solo puede tener **un** riesgo (no se le pueden agregar riesgos).
2. **No se puede renovar** una póliza **cancelada**.
3. La **cancelación de una póliza cancela todos sus riesgos** (en cascada).
4. **Agregar riesgo** exige validar el **tipo de póliza** (solo COLECTIVA).

## Mock del CORE

El endpoint `POST /api/v1/core-mock/evento` recibe un cuerpo como:

```json
{
  "evento": "ACTUALIZACION",
  "polizaId": 555
}
```

Su único propósito es **registrar en logs** que la operación se intentó enviar al CORE. Además, toda operación que modifica el estado de una póliza o riesgo (renovar, cancelar, agregar riesgo) invoca internamente este registro, simulando el envío al servicio agnóstico de edición.

## Ejemplos de uso (curl)

```bash
# Listar pólizas colectivas activas
curl -H "api-key: 123456" "http://localhost:8080/api/v1/polizas?tipo=COLECTIVA&estado=ACTIVA"

# Ver riesgos de la póliza 1
curl -H "api-key: 123456" "http://localhost:8080/api/v1/polizas/1/riesgos"

# Renovar la póliza 1
curl -X POST -H "api-key: 123456" "http://localhost:8080/api/v1/polizas/1/renovar"

# Agregar riesgo a una póliza colectiva
curl -X POST -H "api-key: 123456" -H "Content-Type: application/json" \
  -d "{\"datosInmueble\":\"Apto 303, Edificio Central\"}" \
  "http://localhost:8080/api/v1/polizas/1/riesgos"

# Cancelar la póliza 1 (cancela sus riesgos en cascada)
curl -X POST -H "api-key: 123456" "http://localhost:8080/api/v1/polizas/1/cancelar"

# Cancelar un riesgo
curl -X POST -H "api-key: 123456" "http://localhost:8080/api/v1/riesgos/2/cancelar"

# Enviar evento al mock del CORE
curl -X POST -H "api-key: 123456" -H "Content-Type: application/json" \
  -d "{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}" \
  "http://localhost:8080/api/v1/core-mock/evento"
```

## Datos de ejemplo precargados

| id | tipo | estado | canon mensual | IPC | riesgos |
|----|------|--------|---------------|-----|---------|
| 1 | COLECTIVA | ACTIVA | 2.000.000 | 12% | 2 activos |
| 2 | INDIVIDUAL | ACTIVA | 1.500.000 | 10% | 1 activo |
| 3 | COLECTIVA | CANCELADA | 1.800.000 | 11.5% | 1 cancelado |

## Pruebas y cobertura

El proyecto incluye pruebas unitarias (servicios, con Mockito) y de integración (controladores, con MockMvc y contexto Spring + H2). Los tests de integración corren con `@Transactional` para aislarse entre sí.

```bash
# Ejecutar pruebas y generar el reporte de cobertura
./gradlew test jacocoTestReport
```

Reporte HTML de cobertura: `build/reports/jacoco/test/html/index.html`

Cobertura actual: líneas ~96%, ramas ~88%, clases 100% (la clase de arranque `PolizasApplication` se excluye del cálculo).

## Calidad de código (SonarQube)

El proyecto trae configurado el plugin de SonarQube y un `sonar-project.properties`. El reporte de cobertura de JaCoCo se enlaza automáticamente al análisis.

```bash
# Requiere una instancia de SonarQube/SonarCloud y un token
./gradlew test jacocoTestReport sonar \
  -Dsonar.host.url=<URL_SONAR> \
  -Dsonar.token=<TOKEN>
```

## Compilar y empaquetar

```bash
# Compilar
./gradlew build

# Generar el jar ejecutable
./gradlew bootJar
# Resultado en: build/libs/prueba-tecnica-gestion-polizas-0.0.1-SNAPSHOT.jar
```
