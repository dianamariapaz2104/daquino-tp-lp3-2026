package py.edu.uc.lp3.rest.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "0. Estado del Servicio", description = "Verificación de salud de la API y catálogo de endpoints")
public class IndexController {

    @Operation(summary = "Confirmar estado de la API y listar endpoints", description = "Devuelve la autora, el dominio Counter-Strike 2, las rutas de OpenAPI/Swagger UI y los endpoints del servicio.")
    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("autora", "Diana Aquino");
        respuesta.put("dominio", "Counter-Strike 2");
        respuesta.put("estado", "API funcionando");
        respuesta.put("materia", "Lenguaje de Programación 3 (CYT646)");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("inicio", "GET /");
        endpoints.put("openApiJson", "GET /v3/api-docs");
        endpoints.put("swaggerUi", "GET /swagger-ui/index.html");
        endpoints.put("crearArmaCrud", "POST /api/armas");
        endpoints.put("listarArmasCrud", "GET /api/armas/todas");
        endpoints.put("obtenerArmaPorIdCrud", "GET /api/armas/{id}");
        endpoints.put("actualizarArmaCrud", "PUT /api/armas/{id}");
        endpoints.put("eliminarArmaCrud", "DELETE /api/armas/{id}");
        endpoints.put("crearAgenteCrud", "POST /api/agentes");
        endpoints.put("listarAgentesCrud", "GET /api/agentes/todos");
        endpoints.put("simularCombatePersistido", "POST /api/armas/{id}/simular?agenteObjetivoId=1&distancia=15&headshot=true");
        endpoints.put("recargarArmaPersistida", "POST /api/armas/{id}/recargar?balas=5");
        endpoints.put("protocoloInterfaz", "GET /api/armas/protocolo");
        endpoints.put("escenarioApisCruzadas", "POST /api/escenario/jugadas");
        endpoints.put("listarJugadasEscenarioH2", "GET /api/escenario/jugadas");
        endpoints.put("construirArma", "GET /api/armas?nombre=M4A4&municion=30&daño=30&equipo=Counter-Terrorista");
        endpoints.put("polimorfismo", "GET /api/armas/polimorfismo");
        endpoints.put("disparar", "GET /api/armas/disparar?nombre=M4A4&distancia=25&headshot=true");
        endpoints.put("simularGranada", "POST /api/armas/granadas/simular?equipoAtacante=Counter-Terrorista&equipoObjetivo=Terrorista&distancia=2.0");
        respuesta.put("endpoints", endpoints);

        return respuesta;
    }
}
