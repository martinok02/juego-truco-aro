package juego.reglas;

/**
 * Todo lo que un jugador puede "decir" en la mesa (ademas de tirar una carta).
 * Cada accion trae el texto del boton y la tecla que la dispara, asi la pantalla
 * no tiene que conocer las reglas: solo muestra lo que MesaTruco dice que se puede hacer.
 */
public enum Accion {
    ENVIDO("ENVIDO", "E"),
    REAL_ENVIDO("REAL ENVIDO", "R"),
    FALTA_ENVIDO("FALTA ENVIDO", "F"),
    TRUCO("TRUCO", "T"),
    RETRUCO("RETRUCO", "T"),
    VALE_CUATRO("VALE CUATRO", "T"),
    QUIERO("QUIERO", "Q"),
    NO_QUIERO("NO QUIERO", "N"),
    MAZO("AL MAZO", "M");

    private final String texto;
    private final String tecla;

    Accion(String texto, String tecla) {
        this.texto = texto;
        this.tecla = tecla;
    }

    public String getTexto() {
        return texto;
    }

    public String getTecla() {
        return tecla;
    }

    public boolean esEnvido() {
        return this == ENVIDO || this == REAL_ENVIDO || this == FALTA_ENVIDO;
    }

    public boolean esTruco() {
        return this == TRUCO || this == RETRUCO || this == VALE_CUATRO;
    }
}
