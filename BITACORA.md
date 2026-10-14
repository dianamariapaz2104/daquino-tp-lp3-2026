# Bitácora de desarrollo — Counter-Strike 2 API REST (LP3)

**Proyecto:** daquino-tp-lp3-2026
**Materia:** Lenguaje de Programación 3 (LP3)
**Autora:** Diana
**Tecnologías:** Java 21, Spring Boot, Maven, Spring Web
**Fecha de inicio:** 16/09/2026
**Última actualización:** 14/10/2026

---

## Resumen del proceso

Se desarrolló una API REST en Spring Boot para modelar las armas de
Counter-Strike 2 aplicando herencia, encapsulamiento, sobreescritura y
polimorfismo. Todas las armas se tratan a través de la superclase abstracta
`Arma`. En la última sesión se adaptó la ruta de creación de armas para que
acepte el nombre y un precio personalizado mediante parámetros de consulta.

---

## Cronología

| Fecha | Hito | Commit |
| ----- | ---- | ------ |
| 16/09/2026 | Creación del proyecto y primera clase (`Arma`) | `bf1c303`, `512c165` |
| 20/09/2026 | Modelado del dominio completo: entidades, herencia y jerarquía de armas | `3d2246b`, `c81fcc3`, `d5c363a` |
| 24/09/2026 | API REST funcional (`/api/armas`) y página principal | `18feec6`, `de0927a`, `53e1b3a` |
| 24/09/2026 | Adaptación de la ruta `/armas/crear?nombre=...&precio=...` | Sin commit (trabajo en curso) |
| 14/10/2026 | Interfaz de dominio `ConModoDeDisparo` y endpoint `/api/modo-disparo` | Sin commit (trabajo en curso) |

---

## Etapa 1 — Creación del proyecto (16/09)

- Se inicializó el proyecto Spring Boot con Maven y Java 21.
- Se creó la clase base del dominio: `Arma` (abstracta), con los atributos
  `nombre`, `daño`, `precision`, `tiempoRecarga`, `precio`, `equipo` y
  `municion`.
- Validaciones de dominio: el daño y la munición no pueden ser negativos.

## Etapa 2 — Modelado del dominio (20/09)

Se diseñó la jerarquía de clases del dominio:

- Superclase abstracta `Arma`.
- Subclases abstractas por familia de arma:
  `Pistola`, `Rifle`, `Escopeta`, `SubfusilSMG`, `Francotirador`, `Granada`.
- Clases concretas: `DesertEagle`, `Glock18`, `M4A4`, `Nova`, `MP9`,
  `GranadaHE`.
- Clase auxiliar `Equipo` (representa el bando: "Counter-Terrorista" o
  "Terrorista").
- Cada arma concreta implementa el método abstracto
  `comportamientoDeCombate()` mediante sobreescritura, lo que habilita el
  polimorfismo: la referencia de tipo `Arma` resuelve en tiempo de ejecución
  la implementación correcta.

### Jerarquía real

```
Arma (abstracta)
├── Pistola (abstracta)
│   ├── DesertEagle
│   └── Glock18
├── Rifle (abstracta)
│   └── M4A4
├── Escopeta (abstracta)
│   └── Nova
├── SubfusilSMG (abstracta)
│   └── MP9
├── Francotirador (abstracta)
├── Granada (abstracta)
│   └── GranadaHE
```

## Etapa 3 — API REST (24/09)

- `IndexController`: endpoint `GET /` que responde el estado de la API y la
  autora.
- `ArmaController`: endpoint `GET /api/armas?nombre=M4A4` que construye un
  arma a partir de su nombre usando una **fábrica** (`Map<String, Function>`)
  y responde JSON con munición y comportamiento de combate. Si el arma no
  existe devuelve `404`.
- Se documentaron los endpoints en el `README.md` con diagrama Mermaid de
  clases y ejemplos de respuesta.

---

## Etapa 4 — Adaptación de la ruta `/armas/crear` (24/09)

### Cambios realizados

1. **`Arma.java`**: se agregó el setter `setPrecio(int precio)` con validación
   de precio no negativo, para poder personalizar el precio de una arma ya
   construida.

2. **`ArmaController.java`**:
   - Nuevo endpoint `GET /armas/crear` con parámetros `nombre` (obligatorio)
     y `precio` (opcional).
   - Si se envía `precio`, se sobreescribe el precio por defecto del arma.
   - Se extrajo la lógica de fabricación al método privado `fabricarArma`,
     reutilizado por ambos endpoints (`/api/armas` y `/armas/crear`).
   - El nombre se normaliza: se quitan espacios y guiones y se pasa a
     minúsculas, de modo que acepta variantes como `Glock-18`,
     `desert-eagle` o `m4a4`.
   - Respuestas de error: `404` si el arma no existe, `400` si el precio es
     negativo.

3. **`README.md`**: se documentó el nuevo endpoint con ejemplo de petición y
   respuesta JSON.

### Problemas encontrados y soluciones

| Problema | Síntoma | Causa | Solución |
| -------- | ------- | ----- | -------- |
| Puerto ocupado | `/armas/crear` daba `404` aunque la app respondía en `/` | Había una instancia vieja de la app (compilada antes del cambio) corriendo en el puerto 8080 desde las 16:40 | Detener todos los procesos `spring-boot:run` y levantar la app nuevamente con el código actualizado |
| Conflicto de sobrecarga en el controller | Incluso `/api/armas` (previamente funcional) dejó de responder (`404`) | El método privado auxiliar `crearArma(String)` tenía el mismo nombre que el método anotado `@GetMapping crearArma(String, Integer)` | Renombrar el método privado a `fabricarArma(String)` para evitar la sobrecarga ambigua |
| Lastrado de claves de la fábrica | La normalización pasaba el nombre a minúsculas pero la fábrica tenía claves con mayúsculas (`Glock18`, `M4A4`, etc.), por lo que nunca matcheaba | Desincronización entre el formato de la clave y el formato normalizado | Estandarizar las claves de la fábrica en el mismo formato normalizado (minúsculas, sin guiones) |

### Verificación (pruebas reales ejecutadas)

Petición exitosa con precio personalizado:

```
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

| Caso de prueba | Resultado esperado | Resultado obtenido |
| -------------- | ------------------ | ------------------ |
| `nombre=Glock-18&precio=200` | `200` con precio 200 | OK |
| `nombre=m4a4` (sin precio, minúsculas) | `200` con precio por defecto (3100) | OK |
| `nombre=AK-47&precio=2700` (arma inexistente) | `404` | OK |
| `nombre=Glock-18&precio=-5` (precio negativo) | `400` | OK |
| `/api/armas?nombre=M4A4` (regresión del endpoint anterior) | `200` | OK |
| `./mvnw test` | Pruebas de contexto pasan | OK |

---

## Etapa 5 — Capacidad compartida como interfaz (14/10)

### Cambios realizados

1. **`ConModoDeDisparo.java`** (nueva interfaz de dominio): declara
   `getModoDisparo()` y el mensaje `descripcionDeDisparo()` con una
   implementación por defecto.

2. **`DesertEagle`** (familia `Pistola`) y **`M4A4`** (familia `Rifle`):
   implementan la interfaz. `M4A4` sobreescribe el default con su propia
   descripción; `DesertEagle` conserva el default.

3. **`ArmaController`**: nuevo endpoint `GET /api/modo-disparo?nombre=...` que
   obtiene el arma y pide el mensaje a través del tipo de la interfaz
   `ConModoDeDisparo` (pattern matching con `instanceof`). Si el arma no
   declara la capacidad, responde `404`.

### Verificación (pruebas reales ejecutadas)

| Caso de prueba | Resultado obtenido |
| -------------- | ------------------ |
| `nombre=M4A4` | `{"arma":"M4A4","descripcionDeDisparo":"Modo de disparo: Automático con retroceso 0.55."}` |
| `nombre=desert-eagle` | `{"arma":"Desert Eagle","descripcionDeDisparo":"Modo de disparo: Semiautomático"}` |
| `nombre=Nova` (sin la capacidad) | `404` |
| `nombre=AK-47` (inexistente) | `404` |

---

## Comandos útiles

```bash
./mvnw spring-boot:run   # levantar la API en http://localhost:8080
./mvnw compile           # compilar sin ejecutar
./mvnw test              # ejecutar los tests
```

## Aprendizajes de la sesión

- Verificar siempre que no queden instancias previas de la app sobre el puerto
  antes de probar cambios (`ss -tlnp | grep 8080`).
- Evitar sobrecargar métodos con el mismo nombre que un handler anotado de
  Spring: es preferible usar nombres distintos para los métodos auxiliares.
- Aislar la lógica común (fábrica de armas) en un método reutilizable para
  mantener los endpoints simples y consistentes.
- La normalización de entrada (guiones, mayúsculas) debe estar sincronizada
  con el formato de las claves internas.
