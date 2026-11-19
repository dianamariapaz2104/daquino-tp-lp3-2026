package py.edu.uc.lp3.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GranadaHE")
public class GranadaHE extends Granada {

    public GranadaHE() {
        this(new Equipo("Counter-Terrorista"));
    }

    public GranadaHE(Equipo equipo) {
        super(
            "Granada HE",
            50,
            1.0f,
            0.0f,
            300,
            equipo,
            5.0f,
            1.5f,
            1,
            1
        );
    }

    public GranadaHE(int daño, int municion, Equipo equipo) {
        super(
            "Granada HE",
            daño,
            1.0f,
            0.0f,
            300,
            equipo,
            5.0f,
            1.5f,
            municion,
            1
        );
    }

    @Override
    public String comportamientoDeCombate() {
        return "Granada de fragmentación de alto poder explosivo, daño de área masivo.";
    }

    @Override
    public String disparar() {
        if (!puedeDisparar()) {
            return "[Granada HE] ¡Sin granadas disponibles en el inventario!";
        }
        gastarMunicion();
        return "[Granada HE] ¡KABOOM! Detonación de fragmentación infligiendo " + getDaño()
                + " de daño en área de " + getRadioExplosion() + "m.";
    }

    /**
     * Sobrecarga 1: Detonación en área devolviendo el reporte táctico.
     * Las validaciones de reglas de juego y excepciones viven en ArmaService.
     */
    public String detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo) {
        return detonarEnArea(distanciaMetros, tieneChaleco, equipoObjetivo, false);
    }

    /**
     * Sobrecarga 2: Detonación en área indicando si hubo penalización por fuego amigo.
     */
    public String detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo, boolean aplicarReduccionFuegoAmigo) {
        int dañoEfectivo = calcularDañoOndaExpansiva(distanciaMetros, tieneChaleco);
        if (aplicarReduccionFuegoAmigo && dañoEfectivo > 0) {
            dañoEfectivo = (int) Math.round(dañoEfectivo * 0.65);
        }

        gastarMunicion();

        String nombreEquipoObj = (equipoObjetivo != null) ? equipoObjetivo.getNombre() : "Desconocido";
        if (distanciaMetros > getRadioExplosion()) {
            return "[" + getNombre() + "] Detonó a " + getTiempoExplosion() + "s, pero el objetivo del equipo "
                    + nombreEquipoObj + " estaba a " + distanciaMetros + "m (fuera del radio de "
                    + getRadioExplosion() + "m). Daño: 0.";
        }

        return "[" + getNombre() + "] ¡KABOOM tras " + getTiempoExplosion() + "s! Onda expansiva a "
                + distanciaMetros + "m (radio máx " + getRadioExplosion() + "m) contra equipo "
                + nombreEquipoObj + (tieneChaleco ? " [con Kevlar]" : " [sin Kevlar]")
                + " -> Daño explosivo final: " + dañoEfectivo + ".";
    }
}
