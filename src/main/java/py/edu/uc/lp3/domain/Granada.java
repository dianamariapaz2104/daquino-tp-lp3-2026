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
        if (distanciaMetros < 0 || distanciaMetros > radioExplosion || radioExplosion <= 0) {
            return 0;
        }

        double factorCercania = 1.0 - (distanciaMetros / radioExplosion);
        double dañoBruto = getDaño() * factorCercania;

        if (tieneChaleco) {
            dañoBruto *= 0.60;
        }
        return (int) Math.round(dañoBruto);
    }

    /**
     * Permite a ArmaService actualizar parámetros de explosivo tras validar invariantes.
     * No se exponen setRadioExplosion ni setTiempoExplosion públicos libres.
     */
    public void aplicarParametrosExplosivoValidados(float radioExplosion, float tiempoExplosion) {
        this.radioExplosion = radioExplosion;
        this.tiempoExplosion = tiempoExplosion;
    }

    public float getRadioExplosion() { return radioExplosion; }
    public float getTiempoExplosion() { return tiempoExplosion; }
}
