package py.edu.uc.lp3.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Equipo {

    @Column(name = "equipo_nombre")
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

    /**
     * Verifica si otro equipo pertenece al mismo bando táctico (para control de Fuego Amigo).
     */
    public boolean esMismoEquipo(Equipo otro) {
        if (otro == null || otro.getNombre() == null || this.nombre == null) {
            return false;
        }
        return this.nombre.trim().equalsIgnoreCase(otro.getNombre().trim());
    }

    @Override
    public String toString() {
        return nombre;
    }
}
