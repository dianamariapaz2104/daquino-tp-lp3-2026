package py.edu.uc.lp3.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Segunda jerarquía de clases del dominio Counter-Strike 2:
 * Modela a un Agente (Jugador/Operador táctico) que participa en la partida,
 * equipa armas/granadas y mantiene su estado (vida, chaleco Kevlar, dinero y equipo).
 * Las validaciones de invariantes y reglas de combate viven exclusivamente en ArmaService.
 */
@Entity
@Table(name = "agentes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_agente")
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "tipoAgente",
        visible = true,
        defaultImpl = AgenteCT.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AgenteCT.class, name = "AgenteCT"),
        @JsonSubTypes.Type(value = AgenteTerrorista.class, name = "AgenteTerrorista")
})
public abstract class Agente implements InspeccionableEnPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private int vida;
    private int chalecoKevlar;
    private int dinero;

    @Embedded
    private Equipo equipo;

    public Agente() {
        this("Operador Estándar", 100, 100, 800, new Equipo("Counter-Terrorista"));
    }

    public Agente(String nombre, Equipo equipo) {
        this(nombre, 100, 100, 800, equipo);
    }

    public Agente(String nombre, int vida, int chalecoKevlar, int dinero, Equipo equipo) {
        this.nombre = (nombre != null) ? nombre.trim() : "";
        this.vida = vida;
        this.chalecoKevlar = chalecoKevlar;
        this.dinero = dinero;
        this.equipo = (equipo != null) ? equipo : new Equipo("Counter-Terrorista");
    }

    // ==========================================
    // Métodos Abstractos (Sobreescritura en hijas)
    // ==========================================

    public abstract String rolTactico();

    public abstract String ejecutarAccionEspecial();

    // ==========================================
    // Implementación del Protocolo InspeccionableEnPartida (POO-07)
    // ==========================================

    @Override
    public String inspeccionarEnPartida() {
        String eqNombre = (equipo != null) ? equipo.getNombre() : "Sin equipo";
        return "[Inspección de Agente] " + nombre + " (" + eqNombre
                + ") | Vida: " + vida + " HP | Kevlar: " + chalecoKevlar
                + " | Rol: " + rolTactico();
    }

    @Override
    public int calcularValorEconomico() {
        return dinero + (tieneChaleco() ? 650 : 0);
    }

    // ==========================================
    // Consulta y Transiciones de Estado Controladas
    // ==========================================

    public boolean tieneChaleco() {
        return chalecoKevlar > 0;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    /**
     * Aplica el resultado de un impacto validado y calculado por ArmaService,
     * sin exponer un setVida público libre.
     */
    public void aplicarImpactoValidado(int nuevaVida, int nuevoChaleco) {
        this.vida = nuevaVida;
        this.chalecoKevlar = nuevoChaleco;
    }

    /**
     * Aplica un estado actualizado tras ser validado por ArmaService.
     */
    public void aplicarEstadoValidado(String nombre, int vida, int chalecoKevlar, int dinero, Equipo equipo) {
        this.nombre = (nombre != null) ? nombre.trim() : "";
        this.vida = vida;
        this.chalecoKevlar = chalecoKevlar;
        this.dinero = dinero;
        this.equipo = (equipo != null) ? equipo : new Equipo("Counter-Terrorista");
    }

    // Solo Getters públicos (sin setVida ni setters libres que rompan el ocultamiento)
    public Long getId() { return id; }
    public String getTipoAgente() { return this.getClass().getSimpleName(); }
    public String getNombre() { return nombre; }
    public int getVida() { return vida; }
    public int getChalecoKevlar() { return chalecoKevlar; }
    public int getDinero() { return dinero; }
    public Equipo getEquipo() { return equipo; }
}
