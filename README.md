# Counter-Strike 2 - API REST (TP LP3 - POO-09 y Cierre POO-08)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

Trabajo práctico de Lenguaje de Programación 3 (CYT646). Servicio web desarrollado con Spring Boot 3.3.4, Java 21, Spring Data JPA, base de datos H2 y OpenAPI 3 (`springdoc-openapi`) que modela las armas, agentes, equipos y jugadas tácticas de **Counter-Strike 2** respetando la arquitectura en 3 capas (`Controller` -> `Service` -> `Repository`) y orquestando un escenario de **APIs cruzadas (A1 -> A2 y A3)**.

> **Enlace exacto al commit de la solución (POO-09 + Cierre POO-08):** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/e25dcb4cfa2e18976d5c71ccc6fecc70768222d3  
> **Commit de cierre de código POO-08:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/6d9002dfd47f69a87a98cc60b50d6d4fb11da005  
> **Commit entrega POO-08 inicial:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/2b8dd29af0449bda75290b1d571a031b24019f9f  
> **Commit entrega POO-06:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/f8757155883626bc2b6ccb6883b07afd4650fa70

---

## 1. Cierre de Observaciones de POO-08 (Los 3 puntos de corrección)

Todas las observaciones del documento de corrección de POO-08 fueron cerradas y verificadas con tests automatizados (`25/25 BUILD SUCCESS`):

| Criterio observado en POO-08 | Observación del profesor | Cómo se cerró en esta entrega |
| :--- | :--- | :--- |
| **Criterio 2: Organización en paquetes** *(tenía 2/3)* | Las entidades del dominio (`Arma`, `Agente`, `Equipo`, `Granada`, `GranadaHE`) lanzaban `IllegalArgumentException`, `MunicionInsuficienteException` y `FuegoAmigoException`. Las validaciones y excepciones deben vivir exclusivamente en el `service`. | Se eliminaron **todos** los `throw new ...` de todas las clases de `py.edu.uc.lp3.domain` (y de los controllers). Ahora las entidades solo mantienen su estado y **todas las validaciones, reglas del juego y excepciones viven exclusivamente en `ArmaService` y `EscenarioCruzadoService`**. |
| **Criterio 3: Ocultamiento e invariantes** *(tenía 0/3)* | `Granada` exponía `setRadioExplosion` y `setTiempoExplosion` públicos sin validación, por lo que un `POST` de `GranadaHE` con `radioExplosion: -3` respondió `201` y guardó un radio negativo en H2. Además `setVida` y `setMunicion` eran públicos sin control completo. | Se eliminaron `setRadioExplosion`, `setTiempoExplosion`, `setVida`, `setMunicion`, `setDaño` y todos los setters públicos libres de las entidades (`domain`), usando `@JsonAutoDetect` por campos y métodos de transición validados. `ArmaService.validarReglasDeNegocio` valida todos los invariantes de `Arma` y de cada subclase (incluyendo `radioExplosion > 0` y `tiempoExplosion > 0` en `Granada`, y `municion <= capacidadCargador`). Un `POST /api/armas` con `radioExplosion: -3` ahora responde **`400 Bad Request`** y no guarda nada en H2 (verificado en `ArmaControllerTest.serviceRechazaGranadaConRadioExplosionNegativo`). |
| **Criterio 6: Comportamiento observable** *(tenía 2/3)* | La simulación creaba un agente objetivo nuevo en cada pedido en lugar de trabajar solo sobre entidades guardadas con el CRUD en H2, y `FuegoAmigoExcexption` salía de un parámetro de la URL y no del estado guardado. | Se agregó el CRUD completo de `Agente` (`/api/agentes`) y se modificó `ArmaService.simularCombateConEntidadesPersistidas` para que cargue tanto el `Arma` (`id`) como el `Agente` (`agenteObjetivoId`) guardados en H2, y determine `FuegoAmigoException` (`422`) comparando directamente el `Equipo` del `Arma` guardada en H2 contra el `Equipo` del `Agente` guardado en H2. Además, `GET /api/armas` ya no inserta filas nuevas en H2. |
| **Criterio 8: Documentación** *(tenía 2/3)* | 1) En «PUT y PATCH» el README decía que omitir `daño` daba `400`, pero en realidad Jackson usa el constructor vacío de `DesertEagle` (`daño = 40`, `tiempoRecarga = 2.0`) y responde `200`. <br> 2) Las especificaciones decían que los topes de `500` de daño y `$16,000` de precio los cuidaba el dominio, cuando viven en `ArmaService`. | Se actualizaron tanto el `README.md` como `ESPECIFICACIONES.md` para explicar con exactitud cómo funciona `PUT` con los valores por defecto del constructor de la subclase y para dejar asentado de forma uniforme que todos los invariantes y topes viven exclusivamente en `ArmaService`. |

---

## 2. Mapa del Grupo Round Robin (POO-09: A1, A2 y A3)

Dominio del grupo: **Counter-Strike 2 (CS2)**.

| Rol | Integrante | Repositorio | URL Base Local | URL del Documento OpenAPI 3 | Reglas publicadas por el dueño (en una frase) |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **A1 (Propio)** | **Diana Aquino** | [dianamariapaz2104/daquino-tp-lp3-2026](https://github.com/dianamariapaz2104/daquino-tp-lp3-2026) | `http://localhost:8080` | JSON: `http://localhost:8080/v3/api-docs` <br> UI: `http://localhost:8080/swagger-ui/index.html` | **Regla 1:** Valida munición en cargador (`409`) y bloquea fuego amigo entre entidades del mismo equipo guardadas en H2 (`422`). **Regla 2:** Calcula daño balístico/explosivo con absorción del 35% en chaleco Kevlar y descuenta vida del `Agente` en H2. |
| **A2 (Compañero 1)** | **Matias Enciso** | [MatiasEnc/menciso-tp-lp3-2026](https://github.com/MatiasEnc/menciso-tp-lp3-2026) | `http://localhost:8081` | JSON: `http://localhost:8081/v3/api-docs` <br> UI: `http://localhost:8081/swagger-ui/index.html` | **Regla consultada en A2 (`GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false`):** Ejecuta las 2 reglas de negocio de CS2 (atenuación de daño por distancia + fuego amigo al 35%, y mitigación de daño con chaleco Kevlar), o rechaza con `400 ValidationException` si `distancia < 0`. |
| **A3 (Compañero 2)** | **Fabricio Gonzalez** | [FabricioG29/fg-taller-git-2026](https://github.com/FabricioG29/fg-taller-git-2026) | `http://localhost:8082` | JSON: `http://localhost:8082/v3/api-docs` <br> UI: `http://localhost:8082/swagger-ui.html` | **Regla 1 (`POST /api/reglas/municion`):** Recibe `{"cargador":3,"disparos":2}`, responde `200` si la ráfaga alcanza o `409 SIN_MUNICION`. **Regla 2 consultada (`POST /api/reglas/danio`):** Recibe `{"vida":100,"armadura":100,"danio":36}`, responde `200` con la vida y chaleco restantes (absorbe la mitad) o `422 JUGADOR_ELIMINADO` si la vida ya es `0`. |

*(Nota: Las URLs de A2 y A3 son 100% externalizables mediante `A2_BASE_URL`, `A2_ENDPOINT`, `A3_BASE_URL` y `A3_ENDPOINT` en `application.properties` o variables de entorno, por lo que pueden apuntar tanto a `localhost:8081`/`localhost:8082` en la misma máquina como a las IPs de la red de la sala `http://10.40.x.x:8080`).*

---

## 3. Cómo Clonar y Arrancar los Tres Servicios (A1, A2 y A3)

Para probar el escenario completo en una misma máquina en tres terminales distintas (primero deben estar arriba **A2** y **A3**, y luego se invoca el escenario en **A1**):

### Terminal 1 — Clonar y Arrancar Compañero A2 (Matias Enciso) en puerto `8081`:
```bash
git clone https://github.com/MatiasEnc/menciso-tp-lp3-2026.git
cd menciso-tp-lp3-2026/cs2
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Terminal 2 — Clonar y Arrancar Compañero A3 (Fabricio Gonzalez) en puerto `8082`:
```bash
git clone https://github.com/FabricioG29/fg-taller-git-2026.git
cd fg-taller-git-2026
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

### Terminal 3 — Arrancar Servicio Propio A1 (Diana Aquino) en puerto `8080`:
```bash
cd daquino-tp-lp3-2026
./mvnw spring-boot:run
```

Si A2 o A3 corren en otra notebook de la sala (por ejemplo en `http://10.40.22.56:8080` y `http://10.40.143.189:8080`), se arranca A1 pasando las variables de entorno sin tocar el código:
```bash
A2_BASE_URL=http://10.40.22.56:8080 A3_BASE_URL=http://10.40.143.189:8080 ./mvnw spring-boot:run
```

Para correr las **25 pruebas automatizadas** (que incluyen servidores HTTP embebidos para verificar el escenario feliz y los rechazos de A2 y A3):
```bash
./mvnw test
```

---

## 4. Diagramas UML de Secuencia del Escenario Cruzado (Mermaid)

### Escenario 1: Camino Feliz (`201 Created` — A2 y A3 aceptan y A1 persiste en H2)

```mermaid
sequenceDiagram
    actor Cliente as Insomnia (Cliente)
    participant A1 as Diana Aquino (A1 - Escenario :8080)
    participant A2 as Matias Enciso (A2 - Explosión/Blindaje :8081)
    participant A3 as Fabricio Gonzalez (A3 - Daño con Chaleco :8082)
    participant H2 as Base de Datos H2 de A1

    Cliente->>A1: POST /api/escenario/jugadas?cargador=3&balasDisparo=2&vidaObjetivo=100&armaduraObjetivo=100&danio=36&distanciaExplosion=2.5
    A1->>A1: Arma pedido HTTP según OpenAPI de A2 (log DEBUG)
    A1->>A2: GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false
    A2-->>A1: 200 OK (Atenuación por distancia y mitigación de blindaje Kevlar)
    A1->>A1: Arma pedido HTTP según OpenAPI de A3 (log DEBUG)
    A1->>A3: POST /api/reglas/danio ({"vida":100,"armadura":100,"danio":36})
    A3-->>A1: 200 OK (Vida y chaleco restantes calculados)
    A1->>H2: Actualiza Arma y Agente y guarda JugadaEscenario en tabla jugadas_escenario
    H2-->>A1: JugadaEscenario persistida (ID generado)
    A1-->>Cliente: 201 Created (JugadaEscenario con validaciones de A2 y A3)
```

### Escenario 2: Rechazo del Compañero A2 (`400 Bad Request` — El H2 de A1 queda intacto)

```mermaid
sequenceDiagram
    actor Cliente as Insomnia (Cliente)
    participant A1 as Diana Aquino (A1 - Escenario :8080)
    participant A2 as Matias Enciso (A2 - Explosión/Blindaje :8081)
    participant A3 as Fabricio Gonzalez (A3 - Daño con Chaleco :8082)
    participant H2 as Base de Datos H2 de A1

    Cliente->>A1: POST /api/escenario/jugadas?distanciaExplosion=-5.0&vidaObjetivo=100
    A1->>A2: GET /api/simulacion/explosion?distancia=-5.0&conBlindaje=true&fuegoAmigo=false
    A2-->>A1: 400 Bad Request (ValidationException: La distancia no puede ser negativa)
    Note over A1,H2: A1 no invoca a A3 y deja su base H2 sin cambios
    A1-->>Cliente: 400 Bad Request {"rechazadoPor": "A2", "estadoH2Local": "SIN_CAMBIOS"}
```

### Escenario 3: A2 acepta pero el Compañero A3 rechaza (`422 JUGADOR_ELIMINADO` — El H2 de A1 queda intacto)

```mermaid
sequenceDiagram
    actor Cliente as Insomnia (Cliente)
    participant A1 as Diana Aquino (A1 - Escenario :8080)
    participant A2 as Matias Enciso (A2 - Explosión/Blindaje :8081)
    participant A3 as Fabricio Gonzalez (A3 - Daño con Chaleco :8082)
    participant H2 as Base de Datos H2 de A1

    Cliente->>A1: POST /api/escenario/jugadas?distanciaExplosion=2.5&vidaObjetivo=0&armaduraObjetivo=0&danio=36
    A1->>A2: GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false
    A2-->>A1: 200 OK (Explosión válida)
    A1->>A3: POST /api/reglas/danio ({"vida":0,"armadura":0,"danio":36})
    A3-->>A1: 422 Unprocessable Entity (JUGADOR_ELIMINADO: vida ya es 0)
    Note over A1,H2: A1 aborta la transacción y deja su base H2 igual que antes
    A1-->>Cliente: 422 Unprocessable Entity {"rechazadoPor": "A3", "estadoH2Local": "SIN_CAMBIOS"}
```

---

## 5. Respuestas a «Qué hay que pensar» (POO-09 y POO-08)

### Preguntas de POO-09 (APIs Cruzadas y OpenAPI)

- **OpenAPI:**  
  Mi documento OpenAPI 3 vive en `http://localhost:8080/v3/api-docs` (JSON importable directo en Insomnia) y la interfaz visual Swagger UI vive en `http://localhost:8080/swagger-ui/index.html`. Allí están documentadas con `@Operation` y `@ApiResponses` todas las operaciones del CRUD de `Arma` y `Agente`, la simulación de combate (`POST /api/armas/{id}/simular`), la recarga (`POST /api/armas/{id}/recargar`), la simulación de granadas (`POST /api/armas/granadas/simular`), los códigos de error de las dos reglas (`400`, `404`, `409 Conflict` por `MunicionInsuficienteException`, `422 Unprocessable Entity` por `FuegoAmigoException`) y el escenario cruzado (`POST /api/escenario/jugadas`).

- **Grupo:**  
  Yo soy **A1 (`Diana Aquino`)** en `http://localhost:8080`. Consulto a **A2 (`Matias Enciso`)** en `http://localhost:8081` (regla de atenuación de daño explosivo por distancia y mitigación con chaleco Kevlar en `GET /api/simulacion/explosion`) y a **A3 (`Fabricio Gonzalez`)** en `http://localhost:8082` (regla de validación de daño con absorción del 50% en chaleco y verificación de jugador vivo en `POST /api/reglas/danio`, más `POST /api/reglas/municion`).

- **Escenario:**  
  El hecho del juego Counter-Strike 2 que modelé en `POST /api/escenario/jugadas` es un **«Asalto Táctico Coordinado en Ronda Competitiva»**: un operador ejecuta una jugada combinada sobre la posición enemiga en el sitio de bomba. Para que la jugada sea legal, primero necesita que **A2 (`Matias Enciso`)** valide la detonación explosiva y el blindaje a esa distancia (`distanciaExplosion >= 0` en `GET /api/simulacion/explosion`) y luego que **A3 (`Fabricio Gonzalez`)** valide el daño con chaleco sobre un objetivo vivo (`vidaObjetivo > 0` en `POST /api/reglas/danio`).
  - *¿Qué se ve en mi base después de un escenario exitoso?* Al hacer `GET /api/escenario/jugadas`, aparece una nueva fila `JugadaEscenario` en la tabla `jugadas_escenario` de H2 con el daño aplicado, la munición restante del arma local, la vida restante del agente objetivo y la constancia de que A2 y A3 aceptaron.
  - *¿Qué queda igual si A2 acepta y A3 rechaza?* Si A2 responde `200 OK` pero A3 responde error (`422 JUGADOR_ELIMINADO` o `400`), `EscenarioCruzadoService` lanza `RechazoCompaneroException` antes de tocar los repositorios locales: la tabla `jugadas_escenario`, la munición del `Arma` y la vida del `Agente` en el H2 de A1 quedan exactamente igual que antes.

- **Cliente:**  
  En `CompaneroApiClient` usé `RestClient` de Spring Boot 3 con las URLs y rutas externalizadas en `application.properties` mediante `@Value` y variables de entorno (`cs2.peers.a2.base-url=${A2_BASE_URL:http://localhost:8081}`, `cs2.peers.a2.endpoint=${A2_ENDPOINT:/api/simulacion/explosion}`, `cs2.peers.a3.base-url=${A3_BASE_URL:http://localhost:8082}`, `cs2.peers.a3.endpoint=${A3_ENDPOINT:/api/reglas/danio}`) y logs en nivel `DEBUG` (`logging.level.py.edu.uc.lp3=DEBUG`) para cada paso de la llamada.

- **Errores:**  
  Cuando A2 o A3 devuelve un error HTTP (`4xx` o `5xx`), `CompaneroApiClient` atrapa `RestClientResponseException` y la traduce a `RechazoCompaneroException` (hija de mi jerarquía `ReglaDeJuegoException`), conservando el código HTTP original y el cuerpo JSON que devolvió el compañero. El `@ExceptionHandler(RechazoCompaneroException.class)` en `EscenarioCruzadoController` devuelve el campo `"rechazadoPor": "A2"` o `"rechazadoPor": "A3"` junto con `"nombreCompanero"`, `"urlConsultada"`, `"codigoHttpCompanero"` y `"detalleRechazoCompanero"`, distinguiendo sin ambigüedad cuál de los dos servicios rechazó la jugada.

- **Pregunta de anclaje:**  
  *¿Si el compañero cambia el nombre de una ruta, alcanza con actualizar lo que dice el OpenAPI, o había código copiado de su dominio?*  
  Alcanza únicamente con actualizar la propiedad `cs2.peers.a2.endpoint` o `cs2.peers.a3.endpoint` en `application.properties` (o la variable de entorno `A2_ENDPOINT` / `A3_ENDPOINT`) según lo que indique su nuevo OpenAPI, porque no hay ninguna clase ni regla copiada del dominio del compañero en mi proyecto.

- **Qué se hizo si un compañero no respondía en vivo:**  
  Se dejaron configuradas las propiedades para poder levantar los repositorios de A2 y A3 localmente en los puertos `8081` y `8082`, se incluyeron las **respuestas de ejemplo exportadas** tanto en las descripciones de `insomnia/insomnia-cs2-collection.json` como en este README (camino feliz `201`, rechazo de A2 `400` y rechazo de A3 `422`), y en `ArmaControllerTest` se incluyeron pruebas automatizadas con servidores HTTP reales para ambos compañeros.

---

### Preguntas de POO-08 (Persistencia H2, Invariantes y Excepciones)

- **Recurso y verbo:** Los sustantivos principales son `/api/armas`, `/api/agentes` y `/api/escenario/jugadas`. `POST` crea o ejecuta simulaciones que modifican estado, `GET` solo lee sin modificar H2, `PUT` actualiza y `DELETE` elimina.
- **Identidad:** La clave primaria `id` (`Long`) usa `@Id` y `@GeneratedValue(strategy = GenerationType.IDENTITY)` autogenerada por H2.
- **Herencia y tablas:** `Arma` y `Agente` usan `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` con `@DiscriminatorColumn` (`tipo_arma` y `tipo_agente`) y `@JsonTypeInfo`/`@JsonSubTypes` en JSON.
- **Ocultamiento:** Todos los atributos en `domain` son `private`, `gastarMunicion()` es `protected`, las entidades no lanzan excepciones ni tienen setters públicos libres, y todos los invariantes (`radioExplosion > 0`, `tiempoExplosion > 0`, `municion <= capacidadCargador`, `daño` 1..500, `precio` 0..16000, `vida` 0..100) se validan exclusivamente en `ArmaService`.
- **PUT y PATCH:** En `PUT /api/armas/{id}`, Jackson instancia primero la subclase con su constructor vacío (que asigna los valores oficiales de CS2 por defecto, ej. `daño = 40` y `tiempoRecarga = 2.0` en `DesertEagle`) antes de aplicar el JSON; luego `ArmaService.validarReglasDeNegocio` valida todos los campos antes de actualizar la fila en H2.

---

## 6. Cómo Importar y Recorrer la Colección de Insomnia

El archivo está en `insomnia/insomnia-cs2-collection.json`.

1. Arrancar A2 (`8081`), A3 (`8082`) y A1 (`8080`).
2. Importar `insomnia/insomnia-cs2-collection.json` en Insomnia.
3. Recorrer los pedidos organizados en **2 carpetas**:
   - **Carpeta `1. POO-09 (Escenario Cruzado)`:**
     - `01. OpenAPI (GET)` — Consulta el documento OpenAPI 3 de A1 (`/v3/api-docs`).
     - `02. Escenario Feliz (201)` — A2 (Matias Enciso) y A3 (Fabricio Gonzalez) aceptan (`200 OK`) y la jugada se guarda en el H2 de A1 (`201 Created`).
     - `03. Ver Jugadas en H2 (GET)` — Verifica las jugadas persistidas en la tabla `jugadas_escenario` de H2 (`200 OK`).
     - `04. Rechazo de A2 - Matias (400)` — Envía `distanciaExplosion=-5.0`; A2 rechaza con `400`, A1 devuelve `"rechazadoPor": "A2"` y el H2 de A1 no cambia.
     - `05. Rechazo de A3 - Fabricio (422)` — Envía `vidaObjetivo=0`; A2 acepta pero A3 rechaza con `422 JUGADOR_ELIMINADO`, A1 devuelve `"rechazadoPor": "A3"` y el H2 de A1 no cambia.
     - `06. Guardar en Map (POST)` y `07. API Externa (GET)` — Pruebas del `Map` en memoria y consumo de `https://api.restful-api.dev/objects`.
   - **Carpeta `2. POO-08 (CRUD y Reglas)`:**
     - `1. Crear arma (POST)` (`201`), `2. Ver todas las armas (GET)` (`/api/armas/todas`), `3. Ver arma por ID (GET)`, `4. Editar arma (PUT)` y `5. Eliminar arma (DELETE)` — CRUD completo sobre H2.
     - `6. Granada radio -3 (400)` — Verifica el cierre del Criterio 3 de POO-08 (`400 Bad Request`, no guarda en H2).
     - `7. ID inexistente (404)` y `8. Ver Interfaz (GET)` (`/api/protocolo`, interfaz `InspeccionableEnPartida`).
     - `9. Simular Combate (POST)`, `10. Ver Agente en H2 (GET)`, `11. Error Sin Munición (409)` (`MunicionInsuficienteException`) y `12. Error Fuego Amigo (422)` (`FuegoAmigoException`).

---

## 7. Diagrama de Clases Mermaid Actualizado

```mermaid
classDiagram
  class InspeccionableEnPartida {
    <<interface>>
    +inspeccionarEnPartida() String
    +calcularValorEconomico() int
  }

  class Arma {
    <<abstract>>
    -Long id
    -String nombre
    -int daño
    -float precision
    -float tiempoRecarga
    -int precio
    -Equipo equipo
    -int municion
    -int capacidadCargador
    +comportamientoDeCombate() String*
    +disparar() String*
    +disparar(int distanciaMetros) String
    +disparar(int distanciaMetros, boolean tiroALaCabeza) String
    +puedeDisparar() boolean
    #gastarMunicion() void
    +consumirProyectil() void
    +recargar() void
    +recargar(int balas) void
    +aplicarEstadoValidado(...) void
  }

  class Granada {
    <<abstract>>
    -float radioExplosion
    -float tiempoExplosion
    +calcularDañoOndaExpansiva(double distanciaMetros, boolean tieneChaleco) int
    +aplicarParametrosExplosivoValidados(float radioExplosion, float tiempoExplosion) void
  }

  class GranadaHE {
    +comportamientoDeCombate() String
    +disparar() String
    +detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo) String
    +detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo, boolean aplicarReduccionFuegoAmigo) String
  }

  class Agente {
    <<abstract>>
    -Long id
    -String nombre
    -int vida
    -int chalecoKevlar
    -int dinero
    -Equipo equipo
    +rolTactico() String*
    +ejecutarAccionEspecial() String*
    +aplicarImpactoValidado(int nuevaVida, int nuevoChaleco) void
    +aplicarEstadoValidado(...) void
  }

  class JugadaEscenario {
    -Long id
    -String nombreEscenario
    -Long armaIdLocal
    -String nombreArmaLocal
    -Long agenteObjetivoIdLocal
    -String nombreAgenteObjetivoLocal
    -int balasDisparadas
    -double distanciaExplosionMetros
    -int dañoTotalAplicado
    -int municionRestanteArma
    -int vidaRestanteAgente
    -String validacionA2
    -String validacionA3
    -String resultadoFinal
  }

  class ReglaDeJuegoException {
    -String reglaViolada
  }

  class MunicionInsuficienteException {
    -String nombreArma
    -int municionActual
    -int capacidadCargador
  }

  class FuegoAmigoException {
    -String nombreEquipo
    -String accionBloqueada
  }

  class RechazoCompaneroException {
    -String companeroRol
    -String companeroNombre
    -String urlConsultada
    -int codigoHttpCompanero
    -String detalleCompanero
  }

  class ArmaService {
    +validarReglasDeNegocio(Arma arma) void
    +validarReglasDeAgente(Agente agente) void
    +simularCombateConEntidadesPersistidas(...) Map
    +recargarArmaPersistida(Long armaId, int balas) Map
    +simularExplosionGranada(...) Map
  }

  class CompaneroApiClient {
    -Map mapaCacheIntegracion
    +validarReglaEnA2(int cargadorDisponible, int balasDisparo, String equipoObjetivo, double distanciaMetros) Map
    +validarReglaEnA3(int vidaObjetivo, int armaduraObjetivo, int danioCalculado, int cargador, int disparos, double distanciaMetros, String equipoObjetivo) Map
    +consultarApiExternaRestfulDev() List
    +guardarEnMapaData(String clave, Map datos) Map
  }

  class EscenarioCruzadoService {
    +ejecutarEscenarioAsaltoCoordinado(...) JugadaEscenario
    +listarJugadasPersistidas() List~JugadaEscenario~
  }

  class EscenarioCruzadoController {
    +ejecutarEscenario(...) ResponseEntity~JugadaEscenario~
    +listarJugadas() ResponseEntity~List~
    +manejarRechazoCompanero(RechazoCompaneroException ex) ResponseEntity~Map~
  }

  InspeccionableEnPartida <|.. Arma
  InspeccionableEnPartida <|.. Agente
  Arma <|-- Granada
  Granada <|-- GranadaHE
  ReglaDeJuegoException <|-- MunicionInsuficienteException
  ReglaDeJuegoException <|-- FuegoAmigoException
  ReglaDeJuegoException <|-- RechazoCompaneroException
  EscenarioCruzadoController --> EscenarioCruzadoService : usa
  EscenarioCruzadoService --> CompaneroApiClient : consulta A2 y A3
  EscenarioCruzadoService --> ArmaService : aplica impacto local
  EscenarioCruzadoService ..> JugadaEscenario : persiste en H2
```
