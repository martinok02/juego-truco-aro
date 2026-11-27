package juego.reglas;

/**
 * Excepcion propia del juego: se intento crear un estado imposible (un partido a 20 puntos,
 * un ranking con mas victorias que partidas, un partido de la llave en la ronda 7...).
 *
 * Extiende IllegalArgumentException (es un error de programacion o de datos, no algo que
 * el jugador pueda provocar jugando), asi que no obliga a usar try/catch en todos lados.
 */
public class ReglaInvalidaException extends IllegalArgumentException {

    public ReglaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
