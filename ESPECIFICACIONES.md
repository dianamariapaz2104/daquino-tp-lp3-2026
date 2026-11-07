# Especificaciones del TP POO-08 — Counter-Strike 2 (CRUD REST, H2, Reglas de Juego, Interfaz y Excepciones)

**Alumna:** Diana Aquino  
**Dominio:** Counter-Strike 2  
**Materia:** Lenguaje de Programación 3 (CYT646) — Ejercicio POO-08  
**Enlace exacto al commit de la solución:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/1c30587c801668c9619dd0201fbf2eaa71f654db  

---

## 1. Objetivo del Trabajo

Publicar un servicio HTTP con **Spring Boot 3.3.4**, **Java 21**, **Spring Data JPA** y base de datos **H2** (`./data/armas`) sobre el modelado de **Counter-Strike 2**, integrando:
1. **Persistencia en H2 de tres clases/familias del modelo:**
   - Jerarquía `Arma` (`@Entity`, `@Inheritance(SINGLE_TABLE)`) con sus subclases (`Pistola`, `Rifle`, `Escopeta`, `SubfusilSMG`, `Francotirador`, `Granada`, `DesertEagle`, `Glock18`, `M4A4`, `MP9`, `Nova`, `GranadaHE`).
   - Jerarquía `Agente` (`@Entity`, `@Inheritance(SINGLE_TABLE)`) con sus dos clases hijas (`AgenteCT` y `AgenteTerrorista`).
   - Clase `Equipo` (`@Embeddable`, persistida en las tablas `ARMAS` y `AGENTES`).
2. **CRUD REST completo** sobre la familia `Arma` discriminando el subtipo en el JSON mediante `"tipoArma"`.
3. **Protocolo de dominio (`interface` POO-07):** `InspeccionableEnPartida`, implementado por dos familias distintas (`Arma` y `Agente`) y expuesto en `GET /api/armas/protocolo`.
4. **Dos reglas de negocio en el dominio y simulación persistida:**
   - **Regla 1 (Munición, Cargador y Fuego Amigo en `Arma` / `GranadaHE` / `Equipo`):** Un `Arma` solo puede disparar si tiene proyectiles en el cargador (`municion > 0`), solo permite recargar cuando no está lleno y con una cantidad positiva (de lo contrario lanza `MunicionInsuficienteException`), y bloquea ataques contra un `Agente` del mismo `Equipo` cuando el fuego amigo competitivo está deshabilitado (de lo contrario lanza `FuegoAmigoException`).
   - **Regla 2 (Daño Balístico/Explosivo y Absorción de Kevlar en `Arma` / `Agente`):** Al impactar sobre un `Agente`, el `Arma` descuenta 1 proyectil, calcula el daño atenuado por distancia y *headshot* (o radio de explosión), y el `Agente` absorbe el 35% del daño en su `chalecoKevlar` y descuenta el daño de sus puntos de `vida`, guardando ambos estados actualizados en H2.
5. **Jerarquía de excepciones del dominio y un handler HTTP por tipo:**
   - Base: `ReglaDeJuegoException` (`extends RuntimeException`)
   - Hija 1: `MunicionInsuficienteException` -> atendida con **`409 Conflict`**
   - Hija 2: `FuegoAmigoException` -> atendida con **`422 Unprocessable Entity`**
6. **Colección de Insomnia** versionada en `insomnia/insomnia-cs2-collection.json`.

---

## 2. Invariantes Protegidos del Dominio

- **`Arma`**:
  - `nombre` no nulo ni vacío.
  - `daño > 0` y `daño <= 500` (tope de balance en `ArmaService`).
  - `municion >= 0`, `capacidadCargador > 0` y `municion <= capacidadCargador`.
  - `precio >= 0` y `precio <= 16000` (presupuesto máximo de una partida competitiva de CS2).
  - `0.0 <= precision <= 1.0` y `tiempoRecarga >= 0.0`.
- **`Agente`**:
  - `nombre` no nulo ni vacío.
  - `0 <= vida <= 100`.
  - `0 <= chalecoKevlar <= 100`.
  - `0 <= dinero <= 16000`.
- **`Equipo`**:
  - `nombre` no nulo ni vacío; compara bandos ignorando mayúsculas/minúsculas mediante `esMismoEquipo(Equipo otro)`.

---

## 3. Catálogo de URLs y Endpoints REST

| Operación / Propósito | Método y URL | Código HTTP esperado |
|---|---|---|
| Estado del servicio | `GET /` | `200 OK` |
| **CRUD — Crear arma en H2** | `POST /api/armas` | `201 Created` (o `400 Bad Request` si viola invariante) |
| **CRUD — Listar colección H2** | `GET /api/armas/todas` | `200 OK` |
| **CRUD — Leer arma por ID** | `GET /api/armas/{id}` | `200 OK` o `404 Not Found` |
| **CRUD — Actualizar arma en H2** | `PUT /api/armas/{id}` | `200 OK`, `400 Bad Request` o `404 Not Found` |
| **CRUD — Eliminar arma de H2** | `DELETE /api/armas/{id}` | `200 OK` o `404 Not Found` |
| **Interfaz de dominio (POO-07)** | `GET /api/armas/protocolo` | `200 OK` (`Arma` y `Agente` como `InspeccionableEnPartida`) |
| **Simulación persistida (Caso feliz / Errores)** | `POST /api/armas/{id}/simular?equipoObjetivo=Terrorista&distancia=15&headshot=true&chaleco=true&fuegoAmigo=false` | `200 OK` (persiste cambios), `409 Conflict` (`MunicionInsuficienteException`) o `422 Unprocessable Entity` (`FuegoAmigoException`) |
| **Recarga validada en H2** | `POST /api/armas/{id}/recargar?balas=5` | `200 OK` o `409 Conflict` (`MunicionInsuficienteException`) |
| **Simulación de granadas HE** | `GET /api/armas/granadas/simular?equipoAtacante=Counter-Terrorista&equipoObjetivo=Terrorista&distancia=2.0&chaleco=true` | `200 OK` o `422 Unprocessable Entity` (`FuegoAmigoException`) |
| **Listar agentes persistidos en H2** | `GET /api/agentes/todos` | `200 OK` |
| **Polimorfismo de `Arma` y `Agente`** | `GET /api/armas/polimorfismo` y `GET /api/agentes/polimorfismo` | `200 OK` |
| **Sobrecarga de `disparar`** | `GET /api/armas/disparar?nombre=M4A4&distancia=30&headshot=true` | `200 OK` |
| **Consola web de H2** | `http://localhost:8080/h2-console` | `JDBC URL: jdbc:h2:file:./data/armas` (`user: sa`) |

---

## 4. Cómo probarlo paso a paso (5 a 10 minutos)

1. **Arrancar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```
2. **Importar la colección en Insomnia:**
   Importar el archivo `insomnia/insomnia-cs2-collection.json` y seleccionar **Base Environment** (`base_url = http://localhost:8080`, `arma_id = 1`).
3. **Recorrer los 12 pedidos en orden:**
   - `01. GET /` -> Verifica que el servicio vive (`200 OK`).
   - `02. POST /api/armas` -> Crea la `"Desert Eagle Blaze"` en H2 (`201 Created`, devuelve `id`, `"municion": 7`).
   - `03. GET /api/armas/todas` -> Muestra el recurso en la colección de H2.
   - `04. GET /api/armas/{id}` -> Coincide con el arma creada.
   - `05. PUT /api/armas/{id}` -> Actualiza a `"Desert Eagle Printstream"`, daño `50`, precio `750` (`200 OK`).
   - `06. POST /api/armas (Ilegal)` -> Envía daño `-10` y munición `-5`; responde `400 Bad Request` y no ensucia H2.
   - `07. GET /api/armas/9999` -> Responde `404 Not Found`.
   - `08. GET /api/armas/protocolo` -> Responde el JSON del protocolo `InspeccionableEnPartida` combinando instancias de `Arma` y `Agente`.
   - `09. POST /api/armas/{id}/simular` -> Simula el combate contra `Terrorista` a `15m` con `headshot=true`: descuenta `municion` de `7` a `6` y reduce la `vida` y `chalecoKevlar` del `Agente` en H2. Un `04. GET /api/armas/{id}` posterior muestra `"municion": 6`.
   - `10. POST /api/armas/{id}/recargar?balas=0` -> Dispara `MunicionInsuficienteException`; su handler responde **`409 Conflict`**.
   - `11. POST /api/armas/{id}/simular (Mismo Equipo)` -> Dispara `FuegoAmigoException`; su handler responde **`422 Unprocessable Entity`**.
   - `12. DELETE /api/armas/{id}` -> Elimina el arma de H2; un `GET /api/armas/{id}` posterior devuelve **`404 Not Found`**.
4. **Ejecutar las pruebas automatizadas:**
   ```bash
   ./mvnw test
   ```
