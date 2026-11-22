package py.edu.uc.lp3.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.edu.uc.lp3.domain.Agente;
import py.edu.uc.lp3.domain.AgenteTerrorista;
import py.edu.uc.lp3.domain.Arma;
import py.edu.uc.lp3.domain.DesertEagle;
import py.edu.uc.lp3.domain.Equipo;
import py.edu.uc.lp3.domain.JugadaEscenario;
import py.edu.uc.lp3.repository.AgenteRepository;
import py.edu.uc.lp3.repository.ArmaRepository;
import py.edu.uc.lp3.repository.JugadaEscenarioRepository;

/**
 * Servicio orquestador del escenario de APIs cruzadas (POO-09).
 * Consulta en orden justificado las reglas de A2 y A3 a través de CompaneroApiClient
 * y solo persiste la jugada en H2 si ambas APIs remotas responden 2xx.
 */
@Service
@Transactional
public class EscenarioCruzadoService {

    private static final Logger log = LoggerFactory.getLogger(EscenarioCruzadoService.class);

    private final CompaneroApiClient companeroApiClient;
    private final ArmaService armaService;
    private final ArmaRepository armaRepository;
    private final AgenteRepository agenteRepository;
    private final JugadaEscenarioRepository jugadaEscenarioRepository;

    public EscenarioCruzadoService(
            CompaneroApiClient companeroApiClient,
            ArmaService armaService,
            ArmaRepository armaRepository,
            AgenteRepository agenteRepository,
            JugadaEscenarioRepository jugadaEscenarioRepository) {
        this.companeroApiClient = companeroApiClient;
        this.armaService = armaService;
        this.armaRepository = armaRepository;
        this.agenteRepository = agenteRepository;
        this.jugadaEscenarioRepository = jugadaEscenarioRepository;
    }

    /**
     * Ejecuta el escenario propio de A1 ("Asalto Táctico Coordinado en Ronda Competitiva"):
     * 1. Consulta a A2 (validación de ráfaga y consumo de proyectiles: {"cargador": 7, "disparos": 2} / ?balas=2).
     * 2. Consulta a A3 (validación de daño con chaleco: POST /api/reglas/danio con {"vida": 100, "armadura": 100, "danio": 36}).
     * 3. Solo si A2 y A3 aceptan (2xx), ejecuta la simulación local en A1 y guarda la JugadaEscenario en H2.
     *    Si A2 o A3 rechaza, lanza RechazoCompaneroException sin modificar el H2 de A1.
     */
    public JugadaEscenario ejecutarEscenarioAsaltoCoordinado(
            Long armaIdLocal,
            Long agenteObjetivoIdLocal,
            int cargadorDisponible,
            int balasDisparo,
            int vidaObjetivo,
            int armaduraObjetivo,
            int danioPropuesto,
            double distanciaExplosionMetros,
            String equipoObjetivo,
            boolean headshot) {

        log.debug("[POO-09][Escenario A1] Iniciando escenario 'Asalto Táctico Coordinado' (cargador={}, balas={}, vidaObj={}, armaduraObj={}, danio={}, distancia={}m)",
                cargadorDisponible, balasDisparo, vidaObjetivo, armaduraObjetivo, danioPropuesto, distanciaExplosionMetros);

        // Paso 1: Consultar validación remota de A2 (Matias Enciso: GET /api/simulacion/explosion?distancia=2.5&conBlindaje=true&fuegoAmigo=false)
        Map<String, Object> respuestaA2 = companeroApiClient.validarReglaEnA2(
                cargadorDisponible,
                balasDisparo,
                equipoObjetivo,
                distanciaExplosionMetros
        );

        // Paso 2: Consultar validación remota de A3 (Regla de daño con chaleco de A3: POST /api/reglas/danio)
        Map<String, Object> respuestaA3 = companeroApiClient.validarReglaEnA3(
                vidaObjetivo,
                armaduraObjetivo,
                danioPropuesto,
                cargadorDisponible,
                balasDisparo,
                distanciaExplosionMetros,
                equipoObjetivo
        );

        log.debug("[POO-09][Escenario A1] Tanto A2 como A3 aprobaron la jugada. Procediendo a persistir en el H2 de A1.");

        // Paso 3: Resolver entidades locales en el H2 de A1 y aplicar impacto
        Arma armaLocal = resolverArmaLocalParaEscenario(armaIdLocal);
        Agente agenteLocal = resolverAgenteLocalParaEscenario(agenteObjetivoIdLocal, equipoObjetivo);

        Map<String, Object> simulacionLocal = armaService.simularCombateConEntidadesPersistidas(
                armaLocal.getId(),
                agenteLocal.getId(),
                equipoObjetivo,
                Math.max(0, (int) Math.round(distanciaExplosionMetros)),
                headshot
        );

        int dañoAplicado = ((Number) simulacionLocal.get("dañoInfligido")).intValue();
        int municionRestante = ((Number) simulacionLocal.get("municionDespues")).intValue();
        int vidaRestante = ((Number) simulacionLocal.get("vidaObjetivoDespues")).intValue();

        JugadaEscenario jugada = new JugadaEscenario(
                "Asalto Táctico Coordinado CS2 (A1 + A2 + A3)",
                armaLocal.getId(),
                armaLocal.getNombre(),
                agenteLocal.getId(),
                agenteLocal.getNombre(),
                balasDisparo,
                distanciaExplosionMetros,
                dañoAplicado,
                municionRestante,
                vidaRestante,
                "A2 ACEPTÓ (" + respuestaA2.get("nombre") + ")",
                "A3 ACEPTÓ (" + respuestaA3.get("nombre") + ")",
                "JUGADA PERSISTIDA EN H2 DE A1: Daño=" + dañoAplicado + ", Munición restante=" + municionRestante + ", Vida objetivo=" + vidaRestante
        );

        JugadaEscenario guardada = jugadaEscenarioRepository.save(jugada);
        log.debug("[POO-09][Escenario A1] JugadaEscenario persistida exitosamente en H2 con ID={}", guardada.getId());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<JugadaEscenario> listarJugadasPersistidas() {
        return jugadaEscenarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public JugadaEscenario obtenerJugadaPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la jugada debe ser positivo.");
        }
        return jugadaEscenarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la jugada de escenario con ID: " + id));
    }

    private Arma resolverArmaLocalParaEscenario(Long armaIdLocal) {
        if (armaIdLocal != null) {
            return armaService.obtenerPorId(armaIdLocal);
        }
        List<Arma> todas = armaRepository.findAll();
        for (Arma a : todas) {
            if (a.puedeDisparar()) {
                return a;
            }
        }
        return armaRepository.save(new DesertEagle(new Equipo("Counter-Terrorista")));
    }

    private Agente resolverAgenteLocalParaEscenario(Long agenteIdLocal, String equipoObjetivo) {
        if (agenteIdLocal != null) {
            return armaService.obtenerAgentePorId(agenteIdLocal);
        }
        String eq = (equipoObjetivo != null && !equipoObjetivo.trim().isEmpty()) ? equipoObjetivo.trim() : "Terrorista";
        List<Agente> existentes = agenteRepository.findByEquipo_NombreIgnoreCase(eq);
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }
        return agenteRepository.save(new AgenteTerrorista("Agente Phoenix (T)", 100, 100, 800, true));
    }
}
