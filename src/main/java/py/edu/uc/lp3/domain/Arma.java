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

@Entity
@Table(name = "armas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_arma")
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "tipoArma",
        visible = true,
        defaultImpl = M4A4.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = M4A4.class, name = "M4A4"),
        @JsonSubTypes.Type(value = MP9.class, name = "MP9"),
        @JsonSubTypes.Type(value = DesertEagle.class, name = "DesertEagle"),
        @JsonSubTypes.Type(value = Glock18.class, name = "Glock18"),
        @JsonSubTypes.Type(value = Nova.class, name = "Nova"),
        @JsonSubTypes.Type(value = GranadaHE.class, name = "GranadaHE")
})
public abstract class Arma implements InspeccionableEnPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private int daño;
    private float precision;
    private float tiempoRecarga;
    private int precio;

    @Embedded
    private Equipo equipo;

    private int municion;
    private int capacidadCargador;

    /**
     * Constructor simple requerido por JPA y de negocio por defecto:
     * delega con this(...) en el constructor completo.
     */
    public Arma() {
        this("Arma Genérica", 20, 0.70f, 2.0f, 1000, new Equipo("Counter-Terrorista"), 20, 20);
    }

    /**
     * Constructor sobrecargado (7 parámetros): delega en el de 8 parámetros.
     */
    public Arma(String nombre, int daño, float precision,
                float tiempoRecarga, int precio, Equipo equipo, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, Math.max(municion, 1));
    }

    /**
     * Constructor sobrecargado completo (8 parámetros).
     * Inicializa el estado de la entidad; las validaciones de invariantes y reglas de negocio
     * viven exclusivamente en ArmaService.
     */
    public Arma(String nombre, int daño, float precision,
                float tiempoRecarga, int precio, Equipo equipo,
                int municion, int capacidadCargador) {
        this.nombre = (nombre != null) ? nombre.trim() : "";
        this.daño = daño;
        this.precision = precision;
        this.tiempoRecarga = tiempoRecarga;
        this.precio = precio;
        this.equipo = (equipo != null) ? equipo : new Equipo("Counter-Terrorista");
        this.municion = municion;
        this.capacidadCargador = capacidadCargador;
    }

    // ==========================================
    // Métodos Abstractos (Sobreescritura en hijas)
    // ==========================================

    public abstract String comportamientoDeCombate();

    public abstract String disparar();

    // ==========================================
    // Implementación del Protocolo InspeccionableEnPartida (POO-07)
    // ==========================================

    @Override
    public String inspeccionarEnPartida() {
        return "[Inspección de Arma] " + obtenerInfo() + " | Rol: " + comportamientoDeCombate();
    }

    @Override
    public int calcularValorEconomico() {
        return this.precio;
    }

    // ==========================================
    // Sobrecarga de Métodos del Dominio
    // ==========================================

    /**
     * Sobrecarga 1: Disparo calculando balística por distancia en metros.
     */
    public String disparar(int distanciaMetros) {
        return disparar(distanciaMetros, false);
    }

    /**
     * Sobrecarga 2: Disparo con distancia y posibilidad de impacto crítico en cabeza (headshot).
     */
    public String disparar(int distanciaMetros, boolean tiroALaCabeza) {
        if (!puedeDisparar()) {
            return "[" + nombre + "] *Click* ¡Sin munición! Necesita recargar.";
        }

        gastarMunicion();

        double factorAtenuacion = Math.max(0.15, 1.0 - (distanciaMetros * (1.0 - precision) / 100.0));
        int dañoCalculado = (int) Math.round(daño * factorAtenuacion);

        if (tiroALaCabeza) {
            dañoCalculado *= 4;
            return "[" + nombre + "] ¡HEADSHOT a " + distanciaMetros + "m! Impacto crítico infligiendo "
                    + dañoCalculado + " de daño. Balas restantes: " + municion + "/" + capacidadCargador + ".";
        } else {
            return "[" + nombre + "] Disparo a " + distanciaMetros + "m causando "
                    + dañoCalculado + " de daño efectivo. Balas restantes: " + municion + "/" + capacidadCargador + ".";
        }
    }

    // ==========================================
    // Encapsulamiento y Transiciones de Estado
    // ==========================================

    public boolean puedeDisparar() {
        return municion > 0;
    }

    protected void gastarMunicion() {
        if (municion > 0) {
            municion--;
        }
    }

    public void consumirProyectil() {
        gastarMunicion();
    }

    public void recargar() {
        this.municion = this.capacidadCargador;
    }

    public void recargar(int balas) {
        if (balas > 0) {
            this.municion = Math.min(this.capacidadCargador, this.municion + balas);
        }
    }

    /**
     * Aplica un nuevo estado previamente validado por ArmaService, sin exponer setters públicos descontrolados.
     */
    public void aplicarEstadoValidado(String nombre, int daño, float precision,
                                      float tiempoRecarga, int precio, Equipo equipo,
                                      int municion, int capacidadCargador) {
        this.nombre = (nombre != null) ? nombre.trim() : "";
        this.daño = daño;
        this.precision = precision;
        this.tiempoRecarga = tiempoRecarga;
        this.precio = precio;
        this.equipo = (equipo != null) ? equipo : new Equipo("Counter-Terrorista");
        this.capacidadCargador = capacidadCargador;
        this.municion = municion;
    }

    public String obtenerInfo() {
        return "Arma: " + nombre
                + " | Daño: " + daño
                + " | Precisión: " + precision
                + " | Recarga: " + tiempoRecarga + "s"
                + " | Precio: $" + precio
                + " | Equipo: " + equipo
                + " | Munición: " + municion + "/" + capacidadCargador;
    }

    // Solo Getters públicos (sin setters públicos descontrolados)
    public Long getId() { return id; }
    public String getTipoArma() { return this.getClass().getSimpleName(); }
    public String getNombre() { return nombre; }
    public int getDaño() { return daño; }
    public float getPrecision() { return precision; }
    public float getTiempoRecarga() { return tiempoRecarga; }
    public int getPrecio() { return precio; }
    public Equipo getEquipo() { return equipo; }
    public int getMunicion() { return municion; }
    public int getCapacidadCargador() { return capacidadCargador; }
}
