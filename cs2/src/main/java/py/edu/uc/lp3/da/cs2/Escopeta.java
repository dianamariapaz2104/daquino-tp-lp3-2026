package py.edu.uc.lp3.herencia;

public abstract class Escopeta extends Arma {

    private int perdigones;
    private float dispersion;

    public Escopeta(String nombre, int daño, float precision,
                    float tiempoRecarga, int precio, Equipo equipo,
                    int perdigones, float dispersion) {
        super(nombre, daño, precision, tiempoRecarga, precio, equipo);
        this.perdigones = perdigones;
        this.dispersion = dispersion;
    }

    public void disparoSecundario() {
        System.out.println(getNombre() + " realizando disparo secundario...");
    }

    public void recargar() {
        System.out.println(getNombre() + " recargando...");
    }

    public int getPerdigones() {
        return perdigones;
    }

    public float getDispersion() {
        return dispersion;
    }
}
