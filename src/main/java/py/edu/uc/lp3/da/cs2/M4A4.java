package py.edu.uc.lp3.da.cs2;

public class M4A4 extends Rifle {

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
            30
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Fusil de asalto automático, ráfagas precisas a media y larga distancia.";
    }
}
