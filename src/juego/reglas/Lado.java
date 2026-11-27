package juego.reglas;

/** Los dos lados de la mesa en el truco mano a mano. */
public enum Lado {
    JUGADOR,
    RIVAL;

    /** El otro lado de la mesa. */
    public Lado otro() {
        return this == JUGADOR ? RIVAL : JUGADOR;
    }
}
