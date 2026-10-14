package py.edu.uc.lp3.da.cs2;

public interface ConModoDeDisparo {

    String getModoDisparo();

    default String descripcionDeDisparo() {
        return "Modo de disparo: " + getModoDisparo();
    }
}
