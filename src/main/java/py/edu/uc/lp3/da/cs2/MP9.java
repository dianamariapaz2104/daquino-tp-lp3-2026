package py.edu.uc.lp3.da.cs2;

public class MP9 extends SubfusilSMG {

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
            30
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Subfusil automático de alta cadencia, ágil para disparar en movimiento.";
    }
}
