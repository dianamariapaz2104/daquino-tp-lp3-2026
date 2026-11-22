# POO-09 — Challenge de APIs Cruzadas (Enunciado Aplicado a mi Entrega)

**Alumna:** Diana Aquino  
**Dominio:** Counter-Strike 2 (CS2)  
**Enlace exacto al commit de la solución:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/b7a4b4b160568776746ba37145a5ce9abbb9acbc

---

## 1. Quiénes son A1, A2 y A3

- **A1 (Mi servicio):** **Diana Aquino**
  - **Repositorio:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026
  - **URL Base:** `http://localhost:8080`
  - **OpenAPI 3:** `http://localhost:8080/v3/api-docs` (Swagger UI: `http://localhost:8080/swagger-ui/index.html`)
  - **Reglas publicadas:**
    1. Control de munición en cargador (`POST /api/reglas/municion` y `POST /api/armas/{id}/recargar` → `200 OK` o `409 Conflict` con `MunicionInsuficienteException`).
    2. Daño balístico/explosivo con absorción del 35% en chaleco Kevlar y bloqueo de fuego amigo entre entidades del mismo equipo (`POST /api/reglas/danio` y `POST /api/armas/{id}/simular` → `200 OK` o `422 Unprocessable Entity` con `FuegoAmigoException`).

- **A2 (Compañero 1 consultado):** **Matias Enciso**
  - **Repositorio:** https://github.com/MatiasEnc/menciso-tp-lp3-2026 (subcarpeta `cs2/`)
  - **URL Base:** `http://localhost:8081`
  - **OpenAPI 3:** `http://localhost:8081/v3/api-docs` (Swagger UI: `http://localhost:8081/swagger-ui/index.html`)
  - **Regla consultada:** `GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false` → Valida y calcula la atenuación de daño explosivo por distancia y la mitigación con chaleco Kevlar (`200 OK`), o rechaza con `400 Bad Request` (`ValidationException`) si la distancia es negativa.

- **A3 (Compañero 2 consultado):** **Fabricio Gonzalez**
  - **Repositorio:** https://github.com/FabricioG29/fg-taller-git-2026
  - **URL Base:** `http://localhost:8082`
  - **OpenAPI 3:** `http://localhost:8082/v3/api-docs` (Swagger UI: `http://localhost:8082/swagger-ui.html`)
  - **Regla consultada:** `POST /api/reglas/danio` con cuerpo `{"vida":100,"armadura":100,"danio":36}` → Calcula la vida y el chaleco restantes donde el chaleco absorbe la mitad (`200 OK`), o rechaza con `422 Unprocessable Entity` (`JUGADOR_ELIMINADO`) si la vida ya es `0` (también publica `POST /api/reglas/municion` → `200 OK` o `409 SIN_MUNICION`).

---

## 2. El escenario en una frase

**«Asalto Táctico Coordinado en Ronda Competitiva» (`POST /api/escenario/jugadas`):** Un operador ejecuta una jugada combinada sobre un objetivo en el sitio de bomba que consulta primero la regla de explosión y blindaje en **A2 (Matias Enciso)** y luego la regla de daño con chaleco en **A3 (Fabricio Gonzalez)**, y **solo guarda la jugada (`JugadaEscenario`) en la base de datos H2 de A1 (`201 Created`) si las dos APIs la aceptan**; si alguna la rechaza, la base H2 de A1 queda igual que antes y la respuesta indica explícitamente de quién fue el rechazo (`"rechazadoPor": "A2"` o `"rechazadoPor": "A3"`).

---

## 3. Cómo probarlo

### Paso 1: Arrancar los tres servicios (A2 en `8081`, A3 en `8082` y A1 en `8080`)

**Terminal 1 — Compañero A2 (Matias Enciso):**
```bash
git clone https://github.com/MatiasEnc/menciso-tp-lp3-2026.git
cd menciso-tp-lp3-2026/cs2
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Terminal 2 — Compañero A3 (Fabricio Gonzalez):**
```bash
git clone https://github.com/FabricioG29/fg-taller-git-2026.git
cd fg-taller-git-2026
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

**Terminal 3 — Mi servicio A1 (Diana Aquino):**
```bash
git clone https://github.com/dianamariapaz2104/daquino-tp-lp3-2026.git
cd daquino-tp-lp3-2026
./mvnw spring-boot:run
```

### Paso 2: Ejecutar los pedidos (con Insomnia importando `insomnia/insomnia-cs2-collection.json` o con `curl`)

1. **Escenario feliz (`201 Created` — A2 y A3 aceptan y se guarda en el H2 de A1):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/escenario/jugadas?cargador=3&balasDisparo=2&vidaObjetivo=100&armaduraObjetivo=100&danio=36&distanciaExplosion=2.5&equipoObjetivo=Terrorista"
   ```
2. **Verificar con `GET` que la jugada se guardó en el H2 de A1 (`200 OK`):**
   ```bash
   curl -s "http://localhost:8080/api/escenario/jugadas"
   ```
3. **Rechazo de A2 — Matias Enciso (`400 Bad Request` — `"rechazadoPor": "A2"`, la base H2 de A1 queda igual):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/escenario/jugadas?distanciaExplosion=-5.0&vidaObjetivo=100&equipoObjetivo=Terrorista"
   ```
4. **Rechazo de A3 — Fabricio Gonzalez (`422 Unprocessable Entity` — `"rechazadoPor": "A3"`, la base H2 de A1 queda igual):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/escenario/jugadas?distanciaExplosion=2.5&vidaObjetivo=0&armaduraObjetivo=0&danio=36&equipoObjetivo=Terrorista"
   ```
5. **Verificar con `GET` que la base H2 de A1 no cambió tras los rechazos (`200 OK`):**
   ```bash
   curl -s "http://localhost:8080/api/escenario/jugadas"
   ```
