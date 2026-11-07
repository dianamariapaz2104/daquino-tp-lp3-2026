package py.edu.uc.lp3.domain;

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
 * equipa armas/granadas y recibe daño en las simulaciones de combate y explosivos.
 */
@Entity
@Table(name = "agentes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_agente")
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
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del agente no puede ser nulo ni vacío.");
        }
        if (vida < 0 || vida > 100) {
            throw new IllegalArgumentException("La vida del agente debe estar entre 0 y 100 HP: " + vida);
        }
        if (chalecoKevlar < 0 || chalecoKevlar > 100) {
            throw new IllegalArgumentException("El chaleco Kevlar debe estar entre 0 y 100: " + chalecoKevlar);
        }
        if (dinero < 0 || dinero > 16000) {
            throw new IllegalArgumentException("El dinero del agente debe estar entre $0 y $16,000: " + dinero);
        }

        this.nombre = nombre.trim();
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
        return "[Inspección de Agente] " + nombre + " (" + equipo.getNombre()
                + ") | Vida: " + vida + " HP | Kevlar: " + chalecoKevlar
                + " | Rol: " + rolTactico();
    }

    @Override
    public int calcularValorEconomico() {
        return dinero + (tieneChaleco() ? 650 : 0);
    }

    // ==========================================
    // Comportamiento del Dominio (Impacto Balístico y Explosivo)
    // ==========================================

    public boolean tieneChaleco() {
        return chalecoKevlar > 0;
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    /**
     * Aplica el daño de una explosión o disparo al agente, desgastando su chaleco y su vida.
     */
    public void recibirDañoExplosivo(int dañoFinal) {
        if (dañoFinal <= 0) {
            return;
        }
        if (chalecoKevlar > 0) {
            int desgasteChaleco = Math.min(chalecoKevlar, (int) Math.round(dañoFinal * 0.35));
            chalecoKevlar -= desgasteChaleco;
        }
        this.vida = Math.max(0, this.vida - dañoFinal);
    }

    // Getters y Setters con invariantes protegidos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoAgente() { return this.getClass().getSimpleName(); }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del agente no puede ser nulo ni vacío.");
        }
        this.nombre = nombre.trim();
    }

    public int getVida() { return vida; }
    public void setVida(int vida) {
        if (vida < 0 || vida > 100) {
            throw new IllegalArgumentException("La vida del agente debe estar entre 0 y 100 HP: " + vida);
        }
        this.vida = vida;
    }

    public int getChalecoKevlar() { return chalecoKevlar; }
    public void setChalecoKevlar(int chalecoKevlar) {
        if (chalecoKevlar < 0 || chalecoKevlar > 100) {
            throw new IllegalArgumentException("El chaleco Kevlar debe estar entre 0 y 100: " + chalecoKevlar);
        }
        this.chalecoKevlar = chalecoKevlar;
    }

    public int getDinero() { return dinero; }
    public void setDinero(int dinero) {
        if (dinero < 0 || dinero > 16000) {
            throw new IllegalArgumentException("El dinero del agente debe estar entre $0 y $16,000: " + dinero);
        }
        this.dinero = dinero;
    }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) {
        this.equipo = (equipo != null) ? equipo : new Equipo("Counter-Terrorista");
    }
}
