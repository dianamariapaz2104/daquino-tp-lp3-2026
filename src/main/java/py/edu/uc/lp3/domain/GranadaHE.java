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
     * Simula la detonación táctica de la Granada HE interactuando con Granada, Arma y Equipo.
     */
    public String detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo) {
        if (!puedeDisparar()) {
            throw new IllegalArgumentException("La Granada HE ya fue detonada (munición 0/1).");
        }

        int dañoEfectivo = calcularDañoOndaExpansiva(distanciaMetros, tieneChaleco);
        boolean fuegoAmigo = getEquipo().esMismoEquipo(equipoObjetivo);
        if (fuegoAmigo && dañoEfectivo > 0) {
            // En CS2 el fuego amigo reduce el daño infligido a compañeros de equipo
            dañoEfectivo = (int) Math.round(dañoEfectivo * 0.65);
        }

        gastarMunicion();

        if (distanciaMetros > getRadioExplosion()) {
            return "[" + getNombre() + "] Detonó a " + getTiempoExplosion() + "s, pero el objetivo del equipo "
                    + equipoObjetivo.getNombre() + " estaba a " + distanciaMetros + "m (fuera del radio de "
                    + getRadioExplosion() + "m). Daño: 0.";
        }

        return "[" + getNombre() + "] ¡KABOOM tras " + getTiempoExplosion() + "s! Onda expansiva a "
                + distanciaMetros + "m (radio máx " + getRadioExplosion() + "m) contra equipo "
                + equipoObjetivo.getNombre() + (tieneChaleco ? " [con Kevlar]" : " [sin Kevlar]")
                + " -> Daño explosivo final: " + dañoEfectivo + ".";
    }
}
