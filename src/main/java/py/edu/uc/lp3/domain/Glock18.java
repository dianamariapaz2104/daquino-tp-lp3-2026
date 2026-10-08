package py.edu.uc.lp3.domain;

public class Glock18 extends Pistola {

    public Glock18() {
        this(new Equipo("Terrorista"));
    }

    public Glock18(Equipo equipo) {
        super(
            "Glock-18",
            20,
            0.80f,
            1.5f,
            500,
            equipo,
            20,
            "Semiautomático",
            20,
            20
        );
    }

    public Glock18(int daño, int municion, Equipo equipo) {
        super(
            "Glock-18",
            daño,
            0.80f,
            1.5f,
            500,
            equipo,
            20,
            "Semiautomático",
            municion,
            20
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Pistola semiautomática de 9 mm con cargador amplio y fuego rápido controlable.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[Glock-18] *Click* ¡Sin munición! Necesita recargar.";
        }
        gastarMunicion();
        return "[Glock-18] ¡PEW! Disparo ágil de 9mm infligiendo " + getDaño()
                + " de daño. Balas restantes: " + getMunicion() + "/" + getCapacidadCargador() + ".";
    }
}
