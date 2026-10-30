package py.edu.uc.lp3.domain;

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
 * equipa armas/granadas y recibe daño en las simulaciones de explosivos.
 */
@Entity
@Table(name = "agentes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_agente")
public abstract class Agente {

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
    // Comportamiento del Dominio (Impacto Explosivo)
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

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoAgente() { return this.getClass().getSimpleName(); }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getVida() { return vida; }
    public void setVida(int vida) { this.vida = vida; }

    public int getChalecoKevlar() { return chalecoKevlar; }
    public void setChalecoKevlar(int chalecoKevlar) { this.chalecoKevlar = chalecoKevlar; }

    public int getDinero() { return dinero; }
    public void setDinero(int dinero) { this.dinero = dinero; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
}
