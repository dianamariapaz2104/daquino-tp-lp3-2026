package py.edu.uc.lp3.herencia;

public abstract class SubfusilSMG extends Arma {

    private float cadenciaFuego;
    private float movilidad;

    public SubfusilSMG(String nombre, int daño, float precision,
                       float tiempoRecarga, int precio, Equipo equipo,
                       float cadenciaFuego, float movilidad) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo);
        this.cadenciaFuego = cadenciaFuego;
        this.movilidad = movilidad;
    }

    public void dispararEnMovimiento() {
        System.out.println(getNombre() + " disparando en movimiento...");
    }

    public void recargar() {
        System.out.println(getNombre() + " recargando...");
    }

    public float getCadenciaFuego() {
        return cadenciaFuego;
    }

    public float getMovilidad() {
        return movilidad;
    }
}
