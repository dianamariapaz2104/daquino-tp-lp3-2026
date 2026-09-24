package py.edu.uc.lp3.da.cs2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IndexController {

    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of(
                "autora", "Diana",
                "dominio", "Counter",
                "estado", "API funcionando"
        );
    }
}
