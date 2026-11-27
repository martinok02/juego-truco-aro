package juego.reglas;

/**
 * Una baza ya resuelta: la carta de cada uno, quien salio (tiro primero) y quien la gano.
 * Es un objeto de solo lectura: una vez jugada, la baza no cambia.
 */
public class BazaJugada {

    private final Carta cartaJugador;
    private final Carta cartaRival;
    private final Lado salio;
    private final Lado ganador; // null = parda

    public BazaJugada(Carta cartaJugador, Carta cartaRival, Lado salio, Lado ganador) {
        if (cartaJugador == null || cartaRival == null) {
            throw new ReglaInvalidaException("Una baza jugada tiene una carta de cada lado.");
        }
        if (salio == null) {
            throw new ReglaInvalidaException("Hay que saber quien tiro primero en la baza.");
        }
        this.cartaJugador = cartaJugador;
        this.cartaRival = cartaRival;
        this.salio = salio;
        this.ganador = ganador;
    }

    public Carta getCarta(Lado lado) {
        return lado == Lado.JUGADOR ? cartaJugador : cartaRival;
    }

    /** Quien tiro primero en esta baza (su carta queda abajo en la pila de la mesa). */
    public Lado getSalio() {
        return salio;
    }

    /** Ganador de la baza, o null si fue parda. */
    public Lado getGanador() {
        return ganador;
    }

    public boolean esParda() {
        return ganador == null;
    }
}
