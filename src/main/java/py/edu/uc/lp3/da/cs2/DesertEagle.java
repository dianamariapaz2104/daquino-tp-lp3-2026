package py.edu.uc.lp3.da.cs2;

public class DesertEagle extends Pistola {

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
            7
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Pistola pesada semiautomática, un proyectil de alto poder por gatillazo.";
    }
}
