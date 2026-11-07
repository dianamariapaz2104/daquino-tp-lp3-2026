package py.edu.uc.lp3.domain;

/**
 * Excepción hija 1 de la jerarquía del dominio:
 * Se lanza cuando un Arma intenta disparar o detonar sin proyectiles en el cargador
 * (munición agotada) o cuando una operación de carga de munición viola los límites
 * del cargador.
 */
public class MunicionInsuficienteException extends ReglaDeJuegoException {

    private final String nombreArma;
    private final int municionActual;
    private final int capacidadCargador;

    public MunicionInsuficienteException(String nombreArma, int municionActual, int capacidadCargador, String mensaje) {
        super("Regla de Munición y Cargador CS2", mensaje);
        this.nombreArma = nombreArma;
        this.municionActual = municionActual;
        this.capacidadCargador = capacidadCargador;
    }

    public String getNombreArma() {
        return nombreArma;
    }

    public int getMunicionActual() {
        return municionActual;
    }

    public int getCapacidadCargador() {
        return capacidadCargador;
    }
}
