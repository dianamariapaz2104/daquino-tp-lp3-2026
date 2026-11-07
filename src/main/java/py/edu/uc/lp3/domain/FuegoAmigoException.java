package py.edu.uc.lp3.domain;

/**
 * Excepción hija 2 de la jerarquía del dominio:
 * Se lanza cuando un Arma o GranadaHE intenta infligir daño o detonar contra
 * un Agente del mismo Equipo estando bloqueado el fuego amigo en modo competitivo.
 */
public class FuegoAmigoException extends ReglaDeJuegoException {

    private final String nombreEquipo;
    private final String accionBloqueada;

    public FuegoAmigoException(String nombreEquipo, String accionBloqueada, String mensaje) {
        super("Regla de Fuego Amigo Competitivo CS2", mensaje);
        this.nombreEquipo = nombreEquipo;
        this.accionBloqueada = accionBloqueada;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public String getAccionBloqueada() {
        return accionBloqueada;
    }
}
