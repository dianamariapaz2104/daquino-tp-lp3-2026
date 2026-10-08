package py.edu.uc.lp3.domain;

public abstract class SubfusilSMG extends Arma {

    private float cadenciaFuego;
    private float movilidad;

    public SubfusilSMG() {
        this("Subfusil Estándar", 24, 0.70f, 1.8f, 1250, new Equipo("Counter-Terrorista"), 13.3f, 1.25f, 30, 30);
    }

    public SubfusilSMG(String nombre, int daño, float precision,
                       float tiempoRecarga, int precio, Equipo equipo,
                       float cadenciaFuego, float movilidad, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, cadenciaFuego, movilidad, municion, Math.max(municion, 1));
    }

    public SubfusilSMG(String nombre, int daño, float precision,
                       float tiempoRecarga, int precio, Equipo equipo,
                       float cadenciaFuego, float movilidad, int municion, int capacidadCargador) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, capacidadCargador);
        this.cadenciaFuego = cadenciaFuego;
        this.movilidad = movilidad;
    }

    public void dispararEnMovimiento() {
        if (puedeDisparar()) {
            gastarMunicion();
            System.out.println(getNombre() + " disparando en movimiento...");
        }
    }

    public float getCadenciaFuego() { return cadenciaFuego; }
    public float getMovilidad() { return movilidad; }
}
