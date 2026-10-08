package py.edu.uc.lp3.rest.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import py.edu.uc.lp3.domain.Arma;
import py.edu.uc.lp3.domain.DesertEagle;
import py.edu.uc.lp3.domain.Equipo;
import py.edu.uc.lp3.domain.Glock18;
import py.edu.uc.lp3.domain.GranadaHE;
import py.edu.uc.lp3.domain.M4A4;
import py.edu.uc.lp3.domain.MP9;
import py.edu.uc.lp3.domain.Nova;

@RestController
public class ArmaController {

    /**
     * Consigna 4: Construye una instancia del dominio a partir de parámetros de la URL.
     * Esos valores alimentan los constructores simples y sobrecargados.
     * Si un valor viola una regla del dominio (invariante), la clase lo rechaza
     * (lanzando IllegalArgumentException) y el controller informa el resultado con HTTP 400.
     */
    @GetMapping({"/api/armas", "/api/armas/construir"})
    public Map<String, Object> construirArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "municion", required = false) Integer municion,
            @RequestParam(name = "daño", required = false) Integer daño,
            @RequestParam(name = "equipo", required = false) String equipoNombre) {

        Equipo equipo = (equipoNombre != null && !equipoNombre.trim().isEmpty())
                ? new Equipo(equipoNombre)
                : new Equipo("Counter-Terrorista");

        Arma arma = fabricarArma(nombre, daño, municion, equipo);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("arma", arma.getNombre());
        respuesta.put("municion", arma.getMunicion());
        respuesta.put("capacidadCargador", arma.getCapacidadCargador());
        respuesta.put("daño", arma.getDaño());
        respuesta.put("precio", arma.getPrecio());
        respuesta.put("equipo", arma.getEquipo().getNombre());
        respuesta.put("comportamientoDeCombate", arma.comportamientoDeCombate());
        respuesta.put("disparoBase", arma.disparar());
        return respuesta;
    }

    /**
     * Consigna 5: Respuesta en JSON al pedirle el mensaje abstracto a cada clase hija.
     * El controller habla con ambas como si fueran el tipo padre Arma (polimorfismo).
     * El texto proviene de los métodos sobreescritos en cada clase concreta.
     */
    @GetMapping("/api/armas/polimorfismo")
    public List<Map<String, Object>> demostrarPolimorfismo() {
        // Dos clases hijas independientes (y otras especializaciones) tratadas a través del tipo base Arma
        List<Arma> armas = List.of(
                new M4A4(),
                new MP9(),
                new DesertEagle(),
                new Glock18(),
                new Nova(),
                new GranadaHE()
        );

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Arma arma : armas) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("arma", arma.getNombre());
            item.put("claseJava", arma.getClass().getSimpleName());
            item.put("tipoPadre", Arma.class.getSimpleName());
            item.put("municion", arma.getMunicion() + "/" + arma.getCapacidadCargador());
            // Invocación polimórfica de métodos abstractos sobreescritos
            item.put("comportamientoDeCombate", arma.comportamientoDeCombate());
            item.put("resultadoDisparo", arma.disparar());
            lista.add(item);
        }
        return lista;
    }

    /**
     * Consigna 7: Demuestra la sobrecarga del mensaje del dominio
     * (disparar sin más datos, o disparar indicando una distancia y headshot).
     */
    @GetMapping("/api/armas/disparar")
    public Map<String, Object> dispararArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "distancia", required = false) Integer distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot) {

        Arma arma = fabricarArma(nombre, null, null, new Equipo("Counter-Terrorista"));

        String resultadoAccion;
        String metodoSobrecargadoUsado;

        if (distancia == null) {
            // Sobrecarga 1: disparar() sin argumentos (disparo estándar a quemarropa)
            resultadoAccion = arma.disparar();
            metodoSobrecargadoUsado = "disparar() [sin argumentos]";
        } else {
            // Sobrecarga 2 y 3: disparar(distancia) o disparar(distancia, headshot)
            resultadoAccion = arma.disparar(distancia, headshot);
            metodoSobrecargadoUsado = "disparar(distancia=" + distancia + ", headshot=" + headshot + ")";
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("arma", arma.getNombre());
        respuesta.put("metodoSobrecargado", metodoSobrecargadoUsado);
        respuesta.put("resultado", resultadoAccion);
        respuesta.put("municionRestante", arma.getMunicion());
        return respuesta;
    }

    /**
     * Manejador de excepciones del dominio: cuando un valor viola una regla de negocio
     * (invariante protegido), la capa de dominio rechaza la mutación y el controller
     * informa el resultado al cliente con código HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaViolada(IllegalArgumentException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de dominio violada");
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private Arma fabricarArma(String nombre, Integer daño, Integer municion, Equipo equipo) {
        if (nombre == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro 'nombre' es requerido.");
        }

        String n = nombre.trim();
        boolean usaPersonalizado = (daño != null || municion != null);

        if (n.equalsIgnoreCase("M4A4")) {
            if (usaPersonalizado) {
                int d = (daño != null) ? daño : 30;
                int m = (municion != null) ? municion : 30;
                return new M4A4(d, m, equipo);
            }
            return new M4A4(equipo);
        } else if (n.equalsIgnoreCase("MP9")) {
            if (usaPersonalizado) {
                int d = (daño != null) ? daño : 24;
                int m = (municion != null) ? municion : 30;
                return new MP9(d, m, equipo);
            }
            return new MP9(equipo);
        } else if (n.equalsIgnoreCase("DesertEagle") || n.equalsIgnoreCase("Deagle")) {
            if (usaPersonalizado) {
                int d = (daño != null) ? daño : 40;
                int m = (municion != null) ? municion : 7;
                return new DesertEagle(d, m, equipo);
            }
            return new DesertEagle(equipo);
        } else if (n.equalsIgnoreCase("Glock18") || n.equalsIgnoreCase("Glock")) {
            if (usaPersonalizado) {
                int d = (daño != null) ? daño : 20;
                int m = (municion != null) ? municion : 20;
                return new Glock18(d, m, equipo);
            }
            return new Glock18(equipo);
        } else if (n.equalsIgnoreCase("Nova")) {
            if (usaPersonalizado) {
                int d = (daño != null) ? daño : 26;
                int m = (municion != null) ? municion : 8;
                return new Nova(d, m, equipo);
            }
            return new Nova(equipo);
        } else if (n.equalsIgnoreCase("GranadaHE") || n.equalsIgnoreCase("Granada")) {
            return new GranadaHE(equipo);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arma desconocida en el arsenal: " + nombre);
        }
    }
}
