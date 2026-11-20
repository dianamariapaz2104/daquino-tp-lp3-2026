package py.edu.uc.lp3.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que registra en el H2 de A1 (Diana Aquino) cada jugada exitosa del escenario
 * de APIs cruzadas (POO-09) tras recibir la aprobación tanto de A2 como de A3.
 * Solo mantiene estado; no lanza excepciones ni expone setters públicos sin control.
 */
@Entity
@Table(name = "jugadas_escenario")
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class JugadaEscenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreEscenario;
    private Long armaIdLocal;
    private String nombreArmaLocal;
    private Long agenteObjetivoIdLocal;
    private String nombreAgenteObjetivoLocal;
    private int balasDisparadas;
    private double distanciaExplosionMetros;
    private int dañoTotalAplicado;
    private int municionRestanteArma;
    private int vidaRestanteAgente;

    @Column(length = 1000)
    private String validacionA2;

    @Column(length = 1000)
    private String validacionA3;

    @Column(length = 1000)
    private String resultadoFinal;

    public JugadaEscenario() {
        this("Asalto Táctico Coordinado CS2", 1L, "M4A4", 1L, "Agente Phoenix (T)", 1, 2.0, 30, 29, 70, "OK", "OK", "CONFIRMADA");
    }

    public JugadaEscenario(
            String nombreEscenario,
            Long armaIdLocal,
            String nombreArmaLocal,
            Long agenteObjetivoIdLocal,
            String nombreAgenteObjetivoLocal,
            int balasDisparadas,
            double distanciaExplosionMetros,
            int dañoTotalAplicado,
            int municionRestanteArma,
            int vidaRestanteAgente,
            String validacionA2,
            String validacionA3,
            String resultadoFinal) {
        this.nombreEscenario = nombreEscenario;
        this.armaIdLocal = armaIdLocal;
        this.nombreArmaLocal = nombreArmaLocal;
        this.agenteObjetivoIdLocal = agenteObjetivoIdLocal;
        this.nombreAgenteObjetivoLocal = nombreAgenteObjetivoLocal;
        this.balasDisparadas = balasDisparadas;
        this.distanciaExplosionMetros = distanciaExplosionMetros;
        this.dañoTotalAplicado = dañoTotalAplicado;
        this.municionRestanteArma = municionRestanteArma;
        this.vidaRestanteAgente = vidaRestanteAgente;
        this.validacionA2 = validacionA2;
        this.validacionA3 = validacionA3;
        this.resultadoFinal = resultadoFinal;
    }

    public Long getId() { return id; }
    public String getNombreEscenario() { return nombreEscenario; }
    public Long getArmaIdLocal() { return armaIdLocal; }
    public String getNombreArmaLocal() { return nombreArmaLocal; }
    public Long getAgenteObjetivoIdLocal() { return agenteObjetivoIdLocal; }
    public String getNombreAgenteObjetivoLocal() { return nombreAgenteObjetivoLocal; }
    public int getBalasDisparadas() { return balasDisparadas; }
    public double getDistanciaExplosionMetros() { return distanciaExplosionMetros; }
    public int getDañoTotalAplicado() { return dañoTotalAplicado; }
    public int getMunicionRestanteArma() { return municionRestanteArma; }
    public int getVidaRestanteAgente() { return vidaRestanteAgente; }
    public String getValidacionA2() { return validacionA2; }
    public String getValidacionA3() { return validacionA3; }
    public String getResultadoFinal() { return resultadoFinal; }
}
