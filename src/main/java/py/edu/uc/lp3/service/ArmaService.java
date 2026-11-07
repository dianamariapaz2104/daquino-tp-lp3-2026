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
import py.edu.uc.lp3.domain.Glock18;
import py.edu.uc.lp3.domain.GranadaHE;
import py.edu.uc.lp3.domain.InspeccionableEnPartida;
import py.edu.uc.lp3.domain.M4A4;
import py.edu.uc.lp3.domain.MP9;
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
    // Operaciones CRUD con Persistencia en H2
    // ==========================================

    /**
     * CREATE: Valida las reglas de negocio y persiste una nueva Arma en la base de datos H2.
     */
    public Arma crearArma(Arma arma) {
        validarReglasDeNegocio(arma);
        return armaRepository.save(arma);
    }

    /**
     * READ (Todos): Obtiene todas las armas persistidas en la base de datos H2.
     */
    @Transactional(readOnly = true)
    public List<Arma> listarTodas() {
        return armaRepository.findAll();
    }

    /**
     * READ (Por ID): Busca un arma por su identificador único en H2.
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
     * UPDATE: Valida las reglas de negocio y actualiza un arma existente en H2.
     */
    public Arma actualizarArma(Long id, Arma datosActualizados) {
        Arma existente = obtenerPorId(id);
        validarReglasDeNegocio(datosActualizados);

        existente.setNombre(datosActualizados.getNombre().trim());
        existente.setDaño(datosActualizados.getDaño());
        existente.setPrecision(datosActualizados.getPrecision());
        existente.setTiempoRecarga(datosActualizados.getTiempoRecarga());
        existente.setPrecio(datosActualizados.getPrecio());
        existente.setEquipo(datosActualizados.getEquipo());
        existente.setCapacidadCargador(datosActualizados.getCapacidadCargador());
        existente.setMunicion(datosActualizados.getMunicion());

        return armaRepository.save(existente);
    }

    /**
     * DELETE: Elimina un arma de la base de datos H2 verificando previamente su existencia.
     */
    public void eliminarArma(Long id) {
        Arma existente = obtenerPorId(id);
        armaRepository.delete(existente);
    }

    /**
     * READ Agentes persistidos en H2.
     */
    @Transactional(readOnly = true)
    public List<Agente> listarAgentes() {
        return agenteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Agente obtenerAgentePorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del agente debe ser un número positivo.");
        }
        return agenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el agente con ID: " + id));
    }

    // ==========================================
    // Validaciones de la Capa de Negocio
    // ==========================================

    /**
     * Valida las reglas de negocio del dominio Counter-Strike 2 antes de persistir en H2.
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
            arma.setEquipo(new Equipo("Counter-Terrorista"));
        }
    }

    // ==========================================
    // Simulación Persistida con 2 Reglas del Dominio y 3 Clases (Arma, Agente, Equipo)
    // ==========================================

    /**
     * Simula un enfrentamiento de combate sobre un Arma ya persistida en H2:
     * Participan 3 clases del modelo: Arma (subclase concreta), Agente (AgenteCT o AgenteTerrorista) y Equipo.
     * Dispara las 2 reglas de negocio dentro de los objetos del dominio:
     *   1) Regla de munición y validación de fuego amigo entre Equipos (lanza FuegoAmigoException o MunicionInsuficienteException).
     *   2) Regla de daño balístico con atenuación por distancia, headshot y absorción en chaleco Kevlar y vida del Agente.
     * Persiste el nuevo estado del Arma (munición descontada) y del Agente (vida y Kevlar descontados) en H2.
     */
    public Map<String, Object> simularCombateConArmaPersistida(
            Long armaId,
            String equipoObjetivoNombre,
            int distanciaMetros,
            boolean tiroALaCabeza,
            boolean tieneChalecoKevlar,
            boolean permitirFuegoAmigo) {

        Arma arma = obtenerPorId(armaId);
        int municionAntes = arma.getMunicion();

        Equipo equipoObjetivo = new Equipo(equipoObjetivoNombre);
        Agente agenteObjetivo = crearAgentePorEquipo("Objetivo " + equipoObjetivo.getNombre(), equipoObjetivo, tieneChalecoKevlar);
        int vidaAntes = agenteObjetivo.getVida();
        int chalecoAntes = agenteObjetivo.getChalecoKevlar();

        // El dominio ejecuta las dos reglas y lanza MunicionInsuficienteException o FuegoAmigoException si se violan
        int dañoInfligido = arma.dispararContraAgente(agenteObjetivo, distanciaMetros, tiroALaCabeza, permitirFuegoAmigo);

        // Persistir los cambios de estado en H2
        Arma armaActualizada = armaRepository.save(arma);
        Agente agentePersistido = agenteRepository.save(agenteObjetivo);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idArmaPersistida", armaActualizada.getId());
        resultado.put("arma", armaActualizada.getNombre());
        resultado.put("tipoArma", armaActualizada.getTipoArma());
        resultado.put("equipoArma", armaActualizada.getEquipo().getNombre());
        resultado.put("municionAntes", municionAntes);
        resultado.put("municionDespues", armaActualizada.getMunicion());
        resultado.put("capacidadCargador", armaActualizada.getCapacidadCargador());
        resultado.put("idAgenteObjetivoPersistido", agentePersistido.getId());
        resultado.put("agenteObjetivo", agentePersistido.getNombre());
        resultado.put("tipoAgenteObjetivo", agentePersistido.getTipoAgente());
        resultado.put("equipoObjetivo", agentePersistido.getEquipo().getNombre());
        resultado.put("distanciaMetros", distanciaMetros);
        resultado.put("tiroALaCabeza", tiroALaCabeza);
        resultado.put("dañoInfligido", dañoInfligido);
        resultado.put("vidaObjetivoAntes", vidaAntes);
        resultado.put("vidaObjetivoDespues", agentePersistido.getVida());
        resultado.put("chalecoAntes", chalecoAntes);
        resultado.put("chalecoDespues", agentePersistido.getChalecoKevlar());
        resultado.put("objetivoSigueVivo", agentePersistido.estaVivo());
        resultado.put("clasesParticipantes", List.of(
                armaActualizada.getClass().getSimpleName() + " (jerarquía Arma)",
                agentePersistido.getClass().getSimpleName() + " (jerarquía Agente)",
                Equipo.class.getSimpleName()
        ));
        return resultado;
    }

    /**
     * Ejecuta la regla de negocio de carga de munición sobre un Arma persistida en H2.
     * Si el cargador ya está lleno o las balas <= 0, el dominio lanza MunicionInsuficienteException.
     */
    public Map<String, Object> recargarArmaPersistida(Long armaId, int balas) {
        Arma arma = obtenerPorId(armaId);
        int municionAntes = arma.getMunicion();
        int balasCargadas = arma.cargarMunicionValidada(balas);
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
     * Regla de Negocio conectada a REST donde participan 3 clases del modelo:
     * 1. GranadaHE (el explosivo concreto lanzado y persistido en H2)
     * 2. Agente (AgenteCT / AgenteTerrorista que recibe el impacto explosivo y se persiste en H2)
     * 3. Equipo (bando atacante vs bando objetivo para control de Fuego Amigo en CS2)
     */
    public Map<String, Object> simularExplosionGranada(
            String equipoAtacanteNombre,
            String equipoObjetivoNombre,
            double distanciaMetros,
            boolean tieneChalecoKevlar,
            boolean permitirFuegoAmigo,
            int dañoBase) {

        Equipo equipoAtacante = new Equipo(equipoAtacanteNombre);
        Equipo equipoObjetivo = new Equipo(equipoObjetivoNombre);

        if (distanciaMetros < 0) {
            throw new IllegalArgumentException(
                    "Regla de negocio CS2: La distancia al epicentro de la granada no puede ser negativa (" + distanciaMetros + "m).");
        }
        if (distanciaMetros > ALCANCE_MAXIMO_LANZAMIENTO_METROS) {
            throw new IllegalArgumentException(
                    "Regla de negocio CS2: La distancia (" + distanciaMetros + "m) supera el alcance físico máximo de lanzamiento de una granada (" + ALCANCE_MAXIMO_LANZAMIENTO_METROS + "m).");
        }

        Agente agenteAtacante = crearAgentePorEquipo("Atacante " + equipoAtacante.getNombre(), equipoAtacante, true);
        Agente agenteObjetivo = crearAgentePorEquipo("Objetivo " + equipoObjetivo.getNombre(), equipoObjetivo, tieneChalecoKevlar);

        GranadaHE granada = new GranadaHE(dañoBase, 1, equipoAtacante);
        validarReglasDeNegocio(granada);

        boolean esFuegoAmigo = equipoAtacante.esMismoEquipo(equipoObjetivo);
        int dañoOndaExpansiva = granada.calcularDañoOndaExpansiva(distanciaMetros, agenteObjetivo.tieneChaleco());
        int dañoFinal = (esFuegoAmigo && dañoOndaExpansiva > 0)
                ? (int) Math.round(dañoOndaExpansiva * 0.65)
                : dañoOndaExpansiva;

        // El método del dominio GranadaHE.detonarEnArea valida fuego amigo y munición, lanzando FuegoAmigoException o MunicionInsuficienteException
        String reporteDetonacion = granada.detonarEnArea(
                distanciaMetros,
                agenteObjetivo.tieneChaleco(),
                equipoObjetivo,
                permitirFuegoAmigo
        );

        int vidaInicial = agenteObjetivo.getVida();
        agenteObjetivo.recibirDañoExplosivo(dañoFinal);

        GranadaHE guardadaEnH2 = armaRepository.save(granada);
        agenteRepository.save(agenteAtacante);
        Agente objetivoGuardado = agenteRepository.save(agenteObjetivo);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idPersistidoH2", guardadaEnH2.getId());
        resultado.put("explosivo", guardadaEnH2.getNombre());
        resultado.put("clasesParticipantes", List.of(
                Agente.class.getSimpleName() + " (" + agenteObjetivo.getClass().getSimpleName() + ")",
                GranadaHE.class.getSimpleName() + " (hija de Granada/Arma)",
                Equipo.class.getSimpleName()
        ));
        resultado.put("equipoAtacante", equipoAtacante.getNombre());
        resultado.put("agenteAtacante", agenteAtacante.getNombre() + " - " + agenteAtacante.rolTactico());
        resultado.put("equipoObjetivo", equipoObjetivo.getNombre());
        resultado.put("agenteObjetivo", objetivoGuardado.getNombre());
        resultado.put("vidaObjetivoAntes", vidaInicial);
        resultado.put("vidaObjetivoDespues", objetivoGuardado.getVida());
        resultado.put("chalecoRestanteObjetivo", objetivoGuardado.getChalecoKevlar());
        resultado.put("objetivoSigueVivo", objetivoGuardado.estaVivo());
        resultado.put("fuegoAmigoDetectado", esFuegoAmigo);
        resultado.put("distanciaEpicentroMetros", distanciaMetros);
        resultado.put("radioExplosionMetros", guardadaEnH2.getRadioExplosion());
        resultado.put("tiempoExplosionSegundos", guardadaEnH2.getTiempoExplosion());
        resultado.put("objetivoDentroDelRadio", distanciaMetros <= guardadaEnH2.getRadioExplosion());
        resultado.put("objetivoConChalecoKevlar", tieneChalecoKevlar);
        resultado.put("dañoBaseGranada", guardadaEnH2.getDaño());
        resultado.put("dañoFinalCalculado", dañoFinal);
        resultado.put("municionRestante", guardadaEnH2.getMunicion() + "/" + guardadaEnH2.getCapacidadCargador());
        resultado.put("reporteSimulacion", reporteDetonacion);
        return resultado;
    }

    // ==========================================
    // Protocolo de Dominio POO-07 (Interfaz InspeccionableEnPartida)
    // ==========================================

    /**
     * Devuelve una lista polimórfica tipada con la interfaz InspeccionableEnPartida,
     * combinando instancias de dos familias distintas (Arma y Agente), incluyendo
     * los registros persistidos en H2.
     */
    @Transactional(readOnly = true)
    public List<InspeccionableEnPartida> obtenerElementosProtocolo() {
        List<InspeccionableEnPartida> elementos = new ArrayList<>();
        elementos.addAll(armaRepository.findAll());
        elementos.addAll(agenteRepository.findAll());
        if (elementos.isEmpty()) {
            elementos.add(new M4A4());
            elementos.add(new DesertEagle());
            elementos.add(new AgenteCT("Sargento SAS (CT)", true));
            elementos.add(new AgenteTerrorista("Líder Phoenix (T)", true));
        } else {
            // Garantizar que siempre haya representantes de ambas familias (Arma y Agente)
            boolean hayArma = elementos.stream().anyMatch(e -> e instanceof Arma);
            boolean hayAgente = elementos.stream().anyMatch(e -> e instanceof Agente);
            if (!hayArma) {
                elementos.add(new M4A4());
            }
            if (!hayAgente) {
                elementos.add(new AgenteCT("Sargento SAS (CT)", true));
                elementos.add(new AgenteTerrorista("Líder Phoenix (T)", true));
            }
        }
        return elementos;
    }

    public List<Agente> obtenerAgentesPolimorfismo() {
        return List.of(
                new AgenteCT("Sargento SAS (CT)", true),
                new AgenteTerrorista("Líder Phoenix (T)", true)
        );
    }

    private Agente crearAgentePorEquipo(String nombre, Equipo equipo, boolean conChaleco) {
        int kevlar = conChaleco ? 100 : 0;
        if (equipo.getNombre().toLowerCase().contains("counter") || equipo.getNombre().equalsIgnoreCase("CT")) {
            return new AgenteCT(nombre, 100, kevlar, 800, true);
        }
        return new AgenteTerrorista(nombre, 100, kevlar, 800, true);
    }

    // ==========================================
    // Fábrica y Métodos de Dominio
    // ==========================================

    public Arma construirYGuardarArma(String nombre, Integer daño, Integer municion, String equipoNombre) {
        Equipo equipo = (equipoNombre != null && !equipoNombre.trim().isEmpty())
                ? new Equipo(equipoNombre)
                : new Equipo("Counter-Terrorista");
        Arma arma = fabricarArma(nombre, daño, municion, equipo);
        validarReglasDeNegocio(arma);
        return armaRepository.save(arma);
    }

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

    public Arma fabricarArma(String nombre, Integer daño, Integer municion, Equipo equipo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'nombre' es requerido.");
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
        } else if (n.equalsIgnoreCase("DesertEagle") || n.equalsIgnoreCase("Deagle") || n.equalsIgnoreCase("Desert Eagle")) {
            if (usaPersonalizado) {
                int d = (daño != null) ? daño : 40;
                int m = (municion != null) ? municion : 7;
                return new DesertEagle(d, m, equipo);
            }
            return new DesertEagle(equipo);
        } else if (n.equalsIgnoreCase("Glock18") || n.equalsIgnoreCase("Glock") || n.equalsIgnoreCase("Glock-18")) {
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
        } else if (n.equalsIgnoreCase("GranadaHE") || n.equalsIgnoreCase("Granada") || n.equalsIgnoreCase("Granada HE")) {
            return new GranadaHE(equipo);
        } else {
            throw new NoSuchElementException("Arma desconocida en el arsenal: " + nombre);
        }
    }
}
