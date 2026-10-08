package py.edu.uc.lp3.domain;

public class DesertEagle extends Pistola {

    public DesertEagle() {
        this(new Equipo("Counter-Terrorista"));
    }

    public DesertEagle(Equipo equipo) {
        super(
            "Desert Eagle",
            40,
            0.75f,
            2.0f,
            700,
            equipo,
            7,
            "Semiautomático",
            7,
            7
        );
    }

    public DesertEagle(int daño, int municion, Equipo equipo) {
        super(
            "Desert Eagle",
            daño,
            0.75f,
            2.0f,
            700,
            equipo,
            7,
            "Semiautomático",
            municion,
            7
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Pistola pesada semiautomática, un proyectil de alto poder por gatillazo.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[Desert Eagle] *Click* ¡Sin munición! Necesita recargar.";
        }
        gastarMunicion();
        return "[Desert Eagle] ¡BANG! Cañonazo calibre .50 Action Express infligiendo " + getDaño()
                + " de daño demoledor. Balas restantes: " + getMunicion() + "/" + getCapacidadCargador() + ".";
    }
}
