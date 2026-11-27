package juego.audio;

import javax.sound.sampled.Clip;
import persistencia.RepositorioRecursos;

/**
 * Musica de fondo: el tango sintetizado (TangoSintetizado) sonando en bucle.
 * Se prende y apaga con F6. Si la computadora no tiene salida de audio, el juego sigue mudo.
 */
public final class MusicaTango {

    private static Clip clip;
    private static boolean iniciando;
    private static boolean habilitada = true;

    private MusicaTango() {
    }

    /** Arranca la musica en otro hilo (generarla tarda un momento y no debe trabar la ventana). */
    public static void iniciar() {
        synchronized (MusicaTango.class) {
            if (!habilitada || iniciando || (clip != null && clip.isOpen())) return;
            iniciando = true;
        }
        Thread reproductor = new Thread(MusicaTango::abrirYReproducir, "musica-tango");
        reproductor.setDaemon(true);
        reproductor.start();
    }

    public static synchronized void alternar() {
        habilitada = !habilitada;
        if (clip != null && clip.isOpen()) {
            if (habilitada) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.stop();
        } else if (habilitada) {
            iniciar();
        }
    }

    public static synchronized boolean estaActiva() {
        return habilitada;
    }

    public static synchronized void detener() {
        habilitada = false;
        iniciando = false;
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    private static void abrirYReproducir() {
        Clip nuevo = RepositorioRecursos.abrirSonido(new TangoSintetizado());
        synchronized (MusicaTango.class) {
            iniciando = false;
            if (nuevo == null) {
                return;
            }
            if (!habilitada) {
                nuevo.close();
                return;
            }
            clip = nuevo;
        }
        nuevo.loop(Clip.LOOP_CONTINUOUSLY);
    }
}
