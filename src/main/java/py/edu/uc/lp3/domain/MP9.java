package py.edu.uc.lp3.domain;

public class MP9 extends SubfusilSMG {

    /**
     * Constructor simple: crea un MP9 con valores oficiales de Counter-Strike 2.
     */
    public MP9() {
        this(new Equipo("Counter-Terrorista"));
    }

    /**
     * Constructor sobrecargado (1 parámetro: equipo).
     */
    public MP9(Equipo equipo) {
        super(
            "MP9",
            24,
            0.70f,
            1.8f,
            1250,
            equipo,
            13.3f,
            1.25f,
            30,
            30
        );
    }

    /**
     * Constructor sobrecargado (3 parámetros: daño, munición y equipo)
     * utilizado por la API REST para instanciación personalizada.
     */
    public MP9(int daño, int municion, Equipo equipo) {
        super(
            "MP9",
            daño,
            0.70f,
            1.8f,
            1250,
            equipo,
            13.3f,
            1.25f,
            municion,
            30
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Subfusil automático de alta cadencia, ágil para disparar en movimiento.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[MP9] *Click* ¡Sin munición! Necesita recargar.";
        }
        gastarMunicion();
        return "[MP9] ¡RAT-TAT-TAT! Ráfaga rápida de 9mm a alta cadencia infligiendo " + getDaño()
                + " de daño. Balas restantes: " + getMunicion() + "/" + getCapacidadCargador() + ".";
    }
}
