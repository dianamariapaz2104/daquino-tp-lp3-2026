# Bitácora de uso de IA

**Asistente usado:** Antigravity (Google DeepMind)  
**Modelos exactos:** Gemini 3.8 Flash (High) (Entrega POO-06) y Gemini 4 Argon (Medium) (Entrega POO-08)  
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

## Etapa 2: Persistencia H2, CRUD, Interfaz, Reglas de Juego, Excepciones e Insomnia (POO-08)

1. Le consulté por qué no tenía disponibles las anotaciones de JPA y le pedí agregar las dependencias de Spring Data JPA y H2 en el `pom.xml`, configurando `application.properties` para guardar en `./data/armas` y activar `/h2-console`.
2. Le pedí armar un CRUD REST completo para `Arma` con su repositorio `ArmaRepository`, y agregar la segunda jerarquía `Agente` (`AgenteCT` y `AgenteTerrorista`) con `AgenteRepository` y la clase `Equipo`.
3. Le pedí crear la interfaz `InspeccionableEnPartida` (como en POO-07) para que la implementen `Arma` y `Agente`, y exponerla en el endpoint `GET /api/armas/protocolo`.
4. Le pedí programar las dos reglas del juego en los métodos de `Arma`, `GranadaHE` y `Agente` (control de munición, cargador, fuego amigo y descuento de chaleco Kevlar y vida) y conectarlas en la simulación persistida (`POST /api/armas/{id}/simular`).
5. Le pedí armar la jerarquía de excepciones (`ReglaDeJuegoException` como padre y `MunicionInsuficienteException` y `FuegoAmigoException` como hijas) con su `@ExceptionHandler` por cada tipo en el controller.
6. Por último le pedí armar el archivo de la colección de Insomnia en `insomnia/` y actualizar el `README.md` y las especificaciones con mis respuestas y el diagrama Mermaid completo.
