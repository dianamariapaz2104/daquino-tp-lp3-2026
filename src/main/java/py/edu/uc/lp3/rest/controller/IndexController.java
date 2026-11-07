package py.edu.uc.lp3.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class IndexController {

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("autora", "Diana Aquino");
        respuesta.put("dominio", "Counter-Strike 2");
        respuesta.put("estado", "API funcionando");
        respuesta.put("materia", "Lenguaje de Programación 3 (CYT646)");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("inicio", "GET /");
        endpoints.put("crearArmaCrud", "POST /api/armas");
        endpoints.put("listarArmasCrud", "GET /api/armas/todas");
        endpoints.put("obtenerArmaPorIdCrud", "GET /api/armas/{id}");
        endpoints.put("actualizarArmaCrud", "PUT /api/armas/{id}");
        endpoints.put("eliminarArmaCrud", "DELETE /api/armas/{id}");
        endpoints.put("protocoloInterfaz", "GET /api/armas/protocolo");
        endpoints.put("simularCombateArmaPersistida", "POST /api/armas/{id}/simular?equipoObjetivo=Terrorista&distancia=20&headshot=true&chaleco=true");
        endpoints.put("recargarArmaPersistida", "POST /api/armas/{id}/recargar?balas=10");
        endpoints.put("simularGranada", "GET /api/armas/granadas/simular?equipoAtacante=Counter-Terrorista&equipoObjetivo=Terrorista&distancia=2.0&chaleco=true");
        endpoints.put("listarAgentesPersistidos", "GET /api/agentes/todos");
        endpoints.put("construirArma", "GET /api/armas?nombre=M4A4&municion=30&daño=30&equipo=Counter-Terrorista");
        endpoints.put("polimorfismo", "GET /api/armas/polimorfismo");
        endpoints.put("disparar", "GET /api/armas/disparar?nombre=M4A4&distancia=25&headshot=true");
        respuesta.put("endpoints", endpoints);

        return respuesta;
    }
}
