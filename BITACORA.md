# Bitácora de uso de IA

**Asistente usado:** Antigravity (Google DeepMind)  
**Modelos exactos:** Gemini 3.8 Flash (High) (Entrega POO-06, 07/10/2026) y Gemini 4 Argon (Medium) (Persistencia JPA/H2, CRUD 3 Capas y Correcciones, 28-30/10/2026)  
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

## Etapa 2: Persistencia H2, Arquitectura de 3 Capas, Segunda Jerarquía y Correcciones

1. Le consulté por qué no tenía disponibles las anotaciones de JPA y le pedí agregar las dependencias `spring-boot-starter-data-jpa` y `com.h2database:h2` en el `pom.xml`, configurando `application.properties` para persistir en `./data/armas` y habilitar la consola `/h2-console`.
2. Le pedí implementar un CRUD REST completo para la entidad `Arma` respetando la arquitectura de 3 capas (`ArmaController` -> `ArmaService` -> `ArmaRepository`) con validaciones de negocio en `ArmaService` (tope de presupuesto de CS2 de `$16,000`, daño máximo permitido e invariantes del dominio).
3. Le pedí agregar una segunda jerarquía de clases coherente con Counter-Strike 2 (`Agente` -> `AgenteCT` y `AgenteTerrorista`) con su propio repositorio JPA (`AgenteRepository`).
4. Le pedí conectar una regla de negocio de simulación de granadas/explosivos (`GET /api/armas/granadas/simular`) en la que participan 3 clases del modelo (`Agente`, `GranadaHE`/`Granada` y `Equipo`), validando fuego amigo, alcance de lanzamiento, atenuación por radio de explosión (`5.0m`), absorción de chaleco Kevlar y descuento de vida en H2.
5. A partir de la devolución del profesor en POO-06, le pedí corregir el diagrama de clases Mermaid en `README.md` para que coincida exactamente lado a lado con los constructores, parámetros, atributos y métodos de `src/`, actualizar los enlaces al hash del commit exacto e integrar el historial de Git sin usar `force-push`.
