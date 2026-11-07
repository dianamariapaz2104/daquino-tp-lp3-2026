# Especificaciones del TP - Counter-Strike 2 (POO-08)

**Alumna:** Diana Aquino  
**Dominio:** Counter-Strike 2  
**Materia:** Lenguaje de Programación 3 (CYT646)  
**Enlace exacto al commit de la solución:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/db037aed8fcbf7512b2bbc6cf4d135a3d86d5236  

---

## 1. Objetivo del trabajo

El objetivo de este trabajo práctico fue seguir con el modelado de Counter-Strike 2 en Spring Boot, agregando ahora persistencia en una base de datos H2 con Spring Data JPA. Armé un CRUD completo sobre la familia de clases `Arma`, sumé la persistencia de `Agente` y `Equipo`, conecté dos reglas del juego para hacer una simulación que guarda los cambios en la base de datos, agregué una interfaz de dominio (`InspeccionableEnPartida`) y una jerarquía de excepciones propias con un handler HTTP para cada una.

---

## 2. Qué apliqué en mi modelo e invariantes

- **Tres clases/familias persistidas en H2:**
  - `Arma` (clase padre abstracta) con sus subclases concretas (`M4A4`, `MP9`, `DesertEagle`, `Glock18`, `Nova`, `GranadaHE`).
  - `Agente` (clase padre abstracta) con sus dos hijas (`AgenteCT` y `AgenteTerrorista`).
  - `Equipo` (guardado con `@Embeddable` dentro de las tablas de armas y agentes).
- **Invariantes que cuida el dominio:**
  - En `Arma`: el nombre no puede estar vacío, el daño tiene que ser mayor a `0` y hasta `500`, la munición no puede ser negativa ni superar la `capacidadCargador`, la precisión va entre `0.0` y `1.0`, el tiempo de recarga no puede ser negativo y el precio tiene que estar entre `$0` y `$16,000` (el tope de plata de una partida competitiva de CS2).
  - En `Agente`: el nombre no puede estar vacío, la vida y el chaleco Kevlar van de `0` a `100`, y el dinero va de `$0` a `$16,000`.
  - En `Equipo`: el nombre no puede ser nulo ni vacío.
- **Interfaz del dominio (POO-07):**
  - Creé la interfaz `InspeccionableEnPartida` con los métodos `inspeccionarEnPartida()` y `calcularValorEconomico()`, implementada tanto por `Arma` como por `Agente` (dos familias distintas).
- **Dos reglas de negocio en la simulación:**
  - **Regla 1 (munición, cargador y fuego amigo):** Un `Arma` solo puede disparar si le quedan balas en el cargador (`municion > 0`) y solo deja recargar si el cargador no está lleno y la cantidad es positiva (si no, lanza `MunicionInsuficienteException`). Además, si el `Equipo` del arma y el del `Agente` objetivo son el mismo y el fuego amigo está desactivado, lanza `FuegoAmigoException`.
  - **Regla 2 (daño y chaleco Kevlar):** Cuando el disparo o la granada impacta en el `Agente`, el `Arma` gasta 1 bala, calcula el daño según la distancia y si fue headshot, y el `Agente` absorbe el 35% del golpe con su `chalecoKevlar` antes de restar la `vida`. Todo el estado nuevo queda guardado en H2.
- **Jerarquía de excepciones y handlers:**
  - Clase base: `ReglaDeJuegoException`.
  - Hija 1: `MunicionInsuficienteException` (el controller la atiende y devuelve `409 Conflict`).
  - Hija 2: `FuegoAmigoException` (el controller la atiende y devuelve `422 Unprocessable Entity`).

---

## 3. URLs del servicio

- `GET /` — Muestra mi nombre, el dominio y las rutas disponibles (`200 OK`).
- `POST /api/armas` — Crea un arma nueva en H2 (`201 Created`, o `400 Bad Request` si los datos rompen algún invariante).
- `GET /api/armas/todas` — Lista todas las armas guardadas en H2 (`200 OK`).
- `GET /api/armas/{id}` — Busca un arma por su `id` en H2 (`200 OK` o `404 Not Found` si no existe).
- `PUT /api/armas/{id}` — Actualiza los datos de un arma en H2 (`200 OK`).
- `DELETE /api/armas/{id}` — Borra un arma de H2 por su `id` (`200 OK` o `404 Not Found`).
- `GET /api/armas/protocolo` — Usa la interfaz `InspeccionableEnPartida` para mostrar armas y agentes juntos (`200 OK`).
- `POST /api/armas/{id}/simular?equipoObjetivo=Terrorista&distancia=15&headshot=true&chaleco=true&fuegoAmigo=false` — Corre la simulación sobre el arma guardada en H2, le descuenta munición y le baja vida y chaleco al agente (`200 OK`).
- `POST /api/armas/{id}/recargar?balas=5` — Recarga balas en el arma guardada en H2 (`200 OK`, o `409 Conflict` si se pasa `balas=0` o ya está llena).
- `GET /api/armas/granadas/simular?equipoAtacante=Counter-Terrorista&equipoObjetivo=Terrorista&distancia=2.0&chaleco=true` — Simula la explosión de una `GranadaHE` con `Agente` y `Equipo`.
- `GET /api/agentes/todos` — Lista los agentes que quedaron guardados en H2 después de simular.
- `http://localhost:8080/h2-console` — Consola de H2 (`JDBC URL`: `jdbc:h2:file:./data/armas`, usuario: `sa`, sin contraseña).

---

## 4. Cómo probarlo

Para levantar el proyecto desde la terminal:

```bash
./mvnw spring-boot:run
```

En la carpeta `insomnia/` del repositorio dejé el archivo `insomnia-cs2-collection.json`. Después de importarlo en Insomnia, se pueden probar los pedidos en orden:

1. `01. GET /` para ver que el servidor está levantado.
2. `02. POST /api/armas` para crear una `DesertEagle` en H2 (devuelve `201 Created` y el `id`).
3. `03. GET /api/armas/todas` para ver que aparece en la lista de H2.
4. `04. GET /api/armas/{id}` para consultar esa arma por su `id` (arranca con `7` balas).
5. `05. PUT /api/armas/{id}` para cambiarle el nombre, daño y precio y ver que se guarda el cambio.
6. `06. POST /api/armas (Ilegal)` mandando daño `-10` y munición `-5`: devuelve error `400 Bad Request` y no guarda nada inválido en la base.
7. `07. GET /api/armas/9999` buscando un `id` que no existe: devuelve `404 Not Found`.
8. `08. GET /api/armas/protocolo` para probar el endpoint de la interfaz `InspeccionableEnPartida`.
9. `09. POST /api/armas/{id}/simular` para simular el disparo contra un `Terrorista`: baja la munición de `7` a `6` y le descuenta vida y Kevlar al agente. Si se vuelve a tirar el `04. GET /api/armas/{id}`, se ve que ahora quedó con `6` balas en H2.
10. `10. POST /api/armas/{id}/recargar?balas=0` para provocar la primera excepción hija (`MunicionInsuficienteException`, devuelve `409 Conflict`).
11. `11. POST /api/armas/{id}/simular (Mismo Equipo)` atacando a un aliado con `fuegoAmigo=false` para provocar la segunda excepción hija (`FuegoAmigoException`, devuelve `422 Unprocessable Entity`).
12. `12. DELETE /api/armas/{id}` para borrar el arma de H2 (si después se consulta de nuevo con `GET`, da `404 Not Found`).

Para correr los tests del proyecto:

```bash
./mvnw test
```
