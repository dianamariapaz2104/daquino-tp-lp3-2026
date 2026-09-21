package py.edu.uc.lp3.herencia;

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
            "Semiautomático"
        );
    }
}
