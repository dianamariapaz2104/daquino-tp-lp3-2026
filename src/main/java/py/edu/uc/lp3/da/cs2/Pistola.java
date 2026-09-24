package py.edu.uc.lp3.da.cs2;

public abstract class Pistola extends Arma {

    private int capacidadCargador;
    private String modoDisparo;

    public Pistola(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   int capacidadCargador, String modoDisparo, int municion) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo, municion);
        this.capacidadCargador = capacidadCargador;
        this.modoDisparo = modoDisparo;
    }

    public void disparar() {
        if (puedeDisparar()) {
            gastarMunicion();
            System.out.println(getNombre() + " disparando...");
        }
    }

    public void recargar() {
        System.out.println(getNombre() + " recargando...");
    }

    public int getCapacidadCargador() {
        return capacidadCargador;
    }

    public String getModoDisparo() {
        return modoDisparo;
    }
}
