package py.edu.uc.lp3.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Especialización de Agente: Operador Counter-Terrorista (CT).
 * Posee Kit de Desactivación (Defuse Kit) para desactivar el C4 en la mitad del tiempo.
 */
@Entity
@DiscriminatorValue("AgenteCT")
public class AgenteCT extends Agente {

    private boolean tieneKitDesactivacion;

    public AgenteCT() {
        this("Agente SAS (CT)", true);
    }

    public AgenteCT(String nombre, boolean tieneKitDesactivacion) {
        super(nombre, 100, 100, 800, new Equipo("Counter-Terrorista"));
        this.tieneKitDesactivacion = tieneKitDesactivacion;
    }

    public AgenteCT(String nombre, int vida, int chalecoKevlar, int dinero, boolean tieneKitDesactivacion) {
        super(nombre, vida, chalecoKevlar, dinero, new Equipo("Counter-Terrorista"));
        this.tieneKitDesactivacion = tieneKitDesactivacion;
    }

    @Override
    public String rolTactico() {
        return "Defensor Counter-Terrorista: protege los sitios A y B y desactiva la bomba C4.";
    }

    @Override
    public String ejecutarAccionEspecial() {
        int segundos = tieneKitDesactivacion ? 5 : 10;
        return "[" + getNombre() + "] Desactivando bomba C4 en " + segundos + " segundos"
                + (tieneKitDesactivacion ? " (con Defuse Kit)." : " (sin Defuse Kit).");
    }

    public boolean isTieneKitDesactivacion() { return tieneKitDesactivacion; }
    public void setTieneKitDesactivacion(boolean tieneKitDesactivacion) { this.tieneKitDesactivacion = tieneKitDesactivacion; }
}
