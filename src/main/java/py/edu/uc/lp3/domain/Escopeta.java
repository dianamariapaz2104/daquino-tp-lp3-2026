package py.edu.uc.lp3.domain;

public abstract class Escopeta extends Arma {

    private int perdigones;
    private float dispersion;

    public Escopeta() {
        this("Escopeta Estándar", 26, 0.65f, 2.5f, 1050, new Equipo("Counter-Terrorista"), 8, 1.6f, 8, 8);
    }

    public Escopeta(String nombre, int daño, float precision,
                    float tiempoRecarga, int precio, Equipo equipo,
                    int perdigones, float dispersion, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, perdigones, dispersion, municion, Math.max(municion, 1));
    }

    public Escopeta(String nombre, int daño, float precision,
                    float tiempoRecarga, int precio, Equipo equipo,
                    int perdigones, float dispersion, int municion, int capacidadCargador) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, capacidadCargador);
        this.perdigones = perdigones;
        this.dispersion = dispersion;
    }

    public int getPerdigones() { return perdigones; }
    public float getDispersion() { return dispersion; }
}
