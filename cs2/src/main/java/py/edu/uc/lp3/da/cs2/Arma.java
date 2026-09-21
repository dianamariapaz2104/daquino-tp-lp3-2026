package py.edu.uc.lp3.herencia;

public abstract class Arma {

    private String nombre;
    private int daño;
    private float precision;
    private float tiempoRecarga;
    private int precio;
    private Equipo equipo;

    public Arma(String nombre, int daño, float precision,
                float tiempoRecarga, int precio, Equipo equipo) {
        this.nombre = nombre;
        this.daño = daño;
        this.precision = precision;
        this.tiempoRecarga = tiempoRecarga;
        this.precio = precio;
        this.equipo = equipo;
    }

    public String obtenerInfo() {
        return "Arma: " + nombre
                + " | Daño: " + daño
                + " | Precisión: " + precision
                + " | Recarga: " + tiempoRecarga
                + " | Precio: " + precio
                + " | Equipo: " + equipo;
    }

    public boolean puedeDisparar() {
        return true;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDaño() {
        return daño;
    }

    public float getPrecision() {
        return precision;
    }

    public float getTiempoRecarga() {
        return tiempoRecarga;
    }

    public int getPrecio() {
        return precio;
    }

    public Equipo getEquipo() {
        return equipo;
    }
}
