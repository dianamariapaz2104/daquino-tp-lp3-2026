package py.edu.uc.lp3.domain;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArmaDomainTest {

    @Test
    @DisplayName("Constructor simple de M4A4 y MP9 inicializa con estado legal por defecto")
    void constructoresSimplesEstadoLegal() {
        M4A4 m4a4 = new M4A4();
        assertEquals("M4A4", m4a4.getNombre());
        assertEquals(30, m4a4.getDaño());
        assertEquals(30, m4a4.getMunicion());
        assertEquals(30, m4a4.getCapacidadCargador());
        assertEquals("Counter-Terrorista", m4a4.getEquipo().getNombre());
        assertTrue(m4a4.puedeDisparar());

        MP9 mp9 = new MP9();
        assertEquals("MP9", mp9.getNombre());
        assertEquals(24, mp9.getDaño());
        assertEquals(30, mp9.getMunicion());
        assertEquals(30, mp9.getCapacidadCargador());
        assertTrue(mp9.puedeDisparar());
    }

    @Test
    @DisplayName("Constructor sobrecargado de M4A4 permite personalizar valores válidos")
    void constructoresSobrecargadosValoresValidos() {
        Equipo t = new Equipo("Terrorista");
        M4A4 m4a4Custom = new M4A4(35, 20, t);
        assertEquals(35, m4a4Custom.getDaño());
        assertEquals(20, m4a4Custom.getMunicion());
        assertEquals("Terrorista", m4a4Custom.getEquipo().getNombre());

        MP9 mp9Custom = new MP9(28, 15, t);
        assertEquals(28, mp9Custom.getDaño());
        assertEquals(15, mp9Custom.getMunicion());
    }

    @Test
    @DisplayName("Invariantes del dominio: rechaza valores negativos o ilegales con IllegalArgumentException")
    void invariantesProtegidosRechazanValoresIlegales() {
        Equipo ct = new Equipo("Counter-Terrorista");

        // Daño <= 0
        assertThrows(IllegalArgumentException.class, () -> new M4A4(0, 30, ct));
        assertThrows(IllegalArgumentException.class, () -> new M4A4(-10, 30, ct));

        // Munición negativa
        assertThrows(IllegalArgumentException.class, () -> new M4A4(30, -5, ct));
        assertThrows(IllegalArgumentException.class, () -> new MP9(24, -1, ct));

        // Munición mayor a la capacidad del cargador
        assertThrows(IllegalArgumentException.class, () -> new M4A4(30, 50, ct));

        // Distancia negativa en sobrecarga de disparar
        M4A4 m4a4 = new M4A4();
        assertThrows(IllegalArgumentException.class, () -> m4a4.disparar(-10));
    }

    @Test
    @DisplayName("Sobreescritura: M4A4 y MP9 implementan comportamientoDeCombate y disparar de forma propia")
    void sobreescrituraMetodosAbstractos() {
        Arma arma1 = new M4A4();
        Arma arma2 = new MP9();

        // Polimorfismo a través del tipo base Arma
        String combate1 = arma1.comportamientoDeCombate();
        String combate2 = arma2.comportamientoDeCombate();

        assertNotNull(combate1);
        assertNotNull(combate2);
        assertNotEquals(combate1, combate2);
        assertTrue(combate1.contains("Fusil de asalto"));
        assertTrue(combate2.contains("Subfusil"));

        String disp1 = arma1.disparar();
        String disp2 = arma2.disparar();

        assertTrue(disp1.contains("[M4A4]"));
        assertTrue(disp2.contains("[MP9]"));
        assertEquals(29, arma1.getMunicion());
        assertEquals(29, arma2.getMunicion());
    }

    @Test
    @DisplayName("Sobrecarga de disparar: sin argumentos vs con distancia vs con headshot")
    void sobrecargaMensajeDisparar() {
        M4A4 m4a4 = new M4A4();

        // Sobrecarga 1: disparar()
        String dispBase = m4a4.disparar();
        assertTrue(dispBase.contains("calibre 5.56x45mm"));
        assertEquals(29, m4a4.getMunicion());

        // Sobrecarga 2: disparar(int distancia)
        String dispDistancia = m4a4.disparar(20);
        assertTrue(dispDistancia.contains("Disparo a 20m"));
        assertEquals(28, m4a4.getMunicion());

        // Sobrecarga 3: disparar(int distancia, boolean headshot)
        String dispCritico = m4a4.disparar(20, true);
        assertTrue(dispCritico.contains("HEADSHOT"));
        assertEquals(27, m4a4.getMunicion());
    }

    @Test
    @DisplayName("Recarga respeta la capacidad máxima del cargador")
    void recargaRespetaCapacidadCargador() {
        M4A4 m4a4 = new M4A4(30, 5, new Equipo("Counter-Terrorista"));
        assertEquals(5, m4a4.getMunicion());

        m4a4.recargar(10);
        assertEquals(15, m4a4.getMunicion());

        // Recargar más del máximo no supera el tope
        m4a4.recargar(100);
        assertEquals(30, m4a4.getMunicion());
    }

    @Test
    @DisplayName("Interfaz de dominio InspeccionableEnPartida funciona polimórficamente en familias distintas (Arma y Agente)")
    void interfazInspeccionableEnPartidaEnFamiliasDistintas() {
        List<InspeccionableEnPartida> elementos = List.of(
                new M4A4(),
                new AgenteCT("Sargento SAS", true),
                new AgenteTerrorista("Líder Phoenix", true)
        );

        assertTrue(elementos.get(0).inspeccionarEnPartida().contains("[Inspección de Arma]"));
        assertEquals(3100, elementos.get(0).calcularValorEconomico());

        assertTrue(elementos.get(1).inspeccionarEnPartida().contains("[Inspección de Agente]"));
        assertEquals(1450, elementos.get(1).calcularValorEconomico());
    }

    @Test
    @DisplayName("Jerarquía de excepciones del dominio: lanza MunicionInsuficienteException y FuegoAmigoException")
    void jerarquiaExcepcionesDominio() {
        M4A4 armaSinBalas = new M4A4(30, 0, new Equipo("Counter-Terrorista"));
        AgenteTerrorista enemigo = new AgenteTerrorista("Phoenix", true);
        AgenteCT aliado = new AgenteCT("SAS", true);

        // Hija 1: MunicionInsuficienteException al disparar sin balas
        MunicionInsuficienteException exMunicion = assertThrows(
                MunicionInsuficienteException.class,
                () -> armaSinBalas.dispararContraAgente(enemigo, 15, false, false)
        );
        assertTrue(exMunicion instanceof ReglaDeJuegoException);
        assertEquals(0, exMunicion.getMunicionActual());

        // Hija 2: FuegoAmigoException al atacar aliado con fuego amigo deshabilitado
        M4A4 armaConBalas = new M4A4(30, 10, new Equipo("Counter-Terrorista"));
        FuegoAmigoException exFuegoAmigo = assertThrows(
                FuegoAmigoException.class,
                () -> armaConBalas.dispararContraAgente(aliado, 15, false, false)
        );
        assertTrue(exFuegoAmigo instanceof ReglaDeJuegoException);
        assertEquals("Counter-Terrorista", exFuegoAmigo.getNombreEquipo());
    }
}
