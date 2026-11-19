package py.edu.uc.lp3.rest.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import py.edu.uc.lp3.domain.Agente;
import py.edu.uc.lp3.domain.Arma;
import py.edu.uc.lp3.domain.Equipo;
import py.edu.uc.lp3.domain.FuegoAmigoException;
import py.edu.uc.lp3.domain.InspeccionableEnPartida;
import py.edu.uc.lp3.domain.MunicionInsuficienteException;
import py.edu.uc.lp3.domain.ReglaDeJuegoException;
import py.edu.uc.lp3.service.ArmaService;

@RestController
public class ArmaController {

    private final ArmaService armaService;

    public ArmaController(ArmaService armaService) {
        this.armaService = armaService;
    }

    // ==========================================
    // Endpoints CRUD REST de Arma (3 Capas + H2)
    // ==========================================

    @PostMapping("/api/armas")
    public ResponseEntity<Arma> crearArma(@RequestBody Arma arma) {
        Arma guardada = armaService.crearArma(arma);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/api/armas/todas")
    public ResponseEntity<List<Arma>> listarTodas() {
        return ResponseEntity.ok(armaService.listarTodas());
    }

    @GetMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Arma> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(armaService.obtenerPorId(id));
    }

    @PutMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Arma> actualizarArma(@PathVariable Long id, @RequestBody Arma armaActualizada) {
        Arma actualizada = armaService.actualizarArma(id, armaActualizada);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Map<String, Object>> eliminarArma(@PathVariable Long id) {
        armaService.eliminarArma(id);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Arma eliminada correctamente");
        respuesta.put("idEliminado", id);
        return ResponseEntity.ok(respuesta);
    }

    // ==========================================
    // Endpoints CRUD REST de Agente (3 Capas + H2)
    // ==========================================

    @PostMapping("/api/agentes")
    public ResponseEntity<Agente> crearAgente(@RequestBody Agente agente) {
        Agente guardado = armaService.crearAgente(agente);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping({"/api/agentes", "/api/agentes/todos"})
    public ResponseEntity<List<Agente>> listarAgentesPersistidos() {
        return ResponseEntity.ok(armaService.listarAgentes());
    }

    @GetMapping("/api/agentes/{id:\\d+}")
    public ResponseEntity<Agente> obtenerAgentePorId(@PathVariable Long id) {
        return ResponseEntity.ok(armaService.obtenerAgentePorId(id));
    }

    @PutMapping("/api/agentes/{id:\\d+}")
    public ResponseEntity<Agente> actualizarAgente(@PathVariable Long id, @RequestBody Agente agenteActualizado) {
        Agente actualizado = armaService.actualizarAgente(id, agenteActualizado);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/api/agentes/{id:\\d+}")
    public ResponseEntity<Map<String, Object>> eliminarAgente(@PathVariable Long id) {
        armaService.eliminarAgente(id);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Agente eliminado correctamente");
        respuesta.put("idEliminado", id);
        return ResponseEntity.ok(respuesta);
    }

    // ==========================================
    // Endpoint del Protocolo de Dominio POO-07 (Interfaz InspeccionableEnPartida)
    // ==========================================

    @GetMapping({"/api/armas/protocolo", "/api/protocolo"})
    public ResponseEntity<List<Map<String, Object>>> consultarProtocoloInspeccionable() {
        List<InspeccionableEnPartida> elementos = armaService.obtenerElementosProtocolo();
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (InspeccionableEnPartida elemento : elementos) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("protocoloInterfaz", InspeccionableEnPartida.class.getSimpleName());
            item.put("claseConcreta", elemento.getClass().getSimpleName());
            item.put("familiaJerarquia", (elemento instanceof Arma) ? "Arma" : "Agente");
            item.put("inspeccionEnPartida", elemento.inspeccionarEnPartida());
            item.put("valorEconomicoCS2", elemento.calcularValorEconomico());
            respuesta.add(item);
        }
        return ResponseEntity.ok(respuesta);
    }

    // ==========================================
    // Simulación Persistida sobre Entidades Guardadas en H2 (Arma, Agente y Equipo)
    // ==========================================

    @PostMapping("/api/armas/{id:\\d+}/simular")
    public ResponseEntity<Map<String, Object>> simularCombatePost(
            @PathVariable Long id,
            @RequestParam(name = "agenteObjetivoId", required = false) Long agenteObjetivoId,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @RequestParam(name = "distancia", defaultValue = "20") int distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot) {

        Map<String, Object> resultado = armaService.simularCombateConEntidadesPersistidas(
                id, agenteObjetivoId, equipoObjetivo, distancia, headshot
        );
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/api/armas/{id:\\d+}/recargar")
    public ResponseEntity<Map<String, Object>> recargarArmaPersistida(
            @PathVariable Long id,
            @RequestParam(name = "balas", defaultValue = "10") int balas) {
        return ResponseEntity.ok(armaService.recargarArmaPersistida(id, balas));
    }

    @PostMapping({"/api/armas/granadas/simular", "/api/armas/explosivos/simular"})
    public ResponseEntity<Map<String, Object>> simularExplosionGranadaPost(
            @RequestParam(name = "granadaId", required = false) Long granadaId,
            @RequestParam(name = "agenteObjetivoId", required = false) Long agenteObjetivoId,
            @RequestParam(name = "equipoAtacante", defaultValue = "Counter-Terrorista") String equipoAtacante,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @RequestParam(name = "distancia", defaultValue = "2.0") double distancia) {

        Map<String, Object> resultado = armaService.simularExplosionGranada(
                granadaId, agenteObjetivoId, equipoAtacante, equipoObjetivo, distancia
        );
        return ResponseEntity.ok(resultado);
    }

    @GetMapping({"/api/armas/granadas/simular", "/api/armas/explosivos/simular"})
    public ResponseEntity<Map<String, Object>> simularExplosionGranadaGet(
            @RequestParam(name = "granadaId", required = false) Long granadaId,
            @RequestParam(name = "agenteObjetivoId", required = false) Long agenteObjetivoId,
            @RequestParam(name = "equipoAtacante", defaultValue = "Counter-Terrorista") String equipoAtacante,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @RequestParam(name = "distancia", defaultValue = "2.0") double distancia) {

        Map<String, Object> resultado = armaService.simularExplosionGranada(
                granadaId, agenteObjetivoId, equipoAtacante, equipoObjetivo, distancia
        );
        return ResponseEntity.ok(resultado);
    }

    // ==========================================
    // Endpoints de Dominio / Polimorfismo / Sobrecarga
    // ==========================================

    @GetMapping({"/api/armas", "/api/armas/construir"})
    public Map<String, Object> construirArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "municion", required = false) Integer municion,
            @RequestParam(name = "daño", required = false) Integer daño,
            @RequestParam(name = "equipo", required = false) String equipoNombre) {

        Arma arma = armaService.construirArmaValidada(nombre, daño, municion, equipoNombre);

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

    @GetMapping("/api/armas/polimorfismo")
    public List<Map<String, Object>> demostrarPolimorfismo() {
        List<Arma> armas = armaService.obtenerCatalogoPolimorfico();

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Arma arma : armas) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("arma", arma.getNombre());
            item.put("claseJava", arma.getClass().getSimpleName());
            item.put("tipoPadre", Arma.class.getSimpleName());
            item.put("municion", arma.getMunicion() + "/" + arma.getCapacidadCargador());
            item.put("comportamientoDeCombate", arma.comportamientoDeCombate());
            item.put("resultadoDisparo", arma.disparar());
            lista.add(item);
        }
        return lista;
    }

    @GetMapping("/api/armas/disparar")
    public Map<String, Object> dispararArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "distancia", required = false) Integer distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot) {
        return armaService.ejecutarDisparoSobrecargado(nombre, distancia, headshot);
    }

    @GetMapping("/api/agentes/polimorfismo")
    public List<Map<String, Object>> demostrarPolimorfismoAgentes() {
        List<Agente> agentes = armaService.obtenerAgentesPolimorfismo();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Agente agente : agentes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("agente", agente.getNombre());
            item.put("claseJava", agente.getClass().getSimpleName());
            item.put("tipoPadre", Agente.class.getSimpleName());
            item.put("equipo", agente.getEquipo().getNombre());
            item.put("vida", agente.getVida());
            item.put("chalecoKevlar", agente.getChalecoKevlar());
            item.put("rolTactico", agente.rolTactico());
            item.put("accionEspecial", agente.ejecutarAccionEspecial());
            lista.add(item);
        }
        return lista;
    }

    // ==========================================
    // Manejadores de Excepciones HTTP por Tipo (Consigna 8 POO-08)
    // ==========================================

    @ExceptionHandler(MunicionInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarMunicionInsuficiente(MunicionInsuficienteException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de munición violada");
        error.put("tipoExcepcion", MunicionInsuficienteException.class.getSimpleName());
        error.put("jerarquiaPadre", ReglaDeJuegoException.class.getSimpleName());
        error.put("reglaViolada", ex.getReglaViolada());
        error.put("arma", ex.getNombreArma());
        error.put("municionActual", ex.getMunicionActual());
        error.put("capacidadCargador", ex.getCapacidadCargador());
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(FuegoAmigoException.class)
    public ResponseEntity<Map<String, Object>> manejarFuegoAmigo(FuegoAmigoException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de fuego amigo violada");
        error.put("tipoExcepcion", FuegoAmigoException.class.getSimpleName());
        error.put("jerarquiaPadre", ReglaDeJuegoException.class.getSimpleName());
        error.put("reglaViolada", ex.getReglaViolada());
        error.put("equipo", ex.getNombreEquipo());
        error.put("accionBloqueada", ex.getAccionBloqueada());
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(ReglaDeJuegoException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaDeJuegoBase(ReglaDeJuegoException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de juego CS2 violada");
        error.put("tipoExcepcion", ex.getClass().getSimpleName());
        error.put("reglaViolada", ex.getReglaViolada());
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaViolada(IllegalArgumentException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de dominio violada");
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(NoSuchElementException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Recurso no encontrado");
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
