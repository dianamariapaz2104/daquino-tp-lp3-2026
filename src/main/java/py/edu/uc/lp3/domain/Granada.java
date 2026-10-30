package py.edu.uc.lp3.domain;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Granada extends Arma {

    private float radioExplosion;
    private float tiempoExplosion;

    public Granada() {
        this("Granada Genérica", 50, 1.0f, 0.0f, 300, new Equipo("Counter-Terrorista"), 5.0f, 1.5f, 1, 1);
    }

    public Granada(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   float radioExplosion, float tiempoExplosion, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, radioExplosion, tiempoExplosion, municion, Math.max(municion, 1));
    }

    public Granada(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   float radioExplosion, float tiempoExplosion, int municion, int capacidadCargador) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, capacidadCargador);
        this.radioExplosion = radioExplosion;
        this.tiempoExplosion = tiempoExplosion;
    }

    public void lanzar() {
        if (puedeDisparar()) {
            gastarMunicion();
            System.out.println(getNombre() + " lanzada.");
        }
    }

    public void rebotar() {
        System.out.println(getNombre() + " rebotando en el suelo...");
    }

    /**
     * Calcula el daño de la onda expansiva según la distancia al epicentro,
     * el radio de explosión de la granada y si el objetivo lleva chaleco Kevlar.
     */
    public int calcularDañoOndaExpansiva(double distanciaMetros, boolean tieneChaleco) {
        if (distanciaMetros < 0) {
            throw new IllegalArgumentException("La distancia al epicentro de la explosión no puede ser negativa: " + distanciaMetros);
        }
        if (distanciaMetros > radioExplosion) {
            return 0; // Fuera del radio de explosión la onda expansiva no alcanza al objetivo
        }

        double factorCercania = 1.0 - (distanciaMetros / radioExplosion);
        double dañoBruto = getDaño() * factorCercania;

        if (tieneChaleco) {
            dañoBruto *= 0.60; // El chaleco Kevlar en CS2 absorbe el 40% del daño por metralla
        }
        return (int) Math.round(dañoBruto);
    }

    public float getRadioExplosion() { return radioExplosion; }
    public void setRadioExplosion(float radioExplosion) { this.radioExplosion = radioExplosion; }

    public float getTiempoExplosion() { return tiempoExplosion; }
    public void setTiempoExplosion(float tiempoExplosion) { this.tiempoExplosion = tiempoExplosion; }
}
