package py.edu.uc.lp3.da.cs2;

public class GranadaHE extends Granada {

    public GranadaHE(Equipo equipo) {
        super(
            "Granada HE",
            100,
            1.0f,
            3.0f,
            300,
            equipo,
            400f,
            3.0f,
            1
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Granada de fragmentación de área, con un importante radio de explosión.";
    }
}
