package py.edu.uc.lp3.domain;

public class Nova extends Escopeta {

    public Nova() {
        this(new Equipo("Counter-Terrorista"));
    }

    public Nova(Equipo equipo) {
        super(
            "Nova",
            26,
            0.65f,
            2.5f,
            1050,
            equipo,
            8,
            1.6f,
            8,
            8
        );
    }

    public Nova(int daño, int municion, Equipo equipo) {
        super(
            "Nova",
            daño,
            0.65f,
            2.5f,
            1050,
            equipo,
            8,
            1.6f,
            municion,
            8
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Escopeta de perdigones dispersos, letal en combate cuerpo a cuerpo.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[Nova] *Click* ¡Sin cartuchos! Necesita recargar.";
        }
        gastarMunicion();
        return "[Nova] ¡BOOM! Dispersión de 8 perdigones infligiendo " + getDaño()
                + " de daño a corta distancia. Cartuchos restantes: " + getMunicion() + "/" + getCapacidadCargador() + ".";
    }
}
