package py.edu.uc.lp3.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cs2OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Counter-Strike 2 - API REST (Diana Aquino - A1)")
                        .version("1.0.0-POO09")
                        .description("""
                                API REST de modelado táctico de Counter-Strike 2 con persistencia en H2, arquitectura de 3 capas y escenario de APIs cruzadas (POO-09).
                                
                                ### Reglas de Negocio Publicadas (Ejecutadas en ArmaService):
                                1. **Regla 1 (Munición, Capacidad de Cargador y Fuego Amigo Competitivo):**
                                   - Un `Arma` solo puede disparar o detonar si tiene proyectiles en el cargador (`municion > 0`) y solo permite recargar cuando `balas > 0` y `municion < capacidadCargador` (si se viola, lanza `MunicionInsuficienteException` -> **HTTP 409 Conflict**).
                                   - Si el `Equipo` del `Arma` guardada en H2 y el `Equipo` del `Agente` objetivo guardado en H2 coinciden, se bloquea el ataque por fuego amigo competitivo (lanza `FuegoAmigoException` -> **HTTP 422 Unprocessable Entity**).
                                2. **Regla 2 (Daño Balístico/Explosivo, Absorción en Chaleco Kevlar y Descuento de Vida en H2):**
                                   - Calcula la atenuación de daño por distancia/radio de explosión y multiplicador crítico de headshot (x4), absorbe el 35% del impacto en el `chalecoKevlar` del `Agente` y descuenta el daño de su `vida` (0-100 HP), persistiendo ambos cambios en H2.
                                3. **Invariantes del Juego (`HTTP 400 Bad Request`):**
                                   - `daño` entre `1` y `500`, `precio` entre `$0` y `$16,000`, `municion` entre `0` y `capacidadCargador`, `precision` entre `0.0` y `1.0`, y en `Granada` tanto `radioExplosion > 0` como `tiempoExplosion > 0`.
                                """)
                        .contact(new Contact()
                                .name("Diana Aquino")
                                .url("https://github.com/dianamariapaz2104/daquino-tp-lp3-2026"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
