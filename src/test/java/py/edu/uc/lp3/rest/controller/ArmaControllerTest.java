package py.edu.uc.lp3.rest.controller;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import py.edu.uc.lp3.service.CompaneroApiClient;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ArmaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompaneroApiClient companeroApiClient;

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
    @DisplayName("GET /api/armas/protocolo expone la interfaz InspeccionableEnPartida en familias distintas (Arma y Agente)")
    void getProtocoloInterfazEnFamiliasDistintas() throws Exception {
        mockMvc.perform(get("/api/armas/protocolo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].protocoloInterfaz").value("InspeccionableEnPartida"))
                .andExpect(jsonPath("$[0].inspeccionEnPartida").isNotEmpty())
                .andExpect(jsonPath("$[*].familiaJerarquia", hasItems("Arma", "Agente")));
    }

    @Test
    @DisplayName("GET /api/armas/disparar demuestra sobrecarga sin distancia y con distancia/headshot")
    void getArmasDispararSobrecarga() throws Exception {
        mockMvc.perform(get("/api/armas/disparar").param("nombre", "M4A4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoSobrecargado").value("disparar() [sin argumentos]"))
                .andExpect(jsonPath("$.resultado").value(containsString("[M4A4]")));

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
    @DisplayName("Simulación persistida usando Arma y Agente creados con CRUD en H2 y verificación de excepciones hijas")
    void simulacionPersistidaConAgentesDeH2YHandlersExcepcionesHijas() throws Exception {
        // 1. Crear un AgenteTerrorista objetivo en H2 mediante POST /api/agentes
        String terroristaJson = """
                {
                  "tipoAgente": "AgenteTerrorista",
                  "nombre": "Phoenix Objetivo H2",
                  "vida": 100,
                  "chalecoKevlar": 100,
                  "dinero": 800,
                  "portaBombaC4": true,
                  "equipo": { "nombre": "Terrorista" }
                }
                """;
        MvcResult postTerrorista = mockMvc.perform(post("/api/agentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(terroristaJson))
                .andExpect(status().isCreated())
                .andReturn();
        long terroristaId = ((Number) JsonPath.read(postTerrorista.getResponse().getContentAsString(), "$.id")).longValue();

        // 2. Crear un AgenteCT aliado en H2 mediante POST /api/agentes
        String aliadoCtJson = """
                {
                  "tipoAgente": "AgenteCT",
                  "nombre": "SAS Aliado H2",
                  "vida": 100,
                  "chalecoKevlar": 100,
                  "dinero": 800,
                  "tieneKitDesactivacion": true,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;
        MvcResult postAliado = mockMvc.perform(post("/api/agentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aliadoCtJson))
                .andExpect(status().isCreated())
                .andReturn();
        long aliadoCtId = ((Number) JsonPath.read(postAliado.getResponse().getContentAsString(), "$.id")).longValue();

        // 3. Crear un Arma en H2 con 1 sola bala en el cargador (1/7)
        String deagleUnaBalaJson = """
                {
                  "tipoArma": "DesertEagle",
                  "nombre": "Desert Eagle Kumicho Dragon",
                  "daño": 45,
                  "precision": 0.80,
                  "tiempoRecarga": 2.0,
                  "precio": 700,
                  "municion": 1,
                  "capacidadCargador": 7,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;

        MvcResult postResult = mockMvc.perform(post("/api/armas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deagleUnaBalaJson))
                .andExpect(status().isCreated())
                .andReturn();
        long armaId = ((Number) JsonPath.read(postResult.getResponse().getContentAsString(), "$.id")).longValue();

        // 4. Simulación caso feliz sobre entidades persistidas (armaId y terroristaId)
        mockMvc.perform(post("/api/armas/" + armaId + "/simular")
                        .param("agenteObjetivoId", String.valueOf(terroristaId))
                        .param("distancia", "15")
                        .param("headshot", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArmaPersistida").value(armaId))
                .andExpect(jsonPath("$.idAgenteObjetivoPersistido").value(terroristaId))
                .andExpect(jsonPath("$.municionAntes").value(1))
                .andExpect(jsonPath("$.municionDespues").value(0))
                .andExpect(jsonPath("$.dañoInfligido", greaterThan(0)))
                .andExpect(jsonPath("$.vidaObjetivoDespues", lessThan(100)));

        // 5. GET posterior del arma y del agente confirma que ambos estados quedaron actualizados en H2
        mockMvc.perform(get("/api/armas/" + armaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.municion").value(0));

        mockMvc.perform(get("/api/agentes/" + terroristaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vida", lessThan(100)));

        // 6. Excepción Hija 1 (MunicionInsuficienteException -> 409 Conflict): intentar disparar con munición 0 en H2
        mockMvc.perform(post("/api/armas/" + armaId + "/simular")
                        .param("agenteObjetivoId", String.valueOf(terroristaId))
                        .param("distancia", "15"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.tipoExcepcion").value("MunicionInsuficienteException"))
                .andExpect(jsonPath("$.jerarquiaPadre").value("ReglaDeJuegoException"))
                .andExpect(jsonPath("$.estado").value(409));

        // 7. Recargar el arma en H2
        mockMvc.perform(post("/api/armas/" + armaId + "/recargar").param("balas", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.municionDespues").value(5));

        // 8. Excepción Hija 2 (FuegoAmigoException -> 422 Unprocessable Entity): atacar al AgenteCT aliado guardado en H2
        mockMvc.perform(post("/api/armas/" + armaId + "/simular")
                        .param("agenteObjetivoId", String.valueOf(aliadoCtId))
                        .param("distancia", "10"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.tipoExcepcion").value("FuegoAmigoException"))
                .andExpect(jsonPath("$.jerarquiaPadre").value("ReglaDeJuegoException"))
                .andExpect(jsonPath("$.estado").value(422));
    }

    @Test
    @DisplayName("Validación de invariantes en Service: rechaza GranadaHE con radioExplosion negativo (-3) y no la guarda en H2")
    void serviceRechazaGranadaConRadioExplosionNegativo() throws Exception {
        MvcResult antes = mockMvc.perform(get("/api/armas/todas")).andExpect(status().isOk()).andReturn();
        int cantidadAntes = ((java.util.List<?>) JsonPath.read(antes.getResponse().getContentAsString(), "$")).size();

        String granadaRadioNegativoJson = """
                {
                  "tipoArma": "GranadaHE",
                  "nombre": "Granada HE Ilegal",
                  "daño": 50,
                  "precision": 1.0,
                  "tiempoRecarga": 0.0,
                  "precio": 300,
                  "municion": 1,
                  "capacidadCargador": 1,
                  "radioExplosion": -3.0,
                  "tiempoExplosion": 1.5,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;

        mockMvc.perform(post("/api/armas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(granadaRadioNegativoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de dominio violada"))
                .andExpect(jsonPath("$.mensaje", containsString("El radio de explosión de una granada debe ser mayor a cero")));

        // También rechaza munición (50) mayor al cargador (30)
        String municionMayorACargadorJson = """
                {
                  "tipoArma": "M4A4",
                  "nombre": "M4A4 Sobrecargada",
                  "daño": 30,
                  "precision": 0.85,
                  "tiempoRecarga": 2.0,
                  "precio": 3100,
                  "municion": 50,
                  "capacidadCargador": 30,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;

        mockMvc.perform(post("/api/armas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(municionMayorACargadorJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("no puede superar la capacidad del cargador")));

        // Verificar que la colección en H2 no creció
        MvcResult despues = mockMvc.perform(get("/api/armas/todas")).andExpect(status().isOk()).andReturn();
        int cantidadDespues = ((java.util.List<?>) JsonPath.read(despues.getResponse().getContentAsString(), "$")).size();
        org.junit.jupiter.api.Assertions.assertEquals(cantidadAntes, cantidadDespues);
    }

    @Test
    @DisplayName("PUT sin daño ni tiempoRecarga usa valores por defecto del constructor de DesertEagle (40 y 2.0) y responde 200")
    void putSinDanioNiTiempoRecargaUsaValoresPorDefectoConstructor() throws Exception {
        String crearJson = """
                {
                  "tipoArma": "DesertEagle",
                  "nombre": "Desert Eagle Original",
                  "daño": 48,
                  "precision": 0.80,
                  "tiempoRecarga": 2.2,
                  "precio": 700,
                  "municion": 7,
                  "capacidadCargador": 7,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;
        MvcResult postRes = mockMvc.perform(post("/api/armas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearJson))
                .andExpect(status().isCreated())
                .andReturn();
        long id = ((Number) JsonPath.read(postRes.getResponse().getContentAsString(), "$.id")).longValue();

        String putSinDanioJson = """
                {
                  "tipoArma": "DesertEagle",
                  "nombre": "Desert Eagle Sin Danio Explicito",
                  "precision": 0.82,
                  "precio": 750,
                  "municion": 7,
                  "capacidadCargador": 7,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;

        mockMvc.perform(put("/api/armas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putSinDanioJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.daño").value(40))
                .andExpect(jsonPath("$.tiempoRecarga").value(2.0));
    }

    @Test
    @DisplayName("CRUD de Agente en H2 y rechazo de Agente con vida fuera de rango (150 HP)")
    void crudAgenteYValidacionInvariantesEnService() throws Exception {
        String agenteInvalidoJson = """
                {
                  "tipoAgente": "AgenteCT",
                  "nombre": "Agente Ilegal",
                  "vida": 150,
                  "chalecoKevlar": 100,
                  "dinero": 800,
                  "equipo": { "nombre": "Counter-Terrorista" }
                }
                """;

        mockMvc.perform(post("/api/agentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agenteInvalidoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("La vida del agente debe estar entre 0 y 100")));
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
    @DisplayName("Regla de negocio en granadas: simula explosión con 3 clases del modelo (Equipo, Agente, GranadaHE)")
    void simulacionExplosionGranadaConTresClases() throws Exception {
        // Caso 1: Detonación válida contra equipo rival dentro del radio de 5m
        mockMvc.perform(post("/api/armas/granadas/simular")
                        .param("equipoAtacante", "Counter-Terrorista")
                        .param("equipoObjetivo", "Terrorista")
                        .param("distancia", "2.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explosivo").value("Granada HE"))
                .andExpect(jsonPath("$.clasesParticipantes", hasSize(3)))
                .andExpect(jsonPath("$.objetivoDentroDelRadio").value(true))
                .andExpect(jsonPath("$.dañoFinalCalculado", greaterThan(0)))
                .andExpect(jsonPath("$.municionRestante").value("0/1"));

        // Caso 2: Rechazo por regla de negocio de Fuego Amigo (FuegoAmigoException -> 422 Unprocessable Entity)
        mockMvc.perform(post("/api/armas/granadas/simular")
                        .param("equipoAtacante", "Counter-Terrorista")
                        .param("equipoObjetivo", "Counter-Terrorista")
                        .param("distancia", "1.5"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.tipoExcepcion").value("FuegoAmigoException"))
                .andExpect(jsonPath("$.mensaje", containsString("Fuego amigo bloqueado")));
    }

    @Test
    @DisplayName("POO-09: Documento OpenAPI 3 (/v3/api-docs) y Mapa Data en capas responden correctamente")
    void openApiYMapaDataFuncionan() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value(startsWith("3.")))
                .andExpect(jsonPath("$.info.title", containsString("Counter-Strike 2")));

        mockMvc.perform(post("/api/escenario/mapa-data")
                        .param("clave", "ronda_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mapa\":\"Mirage\",\"bombaPlantada\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mapa").value("Mirage"));

        mockMvc.perform(get("/api/escenario/mapa-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ronda_1.mapa").value("Mirage"));
    }

    @Test
    @DisplayName("POO-09 Escenario de APIs Cruzadas: Camino feliz persiste en H2, y rechazos de A2 o A3 dejan H2 intacta y atribuyen el error")
    void escenarioApisCruzadasFelizYRechazosDeA2YA3() throws Exception {
        HttpServer mockA2 = HttpServer.create(new InetSocketAddress(0), 0);
        mockA2.createContext("/api/simulacion/explosion", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("distancia=-")) {
                byte[] resp = "{\"error\":\"ValidationException: La distancia del impacto no puede ser negativa\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, resp.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
            } else {
                byte[] resp = "{\"armaUtilizada\":\"Granada HE\",\"enRadioAlcance\":true,\"distanciaMetros\":2.5,\"danioFinalVida\":33.69,\"vidaRestante\":66.31}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
            }
        });
        mockA2.start();

        HttpServer mockA3 = HttpServer.create(new InetSocketAddress(0), 0);
        mockA3.createContext("/api/reglas/danio", exchange -> {
            String bodyReq = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (bodyReq.contains("\"vida\":0")) {
                byte[] resp = "{\"error\":\"JUGADOR_ELIMINADO: La vida del objetivo ya es 0\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(422, resp.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
            } else {
                byte[] resp = "{\"vidaRestante\":82,\"armaduraRestante\":82}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
            }
        });
        mockA3.start();

        try {
            String urlA2 = "http://localhost:" + mockA2.getAddress().getPort();
            String urlA3 = "http://localhost:" + mockA3.getAddress().getPort();
            companeroApiClient.configurarUrlsParaTesting(urlA2, "/api/simulacion/explosion", urlA3, "/api/reglas/danio");

            MvcResult antes = mockMvc.perform(get("/api/escenario/jugadas")).andExpect(status().isOk()).andReturn();
            int jugadasAntes = ((java.util.List<?>) JsonPath.read(antes.getResponse().getContentAsString(), "$")).size();

            // 1. Camino Feliz: A2 (200) y A3 (200) aceptan -> Se persiste la jugada en el H2 de A1 (201 Created)
            mockMvc.perform(post("/api/escenario/jugadas")
                            .param("cargador", "3")
                            .param("balasDisparo", "2")
                            .param("vidaObjetivo", "100")
                            .param("armaduraObjetivo", "100")
                            .param("danio", "36")
                            .param("distanciaExplosion", "2.0")
                            .param("equipoObjetivo", "Terrorista"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.validacionA2", containsString("A2 ACEPTÓ")))
                    .andExpect(jsonPath("$.validacionA3", containsString("A3 ACEPTÓ")));

            MvcResult despuesFeliz = mockMvc.perform(get("/api/escenario/jugadas")).andExpect(status().isOk()).andReturn();
            int jugadasDespuesFeliz = ((java.util.List<?>) JsonPath.read(despuesFeliz.getResponse().getContentAsString(), "$")).size();
            org.junit.jupiter.api.Assertions.assertEquals(jugadasAntes + 1, jugadasDespuesFeliz);

            // 2. Rechazo de A2 (balasDisparo = -3) -> Atribuye rechazo a A2 y el H2 de A1 NO cambia
            mockMvc.perform(post("/api/escenario/jugadas")
                            .param("balasDisparo", "-3")
                            .param("vidaObjetivo", "100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.rechazadoPor").value("A2"))
                    .andExpect(jsonPath("$.estadoH2Local", containsString("SIN_CAMBIOS")));

            MvcResult despuesRechazoA2 = mockMvc.perform(get("/api/escenario/jugadas")).andExpect(status().isOk()).andReturn();
            int jugadasDespuesA2 = ((java.util.List<?>) JsonPath.read(despuesRechazoA2.getResponse().getContentAsString(), "$")).size();
            org.junit.jupiter.api.Assertions.assertEquals(jugadasDespuesFeliz, jugadasDespuesA2);

            // 3. Rechazo de A3 (A2 acepta, pero A3 rechaza con 422 JUGADOR_ELIMINADO cuando vidaObjetivo = 0) -> Atribuye rechazo a A3 y el H2 de A1 NO cambia
            mockMvc.perform(post("/api/escenario/jugadas")
                            .param("balasDisparo", "2")
                            .param("vidaObjetivo", "0")
                            .param("armaduraObjetivo", "0")
                            .param("danio", "36"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.rechazadoPor").value("A3"))
                    .andExpect(jsonPath("$.estadoH2Local", containsString("SIN_CAMBIOS")));

            MvcResult despuesRechazoA3 = mockMvc.perform(get("/api/escenario/jugadas")).andExpect(status().isOk()).andReturn();
            int jugadasDespuesA3 = ((java.util.List<?>) JsonPath.read(despuesRechazoA3.getResponse().getContentAsString(), "$")).size();
            org.junit.jupiter.api.Assertions.assertEquals(jugadasDespuesFeliz, jugadasDespuesA3);
        } finally {
            mockA2.stop(0);
            mockA3.stop(0);
        }
    }
}
