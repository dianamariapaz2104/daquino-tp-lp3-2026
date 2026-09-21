package py.edu.uc.lp3.herencia;

public abstract class Rifle extends Arma {

    private String modoDisparo;
    private float retroceso;

    public Rifle(String nombre, int daño, float precision,
                 float tiempoRecarga, int precio, Equipo equipo,
                 String modoDisparo, float retroceso) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo);
        this.modoDisparo = modoDisparo;
        this.retroceso = retroceso;
    }

    public void dispararRafaga() {
        System.out.println(getNombre() + " disparando ráfaga...");
    }

    public void apuntar() {
        System.out.println(getNombre() + " apuntando...");
    }

    public String getModoDisparo() {
        return modoDisparo;
    }

    public float getRetroceso() {
        return retroceso;
    }
}
