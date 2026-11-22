# Especificaciones del TP - POO-09 (Challenge de APIs Cruzadas y Cierre POO-08)

**Alumna (A1):** Diana Aquino  
**Dominio:** Counter-Strike 2 (CS2)  
**Asignatura:** Lenguaje de Programación 3 (CYT646) — Edición 2026  
**Repositorio:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026  
**Enlace exacto al commit de la solución:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/b7a4b4b160568776746ba37145a5ce9abbb9acbc  

---

## 1. Quiénes son A1, A2 y A3 (Mapa del Grupo Round Robin en Counter-Strike 2)

- **A1 (Mi servicio — Orquestador del escenario):** **Diana Aquino**
  - **URL Base:** `http://localhost:8080`
  - **OpenAPI 3 (JSON):** `http://localhost:8080/v3/api-docs`
  - **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
  - **Reglas propias en `ArmaService`:** (1) Control de munición en cargador (`409 MunicionInsuficienteException`) y bloqueo de fuego amigo entre entidades guardadas en H2 del mismo equipo (`422 FuegoAmigoException`); (2) Cálculo de daño balístico/explosivo con absorción del 35% en chaleco Kevlar y descuento de vida del `Agente` en H2.

- **A2 (Compañero 1 consultado):** **Matias Enciso** ([MatiasEnc/menciso-tp-lp3-2026](https://github.com/MatiasEnc/menciso-tp-lp3-2026), subcarpeta `cs2/`)
  - **URL Base (configurable con `A2_BASE_URL`):** `http://localhost:8081`
  - **OpenAPI 3:** `http://localhost:8081/v3/api-docs` (`http://localhost:8081/swagger-ui/index.html`)
  - **Regla consultada (`GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false`):** Ejecuta las 2 reglas de negocio de CS2 (atenuación de daño por distancia + fuego amigo al 35%, y mitigación de daño con chaleco Kevlar), o devuelve `400 Bad Request` (`ValidationException`) si la distancia es negativa.

- **A3 (Compañero 2 consultado):** **Fabricio Gonzalez** ([FabricioG29/fg-taller-git-2026](https://github.com/FabricioG29/fg-taller-git-2026))
  - **URL Base (configurable con `A3_BASE_URL`):** `http://localhost:8082`
  - **OpenAPI 3:** `http://localhost:8082/v3/api-docs` (`http://localhost:8082/swagger-ui.html`)
  - **Reglas publicadas y consultadas:**
    - `POST /api/reglas/municion` (`{"cargador":3,"disparos":2}` -> `200` si la ráfaga alcanza, o `409 SIN_MUNICION`).
    - `POST /api/reglas/danio` (`{"vida":100,"armadura":100,"danio":36}` -> `200` con la vida y el chaleco restantes donde el chaleco absorbe la mitad, o `422 JUGADOR_ELIMINADO` si la vida ya es `0`).

---

## 2. El Escenario en una Frase

**«Asalto Táctico Coordinado en Ronda Competitiva» (`POST /api/escenario/jugadas`):** Un operador ejecuta una jugada combinada de ráfaga de apertura y detonación explosiva sobre un objetivo en el sitio de bomba, la cual consulta primero la regla de disparo/munición en **A2** y luego la regla de explosión/blindaje en **A3**, y **solo persiste la entidad `JugadaEscenario` (y el descuento de munición y vida) en el H2 de A1 si ambas APIs remotas la aceptan** (dejando el H2 de A1 intacto e informando `"rechazadoPor": "A2"` o `"A3"` si alguna la rechaza).

---

## 3. Cierre de las 4 Observaciones de POO-08 (3 de 3 puntos)

1. **Criterio 2 (Organización en paquetes):** Se quitaron todos los `throw new ...` de las entidades en `py.edu.uc.lp3.domain`. Ahora las entidades solo mantienen su estado y todas las validaciones y excepciones viven exclusivamente en `ArmaService` y `EscenarioCruzadoService`.
2. **Criterio 3 (Ocultamiento e invariantes):** Se eliminaron `setRadioExplosion`, `setTiempoExplosion`, `setVida`, `setMunicion` y todos los setters públicos libres de las entidades, y `ArmaService.validarReglasDeNegocio` rechaza con `400 Bad Request` cualquier `Granada` con `radioExplosion <= 0` (como `-3`) o `tiempoExplosion <= 0`, sin guardar filas inválidas en H2.
3. **Criterio 6 (Comportamiento observable):** Se agregó el CRUD de `Agente` (`/api/agentes`) y la simulación carga tanto el `Arma` como el `Agente` guardados en H2 por sus IDs, determinando `FuegoAmigoException` (`422`) según los equipos guardados en H2.
4. **Criterio 8 (Documentación):** Se sincronizaron el `README.md` y las especificaciones con el comportamiento real del `PUT` (valores por defecto del constructor vacío de la subclase) y con las validaciones centralizadas en `ArmaService`.

---

## 4. Cómo Probarlo

1. **Levantar A2 (`8081`), A3 (`8082`) y A1 (`8080`):**
   ```bash
   ./mvnw spring-boot:run
   ```
2. **Probar el Camino Feliz (`201 Created` — Guarda en H2 de A1):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/escenario/jugadas?balasDisparo=2&distanciaExplosion=2.0&equipoObjetivo=Terrorista"
   ```
3. **Verificar que la jugada quedó guardada en el H2 de A1 (`200 OK`):**
   ```bash
   curl -s "http://localhost:8080/api/escenario/jugadas"
   ```
4. **Probar Rechazo de A2 — Matias Enciso (`400 Bad Request` — `"rechazadoPor": "A2"`, H2 de A1 no cambia):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/escenario/jugadas?distanciaExplosion=-5.0&vidaObjetivo=100"
   ```
5. **Probar Rechazo de A3 — Fabricio Gonzalez (`422 JUGADOR_ELIMINADO` — `"rechazadoPor": "A3"`, H2 de A1 no cambia):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/escenario/jugadas?distanciaExplosion=2.5&vidaObjetivo=0&armaduraObjetivo=0&danio=36"
   ```
6. **Correr las 25 pruebas automatizadas:**
   ```bash
   ./mvnw test
   ```
