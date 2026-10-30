package py.edu.uc.lp3.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ArmaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / confirma que el servicio está vivo y responde 200 OK")
    void getIndexConfirmaServicioVivo() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autora").value("Diana Aquino"))
                .andExpect(jsonPath("$.dominio").value("Counter-Strike 2"))
                .andExpect(jsonPath("$.estado").value("API funcionando"));
    }

    @Test
    @DisplayName("GET /api/armas construye instancia legal desde parámetros URL y responde JSON")
    void getArmasConstruyeInstanciaLegal() throws Exception {
        mockMvc.perform(get("/api/armas")
                        .param("nombre", "M4A4")
                        .param("daño", "32")
                        .param("municion", "25")
                        .param("equipo", "Counter-Terrorista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arma").value("M4A4"))
                .andExpect(jsonPath("$.daño").value(32))
                .andExpect(jsonPath("$.municion").value(25))
                .andExpect(jsonPath("$.comportamientoDeCombate").isNotEmpty())
                .andExpect(jsonPath("$.disparoBase").value(containsString("[M4A4]")));
    }

    @Test
    @DisplayName("GET /api/armas rechaza valor ilegal (munición negativa) con 400 Bad Request")
    void getArmasRechazaMunicionNegativaCon400() throws Exception {
        mockMvc.perform(get("/api/armas")
                        .param("nombre", "M4A4")
                        .param("municion", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de dominio violada"))
                .andExpect(jsonPath("$.mensaje", containsString("La munición no puede ser negativa")));
    }

    @Test
    @DisplayName("GET /api/armas rechaza daño cero o negativo con 400 Bad Request")
    void getArmasRechazaDanioInvalidoCon400() throws Exception {
        mockMvc.perform(get("/api/armas")
                        .param("nombre", "MP9")
                        .param("daño", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de dominio violada"))
                .andExpect(jsonPath("$.mensaje", containsString("El daño debe ser mayor a cero")));
    }

    @Test
    @DisplayName("GET /api/armas/polimorfismo responde JSON con mensaje abstracto de ambas clases hijas")
    void getArmasPolimorfismoRespondeAmbasClasesHijas() throws Exception {
        mockMvc.perform(get("/api/armas/polimorfismo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].tipoPadre").value("Arma"))
                .andExpect(jsonPath("$[0].comportamientoDeCombate").isNotEmpty())
                .andExpect(jsonPath("$[1].tipoPadre").value("Arma"))
                .andExpect(jsonPath("$[1].comportamientoDeCombate").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/armas/disparar demuestra sobrecarga sin distancia y con distancia/headshot")
    void getArmasDispararSobrecarga() throws Exception {
        // Disparo sin distancia
        mockMvc.perform(get("/api/armas/disparar").param("nombre", "M4A4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoSobrecargado").value("disparar() [sin argumentos]"))
                .andExpect(jsonPath("$.resultado").value(containsString("[M4A4]")));

        // Disparo con distancia y headshot
        mockMvc.perform(get("/api/armas/disparar")
                        .param("nombre", "M4A4")
                        .param("distancia", "30")
                        .param("headshot", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoSobrecargado", containsString("distancia=30")))
                .andExpect(jsonPath("$.resultado", containsString("HEADSHOT")));
    }

    @Test
    @DisplayName("CRUD completo (POST, GET por ID, GET todas, PUT, DELETE) persiste en H2 respetando 3 capas")
    void crudCompletoPersisteEnH2() throws Exception {
        String nuevaArmaJson = """
                {
                  "tipoArma": "DesertEagle",
                  "nombre": "Desert Eagle Blaze",
                  "daño": 45,
                  "precision": 0.80,
                  "tiempoRecarga": 2.0,
                  "precio": 700,
                  "municion": 7,
                  "capacidadCargador": 7,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;

        // 1. CREATE (POST /api/armas)
        MvcResult postResult = mockMvc.perform(post("/api/armas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevaArmaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Desert Eagle Blaze"))
                .andExpect(jsonPath("$.daño").value(45))
                .andReturn();

        Number idNumber = JsonPath.read(postResult.getResponse().getContentAsString(), "$.id");
        long id = idNumber.longValue();

        // 2. READ BY ID (GET /api/armas/{id})
        mockMvc.perform(get("/api/armas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Desert Eagle Blaze"));

        // 3. READ ALL (GET /api/armas/todas)
        mockMvc.perform(get("/api/armas/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 4. UPDATE (PUT /api/armas/{id})
        String actualizarJson = """
                {
                  "tipoArma": "DesertEagle",
                  "nombre": "Desert Eagle Printstream",
                  "daño": 50,
                  "precision": 0.85,
                  "tiempoRecarga": 1.9,
                  "precio": 750,
                  "municion": 7,
                  "capacidadCargador": 7,
                  "equipo": { "nombre": "Ambos" }
                }
                """;

        mockMvc.perform(put("/api/armas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actualizarJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Desert Eagle Printstream"))
                .andExpect(jsonPath("$.daño").value(50))
                .andExpect(jsonPath("$.precio").value(750));

        // 5. DELETE (DELETE /api/armas/{id})
        mockMvc.perform(delete("/api/armas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEliminado").value(id));

        // 6. Verificar que ya no existe (404 Not Found)
        mockMvc.perform(get("/api/armas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Validación de negocio en Service: rechaza arma que supera el presupuesto máximo de CS2 ($16,000)")
    void validacionNegocioServiceRechazaPrecioExcesivo() throws Exception {
        String armaInvalidaJson = """
                {
                  "nombre": "M4A4 Dorada",
                  "daño": 30,
                  "precision": 0.85,
                  "tiempoRecarga": 2.0,
                  "precio": 25000,
                  "municion": 30,
                  "capacidadCargador": 30
                }
                """;

        mockMvc.perform(post("/api/armas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(armaInvalidaJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de dominio violada"))
                .andExpect(jsonPath("$.mensaje", containsString("presupuesto máximo")));
    }

    @Test
    @DisplayName("Regla de negocio en granadas: simula explosión con 3 clases del modelo (Equipo, Granada, GranadaHE)")
    void simulacionExplosionGranadaConTresClases() throws Exception {
        // Caso 1: Detonación válida contra equipo rival dentro del radio de 5m
        mockMvc.perform(get("/api/armas/granadas/simular")
                        .param("equipoAtacante", "Counter-Terrorista")
                        .param("equipoObjetivo", "Terrorista")
                        .param("distancia", "2.0")
                        .param("chaleco", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explosivo").value("Granada HE"))
                .andExpect(jsonPath("$.clasesParticipantes", hasSize(3)))
                .andExpect(jsonPath("$.objetivoDentroDelRadio").value(true))
                .andExpect(jsonPath("$.dañoFinalCalculado", greaterThan(0)))
                .andExpect(jsonPath("$.municionRestante").value("0/1"));

        // Caso 2: Rechazo por regla de negocio de Fuego Amigo (mismo equipo en modo competitivo)
        mockMvc.perform(get("/api/armas/granadas/simular")
                        .param("equipoAtacante", "Counter-Terrorista")
                        .param("equipoObjetivo", "Counter-Terrorista")
                        .param("distancia", "1.5")
                        .param("fuegoAmigo", "false"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de dominio violada"))
                .andExpect(jsonPath("$.mensaje", containsString("Fuego amigo bloqueado")));
    }
}
