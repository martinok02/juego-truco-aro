package persistencia;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Lectura de los recursos del juego que estan en datos/: imagenes de las cartas y sonidos.
 *
 * Toda la lectura de archivos queda en persistencia (como pide la catedra). Si un archivo
 * falta o esta danado no se corta el juego: se avisa en pantalla y se devuelve null, y quien
 * lo pidio usa un reemplazo (las cartas se dibujan simples; el sonido no suena).
 */
public final class RepositorioRecursos {

    private RepositorioRecursos() {
    }

    /** Imagen PNG de datos/, o null si no se pudo leer (con aviso en pantalla). */
    public static BufferedImage cargarImagen(String ruta) {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            Avisos.registrar("Falta la imagen " + ruta + ": se dibuja una carta simple.");
            return null;
        }
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                Avisos.registrar("La imagen " + ruta + " esta danada: se dibuja una carta simple.");
            }
            return imagen;
        } catch (IOException e) {
            Avisos.registrar("No se pudo leer " + ruta + ": se dibuja una carta simple.");
            return null;
        }
    }

    /** Sonido WAV de datos/ listo para reproducir, o null si no se pudo abrir (con aviso). */
    public static Clip abrirSonido(String ruta) {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            Avisos.registrar("Falta el sonido " + ruta + ": el juego sigue sin ese sonido.");
            return null;
        }
        try (AudioInputStream flujo = AudioSystem.getAudioInputStream(archivo)) {
            Clip clip = AudioSystem.getClip();
            clip.open(flujo);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
            Avisos.registrar("No se pudo abrir el sonido " + ruta + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Sonido generado por el juego (por ejemplo la musica) listo para reproducir, o null si
     * la computadora no tiene salida de audio. Las muestras se piden de a una (sin arreglos).
     */
    public static Clip abrirSonido(FuenteDeMuestras fuente) {
        AudioFormat formato = new AudioFormat(fuente.getMuestrasPorSegundo(), 16, 1, true, false);
        try (AudioInputStream flujo = new AudioInputStream(new FlujoDeAudio(fuente), formato, fuente.getCantidadMuestras())) {
            Clip clip = AudioSystem.getClip();
            clip.open(flujo);
            return clip;
        } catch (IOException | LineUnavailableException | IllegalArgumentException e) {
            System.err.println("Sin salida de audio para la musica: " + e.getMessage());
            return null;
        }
    }
}
