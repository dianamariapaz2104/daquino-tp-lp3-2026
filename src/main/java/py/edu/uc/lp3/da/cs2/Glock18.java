package py.edu.uc.lp3.da.cs2;

public class Glock18 extends Pistola {

    public Glock18(Equipo equipo) {
        super(
            "Glock-18",
            20,
            0.80f,
            1.5f,
            500,
            equipo,
            20,
            "Semiautomático",
            20
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Pistola semiautomática de 9 mm con cargador amplio y fuego rápido controlable.";
    }
}
