package py.edu.uc.lp3.rest.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import py.edu.uc.lp3.domain.FuegoAmigoException;
import py.edu.uc.lp3.domain.InspeccionableEnPartida;
import py.edu.uc.lp3.domain.MunicionInsuficienteException;
import py.edu.uc.lp3.domain.ReglaDeJuegoException;
import py.edu.uc.lp3.service.ArmaService;

@RestController
@Tag(name = "1. Arsenal, Agentes y Simulaciones CS2 (A1)", description = "CRUD en H2 de Armas y Agentes, Protocolo InspeccionableEnPartida y Reglas de Simulación de Combate/Explosivos")
public class ArmaController {

    private final ArmaService armaService;

    public ArmaController(ArmaService armaService) {
        this.armaService = armaService;
    }

    // ==========================================
    // Endpoints CRUD REST de Arma (3 Capas + H2)
    // ==========================================

    @Operation(
            summary = "Crear y persistir un Arma en H2",
            description = "Valida en ArmaService todos los invariantes (daño 1..500, precio 0..16000, munición 0..capacidadCargador, precisión 0..1, y en Granada radioExplosion > 0 y tiempoExplosion > 0) y guarda la entidad en H2."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Arma creada exitosamente en H2"),
            @ApiResponse(responseCode = "400", description = "Invariante o regla de negocio violada (ej. daño <= 0, precio > 16000, munición > cargador o radioExplosion <= 0)")
    })
    @PostMapping("/api/armas")
    public ResponseEntity<Arma> crearArma(@RequestBody Arma arma) {
        Arma guardada = armaService.crearArma(arma);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @Operation(summary = "Listar todas las armas persistidas en H2", description = "Devuelve la colección completa de armas guardadas en la tabla ARMAS de H2.")
    @ApiResponse(responseCode = "200", description = "Lista de armas obtenida correctamente")
    @GetMapping("/api/armas/todas")
    public ResponseEntity<List<Arma>> listarTodas() {
        return ResponseEntity.ok(armaService.listarTodas());
    }

    @Operation(summary = "Obtener un arma por su ID en H2", description = "Busca un arma persistida en H2 por su clave primaria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arma encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe un arma con el ID indicado en H2")
    })
    @GetMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Arma> obtenerPorId(@Parameter(description = "ID del arma en H2") @PathVariable Long id) {
        return ResponseEntity.ok(armaService.obtenerPorId(id));
    }

    @Operation(
            summary = "Actualizar un arma existente en H2",
            description = "Valida los nuevos datos en ArmaService y actualiza el registro existente en H2. Si se omiten campos numéricos en el JSON, Jackson aplica los valores válidos por defecto del constructor de la subclase."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arma actualizada en H2"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos que violan un invariante"),
            @ApiResponse(responseCode = "404", description = "No existe el arma con ese ID en H2")
    })
    @PutMapping("/api/armas/{id:\\d+}")
    public ResponseEntity<Arma> actualizarArma(@PathVariable Long id, @RequestBody Arma armaActualizada) {
        Arma actualizada = armaService.actualizarArma(id, armaActualizada);
        return ResponseEntity.ok(actualizada);
    }

    @Operation(summary = "Eliminar un arma de H2 por su ID", description = "Verifica que el ID exista en H2 y elimina la fila de la tabla ARMAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arma eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe el arma con ese ID en H2")
    })
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

    @Operation(
            summary = "Crear y persistir un Agente (AgenteCT o AgenteTerrorista) en H2",
            description = "Valida en ArmaService los invariantes del agente (vida 0..100, chalecoKevlar 0..100, dinero 0..16000, nombre y equipo no vacíos) y lo guarda en la tabla AGENTES de H2."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agente creado en H2"),
            @ApiResponse(responseCode = "400", description = "Invariante de agente violado")
    })
    @PostMapping("/api/agentes")
    public ResponseEntity<Agente> crearAgente(@RequestBody Agente agente) {
        Agente guardado = armaService.crearAgente(agente);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @Operation(summary = "Listar todos los agentes persistidos en H2", description = "Devuelve todos los operadores tácticos guardados en la tabla AGENTES de H2.")
    @GetMapping({"/api/agentes", "/api/agentes/todos"})
    public ResponseEntity<List<Agente>> listarAgentesPersistidos() {
        return ResponseEntity.ok(armaService.listarAgentes());
    }

    @Operation(summary = "Obtener un agente por su ID en H2")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un agente con ese ID en H2")
    })
    @GetMapping("/api/agentes/{id:\\d+}")
    public ResponseEntity<Agente> obtenerAgentePorId(@PathVariable Long id) {
        return ResponseEntity.ok(armaService.obtenerAgentePorId(id));
    }

    @Operation(summary = "Actualizar un agente existente en H2")
    @PutMapping("/api/agentes/{id:\\d+}")
    public ResponseEntity<Agente> actualizarAgente(@PathVariable Long id, @RequestBody Agente agenteActualizado) {
        Agente actualizado = armaService.actualizarAgente(id, agenteActualizado);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Eliminar un agente de H2 por su ID")
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

    @Operation(
            summary = "Consultar protocolo polimórfico InspeccionableEnPartida (POO-07)",
            description = "Recorre polimórficamente instancias de dos familias distintas (Arma y Agente) usando únicamente el tipo de la interfaz InspeccionableEnPartida."
    )
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

    @Operation(
            summary = "Simular combate entre un Arma y un Agente guardados en H2 (Reglas 1 y 2)",
            description = """
                    Carga el `Arma` (`id`) y el `Agente` (`agenteObjetivoId`) desde H2, ejecuta las 2 reglas de negocio en `ArmaService` y persiste los nuevos estados en H2:
                    - **Regla 1 (Fuego Amigo y Munición):** Si el `Equipo` del `Arma` y el `Equipo` del `Agente` en H2 coinciden, lanza `FuegoAmigoException` (HTTP 422). Si `municion == 0`, lanza `MunicionInsuficienteException` (HTTP 409).
                    - **Regla 2 (Daño, Kevlar y Vida):** Descuenta 1 bala del `Arma`, calcula el daño por distancia y headshot (x4), absorbe 35% en `chalecoKevlar` y descuenta la `vida` del `Agente` en H2.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulación exitosa; munición del arma y vida/Kevlar del agente actualizados en H2"),
            @ApiResponse(responseCode = "400", description = "Distancia negativa o parámetro inválido"),
            @ApiResponse(responseCode = "404", description = "El ID del arma o del agente no existe en H2"),
            @ApiResponse(responseCode = "409", description = "MunicionInsuficienteException: el arma guardada en H2 tiene 0 proyectiles"),
            @ApiResponse(responseCode = "422", description = "FuegoAmigoException: el arma y el agente guardados en H2 pertenecen al mismo Equipo")
    })
    @PostMapping("/api/armas/{id:\\d+}/simular")
    public ResponseEntity<Map<String, Object>> simularCombatePost(
            @Parameter(description = "ID del Arma persistida en H2") @PathVariable Long id,
            @Parameter(description = "ID del Agente objetivo persistido en H2") @RequestParam(name = "agenteObjetivoId", required = false) Long agenteObjetivoId,
            @Parameter(description = "Equipo objetivo si no se indica agenteObjetivoId") @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @Parameter(description = "Distancia de disparo en metros (>= 0)") @RequestParam(name = "distancia", defaultValue = "20") int distancia,
            @Parameter(description = "Indica si el impacto es crítico en la cabeza (x4)") @RequestParam(name = "headshot", defaultValue = "false") boolean headshot) {

        Map<String, Object> resultado = armaService.simularCombateConEntidadesPersistidas(
                id, agenteObjetivoId, equipoObjetivo, distancia, headshot
        );
        return ResponseEntity.ok(resultado);
    }

    @Operation(
            summary = "Recargar proyectiles en un Arma persistida en H2 (Regla de Munición y Cargador)",
            description = "Valida en ArmaService que `balas > 0` y que el cargador no esté ya lleno (`municion < capacidadCargador`). Si falla, lanza `MunicionInsuficienteException` (HTTP 409 Conflict)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recarga aplicada y persistida en H2"),
            @ApiResponse(responseCode = "404", description = "Arma no encontrada en H2"),
            @ApiResponse(responseCode = "409", description = "MunicionInsuficienteException: balas <= 0 o cargador ya lleno")
    })
    @PostMapping("/api/armas/{id:\\d+}/recargar")
    public ResponseEntity<Map<String, Object>> recargarArmaPersistida(
            @PathVariable Long id,
            @RequestParam(name = "balas", defaultValue = "10") int balas) {
        return ResponseEntity.ok(armaService.recargarArmaPersistida(id, balas));
    }

    @Operation(
            summary = "Regla 1 para compañeros (POO-09): Validar munición y ráfaga (sin guardar en H2)",
            description = "Recibe `{\"cargador\": 3, \"disparos\": 2}` (o query params). Responde 200 OK si la ráfaga alcanza, o 409 Conflict (MunicionInsuficienteException / SIN_MUNICION) si no alcanza."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ráfaga válida; devuelve municionRestante"),
            @ApiResponse(responseCode = "409", description = "SIN_MUNICION: disparos > cargador o disparos <= 0")
    })
    @PostMapping("/api/reglas/municion")
    public ResponseEntity<Map<String, Object>> validarReglaMunicion(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(name = "cargador", defaultValue = "3") int cargadorParam,
            @RequestParam(name = "disparos", defaultValue = "2") int disparosParam) {

        int cargador = (body != null && body.get("cargador") instanceof Number n1) ? n1.intValue() : cargadorParam;
        int disparos = (body != null && body.get("disparos") instanceof Number n2) ? n2.intValue() : disparosParam;
        return ResponseEntity.ok(armaService.validarReglaMunicionParaCompaneros(cargador, disparos));
    }

    @Operation(
            summary = "Regla 2 para compañeros (POO-09): Validar daño con chaleco Kevlar y Fuego Amigo (sin guardar en H2)",
            description = "Recibe `{\"vida\": 100, \"armadura\": 100, \"danio\": 36, \"equipoAtacante\": \"Counter-Terrorista\", \"equipoObjetivo\": \"Terrorista\"}`. Responde 200 OK con la vida y armadura que quedan (Kevlar absorbe 35%), o 422 Unprocessable Entity si la vida ya es 0 o si hay fuego amigo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Impacto válido; devuelve vidaRestante y armaduraRestante"),
            @ApiResponse(responseCode = "422", description = "JUGADOR_ELIMINADO (vida <= 0) o FUEGO_AMIGO_BLOQUEADO (mismo equipo)")
    })
    @PostMapping({"/api/reglas/danio", "/api/reglas/fuego-amigo"})
    public ResponseEntity<Map<String, Object>> validarReglaDanio(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(name = "equipoAtacante", defaultValue = "Counter-Terrorista") String equipoAtacanteParam,
            @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivoParam,
            @RequestParam(name = "vida", defaultValue = "100") int vidaParam,
            @RequestParam(name = "armadura", defaultValue = "100") int armaduraParam,
            @RequestParam(name = "danio", defaultValue = "36") int danioParam) {

        String eqAtacante = (body != null && body.get("equipoAtacante") instanceof String s1) ? s1 : equipoAtacanteParam;
        String eqObjetivo = (body != null && body.get("equipoObjetivo") instanceof String s2) ? s2 : equipoObjetivoParam;
        int vida = (body != null && body.get("vida") instanceof Number n1) ? n1.intValue() : vidaParam;
        int armadura = (body != null && body.get("armadura") instanceof Number n2) ? n2.intValue() : armaduraParam;
        int danio = (body != null && body.get("danio") instanceof Number n3) ? n3.intValue() : danioParam;

        return ResponseEntity.ok(armaService.validarReglaDanioYFuegoAmigoParaCompaneros(eqAtacante, eqObjetivo, vida, armadura, danio));
    }

    @Operation(
            summary = "Simular detonación de GranadaHE contra un Agente en H2 (Reglas de Onda Expansiva y Fuego Amigo)",
            description = "Valida alcance de lanzamiento (0..35m), fuego amigo entre el Equipo de la GranadaHE y el Equipo del Agente (422), disponibilidad de la granada (409), atenuación por radio de explosión (5.0m) y absorción de Kevlar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detonación simulada y estados actualizados en H2"),
            @ApiResponse(responseCode = "400", description = "Distancia negativa o superior a 35m"),
            @ApiResponse(responseCode = "409", description = "MunicionInsuficienteException: la granada ya fue detonada (0/1)"),
            @ApiResponse(responseCode = "422", description = "FuegoAmigoException: lanzamiento contra aliado del mismo equipo")
    })
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

    @Operation(summary = "Simular detonación de GranadaHE (GET de compatibilidad)")
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

    @Operation(summary = "Construir y validar un Arma desde parámetros de URL (sin insertar en H2)")
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

    @Operation(summary = "Demostrar polimorfismo y sobreescritura en la jerarquía Arma")
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

    @Operation(summary = "Demostrar sobrecarga del método disparar() con distancia y headshot")
    @GetMapping("/api/armas/disparar")
    public Map<String, Object> dispararArma(
            @RequestParam(name = "nombre", defaultValue = "M4A4") String nombre,
            @RequestParam(name = "distancia", required = false) Integer distancia,
            @RequestParam(name = "headshot", defaultValue = "false") boolean headshot) {
        return armaService.ejecutarDisparoSobrecargado(nombre, distancia, headshot);
    }

    @Operation(summary = "Demostrar polimorfismo y sobreescritura en la jerarquía Agente")
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
