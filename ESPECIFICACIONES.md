# Especificaciones del TP - Counter-Strike 2 (POO-08)

**Alumna:** Diana Aquino  
**Dominio:** Counter-Strike 2  
**Materia:** Lenguaje de Programación 3 (CYT646)  
**Enlace exacto al commit de la solución:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/0ebed925fcbc017cb112a0a7bd1ce74cb73b2c75  

---

## 1. Objetivo del trabajo

El objetivo de este trabajo práctico fue modelar Counter-Strike 2 en Spring Boot 3.3.4 con persistencia en base de datos H2 mediante Spring Data JPA, respetando estrictamente la separación de responsabilidades en 3 capas (`Controller` -> `Service` -> `Repository`). Armé el CRUD completo tanto para la familia de clases `Arma` como para la familia `Agente` (con `Equipo` embebido), centralicé todas las validaciones de invariantes, reglas del juego y lanzamiento de excepciones exclusivamente en `ArmaService` (dejando las entidades solo con su estado y sin setters públicos descontrolados), implementé la simulación trabajando directamente sobre entidades guardadas en H2, agregué la interfaz de dominio `InspeccionableEnPartida` y la jerarquía de excepciones propias con sus handlers HTTP.

---

## 2. Qué apliqué en mi modelo e invariantes (validados en `ArmaService`)

- **Tres clases/familias persistidas en H2 (sin lanzar excepciones ni exponer setters libres):**
  - `Arma` (clase padre abstracta) con sus subclases concretas (`M4A4`, `MP9`, `DesertEagle`, `Glock18`, `Nova`, `GranadaHE`).
  - `Agente` (clase padre abstracta) con sus dos hijas (`AgenteCT` y `AgenteTerrorista`).
  - `Equipo` (guardado con `@Embeddable` dentro de las tablas de armas y agentes).
- **Invariantes y controles de negocio validados exclusivamente por `ArmaService`:**
  - En `ArmaService.validarReglasDeNegocio(Arma)`: el nombre no puede estar vacío, el daño tiene que ser mayor a `0` y hasta `500`, la munición no puede ser negativa ni superar la `capacidadCargador` (`> 0`), la precisión va entre `0.0` y `1.0`, el tiempo de recarga no puede ser negativo, el precio tiene que estar entre `$0` y `$16,000` (el tope de plata de una partida competitiva de CS2) y, si el arma es una `Granada`, tanto `radioExplosion` como `tiempoExplosion` deben ser estrictamente mayores a `0` (rechazando valores como `radioExplosion: -3` con `400 Bad Request`).
  - En `ArmaService.validarReglasDeAgente(Agente)`: el nombre no puede estar vacío, la vida y el chaleco Kevlar van de `0` a `100`, el dinero va de `$0` a `$16,000` y el equipo no puede ser vacío.
- **Interfaz del dominio (POO-07):**
  - Creé la interfaz `InspeccionableEnPartida` con los métodos `inspeccionarEnPartida()` y `calcularValorEconomico()`, implementada tanto por `Arma` como por `Agente` (dos familias distintas).
- **Dos reglas de negocio en la simulación sobre entidades guardadas en H2 (`ArmaService`):**
  - **Regla 1 (munición, cargador y fuego amigo entre entidades guardadas en H2):** `ArmaService` carga el `Arma` y el `Agente` objetivo desde H2 por sus IDs. Si el `Equipo` del `Arma` guardada en H2 y el `Equipo` del `Agente` guardado en H2 son el mismo, `ArmaService` lanza `FuegoAmigoException` (`422 Unprocessable Entity`). Si al `Arma` no le quedan balas en el cargador (`municion == 0`) o se intenta recargar con cantidad `<= 0` o estando lleno, `ArmaService` lanza `MunicionInsuficienteException` (`409 Conflict`).
  - **Regla 2 (daño y chaleco Kevlar):** Cuando el disparo o la granada impacta en el `Agente` cargado de H2, `ArmaService` descuenta 1 proyectil del `Arma`, calcula el daño según la distancia y si fue headshot, absorbe el 35% del golpe en el `chalecoKevlar` del `Agente` antes de restar la `vida`, y guarda los nuevos estados de ambas entidades en H2.

---

## 3. URLs del servicio

- `GET /` — Muestra mi nombre, el dominio y las rutas disponibles (`200 OK`).
- `POST /api/armas` — Crea un arma nueva en H2 tras validar invariantes en `ArmaService` (`201 Created` o `400 Bad Request`).
- `GET /api/armas/todas` — Lista todas las armas guardadas en H2 (`200 OK`).
- `GET /api/armas/{id}` — Busca un arma por su `id` en H2 (`200 OK` o `404 Not Found`).
- `PUT /api/armas/{id}` — Actualiza los datos de un arma en H2 (`200 OK` o `400 Bad Request`).
- `DELETE /api/armas/{id}` — Borra un arma de H2 por su `id` (`200 OK` o `404 Not Found`).
- `POST /api/agentes` — Crea un agente (`AgenteCT` o `AgenteTerrorista`) en H2 (`201 Created` o `400 Bad Request`).
- `GET /api/agentes/todos` — Lista todos los agentes guardados en H2 (`200 OK`).
- `GET /api/agentes/{id}` — Busca un agente por su `id` en H2 (`200 OK` o `404 Not Found`).
- `PUT /api/agentes/{id}` — Actualiza un agente en H2 (`200 OK` o `400 Bad Request`).
- `DELETE /api/agentes/{id}` — Borra un agente de H2 (`200 OK` o `404 Not Found`).
- `GET /api/armas/protocolo` — Usa la interfaz `InspeccionableEnPartida` para mostrar armas y agentes juntos (`200 OK`).
- `POST /api/armas/{id}/simular?agenteObjetivoId={agenteId}&distancia=15&headshot=true` — Corre la simulación cargando el `Arma` y el `Agente` guardados en H2, descuenta munición y baja vida y chaleco del agente en H2 (`200 OK`).
- `POST /api/armas/{id}/recargar?balas=5` — Recarga balas en el arma guardada en H2 (`200 OK` o `409 Conflict`).
- `POST /api/armas/granadas/simular?granadaId={granadaId}&agenteObjetivoId={agenteId}&distancia=2.0` — Simula la explosión de una `GranadaHE` con `Agente` y `Equipo`.
- `http://localhost:8080/h2-console` — Consola de H2 (`JDBC URL`: `jdbc:h2:file:./data/armas`, usuario: `sa`, sin contraseña).

---

## 4. Cómo probarlo

Para levantar el proyecto desde la terminal:

```bash
./mvnw spring-boot:run
```

Para correr los 21 tests automatizados:

```bash
./mvnw test
```
