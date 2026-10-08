# Bitácora de uso de IA

**Asistente usado:** Antigravity (Google DeepMind)  
**Modelo exacto:** Gemini 3.8 Flash (High)  
**Fecha:** 7 de Octubre de 2026  

---

## Resumen de cómo usé la IA (Prompts)

Para hacer este TP me apoyé en el asistente de IA para acelerar el código rutinario y armar bien la estructura inicial. Estos son los pasos principales que le fui pidiendo:

1. Primero le pedí ayuda para instalar Java 21 y configurar bien las variables de entorno en mi Mac para que me funcione el comando `./mvnw`.
2. Le pasé la estructura de paquetes que pedía el template de la cátedra (`py.edu.uc.lp3`) y le pedí que me actualice el `pom.xml` a la versión de Spring Boot 3.3.4.
3. Para la parte del dominio, le expliqué que quería modelar las armas del CS2. Le pedí que arme una clase abstracta `Arma` y que le agregue métodos abstractos y constructores sobrecargados como pedía la consigna. 
4. Después le pedí que me genere varias clases hijas (M4A4, MP9, Escopeta, etc) usando `@Override` para implementar los métodos abstractos del padre.
5. Una vez que estuvo el dominio, le pedí que me ayude a armar los Controllers REST: uno para la raíz `/` y otro para armar el objeto leyendo variables de la URL usando `@RequestParam`.
6. Por último, le pedí que me haga los tests unitarios y de integración para comprobar que el polimorfismo y la validación de los datos estuvieran funcionando bien.
