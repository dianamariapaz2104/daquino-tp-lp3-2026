package py.edu.uc.lp3.herencia;

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
            "Semiautomático"
        );
    }
}
