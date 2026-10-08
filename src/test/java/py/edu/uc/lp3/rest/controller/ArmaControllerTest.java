package py.edu.uc.lp3.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
