# Especificaciones del TP - Counter-Strike 2

**Alumna:** Diana Aquino  
**Dominio:** Counter-Strike 2  
**Materia:** Lenguaje de Programación 3 (LP3) - POO-06  

---

## 1. Objetivo del Trabajo
El objetivo de este trabajo práctico es publicar una API REST usando Spring Boot y Java 21. La idea era modelar un dominio a elección (yo elegí las armas del Counter-Strike 2) y aplicar los conceptos de POO que estuvimos viendo en clase: herencia, polimorfismo, constructores sobrecargados y métodos abstractos.

## 2. Consignas aplicadas en mi código
- **Paquetes:** Usé el template de la catedra tal cual lo pidieron. Todo el codigo del dominio está adentro de `py.edu.uc.lp3.domain` y los controllers los puse en `py.edu.uc.lp3.rest.controller`.
- **Sobrecarga:** En las clases hice por lo menos 3 constructores distintos. Además, el metodo `disparar()` está sobrecargado para recibir distintos parámetros según el caso.
- **Sobreescritura:** Hice que la clase padre `Arma` sea abstracta y le puse métodos abstractos como `comportamientoDeCombate()`. Después creé clases hijas (como la `M4A4`, `Glock18` o `Nova`) y les puse `@Override` a esos metodos para darles su comportamiento propio a cada arma.
- **Servicios REST:** Armé un `IndexController` para la ruta principal `/` y un `ArmaController` para `/api/armas` que se encarga de crear el objeto leyendo los parámetros que le pasamos por la URL.

## 3. Cómo probar el código

Primero hay que clonar el repositorio. No hace falta instalar Maven a mano porque dejé configurado el wrapper, solo necesitan tener instalado Java 21.

Para levantar la aplicación, se corre esto en la terminal desde la carpeta del proyecto:
```bash
./mvnw spring-boot:run
```

Una vez que levantó, se puede probar estas URLs en el navegador o en Postman:
- `http://localhost:8080/` (Para ver que funcione y tire mis datos)
- `http://localhost:8080/api/armas?nombre=M4A4&municion=25&equipo=CT` (Para crear el arma pasandole parámetros)
- `http://localhost:8080/api/armas/polimorfismo` (Muestra cómo actúan distintas armas con el mismo método)
- `http://localhost:8080/api/armas/disparar?nombre=M4A4&distancia=30&headshot=true`

Si se quiere probar los tests automatizados, pueden correr:
```bash
./mvnw test
```

---

## 4. Enlace de la entrega
**Commit final de la solución:** https://github.com/dianamariapaz2104/daquino-tp-lp3-2026/tree/main
