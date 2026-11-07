# Bitácora de uso de IA

**Asistente usado:** Antigravity (Google DeepMind)  
**Modelos exactos:** Gemini 3.8 Flash (High) (Entrega POO-06, 07/10/2026) y Gemini 4 Argon (Medium) (POO-08: Persistencia JPA/H2, CRUD REST, Interfaz de Dominio, Simulación con 2 Reglas, Jerarquía de Excepciones y Colección Insomnia, 28/10/2026 - 06/11/2026)  
**Autora:** Diana Aquino  

---

## Etapa 1: Modelado de Dominio y API REST Inicial (POO-06)

Para hacer este TP me apoyé en el asistente de IA para acelerar el código rutinario y armar bien la estructura inicial. Estos son los pasos principales que le fui pidiendo:

1. Primero le pedí ayuda para instalar Java 21 y configurar bien las variables de entorno en mi Mac para que me funcione el comando `./mvnw`.
2. Le pasé la estructura de paquetes que pedía el template de la cátedra (`py.edu.uc.lp3`) y le pedí que me actualice el `pom.xml` a la versión de Spring Boot 3.3.4.
3. Para la parte del dominio, le expliqué que quería modelar las armas del CS2. Le pedí que arme una clase abstracta `Arma` y que le agregue métodos abstractos y constructores sobrecargados como pedía la consigna.
4. Después le pedí que me genere varias clases hijas (`M4A4`, `MP9`, `DesertEagle`, `Glock18`, `Nova`, `GranadaHE`) usando `@Override` para implementar los métodos abstractos del padre.
5. Una vez que estuvo el dominio, le pedí que me ayude a armar los Controllers REST: uno para la raíz `/` y otro para armar el objeto leyendo variables de la URL usando `@RequestParam`.
6. Por último, le pedí que me haga los tests unitarios y de integración para comprobar que el polimorfismo y la validación de los datos estuvieran funcionando bien.

---

## Etapa 2: Persistencia H2, CRUD REST, Interfaz, Reglas de Juego, Excepciones e Insomnia (POO-08)

1. Le consulté por qué no tenía disponibles las anotaciones de JPA y le pedí agregar las dependencias `spring-boot-starter-data-jpa` y `com.h2database:h2` en el `pom.xml`, configurando `application.properties` para persistir en `./data/armas` y habilitar la consola `/h2-console`.
2. Le pedí implementar un CRUD REST completo para la jerarquía `Arma` respetando la arquitectura en capas (`ArmaController` -> `ArmaService` -> `ArmaRepository`) con discriminación polimórfica JSON (`@JsonTypeInfo` / `@JsonSubTypes`) y validaciones de negocio (tope de presupuesto de CS2 de `$16,000`, daño máximo permitido e invariantes del dominio).
3. Le pedí agregar la segunda jerarquía persistida en H2 (`Agente` -> `AgenteCT` y `AgenteTerrorista`) con su propio repositorio JPA (`AgenteRepository`) y el objeto de valor `@Embeddable` `Equipo`.
4. Le pedí definir la interfaz de dominio `InspeccionableEnPartida` (protocolo POO-07) e implementarla en dos familias distintas (`Arma` y `Agente`), exponiendo el endpoint `GET /api/armas/protocolo` que opera sobre `List<InspeccionableEnPartida>`.
5. Le pedí programar las dos reglas de negocio de Counter-Strike 2 dentro de los métodos del dominio (`Arma.dispararContraAgente`, `Arma.cargarMunicionValidada`, `GranadaHE.detonarEnArea` y `Agente.recibirDañoExplosivo`) y exponer la simulación persistida (`POST /api/armas/{id}/simular` y `GET /api/armas/granadas/simular`) donde participan las tres clases (`Arma`, `Agente` y `Equipo`) y un `GET` posterior refleja el cambio de estado en H2.
6. Le pedí crear la jerarquía de excepciones del dominio (`ReglaDeJuegoException` como base y dos hijas: `MunicionInsuficienteException` y `FuegoAmigoException`) con un `@ExceptionHandler` HTTP por tipo (`409 Conflict` y `422 Unprocessable Entity`), generar la colección de Insomnia en `insomnia/insomnia-cs2-collection.json` y actualizar el `README.md` con las propiedades de H2, las respuestas a «Qué hay que pensar» y el diagrama Mermaid completo.
