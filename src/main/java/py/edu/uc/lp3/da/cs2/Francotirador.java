package py.edu.uc.lp3.da.cs2;

public abstract class Francotirador extends Arma {

    private float zoom;
    private int penetracion;

    public Francotirador(String nombre, int daño, float precision,
                         float tiempoRecarga, int precio, Equipo equipo,
                         float zoom, int penetracion, int municion) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion);
        this.zoom = zoom;
        this.penetracion = penetracion;
    }

    public void apuntarConMira() {
        System.out.println(getNombre() + " apuntando con mira...");
    }

    public void contenerRespiracion() {
        System.out.println(getNombre() + " conteniendo la respiración...");
    }

    public float getZoom() {
        return zoom;
    }

    public int getPenetracion() {
        return penetracion;
    }
}
