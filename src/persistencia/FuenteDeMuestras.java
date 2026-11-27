package persistencia;

/**
 * Interfaz para sonido generado por el juego (no leido de un archivo): entrega las muestras
 * de a una, en orden, con valores entre -1 y 1. La implementa la musica de tango.
 * RepositorioRecursos la convierte en un flujo de audio que Java puede reproducir.
 */
public interface FuenteDeMuestras {

    /** Muestras por segundo (por ejemplo 22050). */
    int getMuestrasPorSegundo();

    /** Cuantas muestras tiene el sonido completo. */
    int getCantidadMuestras();

    /** Vuelve al principio. */
    void reiniciar();

    /** La siguiente muestra, entre -1 y 1. */
    double siguienteMuestra();
}
