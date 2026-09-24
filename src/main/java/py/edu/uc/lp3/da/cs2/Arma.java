package py.edu.uc.lp3.da.cs2;

public abstract class Arma {

    private String nombre;
    private int daño;
    private float precision;
    private float tiempoRecarga;
    private int precio;
    private Equipo equipo;
    private int municion;

    public Arma(String nombre, int daño, float precision,
                float tiempoRecarga, int precio, Equipo equipo, int municion) {
        if (daño < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo: " + daño);
        }
        if (municion < 0) {
            throw new IllegalArgumentException("La munición no puede ser negativa: " + municion);
        }
        this.nombre = nombre;
        this.daño = daño;
        this.precision = precision;
        this.tiempoRecarga = tiempoRecarga;
        this.precio = precio;
        this.equipo = equipo;
        this.municion = municion;
    }

    public abstract String comportamientoDeCombate();

    public String obtenerInfo() {
        return "Arma: " + nombre
                + " | Daño: " + daño
                + " | Precisión: " + precision
                + " | Recarga: " + tiempoRecarga
                + " | Precio: " + precio
                + " | Equipo: " + equipo;
    }

    public boolean puedeDisparar() {
        return municion > 0;
    }

    protected void gastarMunicion() {
        if (municion > 0) {
            municion--;
        }
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

    public int getMunicion() {
        return municion;
    }
}
