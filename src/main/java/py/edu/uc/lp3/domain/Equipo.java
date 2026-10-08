package py.edu.uc.lp3.domain;

public class Equipo {

    private String nombre;

    public Equipo() {
        this("Counter-Terrorista");
    }

    public Equipo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo no puede ser nulo ni vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo no puede ser nulo ni vacío.");
        }
        this.nombre = nombre.trim();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
