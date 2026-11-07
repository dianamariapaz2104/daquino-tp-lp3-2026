package py.edu.uc.lp3.domain;

/**
 * Protocolo de dominio (POO-07):
 * Define el contrato común para elementos tácticos de familias distintas
 * (jerarquía Arma y jerarquía Agente) que pueden ser inspeccionados y
 * tasados económicamente durante una ronda de Counter-Strike 2.
 */
public interface InspeccionableEnPartida {

    /**
     * Devuelve la descripción táctica detallada del elemento en la partida.
     */
    String inspeccionarEnPartida();

    /**
     * Calcula el valor económico total del elemento en dólares de CS2 ($).
     */
    int calcularValorEconomico();
}
