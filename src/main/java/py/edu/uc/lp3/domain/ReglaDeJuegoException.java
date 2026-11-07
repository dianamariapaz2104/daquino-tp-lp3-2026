package py.edu.uc.lp3.domain;

/**
 * Clase base de la jerarquía de excepciones del dominio Counter-Strike 2.
 * Extiende RuntimeException (excepción no comprobada) para representar
 * violaciones a las reglas de negocio tácticas del juego lanzadas por
 * las propias entidades del dominio.
 */
public class ReglaDeJuegoException extends RuntimeException {

    private final String reglaViolada;

    public ReglaDeJuegoException(String reglaViolada, String mensaje) {
        super(mensaje);
        this.reglaViolada = reglaViolada;
    }

    public String getReglaViolada() {
        return reglaViolada;
    }
}
