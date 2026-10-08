package py.edu.uc.lp3.domain;

public abstract class Arma {

    private String nombre;
    private int daño;
    private float precision;
    private float tiempoRecarga;
    private int precio;
    private Equipo equipo;
    private int municion;
    private int capacidadCargador;

    /**
     * Constructor simple: deja el objeto en un estado legal por defecto.
     */
    public Arma() {
        this("Arma Genérica", 20, 0.70f, 2.0f, 1000, new Equipo("Counter-Terrorista"), 20, 20);
    }

    /**
     * Constructor sobrecargado (7 parámetros).
     */
    public Arma(String nombre, int daño, float precision,
                float tiempoRecarga, int precio, Equipo equipo, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, Math.max(municion, 1));
    }

    /**
     * Constructor sobrecargado completo (8 parámetros) con verificación rigurosa de invariantes.
     */
    public Arma(String nombre, int daño, float precision,
                float tiempoRecarga, int precio, Equipo equipo,
                int municion, int capacidadCargador) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del arma no puede ser nulo ni vacío.");
        }
        if (daño <= 0) {
            throw new IllegalArgumentException("El daño debe ser mayor a cero: " + daño);
        }
        if (municion < 0) {
            throw new IllegalArgumentException("La munición no puede ser negativa: " + municion);
        }
        if (capacidadCargador <= 0) {
            throw new IllegalArgumentException("La capacidad del cargador debe ser positiva: " + capacidadCargador);
        }
        if (municion > capacidadCargador) {
            throw new IllegalArgumentException("La munición (" + municion + ") no puede superar la capacidad del cargador (" + capacidadCargador + ").");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo: " + precio);
        }
        if (precision < 0.0f || precision > 1.0f) {
            throw new IllegalArgumentException("La precisión debe estar comprendida entre 0.0 y 1.0: " + precision);
        }
        if (tiempoRecarga < 0.0f) {
            throw new IllegalArgumentException("El tiempo de recarga no puede ser negativo: " + tiempoRecarga);
        }

        this.nombre = nombre.trim();
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

    /**
     * Describe el rol táctico y estilo de combate distintivo de cada arma en CS2.
     */
    public abstract String comportamientoDeCombate();

    /**
     * Efectúa la acción básica de disparo de cada subclase, resolviendo su propia mecánica y sonido.
     */
    public abstract String disparar();

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
        if (distanciaMetros < 0) {
            throw new IllegalArgumentException("La distancia de disparo no puede ser negativa: " + distanciaMetros);
        }
        if (!puedeDisparar()) {
            return "[" + nombre + "] *Click* ¡Sin munición! Necesita recargar.";
        }

        gastarMunicion();

        // Atenuación de daño en base a la distancia y precisión del arma
        double factorAtenuacion = Math.max(0.15, 1.0 - (distanciaMetros * (1.0 - precision) / 100.0));
        int dañoCalculado = (int) Math.round(daño * factorAtenuacion);

        if (tiroALaCabeza) {
            dañoCalculado *= 4; // Multiplicador crítico de headshot en CS2
            return "[" + nombre + "] ¡HEADSHOT a " + distanciaMetros + "m! Impacto crítico infligiendo "
                    + dañoCalculado + " de daño. Balas restantes: " + municion + "/" + capacidadCargador + ".";
        } else {
            return "[" + nombre + "] Disparo a " + distanciaMetros + "m causando "
                    + dañoCalculado + " de daño efectivo. Balas restantes: " + municion + "/" + capacidadCargador + ".";
        }
    }

    // ==========================================
    // Encapsulamiento y Gestión de Estado
    // ==========================================

    public boolean puedeDisparar() {
        return municion > 0;
    }

    protected void gastarMunicion() {
        if (municion > 0) {
            municion--;
        }
    }

    public void recargar() {
        this.municion = this.capacidadCargador;
    }

    public void recargar(int balas) {
        if (balas < 0) {
            throw new IllegalArgumentException("Las balas a recargar no pueden ser negativas: " + balas);
        }
        this.municion = Math.min(this.capacidadCargador, this.municion + balas);
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

    // Getters públicos (invariantes protegidos, sin setters directos de munición/daño)
    public String getNombre() { return nombre; }
    public int getDaño() { return daño; }
    public float getPrecision() { return precision; }
    public float getTiempoRecarga() { return tiempoRecarga; }
    public int getPrecio() { return precio; }
    public Equipo getEquipo() { return equipo; }
    public int getMunicion() { return municion; }
    public int getCapacidadCargador() { return capacidadCargador; }
}
