# Counter-Strike 2 - API REST con H2, Reglas de Juego, Interfaz y Excepciones (TP LP3 - POO-08)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

Trabajo práctico de **Lenguaje de Programación 3 (CYT646)**. Es un servicio HTTP desarrollado con **Spring Boot 3.3.4**, **Spring Data JPA** y base de datos **H2** sobre el dominio de **Counter-Strike 2**, respetando la arquitectura en capas (`Controller` -> `Service` -> `Repository`), persistencia de tres clases/familias del modelo (`Arma`, `Agente` y `Equipo`), protocolo de dominio (`InspeccionableEnPartida`), simulación persistida alimentada por dos reglas de negocio del juego y una jerarquía de excepciones del dominio (`ReglaDeJuegoException` -> `MunicionInsuficienteException` y `FuegoAmigoException`) con un `@ExceptionHandler` HTTP por tipo.

> **Enlace exacto al commit de la solución (POO-08):** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/1c30587c801668c9619dd0201fbf2eaa71f654db  
> **Commit anterior (CRUD H2 inicial):** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/465a7d8726658c6525f6a1041c2ff58f26a11635  
> **Commit de la entrega POO-06:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/f8757155883626bc2b6ccb6883b07afd4650fa70

---

## 1. Cómo arrancar el servicio y abrir la consola H2

Con **Java 21**, desde la raíz del proyecto se arranca el servidor con:

```bash
./mvnw spring-boot:run
```

- **Servicio vivo:** `http://localhost:8080/`
- **Consola H2:** `http://localhost:8080/h2-console`
  - **JDBC URL:** `jdbc:h2:file:./data/armas`
  - **User Name:** `sa`
  - **Password:** *(dejar vacío)*

Para ejecutar la suite de **20 pruebas automatizadas** (unitarias de dominio e integración MockMvc + H2):

```bash
./mvnw test
```

---

## 2. Propiedades de H2 y JPA explicadas (`src/main/resources/application.properties`)

Cada propiedad configurada en `application.properties` cumple una función específica:

- **`spring.application.name=cs2`**: Define el nombre identificador de la aplicación dentro del contexto de Spring Boot.
- **`spring.datasource.url=jdbc:h2:file:./data/armas`**: Indica a H2 que guarde los datos en un archivo local (`./data/armas.mv.db`) para que el estado persista en disco entre peticiones y reinicios.
- **`spring.datasource.driverClassName=org.h2.Driver`**: Especifica el driver JDBC oficial de H2 que utiliza el pool de conexiones HikariCP para comunicarse con el motor.
- **`spring.datasource.username=sa`**: Configura el usuario administrador por defecto (`System Administrator`) de la base de datos H2 en entorno de desarrollo.
- **`spring.datasource.password=`**: Define la contraseña vacía por defecto para el usuario `sa` en el perfil local del taller.
- **`spring.h2.console.enabled=true`**: Habilita la interfaz web interactiva en `/h2-console` para inspeccionar las tablas `ARMAS` y `AGENTES` con consultas SQL desde el navegador.
- **`spring.jpa.hibernate.ddl-auto=update`**: Ordena a Hibernate crear o actualizar automáticamente las tablas y columnas del esquema relacional a partir de las entidades `@Entity` sin borrar los datos existentes.
- **`spring.jpa.defer-datasource-initialization=true`**: Asegura que Hibernate termine de construir el esquema de tablas antes de ejecutar cualquier inicialización de datos.
- **`spring.jpa.show-sql=true`**: Muestra en la consola las sentencias SQL (`SELECT`, `INSERT`, `UPDATE`, `DELETE`) que Hibernate ejecuta contra H2.
- **`spring.jpa.properties.hibernate.format_sql=true`**: Formatea e indenta las sentencias SQL impresas en la terminal para facilitar su lectura durante las pruebas.

---

## 3. Las dos reglas de negocio del juego (en una frase cada una)

En la simulación participan las tres clases/familias principales del modelado (`Arma` con sus subclases como `DesertEagle`, `M4A4` y `GranadaHE`; `Agente` con sus subclases `AgenteCT` y `AgenteTerrorista`; y `Equipo`):

1. **Regla 1 — Validación de Munición, Cargador y Fuego Amigo (`Arma` / `GranadaHE` / `Equipo`):** Un `Arma` solo puede ejecutar un disparo o recarga si posee proyectiles disponibles en el cargador (`municion > 0`) sin exceder su `capacidadCargador` (de lo contrario lanza `MunicionInsuficienteException`) y si el `Equipo` del `Agente` objetivo no pertenece al mismo bando cuando el fuego amigo competitivo está desactivado (de lo contrario lanza `FuegoAmigoException`).
2. **Regla 2 — Cálculo de Daño Balístico/Explosivo y Absorción de Kevlar (`Arma` / `Agente`):** Al impactar sobre un `Agente`, el `Arma` descuenta un proyectil de su cargador, calcula el daño atenuado según la distancia y el multiplicador crítico de *headshot* (o radio de onda expansiva en `GranadaHE`), y el `Agente` absorbe el 35% del impacto en su `chalecoKevlar` antes de descontar el daño restante de sus puntos de `vida` en H2.

---

## 4. Cambios de Sobrecarga y Sobreescritura

- **Sobrecarga (*Overloading* — misma clase, mismo nombre de método, distinta lista de parámetros):**
  - En `Arma` (y en sus subclases) definí **3 constructores sobrecargados**: el constructor sin argumentos `Arma()` (que también usa JPA y deja valores legales por defecto), el constructor intermedio de 7 parámetros y el constructor completo de 8 parámetros.
  - En `Arma` sobrecargué el método `disparar()`: sin parámetros (`disparar()`), con distancia (`disparar(int distanciaMetros)`) y con distancia más tiro a la cabeza (`disparar(int distanciaMetros, boolean tiroALaCabeza)`). También sobrecargué `recargar()` y `recargar(int balas)`, y en `GranadaHE` sobrecargué `detonarEnArea(...)`.
- **Sobreescritura (*Overriding* — relación padre-hija con `@Override`, misma firma, comportamiento propio polimórfico):**
  - En la jerarquía `Arma`, la clase base declara los métodos abstractos `comportamientoDeCombate()` y `disparar()`, y cada clase hija (`M4A4`, `MP9`, `DesertEagle`, `Glock18`, `Nova`, `GranadaHE`) los sobreescribe con `@Override`.
  - En la jerarquía `Agente`, la clase base declara `rolTactico()` y `ejecutarAccionEspecial()`, y las clases hijas `AgenteCT` y `AgenteTerrorista` los sobreescriben con `@Override` (desactivar C4 con Defuse Kit vs plantar C4 con código `7355608`).

---

## 5. Respuestas a «Qué hay que pensar» del enunciado

### Recurso y verbo
El sustantivo principal de la API es `/api/armas` (acompañado por `/api/agentes`). La URL nombra siempre al recurso (`armas`, `armas/{id}`) y el verbo HTTP indica la operación: `POST /api/armas` para crear (`201 Created`), `GET /api/armas/todas` y `GET /api/armas/{id}` para leer (`200 OK`), `PUT /api/armas/{id}` para actualizar (`200 OK`) y `DELETE /api/armas/{id}` para borrar (`200 OK`).

### Identidad
El campo clave es `id` de tipo `Long`, anotado con `@Id` y `@GeneratedValue(strategy = GenerationType.IDENTITY)` en `Arma` y en `Agente`. Lo asigna automáticamente la base de datos H2 mediante una columna autoincremental al insertar la fila, por lo que el JSON de creación (`POST`) omite el campo `id` y el servidor lo devuelve asignado en la respuesta `201 Created`.

### Herencia y tablas
Elegí la estrategia `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` con `@DiscriminatorColumn(name = "tipo_arma")` para `Arma` y `@DiscriminatorColumn(name = "tipo_agente")` para `Agente`. En este dominio todas las armas comparten el núcleo de atributos balísticos (`daño`, `precision`, `precio`, `municion`, `capacidadCargador`) y las subclases agregan pocos campos específicos, por lo que una tabla única evita múltiples `JOIN` innecesarios al listar el arsenal polimórfico. En el JSON el subtipo se distingue mediante `@JsonTypeInfo` y `@JsonSubTypes` de Jackson sobre la propiedad `"tipoArma"` (ej. `"DesertEagle"`, `"M4A4"`, `"GranadaHE"`).

### Interfaz y repositorio
`ArmaRepository` y `AgenteRepository` son interfaces que extienden `JpaRepository`. Al depender de una interfaz, `ArmaService` y `ArmaController` se acoplan únicamente al contrato de mensajes (`save`, `findById`, `findAll`, `findByNombreIgnoreCase`) sin conocer el código SQL ni la implementación concreta que Spring Data genera en tiempo de ejecución, aplicando el mismo principio de inversión de dependencias y polimorfismo que vimos con los protocolos en POO-07.

### Protocolo del dominio
Definí la interfaz propia `InspeccionableEnPartida` en `py.edu.uc.lp3.domain` con los métodos `inspeccionarEnPartida()` y `calcularValorEconomico()`. La usa el endpoint de lectura `GET /api/armas/protocolo` (y `/api/protocolo`), que recorre una `List<InspeccionableEnPartida>` capaz de hablar de forma transparente con instancias de **dos familias distintas**: la jerarquía `Arma` y la jerarquía `Agente`.

### Ocultamiento (frente al JSON y frente a JPA)
Las reglas numéricas del juego las sostienen los constructores y los setters validados de `Arma`, `Agente` y `Equipo` (además de las reglas de presupuesto en `ArmaService`). El constructor sin argumentos que exige JPA inicializa el objeto con valores legales por defecto y todos los setters verifican los invariantes antes de mutar el estado, impidiendo que Jackson o JPA dejen munición o daño negativos. En la colección de Insomnia, el pedido **`06. POST /api/armas (Ilegal)`** envía `"daño": -10` y `"municion": -5`, recibe `400 Bad Request`, y al ejecutar nuevamente **`03. GET /api/armas/todas`** se comprueba que la tabla en H2 quedó intacta sin basura.

### PUT y PATCH
En nuestra API usamos `PUT /api/armas/{id}`, el cual reemplaza el estado completo de los atributos editables del recurso existente (`nombre`, `daño`, `precision`, `tiempoRecarga`, `precio`, `equipo`, `capacidadCargador`, `municion`). Si en el cuerpo JSON de un `PUT` se omite un campo primitivo numérico (como `daño`), Jackson lo deserializa como `0`, lo cual dispara inmediatamente el invariante del dominio (`daño <= 0`) y rechaza la petición con `400 Bad Request` para evitar sobrescribir el arma con valores inválidos.

### DELETE
En nuestro modelo, borrar (`DELETE /api/armas/{id}`) significa retirar definitivamente esa pieza de armamento del inventario persistido en H2 (`armaRepository.delete(existente)`). Ese verbo HTTP verifica primero que el recurso exista en la base (si no está, lanza `NoSuchElementException` -> `404 Not Found`) y elimina la fila de la tabla `ARMAS`; un `GET /api/armas/{id}` posterior devuelve `404 Not Found`.

### Dos reglas y simulación
Los dos hechos del juego que cambian el estado de más de un objeto a la vez son el **disparo/detonación con validación de munición y fuego amigo** y la **recepción de daño balístico/explosivo con desgaste de chaleco Kevlar y vida**. El recurso HTTP de la simulación es `POST /api/armas/{id}/simular` (además de `/api/armas/granadas/simular`). Al ejecutarlo, se descuenta 1 proyectil del `Arma` y se reducen el `chalecoKevlar` y la `vida` del `Agente` objetivo; ambos cambios se guardan en H2 y un `GET /api/armas/{id}` (o `GET /api/agentes/todos`) posterior muestra el nuevo estado ya persistido.

### Excepciones (jerarquía y comprobadas vs no comprobadas)
La jerarquía de excepciones del dominio tiene como clase base a `ReglaDeJuegoException` y dos clases hijas nombradas según la regla específica que falló:
1. `MunicionInsuficienteException` (cuando se intenta disparar con cargador en `0` o recargar una cantidad inválida / con el cargador lleno -> respondida con **`409 Conflict`**).
2. `FuegoAmigoException` (cuando se intenta atacar a un `Agente` del mismo `Equipo` con fuego amigo deshabilitado -> respondida con **`422 Unprocessable Entity`**).

Un recurso inexistente (`NoSuchElementException` -> `404 Not Found`) y un invariante básico de construcción (`IllegalArgumentException` -> `400 Bad Request`) son tipos distintos de las excepciones de reglas tácticas de partida. El JSON de error incluye `error`, `tipoExcepcion`, `jerarquiaPadre`, `reglaViolada`, `mensaje`, datos de contexto (`arma`/`municionActual` o `equipo`/`accionBloqueada`) y `estado`. Elegimos **excepciones no comprobadas** (*unchecked*, extendiendo `RuntimeException`) porque representan violaciones a precondiciones y reglas del dominio que deben hacer *rollback* automático de la transacción `@Transactional` de Spring sin ensuciar las firmas de los métodos ni de las interfaces con cláusulas `throws`.

### Preguntas de anclaje de la cátedra
- **¿Si mañana cambiamos H2 por otra base (ej. PostgreSQL), qué archivos tocan y cuáles se quedan quietos?**  
  Solo se tocan `pom.xml` (para agregar el driver JDBC de PostgreSQL) y `src/main/resources/application.properties` (para cambiar `spring.datasource.url`, `driverClassName`, usuario y clave). Todas las clases del dominio (`domain`), los repositorios (`repository`), el servicio (`service`) y los controladores (`rest.controller`) **se quedan completamente quietos**.
- **¿Quién lanza la excepción, el controller o el objeto del juego?**  
  La excepción la lanza **el objeto del juego en el dominio** (`Arma` y `GranadaHE` lanzan `MunicionInsuficienteException` y `FuegoAmigoException` en sus propios métodos `dispararContraAgente`, `cargarMunicionValidada` y `detonarEnArea`). El `ArmaController` solo recibe el pedido HTTP, invoca al servicio/dominio y captura cada excepción en su `@ExceptionHandler` correspondiente para traducirla a JSON y código de estado HTTP.

---

## 6. Cómo importar y recorrer la colección de Insomnia

El archivo de la colección está versionado en **`insomnia/insomnia-cs2-collection.json`**.

### Pasos para importar en Insomnia:
1. Abrir **Insomnia** -> hacer clic en **Import** (o `Application` -> `Preferences` -> `Data` -> `Import Data`).
2. Seleccionar el archivo `insomnia/insomnia-cs2-collection.json` del repositorio.
3. Verificar que esté activo el entorno **Base Environment** (`base_url = http://localhost:8080`, `arma_id = 1`).

### Orden de recorrido paso a paso (pedidos 01 al 12):
1. **`01. GET /`**: Confirma que el servicio está vivo (`200 OK`).
2. **`02. POST /api/armas`**: Crea una `DesertEagle` válida en H2 y devuelve `201 Created` con su `"id"` (si el `id` devuelto es distinto de `1`, actualizar la variable `arma_id` en el entorno de Insomnia).
3. **`03. GET /api/armas/todas`**: Lista la colección desde H2 y muestra el arma recién persistida.
4. **`04. GET /api/armas/{id}`**: Consulta el arma por su `id` (`200 OK`, `"municion": 7`).
5. **`05. PUT /api/armas/{id}`**: Actualiza el nombre a `"Desert Eagle Printstream"`, daño a `50` y precio a `750` (`200 OK`). Un `GET` posterior confirma el cambio.
6. **`06. POST /api/armas (Ilegal)`**: Envía daño `-10` y munición `-5`; recibe `400 Bad Request` y la colección en H2 no crece con basura.
7. **`07. GET /api/armas/9999`**: Consulta un `id` inexistente y recibe `404 Not Found`.
8. **`08. GET /api/armas/protocolo`**: Ejecuta el endpoint del protocolo `InspeccionableEnPartida` devolviendo instancias de ambas familias (`Arma` y `Agente`).
9. **`09. POST /api/armas/{id}/simular`**: Ejecuta la simulación en el caso feliz (contra `Terrorista` a `15m` con `headshot=true`). Descuenta la munición de `7` a `6` en el `Arma` y reduce la vida y el Kevlar del `Agente` en H2. Si volvés a ejecutar el pedido **`04. GET /api/armas/{id}`**, verás `"municion": 6` persistido en H2.
10. **`10. POST /api/armas/{id}/recargar?balas=0`**: Dispara la **Excepción Hija 1 (`MunicionInsuficienteException`)**; su handler específico responde **`409 Conflict`** con `"tipoExcepcion": "MunicionInsuficienteException"`.
11. **`11. POST /api/armas/{id}/simular (Mismo Equipo)`**: Dispara la **Excepción Hija 2 (`FuegoAmigoException`)** al atacar a `Counter-Terrorista` con `fuegoAmigo=false`; su handler específico responde **`422 Unprocessable Entity`** con `"tipoExcepcion": "FuegoAmigoException"`.
12. **`12. DELETE /api/armas/{id}`**: Elimina el arma de H2 (`200 OK`). Si ejecutás **`04. GET /api/armas/{id}`** después, responde **`404 Not Found`**.

---

## 7. Diagrama de Clases Mermaid (Dominio, Interfaz, Repositorios, Controller y Excepciones)

El diagrama distingue explícitamente:
- **Herencia (`<|--`)**: entre `Arma` y sus subclases (`Pistola`, `Rifle`, `Escopeta`, `SubfusilSMG`, `Francotirador`, `Granada`, `DesertEagle`, `Glock18`, `M4A4`, `MP9`, `Nova`, `GranadaHE`), y entre `Agente` y sus dos hijas (`AgenteCT`, `AgenteTerrorista`).
- **Implementación de interfaz (`<|..`)**: tanto `Arma` como `Agente` implementan el protocolo de dominio `InspeccionableEnPartida`.
- **Uso del repositorio y capas (`-->`)**: `ArmaController` usa `ArmaService`, que depende de las interfaces `ArmaRepository` y `AgenteRepository`.
- **Jerarquía de excepciones (`<|--`)**: `ReglaDeJuegoException` es la clase base del dominio y `MunicionInsuficienteException` y `FuegoAmigoException` son sus dos clases hijas atendidas por `ArmaController`.

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
    +Arma()
    +Arma(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, int municion)
    +Arma(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, int municion, int capacidadCargador)
    +comportamientoDeCombate() String*
    +disparar() String*
    +inspeccionarEnPartida() String
    +calcularValorEconomico() int
    +disparar(int distanciaMetros) String
    +disparar(int distanciaMetros, boolean tiroALaCabeza) String
    +dispararContraAgente(Agente objetivo, int distanciaMetros, boolean tiroALaCabeza, boolean permitirFuegoAmigo) int
    +cargarMunicionValidada(int balas) int
    +puedeDisparar() boolean
    #gastarMunicion() void
    +recargar() void
    +recargar(int balas) void
    +obtenerInfo() String
  }

  class Equipo {
    -String nombre
    +Equipo()
    +Equipo(String nombre)
    +getNombre() String
    +setNombre(String nombre) void
    +esMismoEquipo(Equipo otro) boolean
    +toString() String
  }

  class Pistola {
    <<abstract>>
    -int capacidadCargadorPistola
    -String modoDisparo
    +Pistola()
    +Pistola(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, int capacidadCargador, String modoDisparo, int municion)
    +Pistola(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, int capacidadCargador, String modoDisparo, int municion, int capacidadCargadorBase)
    +getCapacidadCargadorPistola() int
    +getModoDisparo() String
  }

  class Rifle {
    <<abstract>>
    -String modoDisparo
    -float retroceso
    +Rifle()
    +Rifle(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, String modoDisparo, float retroceso, int municion)
    +Rifle(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, String modoDisparo, float retroceso, int municion, int capacidadCargador)
    +dispararRafaga() void
    +apuntar() void
    +getModoDisparo() String
    +getRetroceso() float
  }

  class Escopeta {
    <<abstract>>
    -int perdigones
    -float dispersion
    +Escopeta()
    +Escopeta(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, int perdigones, float dispersion, int municion)
    +Escopeta(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, int perdigones, float dispersion, int municion, int capacidadCargador)
    +getPerdigones() int
    +getDispersion() float
  }

  class SubfusilSMG {
    <<abstract>>
    -float cadenciaFuego
    -float movilidad
    +SubfusilSMG()
    +SubfusilSMG(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, float cadenciaFuego, float movilidad, int municion)
    +SubfusilSMG(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, float cadenciaFuego, float movilidad, int municion, int capacidadCargador)
    +dispararEnMovimiento() void
    +getCadenciaFuego() float
    +getMovilidad() float
  }

  class Francotirador {
    <<abstract>>
    -float zoom
    -int penetracion
    +Francotirador()
    +Francotirador(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, float zoom, int penetracion, int municion)
    +Francotirador(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, float zoom, int penetracion, int municion, int capacidadCargador)
    +apuntarConMira() void
    +contenerRespiracion() void
    +getZoom() float
    +getPenetracion() int
  }

  class Granada {
    <<abstract>>
    -float radioExplosion
    -float tiempoExplosion
    +Granada()
    +Granada(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, float radioExplosion, float tiempoExplosion, int municion)
    +Granada(String nombre, int daño, float precision, float tiempoRecarga, int precio, Equipo equipo, float radioExplosion, float tiempoExplosion, int municion, int capacidadCargador)
    +lanzar() void
    +rebotar() void
    +calcularDañoOndaExpansiva(double distanciaMetros, boolean tieneChaleco) int
    +getRadioExplosion() float
    +getTiempoExplosion() float
  }

  class DesertEagle {
    +DesertEagle()
    +DesertEagle(Equipo equipo)
    +DesertEagle(int daño, int municion, Equipo equipo)
    +comportamientoDeCombate() String
    +disparar() String
  }

  class Glock18 {
    +Glock18()
    +Glock18(Equipo equipo)
    +Glock18(int daño, int municion, Equipo equipo)
    +comportamientoDeCombate() String
    +disparar() String
  }

  class M4A4 {
    +M4A4()
    +M4A4(Equipo equipo)
    +M4A4(int daño, int municion, Equipo equipo)
    +comportamientoDeCombate() String
    +disparar() String
  }

  class MP9 {
    +MP9()
    +MP9(Equipo equipo)
    +MP9(int daño, int municion, Equipo equipo)
    +comportamientoDeCombate() String
    +disparar() String
  }

  class Nova {
    +Nova()
    +Nova(Equipo equipo)
    +Nova(int daño, int municion, Equipo equipo)
    +comportamientoDeCombate() String
    +disparar() String
  }

  class GranadaHE {
    +GranadaHE()
    +GranadaHE(Equipo equipo)
    +GranadaHE(int daño, int municion, Equipo equipo)
    +comportamientoDeCombate() String
    +disparar() String
    +detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo) String
    +detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo, boolean permitirFuegoAmigo) String
  }

  class Agente {
    <<abstract>>
    -Long id
    -String nombre
    -int vida
    -int chalecoKevlar
    -int dinero
    -Equipo equipo
    +Agente()
    +Agente(String nombre, Equipo equipo)
    +Agente(String nombre, int vida, int chalecoKevlar, int dinero, Equipo equipo)
    +rolTactico() String*
    +ejecutarAccionEspecial() String*
    +inspeccionarEnPartida() String
    +calcularValorEconomico() int
    +tieneChaleco() boolean
    +estaVivo() boolean
    +recibirDañoExplosivo(int dañoFinal) void
  }

  class AgenteCT {
    -boolean tieneKitDesactivacion
    +AgenteCT()
    +AgenteCT(String nombre, boolean tieneKitDesactivacion)
    +AgenteCT(String nombre, int vida, int chalecoKevlar, int dinero, boolean tieneKitDesactivacion)
    +rolTactico() String
    +ejecutarAccionEspecial() String
    +isTieneKitDesactivacion() boolean
    +setTieneKitDesactivacion(boolean tieneKitDesactivacion) void
  }

  class AgenteTerrorista {
    -boolean portaBombaC4
    +AgenteTerrorista()
    +AgenteTerrorista(String nombre, boolean portaBombaC4)
    +AgenteTerrorista(String nombre, int vida, int chalecoKevlar, int dinero, boolean portaBombaC4)
    +rolTactico() String
    +ejecutarAccionEspecial() String
    +isPortaBombaC4() boolean
    +setPortaBombaC4(boolean portaBombaC4) void
  }

  class ReglaDeJuegoException {
    -String reglaViolada
    +ReglaDeJuegoException(String reglaViolada, String mensaje)
    +getReglaViolada() String
  }

  class MunicionInsuficienteException {
    -String nombreArma
    -int municionActual
    -int capacidadCargador
    +MunicionInsuficienteException(String nombreArma, int municionActual, int capacidadCargador, String mensaje)
    +getNombreArma() String
    +getMunicionActual() int
    +getCapacidadCargador() int
  }

  class FuegoAmigoException {
    -String nombreEquipo
    -String accionBloqueada
    +FuegoAmigoException(String nombreEquipo, String accionBloqueada, String mensaje)
    +getNombreEquipo() String
    +getAccionBloqueada() String
  }

  class ArmaRepository {
    <<interface>>
    +findByNombreIgnoreCase(String nombre) Optional~Arma~
    +existsByNombreIgnoreCase(String nombre) boolean
    +findByEquipo_NombreIgnoreCase(String nombreEquipo) List~Arma~
  }

  class AgenteRepository {
    <<interface>>
    +findByNombreIgnoreCase(String nombre) Optional~Agente~
    +findByEquipo_NombreIgnoreCase(String nombreEquipo) List~Agente~
  }

  class ArmaService {
    -ArmaRepository armaRepository
    -AgenteRepository agenteRepository
    +ArmaService(ArmaRepository armaRepository, AgenteRepository agenteRepository)
    +crearArma(Arma arma) Arma
    +listarTodas() List~Arma~
    +obtenerPorId(Long id) Arma
    +actualizarArma(Long id, Arma datosActualizados) Arma
    +eliminarArma(Long id) void
    +listarAgentes() List~Agente~
    +obtenerAgentePorId(Long id) Agente
    +validarReglasDeNegocio(Arma arma) void
    +simularCombateConArmaPersistida(Long armaId, String equipoObjetivoNombre, int distanciaMetros, boolean tiroALaCabeza, boolean tieneChalecoKevlar, boolean permitirFuegoAmigo) Map
    +recargarArmaPersistida(Long armaId, int balas) Map
    +simularExplosionGranada(String equipoAtacanteNombre, String equipoObjetivoNombre, double distanciaMetros, boolean tieneChalecoKevlar, boolean permitirFuegoAmigo, int dañoBase) Map
    +obtenerElementosProtocolo() List~InspeccionableEnPartida~
    +obtenerAgentesPolimorfismo() List~Agente~
    +construirYGuardarArma(String nombre, Integer daño, Integer municion, String equipoNombre) Arma
    +obtenerCatalogoPolimorfico() List~Arma~
    +fabricarArma(String nombre, Integer daño, Integer municion, Equipo equipo) Arma
  }

  class ArmaController {
    -ArmaService armaService
    +ArmaController(ArmaService armaService)
    +crearArma(Arma arma) ResponseEntity~Arma~
    +listarTodas() ResponseEntity~List~
    +obtenerPorId(Long id) ResponseEntity~Arma~
    +actualizarArma(Long id, Arma armaActualizada) ResponseEntity~Arma~
    +eliminarArma(Long id) ResponseEntity~Map~
    +consultarProtocoloInspeccionable() ResponseEntity~List~
    +simularCombatePost(Long id, String equipoObjetivo, int distancia, boolean headshot, boolean chaleco, boolean fuegoAmigo) ResponseEntity~Map~
    +simularCombateGet(Long id, String equipoObjetivo, int distancia, boolean headshot, boolean chaleco, boolean fuegoAmigo) ResponseEntity~Map~
    +recargarArmaPersistida(Long id, int balas) ResponseEntity~Map~
    +listarAgentesPersistidos() ResponseEntity~List~
    +obtenerAgentePorId(Long id) ResponseEntity~Agente~
    +construirArma(String nombre, Integer municion, Integer daño, String equipoNombre) Map
    +demostrarPolimorfismo() List~Map~
    +dispararArma(String nombre, Integer distancia, boolean headshot) Map
    +simularExplosionGranada(String equipoAtacante, String equipoObjetivo, double distancia, boolean chaleco, boolean fuegoAmigo, int dañoBase) ResponseEntity~Map~
    +demostrarPolimorfismoAgentes() List~Map~
    +manejarMunicionInsuficiente(MunicionInsuficienteException ex) ResponseEntity~Map~
    +manejarFuegoAmigo(FuegoAmigoException ex) ResponseEntity~Map~
    +manejarReglaDeJuegoBase(ReglaDeJuegoException ex) ResponseEntity~Map~
    +manejarReglaViolada(IllegalArgumentException ex) ResponseEntity~Map~
    +manejarNoEncontrado(NoSuchElementException ex) ResponseEntity~Map~
  }

  InspeccionableEnPartida <|.. Arma
  InspeccionableEnPartida <|.. Agente

  Arma <|-- Pistola
  Arma <|-- Rifle
  Arma <|-- Escopeta
  Arma <|-- SubfusilSMG
  Arma <|-- Francotirador
  Arma <|-- Granada

  Pistola <|-- DesertEagle
  Pistola <|-- Glock18
  Rifle <|-- M4A4
  SubfusilSMG <|-- MP9
  Escopeta <|-- Nova
  Granada <|-- GranadaHE

  Agente <|-- AgenteCT
  Agente <|-- AgenteTerrorista

  Arma --> Equipo : equipo
  Agente --> Equipo : equipo

  ReglaDeJuegoException <|-- MunicionInsuficienteException
  ReglaDeJuegoException <|-- FuegoAmigoException

  ArmaController --> ArmaService : usa
  ArmaService --> ArmaRepository : usa
  ArmaService --> AgenteRepository : usa
  ArmaRepository ..> Arma : gestiona
  AgenteRepository ..> Agente : gestiona
  ArmaController ..> MunicionInsuficienteException : maneja (409)
  ArmaController ..> FuegoAmigoException : maneja (422)
```
