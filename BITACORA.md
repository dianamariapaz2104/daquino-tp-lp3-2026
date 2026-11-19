# Bitácora de uso de IA

**Asistente usado:** Antigravity (Google DeepMind)  
**Modelos exactos:** Gemini 3.8 Flash (High) y Gemini 4 Argon (Medium)  
**Autora:** Diana Aquino  

---

## Etapa 1: Modelado de Dominio y API REST Inicial (POO-06)

1. Configuré Java 21, el wrapper `./mvnw` y la estructura de paquetes `py.edu.uc.lp3` con Spring Boot 3.3.4.
2. Modelé la jerarquía `Arma` con constructores sobrecargados, sobrecarga de `disparar()` y sobreescritura (`@Override`) en las subclases concretas (`M4A4`, `MP9`, `DesertEagle`, `Glock18`, `Nova`, `GranadaHE`).
3. Armé `IndexController` y `ArmaController` con sus pruebas unitarias y de integración.

---

## Etapa 2: Persistencia H2, Arquitectura de 3 Capas, Interfaz y Excepciones (POO-08)

1. Agregué `spring-boot-starter-data-jpa` y `com.h2database:h2` en `pom.xml`, configurando H2 en archivo local (`./data/armas`) y habilitando `/h2-console`.
2. Implementé la segunda jerarquía `Agente` (`AgenteCT` y `AgenteTerrorista`), la interfaz `InspeccionableEnPartida` implementada por `Arma` y `Agente`, los repositorios `ArmaRepository` y `AgenteRepository`, y la jerarquía de excepciones `ReglaDeJuegoException` -> `MunicionInsuficienteException` (409) y `FuegoAmigoException` (422).

---

## Etapa 3: Corrección Arquitectónica Posterior a Devolución POO-08

A partir de la corrección detallada del profesor en POO-08, realicé los siguientes ajustes:
1. **Criterio 2 (Separación Dominio vs Service):** Quité todos los `throw new ...` de las entidades del dominio (`Arma`, `Agente`, `Equipo`, `Granada`, `GranadaHE`) y trasladé todas las validaciones, reglas de combate y lanzamiento de excepciones exclusivamente a `ArmaService`.
2. **Criterio 3 (Ocultamiento e Invariantes):** Eliminé los setters públicos sin control de las entidades (incluyendo `setRadioExplosion`, `setTiempoExplosion`, `setVida`, `setMunicion`) usando `@JsonAutoDetect` por campos y validando en `ArmaService.validarReglasDeNegocio` que una `Granada` tenga siempre `radioExplosion > 0` y `tiempoExplosion > 0` (rechazando con `400 Bad Request` valores como `radioExplosion: -3`).
3. **Criterio 6 (Simulación sobre Entidades Guardadas):** Agregué el CRUD de `Agente` (`/api/agentes`) e hice que `simularCombateConEntidadesPersistidas` cargue tanto el `Arma` como el `Agente` objetivo desde H2 por sus IDs y determine `FuegoAmigoException` comparando los equipos guardados en H2.
4. **Criterio 8 (Documentación):** Sincronicé el `README.md` (explicando cómo Jackson usa el constructor vacío con valores por defecto en `PUT`) y `ESPECIFICACIONES.md` con el comportamiento exacto de `ArmaService`.
