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
     * Lanza las excepciones hijas del dominio cuando se incumple la regla de munición o fuego amigo.
     */
    public String detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo) {
        return detonarEnArea(distanciaMetros, tieneChaleco, equipoObjetivo, true);
    }

    /**
     * Sobrecarga con validación explícita de Fuego Amigo en el dominio.
     */
    public String detonarEnArea(double distanciaMetros, boolean tieneChaleco, Equipo equipoObjetivo, boolean permitirFuegoAmigo) {
        if (!puedeDisparar()) {
            throw new MunicionInsuficienteException(
                    getNombre(),
                    getMunicion(),
                    getCapacidadCargador(),
                    "La Granada HE ya fue detonada (munición 0/" + getCapacidadCargador() + ")."
            );
        }

        boolean fuegoAmigo = getEquipo().esMismoEquipo(equipoObjetivo);
        if (fuegoAmigo && !permitirFuegoAmigo) {
            throw new FuegoAmigoException(
                    getEquipo().getNombre(),
                    "Lanzamiento de Granada HE contra aliado",
                    "Regla de negocio CS2: Fuego amigo bloqueado en modo competitivo. El equipo '"
                            + getEquipo().getNombre() + "' no puede lanzar una Granada HE contra un aliado de su mismo equipo."
            );
        }

        int dañoEfectivo = calcularDañoOndaExpansiva(distanciaMetros, tieneChaleco);
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
