package py.edu.uc.lp3.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.edu.uc.lp3.domain.Agente;
import py.edu.uc.lp3.domain.AgenteCT;
import py.edu.uc.lp3.domain.AgenteTerrorista;
import py.edu.uc.lp3.domain.Arma;
import py.edu.uc.lp3.domain.DesertEagle;
import py.edu.uc.lp3.domain.Equipo;
import py.edu.uc.lp3.domain.FuegoAmigoException;
import py.edu.uc.lp3.domain.Glock18;
import py.edu.uc.lp3.domain.Granada;
import py.edu.uc.lp3.domain.GranadaHE;
import py.edu.uc.lp3.domain.InspeccionableEnPartida;
import py.edu.uc.lp3.domain.M4A4;
import py.edu.uc.lp3.domain.MP9;
import py.edu.uc.lp3.domain.MunicionInsuficienteException;
import py.edu.uc.lp3.domain.Nova;
import py.edu.uc.lp3.repository.AgenteRepository;
import py.edu.uc.lp3.repository.ArmaRepository;

@Service
@Transactional
public class ArmaService {

    public static final int PRESUPUESTO_MAXIMO_CS2 = 16000;
    public static final int DAÑO_MAXIMO_PERMITIDO = 500;
    public static final double ALCANCE_MAXIMO_LANZAMIENTO_METROS = 35.0;

    private final ArmaRepository armaRepository;
    private final AgenteRepository agenteRepository;

    public ArmaService(ArmaRepository armaRepository, AgenteRepository agenteRepository) {
        this.armaRepository = armaRepository;
        this.agenteRepository = agenteRepository;
    }

    // ==========================================
    // Operaciones CRUD de Arma en H2
    // ==========================================

    /**
     * CREATE Arma: Valida todos los invariantes y reglas de negocio en el Service y persiste en H2.
     */
    public Arma crearArma(Arma arma) {
        validarReglasDeNegocio(arma);
        return armaRepository.save(arma);
    }

    /**
     * READ (Todas las Armas): Obtiene todas las armas persistidas en H2.
     */
    @Transactional(readOnly = true)
    public List<Arma> listarTodas() {
        return armaRepository.findAll();
    }

    /**
     * READ (Arma por ID): Busca un arma por su identificador único en H2.
     */
    @Transactional(readOnly = true)
    public Arma obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del arma debe ser un número positivo.");
        }
        return armaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el arma con ID: " + id));
    }

    /**
     * UPDATE Arma: Valida todos los invariantes (incluyendo radioExplosion/tiempoExplosion si es Granada)
     * y actualiza la entidad existente en H2.
     */
    public Arma actualizarArma(Long id, Arma datosActualizados) {
        Arma existente = obtenerPorId(id);
        validarReglasDeNegocio(datosActualizados);

        existente.aplicarEstadoValidado(
                datosActualizados.getNombre(),
                datosActualizados.getDaño(),
                datosActualizados.getPrecision(),
                datosActualizados.getTiempoRecarga(),
                datosActualizados.getPrecio(),
                datosActualizados.getEquipo(),
                datosActualizados.getMunicion(),
                datosActualizados.getCapacidadCargador()
        );

        if (existente instanceof Granada granadaExistente && datosActualizados instanceof Granada granadaNueva) {
            granadaExistente.aplicarParametrosExplosivoValidados(
                    granadaNueva.getRadioExplosion(),
                    granadaNueva.getTiempoExplosion()
            );
        }

        return armaRepository.save(existente);
    }

    /**
     * DELETE Arma: Elimina un arma de la base de datos H2 verificando previamente su existencia.
     */
    public void eliminarArma(Long id) {
        Arma existente = obtenerPorId(id);
        armaRepository.delete(existente);
    }

    // ==========================================
    // Operaciones CRUD de Agente en H2
    // ==========================================

    /**
     * CREATE Agente: Valida todos los invariantes del agente en el Service y lo persiste en H2.
     */
    public Agente crearAgente(Agente agente) {
        validarReglasDeAgente(agente);
        return agenteRepository.save(agente);
    }

    /**
     * READ (Todos los Agentes): Lista todos los agentes persistidos en H2.
     */
    @Transactional(readOnly = true)
    public List<Agente> listarAgentes() {
        return agenteRepository.findAll();
    }

    /**
     * READ (Agente por ID): Busca un agente por su ID en H2.
     */
    @Transactional(readOnly = true)
    public Agente obtenerAgentePorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del agente debe ser un número positivo.");
        }
        return agenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el agente con ID: " + id));
    }

    /**
     * UPDATE Agente: Valida invariantes y actualiza un agente existente en H2.
     */
    public Agente actualizarAgente(Long id, Agente datosActualizados) {
        Agente existente = obtenerAgentePorId(id);
        validarReglasDeAgente(datosActualizados);

        existente.aplicarEstadoValidado(
                datosActualizados.getNombre(),
                datosActualizados.getVida(),
                datosActualizados.getChalecoKevlar(),
                datosActualizados.getDinero(),
                datosActualizados.getEquipo()
        );
        return agenteRepository.save(existente);
    }

    /**
     * DELETE Agente: Elimina un agente de H2 por su ID.
     */
    public void eliminarAgente(Long id) {
        Agente existente = obtenerAgentePorId(id);
        agenteRepository.delete(existente);
    }

    // ==========================================
    // Validaciones de Invariantes y Reglas de Negocio (Exclusivas del Service)
    // ==========================================

    /**
     * Valida todos los invariantes y reglas de negocio de un Arma (y de Granada si corresponde)
     * antes de crear o actualizar en H2. Las entidades del dominio no lanzan excepciones.
     */
    public void validarReglasDeNegocio(Arma arma) {
        if (arma == null) {
            throw new IllegalArgumentException("Los datos del arma no pueden ser nulos.");
        }
        if (arma.getNombre() == null || arma.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del arma no puede ser nulo ni vacío.");
        }
        if (arma.getDaño() <= 0) {
            throw new IllegalArgumentException("El daño debe ser mayor a cero: " + arma.getDaño());
        }
        if (arma.getDaño() > DAÑO_MAXIMO_PERMITIDO) {
            throw new IllegalArgumentException(
                    "El daño (" + arma.getDaño() + ") supera el límite máximo de balance permitido (" + DAÑO_MAXIMO_PERMITIDO + ").");
        }
        if (arma.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo: " + arma.getPrecio());
        }
        if (arma.getPrecio() > PRESUPUESTO_MAXIMO_CS2) {
            throw new IllegalArgumentException(
                    "El precio ($" + arma.getPrecio() + ") supera el presupuesto máximo de una partida en CS2 ($" + PRESUPUESTO_MAXIMO_CS2 + ").");
        }
        if (arma.getCapacidadCargador() <= 0) {
            throw new IllegalArgumentException("La capacidad del cargador debe ser positiva: " + arma.getCapacidadCargador());
        }
        if (arma.getMunicion() < 0) {
            throw new IllegalArgumentException("La munición no puede ser negativa: " + arma.getMunicion());
        }
        if (arma.getMunicion() > arma.getCapacidadCargador()) {
            throw new IllegalArgumentException(
                    "La munición (" + arma.getMunicion() + ") no puede superar la capacidad del cargador (" + arma.getCapacidadCargador() + ").");
        }
        if (arma.getPrecision() < 0.0f || arma.getPrecision() > 1.0f) {
            throw new IllegalArgumentException("La precisión debe estar comprendida entre 0.0 y 1.0: " + arma.getPrecision());
        }
        if (arma.getTiempoRecarga() < 0.0f) {
            throw new IllegalArgumentException("El tiempo de recarga no puede ser negativo: " + arma.getTiempoRecarga());
        }
        if (arma.getEquipo() == null || arma.getEquipo().getNombre() == null || arma.getEquipo().getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo no puede ser nulo ni vacío.");
        }

        // Validación estricta de invariantes específicos de Granada (radioExplosion > 0 y tiempoExplosion > 0)
        if (arma instanceof Granada granada) {
            if (granada.getRadioExplosion() <= 0.0f) {
                throw new IllegalArgumentException(
                        "El radio de explosión de una granada debe ser mayor a cero: " + granada.getRadioExplosion());
            }
            if (granada.getTiempoExplosion() <= 0.0f) {
                throw new IllegalArgumentException(
                        "El tiempo de explosión de una granada debe ser mayor a cero: " + granada.getTiempoExplosion());
            }
        }
    }

    /**
     * Valida todos los invariantes de un Agente antes de crear o actualizar en H2.
     */
    public void validarReglasDeAgente(Agente agente) {
        if (agente == null) {
            throw new IllegalArgumentException("Los datos del agente no pueden ser nulos.");
        }
        if (agente.getNombre() == null || agente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del agente no puede ser nulo ni vacío.");
        }
        if (agente.getVida() < 0 || agente.getVida() > 100) {
            throw new IllegalArgumentException("La vida del agente debe estar entre 0 y 100 HP: " + agente.getVida());
        }
        if (agente.getChalecoKevlar() < 0 || agente.getChalecoKevlar() > 100) {
            throw new IllegalArgumentException("El chaleco Kevlar debe estar entre 0 y 100: " + agente.getChalecoKevlar());
        }
        if (agente.getDinero() < 0 || agente.getDinero() > PRESUPUESTO_MAXIMO_CS2) {
            throw new IllegalArgumentException("El dinero del agente debe estar entre $0 y $" + PRESUPUESTO_MAXIMO_CS2 + ": " + agente.getDinero());
        }
        if (agente.getEquipo() == null || agente.getEquipo().getNombre() == null || agente.getEquipo().getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo del agente no puede ser nulo ni vacío.");
        }
    }

    // ==========================================
    // Simulación sobre Entidades Persistidas en H2 (2 Reglas + 3 Clases: Arma, Agente, Equipo)
    // ==========================================

    /**
     * Simula un combate cargando tanto el Arma como el Agente objetivo desde la base de datos H2:
     * - Si se indica agenteObjetivoId, carga ese Agente persistido en H2 con obtenerAgentePorId(agenteObjetivoId).
     * - Si no se indica agenteObjetivoId, busca en H2 un Agente existente del equipo indicado.
     * - Regla 1 (Fuego Amigo y Munición): Compara el Equipo guardado en H2 del Arma con el Equipo guardado
     *   en H2 del Agente objetivo. Si pertenecen al mismo Equipo, lanza FuegoAmigoException (422).
     *   Si el Arma guardada en H2 tiene munición 0, lanza MunicionInsuficienteException (409).
     * - Regla 2 (Daño balístico, Kevlar y Vida): Descuenta 1 proyectil del Arma, calcula el daño con
     *   atenuación por distancia y headshot, absorbe el 35% en el chaleco Kevlar del Agente y descuenta su vida.
     * - Guarda los nuevos estados de ambas entidades en H2.
     */
    public Map<String, Object> simularCombateConEntidadesPersistidas(
            Long armaId,
            Long agenteObjetivoId,
            String equipoObjetivoFallback,
            int distanciaMetros,
            boolean tiroALaCabeza) {

        if (distanciaMetros < 0) {
            throw new IllegalArgumentException("La distancia de combate no puede ser negativa: " + distanciaMetros);
        }

        Arma arma = obtenerPorId(armaId);
        Agente agenteObjetivo = resolverAgentePersistidoEnH2(agenteObjetivoId, equipoObjetivoFallback);

        // Regla 1A: Fuego Amigo determinado exclusivamente por el estado guardado en H2 (Equipo del Arma vs Equipo del Agente)
        if (arma.getEquipo() != null && arma.getEquipo().esMismoEquipo(agenteObjetivo.getEquipo())) {
            throw new FuegoAmigoException(
                    arma.getEquipo().getNombre(),
                    "Ataque con " + arma.getNombre() + " (ID " + arma.getId() + ") contra aliado "
                            + agenteObjetivo.getNombre() + " (ID " + agenteObjetivo.getId() + ")",
                    "Regla de negocio CS2: Fuego amigo bloqueado. El arma '" + arma.getNombre()
                            + "' y el agente objetivo '" + agenteObjetivo.getNombre()
                            + "' pertenecen al mismo equipo guardado en H2 ('" + arma.getEquipo().getNombre() + "')."
            );
        }

        // Regla 1B: Disponibilidad de proyectiles en el cargador del Arma guardada en H2
        if (!arma.puedeDisparar()) {
            throw new MunicionInsuficienteException(
                    arma.getNombre(),
                    arma.getMunicion(),
                    arma.getCapacidadCargador(),
                    "Regla de negocio CS2: El arma '" + arma.getNombre()
                            + "' tiene el cargador vacío (0/" + arma.getCapacidadCargador() + ") en H2. Debe recargar antes de combatir."
            );
        }

        int municionAntes = arma.getMunicion();
        int vidaAntes = agenteObjetivo.getVida();
        int chalecoAntes = agenteObjetivo.getChalecoKevlar();

        // Regla 2: Consumo de proyectil, cálculo balístico y absorción en Kevlar y vida
        arma.consumirProyectil();

        double factorAtenuacion = Math.max(0.15, 1.0 - (distanciaMetros * (1.0 - arma.getPrecision()) / 100.0));
        int dañoInfligido = (int) Math.round(arma.getDaño() * factorAtenuacion);
        if (tiroALaCabeza) {
            dañoInfligido *= 4;
        }

        aplicarDañoAAgenteEnService(agenteObjetivo, dañoInfligido);

        Arma armaActualizada = armaRepository.save(arma);
        Agente agenteActualizado = agenteRepository.save(agenteObjetivo);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idArmaPersistida", armaActualizada.getId());
        resultado.put("arma", armaActualizada.getNombre());
        resultado.put("tipoArma", armaActualizada.getTipoArma());
        resultado.put("equipoArma", armaActualizada.getEquipo().getNombre());
        resultado.put("municionAntes", municionAntes);
        resultado.put("municionDespues", armaActualizada.getMunicion());
        resultado.put("capacidadCargador", armaActualizada.getCapacidadCargador());
        resultado.put("idAgenteObjetivoPersistido", agenteActualizado.getId());
        resultado.put("agenteObjetivo", agenteActualizado.getNombre());
        resultado.put("tipoAgenteObjetivo", agenteActualizado.getTipoAgente());
        resultado.put("equipoObjetivo", agenteActualizado.getEquipo().getNombre());
        resultado.put("distanciaMetros", distanciaMetros);
        resultado.put("tiroALaCabeza", tiroALaCabeza);
        resultado.put("dañoInfligido", dañoInfligido);
        resultado.put("vidaObjetivoAntes", vidaAntes);
        resultado.put("vidaObjetivoDespues", agenteActualizado.getVida());
        resultado.put("chalecoAntes", chalecoAntes);
        resultado.put("chalecoDespues", agenteActualizado.getChalecoKevlar());
        resultado.put("objetivoSigueVivo", agenteActualizado.estaVivo());
        resultado.put("clasesParticipantes", List.of(
                armaActualizada.getClass().getSimpleName() + " (jerarquía Arma)",
                agenteActualizado.getClass().getSimpleName() + " (jerarquía Agente)",
                Equipo.class.getSimpleName()
        ));
        return resultado;
    }

    /**
     * Ejecuta la regla de negocio de recarga de munición en el Service sobre un Arma persistida en H2.
     * Lanza MunicionInsuficienteException si balas <= 0 o si el cargador ya está lleno.
     */
    public Map<String, Object> recargarArmaPersistida(Long armaId, int balas) {
        Arma arma = obtenerPorId(armaId);

        if (balas <= 0) {
            throw new MunicionInsuficienteException(
                    arma.getNombre(),
                    arma.getMunicion(),
                    arma.getCapacidadCargador(),
                    "Regla de negocio CS2: La cantidad de proyectiles a cargar debe ser mayor a cero (recibido: " + balas + ")."
            );
        }
        if (arma.getMunicion() >= arma.getCapacidadCargador()) {
            throw new MunicionInsuficienteException(
                    arma.getNombre(),
                    arma.getMunicion(),
                    arma.getCapacidadCargador(),
                    "Regla de negocio CS2: El cargador de '" + arma.getNombre() + "' ya está al máximo de su capacidad ("
                            + arma.getMunicion() + "/" + arma.getCapacidadCargador() + ")."
            );
        }

        int municionAntes = arma.getMunicion();
        arma.recargar(balas);
        int balasCargadas = arma.getMunicion() - municionAntes;

        Arma actualizada = armaRepository.save(arma);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idArmaPersistida", actualizada.getId());
        resultado.put("arma", actualizada.getNombre());
        resultado.put("municionAntes", municionAntes);
        resultado.put("balasCargadas", balasCargadas);
        resultado.put("municionDespues", actualizada.getMunicion());
        resultado.put("capacidadCargador", actualizada.getCapacidadCargador());
        return resultado;
    }

    /**
     * Simula la explosión de una GranadaHE persistida en H2 contra un Agente objetivo persistido en H2.
     */
    public Map<String, Object> simularExplosionGranada(
            Long granadaId,
            Long agenteObjetivoId,
            String equipoAtacanteFallback,
            String equipoObjetivoFallback,
            double distanciaMetros) {

        if (distanciaMetros < 0) {
            throw new IllegalArgumentException(
                    "Regla de negocio CS2: La distancia al epicentro de la granada no puede ser negativa (" + distanciaMetros + "m).");
        }
        if (distanciaMetros > ALCANCE_MAXIMO_LANZAMIENTO_METROS) {
            throw new IllegalArgumentException(
                    "Regla de negocio CS2: La distancia (" + distanciaMetros + "m) supera el alcance físico máximo de lanzamiento de una granada (" + ALCANCE_MAXIMO_LANZAMIENTO_METROS + "m).");
        }

        GranadaHE granada = resolverGranadaPersistidaEnH2(granadaId, equipoAtacanteFallback);
        Agente agenteObjetivo = resolverAgentePersistidoEnH2(agenteObjetivoId, equipoObjetivoFallback);

        // Regla 1: Control de Fuego Amigo según los equipos guardados en H2
        if (granada.getEquipo() != null && granada.getEquipo().esMismoEquipo(agenteObjetivo.getEquipo())) {
            throw new FuegoAmigoException(
                    granada.getEquipo().getNombre(),
                    "Detonación de " + granada.getNombre() + " contra aliado " + agenteObjetivo.getNombre(),
                    "Regla de negocio CS2: Fuego amigo bloqueado en modo competitivo. La granada y el agente objetivo pertenecen al mismo equipo en H2 ('"
                            + granada.getEquipo().getNombre() + "')."
            );
        }

        if (!granada.puedeDisparar()) {
            throw new MunicionInsuficienteException(
                    granada.getNombre(),
                    granada.getMunicion(),
                    granada.getCapacidadCargador(),
                    "La Granada HE (ID " + granada.getId() + ") ya fue detonada (munición 0/" + granada.getCapacidadCargador() + ")."
            );
        }

        int vidaInicial = agenteObjetivo.getVida();
        int dañoFinal = granada.calcularDañoOndaExpansiva(distanciaMetros, agenteObjetivo.tieneChaleco());
        String reporteDetonacion = granada.detonarEnArea(distanciaMetros, agenteObjetivo.tieneChaleco(), agenteObjetivo.getEquipo(), false);

        aplicarDañoAAgenteEnService(agenteObjetivo, dañoFinal);

        GranadaHE guardadaEnH2 = armaRepository.save(granada);
        Agente objetivoGuardado = agenteRepository.save(agenteObjetivo);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idPersistidoH2", guardadaEnH2.getId());
        resultado.put("idAgenteObjetivoPersistido", objetivoGuardado.getId());
        resultado.put("explosivo", guardadaEnH2.getNombre());
        resultado.put("clasesParticipantes", List.of(
                objetivoGuardado.getClass().getSimpleName() + " (jerarquía Agente)",
                GranadaHE.class.getSimpleName() + " (jerarquía Arma/Granada)",
                Equipo.class.getSimpleName()
        ));
        resultado.put("equipoAtacante", guardadaEnH2.getEquipo().getNombre());
        resultado.put("equipoObjetivo", objetivoGuardado.getEquipo().getNombre());
        resultado.put("agenteObjetivo", objetivoGuardado.getNombre());
        resultado.put("vidaObjetivoAntes", vidaInicial);
        resultado.put("vidaObjetivoDespues", objetivoGuardado.getVida());
        resultado.put("chalecoRestanteObjetivo", objetivoGuardado.getChalecoKevlar());
        resultado.put("objetivoSigueVivo", objetivoGuardado.estaVivo());
        resultado.put("fuegoAmigoDetectado", false);
        resultado.put("distanciaEpicentroMetros", distanciaMetros);
        resultado.put("radioExplosionMetros", guardadaEnH2.getRadioExplosion());
        resultado.put("tiempoExplosionSegundos", guardadaEnH2.getTiempoExplosion());
        resultado.put("objetivoDentroDelRadio", distanciaMetros <= guardadaEnH2.getRadioExplosion());
        resultado.put("dañoBaseGranada", guardadaEnH2.getDaño());
        resultado.put("dañoFinalCalculado", dañoFinal);
        resultado.put("municionRestante", guardadaEnH2.getMunicion() + "/" + guardadaEnH2.getCapacidadCargador());
        resultado.put("reporteSimulacion", reporteDetonacion);
        return resultado;
    }

    /**
     * Regla de absorción de Kevlar (35%) y descuento de vida ejecutada en el Service.
     */
    private void aplicarDañoAAgenteEnService(Agente agente, int dañoFinal) {
        if (dañoFinal <= 0) {
            return;
        }
        int nuevoChaleco = agente.getChalecoKevlar();
        if (nuevoChaleco > 0) {
            int desgasteChaleco = Math.min(nuevoChaleco, (int) Math.round(dañoFinal * 0.35));
            nuevoChaleco -= desgasteChaleco;
        }
        int nuevaVida = Math.max(0, agente.getVida() - dañoFinal);
        agente.aplicarImpactoValidado(nuevaVida, nuevoChaleco);
    }

    /**
     * Obtiene un Agente ya persistido en H2 por su ID, o reutiliza/asegura el Agente persistido del equipo indicado.
     */
    private Agente resolverAgentePersistidoEnH2(Long agenteId, String equipoNombreFallback) {
        if (agenteId != null) {
            return obtenerAgentePorId(agenteId);
        }
        String nombreEquipo = (equipoNombreFallback != null && !equipoNombreFallback.trim().isEmpty())
                ? equipoNombreFallback.trim()
                : "Terrorista";

        List<Agente> existentes = agenteRepository.findByEquipo_NombreIgnoreCase(nombreEquipo);
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }

        Agente nuevo = (nombreEquipo.toLowerCase().contains("counter") || nombreEquipo.equalsIgnoreCase("CT"))
                ? new AgenteCT("Agente SAS (CT)", 100, 100, 800, true)
                : new AgenteTerrorista("Agente Phoenix (T)", 100, 100, 800, true);
        return agenteRepository.save(nuevo);
    }

    private GranadaHE resolverGranadaPersistidaEnH2(Long granadaId, String equipoAtacanteFallback) {
        if (granadaId != null) {
            Arma arma = obtenerPorId(granadaId);
            if (arma instanceof GranadaHE granadaHE) {
                return granadaHE;
            }
            throw new IllegalArgumentException("El arma con ID " + granadaId + " no es una GranadaHE.");
        }
        String eq = (equipoAtacanteFallback != null && !equipoAtacanteFallback.trim().isEmpty())
                ? equipoAtacanteFallback.trim()
                : "Counter-Terrorista";
        GranadaHE granada = new GranadaHE(98, 1, new Equipo(eq));
        validarReglasDeNegocio(granada);
        return armaRepository.save(granada);
    }

    // ==========================================
    // Protocolo de Dominio POO-07 (Interfaz InspeccionableEnPartida)
    // ==========================================

    @Transactional(readOnly = true)
    public List<InspeccionableEnPartida> obtenerElementosProtocolo() {
        List<InspeccionableEnPartida> elementos = new ArrayList<>();
        elementos.addAll(armaRepository.findAll());
        elementos.addAll(agenteRepository.findAll());
        boolean hayArma = elementos.stream().anyMatch(e -> e instanceof Arma);
        boolean hayAgente = elementos.stream().anyMatch(e -> e instanceof Agente);
        if (!hayArma) {
            elementos.add(new M4A4());
        }
        if (!hayAgente) {
            elementos.add(new AgenteCT("Sargento SAS (CT)", true));
            elementos.add(new AgenteTerrorista("Líder Phoenix (T)", true));
        }
        return elementos;
    }

    @Transactional(readOnly = true)
    public List<Agente> obtenerAgentesPolimorfismo() {
        return List.of(
                new AgenteCT("Sargento SAS (CT)", true),
                new AgenteTerrorista("Líder Phoenix (T)", true)
        );
    }

    // ==========================================
    // Fábrica y Métodos de Dominio (Sin efectos secundarios en GET)
    // ==========================================

    /**
     * Construye y valida una instancia del dominio desde parámetros URL sin ensuciar H2 en un GET.
     */
    @Transactional(readOnly = true)
    public Arma construirArmaValidada(String nombre, Integer daño, Integer municion, String equipoNombre) {
        Equipo equipo = (equipoNombre != null && !equipoNombre.trim().isEmpty())
                ? new Equipo(equipoNombre)
                : new Equipo("Counter-Terrorista");
        Arma arma = fabricarArma(nombre, daño, municion, equipo);
        validarReglasDeNegocio(arma);
        return arma;
    }

    @Transactional(readOnly = true)
    public List<Arma> obtenerCatalogoPolimorfico() {
        return List.of(
                new M4A4(),
                new MP9(),
                new DesertEagle(),
                new Glock18(),
                new Nova(),
                new GranadaHE()
        );
    }

    @Transactional(readOnly = true)
    public Arma fabricarArma(String nombre, Integer daño, Integer municion, Equipo equipo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'nombre' es requerido.");
        }
        if (equipo == null || equipo.getNombre() == null || equipo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo no puede ser nulo ni vacío.");
        }

        String n = nombre.trim();
        boolean usaPersonalizado = (daño != null || municion != null);

        Arma arma;
        if (n.equalsIgnoreCase("M4A4")) {
            arma = usaPersonalizado
                    ? new M4A4((daño != null) ? daño : 30, (municion != null) ? municion : 30, equipo)
                    : new M4A4(equipo);
        } else if (n.equalsIgnoreCase("MP9")) {
            arma = usaPersonalizado
                    ? new MP9((daño != null) ? daño : 24, (municion != null) ? municion : 30, equipo)
                    : new MP9(equipo);
        } else if (n.equalsIgnoreCase("DesertEagle") || n.equalsIgnoreCase("Deagle") || n.equalsIgnoreCase("Desert Eagle")) {
            arma = usaPersonalizado
                    ? new DesertEagle((daño != null) ? daño : 40, (municion != null) ? municion : 7, equipo)
                    : new DesertEagle(equipo);
        } else if (n.equalsIgnoreCase("Glock18") || n.equalsIgnoreCase("Glock") || n.equalsIgnoreCase("Glock-18")) {
            arma = usaPersonalizado
                    ? new Glock18((daño != null) ? daño : 20, (municion != null) ? municion : 20, equipo)
                    : new Glock18(equipo);
        } else if (n.equalsIgnoreCase("Nova")) {
            arma = usaPersonalizado
                    ? new Nova((daño != null) ? daño : 26, (municion != null) ? municion : 8, equipo)
                    : new Nova(equipo);
        } else if (n.equalsIgnoreCase("GranadaHE") || n.equalsIgnoreCase("Granada") || n.equalsIgnoreCase("Granada HE")) {
            arma = usaPersonalizado
                    ? new GranadaHE((daño != null) ? daño : 50, (municion != null) ? municion : 1, equipo)
                    : new GranadaHE(equipo);
        } else {
            throw new NoSuchElementException("Arma desconocida en el arsenal: " + nombre);
        }

        validarReglasDeNegocio(arma);
        return arma;
    }
}
