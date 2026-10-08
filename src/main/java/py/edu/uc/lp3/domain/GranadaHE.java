package py.edu.uc.lp3.domain;

public class GranadaHE extends Granada {

    public GranadaHE() {
        this(new Equipo("Counter-Terrorista"));
    }

    public GranadaHE(Equipo equipo) {
        super(
            "Granada HE",
            50,
            1.0f,
            0.0f,
            300,
            equipo,
            5.0f,
            1.5f,
            1,
            1
        );
    }

    public GranadaHE(int daño, int municion, Equipo equipo) {
        super(
            "Granada HE",
            daño,
            1.0f,
            0.0f,
            300,
            equipo,
            5.0f,
            1.5f,
            municion,
            1
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Granada de fragmentación de alto poder explosivo, daño de área masivo.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[Granada HE] ¡Sin granadas disponibles en el inventario!";
        }
        gastarMunicion();
        return "[Granada HE] ¡KABOOM! Detonación de fragmentación infligiendo " + getDaño()
                + " de daño en área de " + getRadioExplosion() + "m.";
    }
}
