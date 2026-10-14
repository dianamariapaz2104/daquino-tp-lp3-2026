# Counter-Strike 2 - API REST

Servicio HTTP desarrollado con Spring Boot y Java 21 para modelar las armas de
Counter-Strike 2. El dominio aplica herencia, sobreescritura, encapsulamiento y
polimorfismo: todas las armas se tratan a través de la superclase `Arma`.

## Tecnologías

- Java 21
- Spring Boot
- Maven
- Spring Web

## Ejecución

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8080`.

## Endpoints

### `GET /`

Confirma que el servicio está vivo y muestra la autora y el estado de la API:

```json
{
  "autora": "Diana",
  "dominio": "Counter",
  "estado": "API funcionando"
}
```

### `GET /api/armas?nombre=M4A4`

Construye un arma a partir del parámetro `nombre` y responde JSON con su estado
y comportamiento de combate. El nombre debe coincidir con una clase concreta del
dominio. Si el arma no existe, responde `404`.

Ejemplo:

```http
GET /api/armas?nombre=M4A4
```

```json
{
  "arma": "M4A4",
  "municion": 30,
  "comportamientoDeCombate": "Fusil de asalto automático, ráfagas precisas a media y larga distancia."
}
```

Otros ejemplos:

```json
{
  "arma": "Glock-18",
  "municion": 20,
  "comportamientoDeCombate": "Pistola semiautomática de 9 mm con cargador amplio y fuego rápido controlable."
}
```

### `GET /armas/crear?nombre=Glock-18&precio=200`

Crea un arma indicando su `precio` de forma opcional. El `nombre` tolera
guiones y mayúsculas/minúsculas (por ejemplo `Glock-18`, `desert-eagle`,
`M4A4`). Si `precio` se omite, se usa el precio por defecto del arma.

```http
GET /armas/crear?nombre=Glock-18&precio=200
```

```json
{
  "arma": "Glock-18",
  "precio": 200,
  "municion": 20,
  "comportamientoDeCombate": "Pistola semiautomática de 9 mm con cargador amplio y fuego rápido controlable."
}
```

```json
{
  "arma": "Nova",
  "municion": 8,
  "comportamientoDeCombate": "Escopeta de perdigones dispersos, letal en combate cuerpo a cuerpo."
}
```

### `GET /api/modo-disparo?nombre=M4A4`

Pide el mensaje de la capacidad **"modo de disparo"** a través del tipo de la
interfaz `ConModoDeDisparo` y responde JSON. `DesertEagle` (familia `Pistola`)
usa la implementación por defecto, mientras que `M4A4` (familia `Rifle`)
sobreescribe el default. Si el arma no declara el modo de disparo, responde
`404`.

```http
GET /api/modo-disparo?nombre=M4A4
```

```json
{
  "arma": "M4A4",
  "descripcionDeDisparo": "Modo de disparo: Automático con retroceso 0.55."
}
```

```json
{
  "arma": "Desert Eagle",
  "descripcionDeDisparo": "Modo de disparo: Semiautomático"
}
```

## Diseño de clases

Diagrama Mermaid de las clases reales del dominio y sus relaciones de herencia.

```mermaid
classDiagram
  class Arma {
    <<abstract>>
    -String nombre
    -int daño
    -float precision
    -float tiempoRecarga
    -int precio
    -Equipo equipo
    -int municion
    +obtenerInfo() String
    +puedeDisparar() boolean
    +getMunicion() int
    +comportamientoDeCombate() String*
  }

  class Equipo {
    -String nombre
    +getNombre() String
    +setNombre(String nombre)
  }

  class Pistola {
    <<abstract>>
    -int capacidadCargador
    -String modoDisparo
    +disparar()
    +recargar()
  }

  class Rifle {
    <<abstract>>
    -String modoDisparo
    -float retroceso
    +dispararRafaga()
    +apuntar()
  }

  class Escopeta {
    <<abstract>>
    -int perdigones
    -float dispersion
    +disparoSecundario()
    +recargar()
  }

  class SubfusilSMG {
    <<abstract>>
    -float cadenciaFuego
    -float movilidad
    +dispararEnMovimiento()
    +recargar()
  }

  class Francotirador {
    <<abstract>>
    -float zoom
    -int penetracion
    +apuntarConMira()
    +contenerRespiracion()
  }

  class Granada {
    <<abstract>>
    -float radioExplosion
    -float tiempoExplosion
    +lanzar()
    +rebotar()
  }

  class DesertEagle {
    +comportamientoDeCombate() String
  }

  class Glock18 {
    +comportamientoDeCombate() String
  }

  class M4A4 {
    +comportamientoDeCombate() String
  }

  class Nova {
    +comportamientoDeCombate() String
  }

  class MP9 {
    +comportamientoDeCombate() String
  }

  class GranadaHE {
    +comportamientoDeCombate() String
  }

  class ConModoDeDisparo {
    <<interface>>
    +getModoDisparo() String
    +descripcionDeDisparo() String
  }

  Arma <|-- Pistola
  Arma <|-- Rifle
  Arma <|-- Escopeta
  Arma <|-- SubfusilSMG
  Arma <|-- Francotirador
  Arma <|-- Granada

  Pistola <|-- DesertEagle
  Pistola <|-- Glock18
  Rifle <|-- M4A4
  Escopeta <|-- Nova
  SubfusilSMG <|-- MP9
  Granada <|-- GranadaHE

  ConModoDeDisparo <|.. DesertEagle
  ConModoDeDisparo <|.. M4A4

  Arma --> Equipo : equipo
```

## Polimorfismo

El controller construye un arma a través de una fábrica y la guarda en una
referencia del tipo padre `Arma`. Luego pide el mensaje abstracto
`comportamientoDeCombate()` sobre esa referencia: cada arma concreta responde
con su propia implementación mediante sobreescritura (`@Override`), sin que el
controller conozca el tipo concreto ni use `instanceof`.

## Capacidad compartida entre familias

`DesertEagle` (familia `Pistola`) y `M4A4` (familia `Rifle`) comparten la
capacidad de tener un modo de disparo, así que se declaró la interfaz
`ConModoDeDisparo` en el dominio con el mensaje `descripcionDeDisparo()`. La
interfaz provee un default y `M4A4` lo reemplaza con su propia versión, mientras
`DesertEagle` conserva el default. El endpoint `/api/modo-disparo` pide el
mensaje a través del tipo de la interfaz (no del tipo concreto).