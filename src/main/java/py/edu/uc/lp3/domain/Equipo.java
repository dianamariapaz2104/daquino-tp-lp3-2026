package py.edu.uc.lp3.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Equipo {

    @Column(name = "equipo_nombre")
    private String nombre;

    public Equipo() {
        this("Counter-Terrorista");
    }

    public Equipo(String nombre) {
        this.nombre = (nombre != null) ? nombre.trim() : "";
    }

    public String getNombre() {
        return nombre;
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
