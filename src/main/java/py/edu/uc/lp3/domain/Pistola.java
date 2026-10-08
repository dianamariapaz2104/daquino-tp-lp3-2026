package py.edu.uc.lp3.domain;

public abstract class Pistola extends Arma {

    private int capacidadCargadorPistola;
    private String modoDisparo;

    public Pistola() {
        this("Pistola Estándar", 20, 0.75f, 1.8f, 500, new Equipo("Counter-Terrorista"), 12, "Semiautomático", 12, 12);
    }

    public Pistola(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   int capacidadCargador, String modoDisparo, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, capacidadCargador, modoDisparo, municion, Math.max(municion, 1));
    }

    public Pistola(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   int capacidadCargador, String modoDisparo, int municion, int capacidadCargadorBase) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, capacidadCargadorBase);
        this.capacidadCargadorPistola = capacidadCargador;
        this.modoDisparo = modoDisparo;
    }

    public int getCapacidadCargadorPistola() { return capacidadCargadorPistola; }
    public String getModoDisparo() { return modoDisparo; }
}
