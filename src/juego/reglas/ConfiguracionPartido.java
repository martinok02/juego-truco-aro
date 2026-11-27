package juego.reglas;

/**
 * Opciones que se eligen antes de cada partido: a cuantos puntos se juega y si va con flor.
 *
 * Encapsulamiento: solo se aceptan 15 o 30 puntos. Asi no puede existir un partido
 * "a 0" (terminaria antes de empezar) ni a un numero que rompa la regla de malas y buenas.
 */
public class ConfiguracionPartido {

    public static final int A_15 = 15;
    public static final int A_30 = 30;

    private int puntosObjetivo;
    private boolean conFlor;

    public ConfiguracionPartido(int puntosObjetivo, boolean conFlor) {
        setPuntosObjetivo(puntosObjetivo);
        this.conFlor = conFlor;
    }

    public int getPuntosObjetivo() {
        return puntosObjetivo;
    }

    public void setPuntosObjetivo(int puntosObjetivo) {
        if (puntosObjetivo != A_15 && puntosObjetivo != A_30) {
            throw new ReglaInvalidaException("El partido se juega a 15 o a 30 puntos, no a " + puntosObjetivo + ".");
        }
        this.puntosObjetivo = puntosObjetivo;
    }

    public boolean isConFlor() {
        return conFlor;
    }

    public void setConFlor(boolean conFlor) {
        this.conFlor = conFlor;
    }

    /** "Buenas" y "malas": en el partido a 30, la mitad (15) separa las malas de las buenas. */
    public boolean seJuegaConMalasYBuenas() {
        return puntosObjetivo == A_30;
    }

    @Override
    public String toString() {
        return "A " + puntosObjetivo + (conFlor ? " con flor" : " sin flor");
    }
}
