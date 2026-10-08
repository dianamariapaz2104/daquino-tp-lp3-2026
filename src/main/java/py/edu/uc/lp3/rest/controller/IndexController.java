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
        endpoints.put("construirArma", "GET /api/armas?nombre=M4A4&municion=30&daño=30&equipo=Counter-Terrorista");
        endpoints.put("polimorfismo", "GET /api/armas/polimorfismo");
        endpoints.put("disparar", "GET /api/armas/disparar?nombre=M4A4&distancia=25&headshot=true");
        respuesta.put("endpoints", endpoints);

        return respuesta;
    }
}
