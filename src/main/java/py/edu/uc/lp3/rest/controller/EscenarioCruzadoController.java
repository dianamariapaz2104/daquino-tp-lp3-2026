package py.edu.uc.lp3.rest.controller;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import py.edu.uc.lp3.domain.FuegoAmigoException;
import py.edu.uc.lp3.domain.JugadaEscenario;
import py.edu.uc.lp3.domain.MunicionInsuficienteException;
import py.edu.uc.lp3.domain.RechazoCompaneroException;
import py.edu.uc.lp3.domain.ReglaDeJuegoException;
import py.edu.uc.lp3.service.CompaneroApiClient;
import py.edu.uc.lp3.service.EscenarioCruzadoService;

@RestController
@Tag(name = "2. Escenario de APIs Cruzadas (POO-09: A1 -> A2 y A3)", description = "Orquestación del escenario propio consultando las reglas de A2 y A3, persistencia en H2 y cliente externo")
public class EscenarioCruzadoController {

    private final EscenarioCruzadoService escenarioCruzadoService;
    private final CompaneroApiClient companeroApiClient;

    public EscenarioCruzadoController(
            EscenarioCruzadoService escenarioCruzadoService,
            CompaneroApiClient companeroApiClient) {
        this.escenarioCruzadoService = escenarioCruzadoService;
        this.companeroApiClient = companeroApiClient;
    }

    @Operation(
            summary = "Ejecutar escenario propio ('Asalto Táctico Coordinado') consultando las reglas de A2 y A3",
            description = """
                    Orquesta el escenario de Counter-Strike 2 en 3 pasos:
                    1. Consulta la API del compañero **A2** para validar la regla de disparo y munición (`balasDisparo`).
                    2. Si A2 acepta, consulta la API del compañero **A3** para validar la regla de impacto explosivo y blindaje (`distanciaExplosion`).
                    3. **Solo si A2 y A3 responden 2xx**, ejecuta el impacto local en A1 y **persiste la `JugadaEscenario` en H2** (HTTP 201 Created).
                    Si A2 o A3 rechaza, la base de datos H2 de A1 queda intacta y la respuesta indica explícitamente de quién fue el rechazo (`"rechazadoPor": "A2"` o `"A3"`).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "A2 y A3 aceptaron la jugada; escenario persistido exitosamente en el H2 de A1"),
            @ApiResponse(responseCode = "400", description = "Rechazo de regla en A2 o A3 por parámetro inválido, dejando el H2 de A1 sin cambios"),
            @ApiResponse(responseCode = "409", description = "Rechazo en A2 o A3 por munición/carga insuficiente, dejando el H2 de A1 sin cambios"),
            @ApiResponse(responseCode = "422", description = "Rechazo por regla de fuego amigo, dejando el H2 de A1 sin cambios")
    })
    @PostMapping("/api/escenario/jugadas")
    public ResponseEntity<JugadaEscenario> ejecutarEscenario(
            @Parameter(description = "ID del Arma local en el H2 de A1 (opcional)") @RequestParam(name = "armaIdLocal", required = false) Long armaIdLocal,
            @Parameter(description = "ID del Agente objetivo en el H2 de A1 (opcional)") @RequestParam(name = "agenteObjetivoIdLocal", required = false) Long agenteObjetivoIdLocal,
            @Parameter(description = "Cantidad de balas para validar en A2 (> 0 es válido; <= 0 provoca rechazo de A2)") @RequestParam(name = "balasDisparo", defaultValue = "2") int balasDisparo,
            @Parameter(description = "Distancia en metros para validar en A3 (>= 0 es válido; < 0 provoca rechazo de A3)") @RequestParam(name = "distanciaExplosion", defaultValue = "2.0") double distanciaExplosion,
            @Parameter(description = "Equipo objetivo de la jugada") @RequestParam(name = "equipoObjetivo", defaultValue = "Terrorista") String equipoObjetivo,
            @Parameter(description = "Indica si el disparo local es headshot") @RequestParam(name = "headshot", defaultValue = "true") boolean headshot) {

        JugadaEscenario guardada = escenarioCruzadoService.ejecutarEscenarioAsaltoCoordinado(
                armaIdLocal,
                agenteObjetivoIdLocal,
                balasDisparo,
                distanciaExplosion,
                equipoObjetivo,
                headshot
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @Operation(
            summary = "Listar todas las jugadas del escenario persistidas en el H2 de A1",
            description = "Permite comprobar mediante GET que las jugadas aprobadas por A2 y A3 quedaron guardadas en H2, y que los rechazos de A2 o A3 dejaron la tabla intacta."
    )
    @ApiResponse(responseCode = "200", description = "Lista de jugadas persistidas en H2")
    @GetMapping("/api/escenario/jugadas")
    public ResponseEntity<List<JugadaEscenario>> listarJugadas() {
        return ResponseEntity.ok(escenarioCruzadoService.listarJugadasPersistidas());
    }

    @Operation(summary = "Obtener una jugada persistida del escenario por su ID en H2")
    @GetMapping("/api/escenario/jugadas/{id:\\d+}")
    public ResponseEntity<JugadaEscenario> obtenerJugadaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(escenarioCruzadoService.obtenerJugadaPorId(id));
    }

    @Operation(summary = "Ver mapa de configuración del grupo Round Robin (A1, A2 y A3)")
    @GetMapping("/api/escenario/grupo")
    public ResponseEntity<Map<String, Object>> verConfiguracionGrupo() {
        return ResponseEntity.ok(companeroApiClient.obtenerConfiguracionGrupo());
    }

    // ==========================================
    // Endpoints de Práctica en Clase (Mapa en Memoria + Cliente HTTP restful-api.dev)
    // ==========================================

    @Operation(
            summary = "Guardar datos tácticos en el Map en memoria del Service",
            description = "Endpoint en capas (Controller -> Service) para almacenar entradas clave-valor en un Map."
    )
    @PostMapping("/api/escenario/mapa-data")
    public ResponseEntity<Map<String, Object>> guardarEnMapa(
            @RequestParam(name = "clave", defaultValue = "config_ronda") String clave,
            @RequestBody Map<String, Object> datos) {
        Map<String, Object> guardado = companeroApiClient.guardarEnMapaData(clave, datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @Operation(summary = "Consultar el Map completo de datos en memoria")
    @GetMapping("/api/escenario/mapa-data")
    public ResponseEntity<Map<String, Map<String, Object>>> obtenerMapa() {
        return ResponseEntity.ok(companeroApiClient.obtenerMapaDataCompleto());
    }

    @Operation(
            summary = "Consultar objetos de API REST externa (https://api.restful-api.dev/objects)",
            description = "Utiliza el cliente HTTP de Spring Boot en la capa Service para consultar la API externa y guardar los objetos en el Map."
    )
    @GetMapping("/api/escenario/externo/objetos")
    public ResponseEntity<List<Map<String, Object>>> consultarApiExterna() {
        return ResponseEntity.ok(companeroApiClient.consultarApiExternaRestfulDev());
    }

    // ==========================================
    // Manejadores de Excepciones con Atribución de Compañero (A2 vs A3)
    // ==========================================

    @ExceptionHandler(RechazoCompaneroException.class)
    public ResponseEntity<Map<String, Object>> manejarRechazoCompanero(RechazoCompaneroException ex) {
        int codigo = (ex.getCodigoHttpCompanero() >= 400 && ex.getCodigoHttpCompanero() < 600)
                ? ex.getCodigoHttpCompanero()
                : HttpStatus.BAD_GATEWAY.value();

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Escenario rechazado por servicio de compañero");
        error.put("rechazadoPor", ex.getCompaneroRol());
        error.put("nombreCompanero", ex.getCompaneroNombre());
        error.put("urlConsultada", ex.getUrlConsultada());
        error.put("codigoHttpCompanero", ex.getCodigoHttpCompanero());
        error.put("tipoExcepcion", RechazoCompaneroException.class.getSimpleName());
        error.put("jerarquiaPadre", ReglaDeJuegoException.class.getSimpleName());
        error.put("reglaViolada", ex.getReglaViolada());
        error.put("detalleRechazoCompanero", ex.getDetalleCompanero());
        error.put("estadoH2Local", "SIN_CAMBIOS (La base de datos H2 de A1 quedó igual que antes)");
        error.put("mensaje", ex.getMessage());
        error.put("estado", codigo);
        return ResponseEntity.status(codigo).body(error);
    }

    @ExceptionHandler(MunicionInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarMunicionInsuficiente(MunicionInsuficienteException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de munición violada en A1");
        error.put("rechazadoPor", "A1");
        error.put("tipoExcepcion", MunicionInsuficienteException.class.getSimpleName());
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(FuegoAmigoException.class)
    public ResponseEntity<Map<String, Object>> manejarFuegoAmigo(FuegoAmigoException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Regla de fuego amigo violada en A1");
        error.put("rechazadoPor", "A1");
        error.put("tipoExcepcion", FuegoAmigoException.class.getSimpleName());
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
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
