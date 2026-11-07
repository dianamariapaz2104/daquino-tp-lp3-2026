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
    // Endpoints CRUD REST (3 Capas + H2)
    // ==========================================

    /**
     * CREATE (POST /api/armas): Crea una nueva entidad Arma validando en el Service y persistiendo en H2.
     */
    @PostMapping("/api/armas")
    public ResponseEntity<Arma> crearArma(@RequestBody Arma arma) {
        Arma guardada = armaService.crearArma(arma);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    /**
     * READ ALL (GET /api/armas/todas): Lista todas las armas almacenadas en la base de datos H2.
     */
    @GetMapping("/api/armas/todas")
    public ResponseEntity<List<Arma>> listarTodas() {
        return ResponseEntity.ok(armaService.listarTodas());
    }

    /**
     * READ BY ID (GET /api/armas/{id}): Obtiene un arma por su ID desde la base de datos H2.
     */
    @GetMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Arma> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(armaService.obtenerPorId(id));
    }

    /**
     * UPDATE (PUT /api/armas/{id}): Actualiza un arma existente en H2 pasando por las validaciones de negocio.
     */
    @PutMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Arma> actualizarArma(@PathVariable Long id, @RequestBody Arma armaActualizada) {
        Arma actualizada = armaService.actualizarArma(id, armaActualizada);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * DELETE (DELETE /api/armas/{id}): Elimina un arma de la base de datos H2 por su ID.
     */
    @DeleteMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Map<String, Object>> eliminarArma(@PathVariable Long id) {
        armaService.eliminarArma(id);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Arma eliminada correctamente");
        respuesta.put("idEliminado", id);
        return ResponseEntity.ok(respuesta);
    }

    // ==========================================
    // Endpoint del Protocolo de Dominio POO-07 (Interfaz InspeccionableEnPartida)
    // ==========================================

    /**
     * Consigna 3 (POO-08): Endpoint de lectura que usa el tipo de la interfaz InspeccionableEnPartida
     * sobre instancias de familias distintas (Arma y Agente).
     */
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
    // Simulación Persistida con 2 Reglas del Juego y 3 Clases (Arma, Agente, Equipo)
    // ==========================================

    /**
     * Consigna 7 (POO-08): Simulación de combate y consumo de munición sobre un Arma persistida en H2.
     * Participan Arma, Agente y Equipo. Un GET posterior a /api/armas/{id} muestra la munición descontada en H2.
     */
    @PostMapping("/api/armas/{id:\\d+}/simular")
    public ResponseEntity<Map<String, Object>> simularCombatePost(
            @PathVariable Long id,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @RequestParam(name = "distancia", defaultValue = "20") int distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot,
            @RequestParam(name = "chaleco", defaultValue = "true") boolean chaleco,
            @RequestParam(name = "fuegoAmigo", defaultValue = "false") boolean fuegoAmigo) {

        Map<String, Object> resultado = armaService.simularCombateConArmaPersistida(
                id, equipoObjetivo, distancia, headshot, chaleco, fuegoAmigo
        );
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/api/armas/{id:\\d+}/simular")
    public ResponseEntity<Map<String, Object>> simularCombateGet(
            @PathVariable Long id,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @RequestParam(name = "distancia", defaultValue = "20") int distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot,
            @RequestParam(name = "chaleco", defaultValue = "true") boolean chaleco,
            @RequestParam(name = "fuegoAmigo", defaultValue = "false") boolean fuegoAmigo) {

        Map<String, Object> resultado = armaService.simularCombateConArmaPersistida(
                id, equipoObjetivo, distancia, headshot, chaleco, fuegoAmigo
        );
        return ResponseEntity.ok(resultado);
    }

    /**
     * Regla de carga de munición sobre un Arma persistida en H2.
     * Si el cargador ya está lleno o las balas <= 0, lanza MunicionInsuficienteException.
     */
    @PostMapping("/api/armas/{id:\\d+}/recargar")
    public ResponseEntity<Map<String, Object>> recargarArmaPersistida(
            @PathVariable Long id,
            @RequestParam(name = "balas", defaultValue = "10") int balas) {
        return ResponseEntity.ok(armaService.recargarArmaPersistida(id, balas));
    }

    /**
     * Consulta los Agentes persistidos en H2 tras las simulaciones.
     */
    @GetMapping("/api/agentes/todos")
    public ResponseEntity<List<Agente>> listarAgentesPersistidos() {
        return ResponseEntity.ok(armaService.listarAgentes());
    }

    @GetMapping("/api/agentes/{id:\\d+}")
    public ResponseEntity<Agente> obtenerAgentePorId(@PathVariable Long id) {
        return ResponseEntity.ok(armaService.obtenerAgentePorId(id));
    }

    // ==========================================
    // Endpoints de Dominio / Polimorfismo / Sobrecarga
    // ==========================================

    /**
     * Construye una instancia del dominio a partir de parámetros de la URL,
     * la valida mediante ArmaService y la persiste en H2.
     */
    @GetMapping({"/api/armas", "/api/armas/construir"})
    public Map<String, Object> construirArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "municion", required = false) Integer municion,
            @RequestParam(name = "daño", required = false) Integer daño,
            @RequestParam(name = "equipo", required = false) String equipoNombre) {

        Arma arma = armaService.construirYGuardarArma(nombre, daño, municion, equipoNombre);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", arma.getId());
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
     * Respuesta en JSON al pedirle el mensaje abstracto a cada clase hija.
     */
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

    /**
     * Demuestra la sobrecarga del mensaje del dominio.
     */
    @GetMapping("/api/armas/disparar")
    public Map<String, Object> dispararArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "distancia", required = false) Integer distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot) {

        Arma arma = armaService.fabricarArma(nombre, null, null, new Equipo("Counter-Terrorista"));

        String resultadoAccion;
        String metodoSobrecargadoUsado;

        if (distancia == null) {
            resultadoAccion = arma.disparar();
            metodoSobrecargadoUsado = "disparar() [sin argumentos]";
        } else {
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
     * Regla de Negocio en Counter-Strike 2 (Simulación de Granadas / Explosivos):
     * Participan 3 clases del modelo (Equipo, Agente y GranadaHE) validando fuego amigo,
     * radio de onda expansiva (5.0m), absorción de chaleco Kevlar y persistencia en H2.
     */
    @GetMapping({"/api/armas/granadas/simular", "/api/armas/explosivos/simular"})
    public ResponseEntity<Map<String, Object>> simularExplosionGranada(
            @RequestParam(name = "equipoAtacante", defaultValue = "Counter-Terrorista") String equipoAtacante,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @RequestParam(name = "distancia", defaultValue = "2.0") double distancia,
            @RequestParam(name = "chaleco", defaultValue = "true") boolean chaleco,
            @RequestParam(name = "fuegoAmigo", defaultValue = "false") boolean fuegoAmigo,
            @RequestParam(name = "dañoBase", defaultValue = "98") int dañoBase) {

        Map<String, Object> resultado = armaService.simularExplosionGranada(
                equipoAtacante, equipoObjetivo, distancia, chaleco, fuegoAmigo, dañoBase
        );
        return ResponseEntity.ok(resultado);
    }

    /**
     * Demuestra la segunda jerarquía del dominio (Agente -> AgenteCT y AgenteTerrorista).
     */
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

    /**
     * Handler HTTP para la Excepción Hija 1: MunicionInsuficienteException (409 Conflict).
     */
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

    /**
     * Handler HTTP para la Excepción Hija 2: FuegoAmigoException (422 Unprocessable Entity).
     */
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

    /**
     * Handler HTTP para la clase base de excepciones del dominio: ReglaDeJuegoException.
     */
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

    /**
     * Handler HTTP para invariantes de construcción o argumentos inválidos (400 Bad Request).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaViolada(IllegalArgumentException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de dominio violada");
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handler HTTP para recursos inexistentes en H2 (404 Not Found).
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(NoSuchElementException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Recurso no encontrado");
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
