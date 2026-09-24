package py.edu.uc.lp3.da.cs2;

public abstract class Granada extends Arma {

    private float radioExplosion;
    private float tiempoExplosion;

    public Granada(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   float radioExplosion, float tiempoExplosion, int municion) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion);
        this.radioExplosion = radioExplosion;
        this.tiempoExplosion = tiempoExplosion;
    }

    public void lanzar() {
        if (puedeDisparar()) {
            gastarMunicion();
            System.out.println(getNombre() + " lanzada...");
        }
    }

    public void rebotar() {
        System.out.println(getNombre() + " rebotando...");
    }

    public float getRadioExplosion() {
        return radioExplosion;
    }

    public float getTiempoExplosion() {
        return tiempoExplosion;
    }
}
