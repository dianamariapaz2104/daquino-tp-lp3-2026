package py.edu.uc.lp3.domain;

public abstract class Rifle extends Arma {

    private String modoDisparo;
    private float retroceso;

    public Rifle() {
        this("Rifle Estándar", 30, 0.85f, 2.0f, 3100, new Equipo("Counter-Terrorista"), "Automático", 0.55f, 30, 30);
    }

    public Rifle(String nombre, int daño, float precision,
                 float tiempoRecarga, int precio, Equipo equipo,
                 String modoDisparo, float retroceso, int municion) {
        this(nombre, daño, precision, tiempoRecarga, precio, equipo, modoDisparo, retroceso, municion, Math.max(municion, 1));
    }

    public Rifle(String nombre, int daño, float precision,
                 float tiempoRecarga, int precio, Equipo equipo,
                 String modoDisparo, float retroceso, int municion, int capacidadCargador) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion, capacidadCargador);
        this.modoDisparo = modoDisparo;
        this.retroceso = retroceso;
    }

    public void dispararRafaga() {
        if (puedeDisparar()) {
            gastarMunicion();
            System.out.println(getNombre() + " disparando ráfaga...");
        }
    }

    public void apuntar() {
        System.out.println(getNombre() + " apuntando...");
    }

    public String getModoDisparo() { return modoDisparo; }
    public float getRetroceso() { return retroceso; }
}
