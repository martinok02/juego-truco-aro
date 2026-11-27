package persistencia;

import java.io.InputStream;

/**
 * Convierte una FuenteDeMuestras en bytes de audio PCM de 16 bits (little endian), que es lo
 * que necesita javax.sound para reproducir. Entrega los bytes de a uno a medida que se piden:
 * asi no hace falta guardar todo el sonido en un arreglo.
 */
class FlujoDeAudio extends InputStream {

    private final FuenteDeMuestras fuente;
    private int muestrasEntregadas = 0;
    private int byteAlto = -1; // segundo byte de la muestra actual, pendiente de entregar

    FlujoDeAudio(FuenteDeMuestras fuente) {
        this.fuente = fuente;
        fuente.reiniciar();
    }

    @Override
    public int read() {
        if (byteAlto >= 0) {
            int b = byteAlto;
            byteAlto = -1;
            return b;
        }
        if (muestrasEntregadas >= fuente.getCantidadMuestras()) {
            return -1; // fin del sonido
        }
        double muestra = Math.max(-1, Math.min(1, fuente.siguienteMuestra()));
        muestrasEntregadas++;
        int valor = (int) Math.round(muestra * Short.MAX_VALUE);
        byteAlto = (valor >>> 8) & 0xff;
        return valor & 0xff;
    }
}
