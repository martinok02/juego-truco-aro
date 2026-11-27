package juego.pantallas;

import juego.reglas.IARival;
import motor.Escena;

/**
 * Interfaz propia: en que "modo" se juega un partido (gira por Argentina o torneo).
 *
 * EscenaPartida no sabe si esta en una gira o en un torneo: le pide al modo el rival, le
 * avisa como termino y le pregunta a donde volver. Cada implementacion responde distinto
 * (polimorfismo real, sin instanceof): la gira descuenta vidas y avanza provincias; el
 * torneo avanza en la llave o te elimina.
 */
public interface ModoPartido {

    enum PerfilRival {
        CONSERVADOR,
        ARRIESGADO,
        MENTIROSO
    }

    /** Para mostrar en pantalla, por ejemplo "Gira · Formosa" o "Torneo · Semifinal". */
    String getTitulo();

    /** Nombre del rival para el globo de dialogo. */
    String getNombreRival();

    /** El rival de este partido (cada modo elige su estrategia). */
    IARival crearRival();

    /** Perfil que define el estilo de juego y la apariencia del rival de este partido. */
    PerfilRival getPerfilRival();

    /** Registra el resultado, lo guarda y devuelve el mensaje para la pantalla de fin. */
    String registrarResultado(boolean gano);

    /** Registra que el jugador abandono el partido desde la pausa. */
    String registrarAbandono();

    /** Lo que se avisa antes de confirmar el abandono. */
    String getAvisoAbandono();

    /** A donde se vuelve despues del partido (o al abandonar). */
    Escena escenaDeVuelta();
}
