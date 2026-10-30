package py.edu.uc.lp3.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Especialización de Agente: Operador Terrorista (T).
 * Puede portar y plantar el explosivo C4 en los sitios de bomba.
 */
@Entity
@DiscriminatorValue("AgenteTerrorista")
public class AgenteTerrorista extends Agente {

    private boolean portaBombaC4;

    public AgenteTerrorista() {
        this("Agente Phoenix (T)", true);
    }

    public AgenteTerrorista(String nombre, boolean portaBombaC4) {
        super(nombre, 100, 100, 800, new Equipo("Terrorista"));
        this.portaBombaC4 = portaBombaC4;
    }

    public AgenteTerrorista(String nombre, int vida, int chalecoKevlar, int dinero, boolean portaBombaC4) {
        super(nombre, vida, chalecoKevlar, dinero, new Equipo("Terrorista"));
        this.portaBombaC4 = portaBombaC4;
    }

    @Override
    public String rolTactico() {
        return "Atacante Terrorista: toma el control del sitio y planta el explosivo C4.";
    }

    @Override
    public String ejecutarAccionEspecial() {
        if (!portaBombaC4) {
            return "[" + getNombre() + "] No porta el explosivo C4; cubre el avance del equipo.";
        }
        return "[" + getNombre() + "] ¡Código 7355608 ingresado! Bomba C4 plantada (detonación en 40s).";
    }

    public boolean isPortaBombaC4() { return portaBombaC4; }
    public void setPortaBombaC4(boolean portaBombaC4) { this.portaBombaC4 = portaBombaC4; }
}
