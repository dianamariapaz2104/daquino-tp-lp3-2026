package py.edu.uc.lp3.herencia;

public abstract class Pistola extends Arma {

    private int capacidadCargador;
    private String modoDisparo;

    public Pistola(String nombre, int daño, float precision,
                   float tiempoRecarga, int precio, Equipo equipo,
                   int capacidadCargador, String modoDisparo) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo);
        this.capacidadCargador = capacidadCargador;
        this.modoDisparo = modoDisparo;
    }

    public void disparar() {
        System.out.println(getNombre() + " disparando...");
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
