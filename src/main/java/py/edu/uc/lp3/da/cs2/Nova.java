package py.edu.uc.lp3.da.cs2;

public class Nova extends Escopeta {

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
            8
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Escopeta de perdigones dispersos, letal en combate cuerpo a cuerpo.";
    }
}
