# Especificaciones del TP - Counter-Strike 2

**Alumna:** Diana Aquino  
**Dominio:** Counter-Strike 2  
**Materia:** Lenguaje de Programación 3 (LP3) - POO-06 / Persistencia H2  

---

## 1. Objetivo del Trabajo
El objetivo de este trabajo práctico es publicar una API REST usando Spring Boot 3.3.4, Java 21, Spring Data JPA y base de datos H2. Modelé el dominio de Counter-Strike 2 (armas, explosivos, agentes y equipos) aplicando herencia, polimorfismo, constructores sobrecargados, métodos abstractos, invariantes protegidos y arquitectura de 3 capas (`Controller` -> `Service` -> `Repository`).

## 2. Consignas aplicadas en mi código
- **Paquetes y Arquitectura de 3 Capas:** El dominio está en `py.edu.uc.lp3.domain`, los repositorios JPA en `py.edu.uc.lp3.repository`, la capa de negocio en `py.edu.uc.lp3.service` y los controladores REST en `py.edu.uc.lp3.rest.controller`.
- **Sobrecarga:** En `Arma` y en las clases hijas definí 3 constructores sobrecargados (`Arma()`, `Arma(...)` de 7 parámetros y `Arma(...)` completo de 8 parámetros). Además, los métodos `disparar()` y `recargar()` están sobrecargados para recibir distintos parámetros según el caso.
- **Sobreescritura y Dos Jerarquías:**
  - Jerarquía 1 (`Arma`): Clase abstracta con `comportamientoDeCombate()` y `disparar()` sobreescritos con `@Override` en `M4A4`, `MP9`, `DesertEagle`, `Glock18`, `Nova` y `GranadaHE`.
  - Jerarquía 2 (`Agente`): Clase abstracta con `rolTactico()` y `ejecutarAccionEspecial()` sobreescritos con `@Override` en `AgenteCT` y `AgenteTerrorista`.
- **CRUD H2 y Regla de Negocio con 3 Clases:** `ArmaService` valida el presupuesto máximo de CS2 (`$16,000`), el límite de daño y ejecuta la simulación de explosivos (`simularExplosionGranada`) donde colaboran `Agente`, `GranadaHE`/`Granada` y `Equipo` persistiendo en H2 (`./data/armas`).

## 3. Cómo probar el código

Para levantar la aplicación desde la carpeta del proyecto:
```bash
./mvnw spring-boot:run
```

Una vez que levantó, se pueden probar estas URLs en el navegador o en Insomnia/Postman:
- `http://localhost:8080/` (Estado de la API y listado de endpoints)
- `http://localhost:8080/api/armas?nombre=M4A4&municion=25&equipo=Counter-Terrorista` (Construye y persiste el arma desde parámetros URL)
- `http://localhost:8080/api/armas/todas` (Lista todas las armas guardadas en H2)
- `http://localhost:8080/api/armas/polimorfismo` (Muestra el polimorfismo de la jerarquía `Arma`)
- `http://localhost:8080/api/agentes/polimorfismo` (Muestra el polimorfismo de la jerarquía `Agente`)
- `http://localhost:8080/api/armas/disparar?nombre=M4A4&distancia=30&headshot=true` (Muestra las sobrecargas de `disparar`)
- `http://localhost:8080/api/armas/granadas/simular?equipoAtacante=Counter-Terrorista&equipoObjetivo=Terrorista&distancia=2.0&chaleco=true` (Simulación de explosión de granada con 3 clases del modelo)
- `http://localhost:8080/h2-console` (Consola H2 con JDBC URL `jdbc:h2:file:./data/armas` y usuario `sa`)

Para correr los 16 tests automatizados:
```bash
./mvnw test
```

---

## 4. Enlaces exactos de entrega (Hash del Commit)
- **Commit exacto de la versión con CRUD H2, 3 Capas y Simulación de Granadas:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/058c8521c384223c4d7a5f41f01d557dccc11899
- **Commit exacto de la entrega POO-06:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/commit/f8757155883626bc2b6ccb6883b07afd4650fa70
