package py.edu.uc.lp3.domain;

public abstract class Francotirador extends Arma {

    private float zoom;
    private int penetracion;

    public Francotirador() {
        this("Francotirador Estándar", 115, 0.95f, 3.5f, 4750, new Equipo("Counter-Terrorista"), 2.5f, 300, 5, 5);
    }

    public Francotirador(String nombre, int daño, float precision,
                         float tiempoRecarga, int precio, Equipo equipo,
                         float zoom, int penetracion, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, zoom, penetracion, municion, Math.max(municion, 1));
    }

    public Francotirador(String nombre, int daño, float precision,
                         float tiempoRecarga, int precio, Equipo equipo,
                         float zoom, int penetracion, int municion, int capacidadCargador) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, capacidadCargador);
        this.zoom = zoom;
        this.penetracion = penetracion;
    }

    public void apuntarConMira() {
        System.out.println(getNombre() + " apuntando con mira telescópica (zoom x" + zoom + ")...");
    }

    public void contenerRespiracion() {
        System.out.println(getNombre() + " conteniendo la respiración...");
    }

    public float getZoom() { return zoom; }
    public int getPenetracion() { return penetracion; }
}
