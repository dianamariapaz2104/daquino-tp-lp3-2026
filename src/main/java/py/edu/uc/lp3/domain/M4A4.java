package py.edu.uc.lp3.domain;

public class M4A4 extends Rifle {

    /**
     * Constructor simple: crea un M4A4 con valores oficiales de Counter-Strike 2.
     */
    public M4A4() {
        this(new Equipo("Counter-Terrorista"));
    }

    /**
     * Constructor sobrecargado (1 parámetro: equipo).
     */
    public M4A4(Equipo equipo) {
        super(
            "M4A4",
            30,
            0.85f,
            2.0f,
            3100,
            equipo,
            "Automático",
            0.55f,
            30,
            30
        );
    }

    /**
     * Constructor sobrecargado (3 parámetros: daño, munición y equipo)
     * utilizado por la API REST para instanciación personalizada.
     */
    public M4A4(int daño, int municion, Equipo equipo) {
        super(
            "M4A4",
            daño,
            0.85f,
            2.0f,
            3100,
            equipo,
            "Automático",
            0.55f,
            municion,
            30
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Fusil de asalto automático, ráfagas precisas a media y larga distancia.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[M4A4] *Click* ¡Sin munición! Necesita recargar.";
        }
        gastarMunicion();
        return "[M4A4] ¡PUM! Disparo preciso de calibre 5.56x45mm infligiendo " + getDaño()
                + " de daño. Balas restantes: " + getMunicion() + "/" + getCapacidadCargador() + ".";
    }
}
