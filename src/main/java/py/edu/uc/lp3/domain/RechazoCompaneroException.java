package py.edu.uc.lp3.domain;

/**
 * Excepción de la jerarquía ReglaDeJuegoException que representa el rechazo de una regla
 * al consultar la API REST de un compañero (A2 o A3) durante el escenario de APIs cruzadas (POO-09).
 * Es lanzada exclusivamente desde la capa Service/Client.
 */
public class RechazoCompaneroException extends ReglaDeJuegoException {

    private final String companeroRol;
    private final String companeroNombre;
    private final String urlConsultada;
    private final int codigoHttpCompanero;
    private final String detalleCompanero;

    public RechazoCompaneroException(
            String companeroRol,
            String companeroNombre,
            String urlConsultada,
            int codigoHttpCompanero,
            String detalleCompanero,
            String mensaje) {
        super("Rechazo de Regla Remota (" + companeroRol + " - " + companeroNombre + ")", mensaje);
        this.companeroRol = companeroRol;
        this.companeroNombre = companeroNombre;
        this.urlConsultada = urlConsultada;
        this.codigoHttpCompanero = codigoHttpCompanero;
        this.detalleCompanero = detalleCompanero;
    }

    public String getCompaneroRol() {
        return companeroRol;
    }

    public String getCompaneroNombre() {
        return companeroNombre;
    }

    public String getUrlConsultada() {
        return urlConsultada;
    }

    public int getCodigoHttpCompanero() {
        return codigoHttpCompanero;
    }

    public String getDetalleCompanero() {
        return detalleCompanero;
    }
}
