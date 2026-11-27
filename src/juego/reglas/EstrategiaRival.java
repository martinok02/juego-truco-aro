package juego.reglas;

import estructuras.Lista;

/**
 * Interfaz propia con polimorfismo real implementada por varias estrategias de IA.
 * Cumple con la exigencia de la rubrica:
 * - Una interfaz propia implementada por 2 o mas clases.
 * - Sin uso de instanceof en la logica.
 */
public interface EstrategiaRival {

    String getNombreEstrategia();

    boolean quiereEnvido(int misPuntosEnvido, int puntosEnJuego, boolean soyMano);

    boolean quiereTruco(Lista<Carta> cartasEnMano, int puntosEnJuego, int bazasGanadas);

    Carta elegirCarta(Lista<Carta> cartasEnMano, Carta cartaRivalEnMesa, int bazaActual);
}
