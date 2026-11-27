package juego.audio;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import persistencia.RepositorioRecursos;

/**
 * Sonido de repartir cartas al entrar al menu (se reproduce una vez y se corta al salir).
 * El archivo lo lee persistencia; si falta, se avisa en pantalla y el menu queda en silencio.
 */
public final class EfectoReparto {

    private static final String ARCHIVO = "datos/audio/freesound_community-playing-cards-being-dealt-27024.wav";

    private static Clip clip;
    private static boolean iniciando;
    private static long generacion;

    private EfectoReparto() {
    }

    public static void reproducir() {
        final long solicitud;
        synchronized (EfectoReparto.class) {
            if (iniciando || (clip != null && clip.isRunning())) return;
            if (clip != null) {
                clip.close();
                clip = null;
            }
            iniciando = true;
            solicitud = ++generacion;
        }
        Thread reproductor = new Thread(() -> abrirYReproducir(solicitud), "efecto-reparto");
        reproductor.setDaemon(true);
        reproductor.start();
    }

    public static synchronized void detener() {
        generacion++;
        iniciando = false;
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    private static void abrirYReproducir(long solicitud) {
        Clip nuevo = RepositorioRecursos.abrirSonido(ARCHIVO);
        synchronized (EfectoReparto.class) {
            iniciando = false;
            if (nuevo == null) {
                return;
            }
            if (solicitud != generacion) { // mientras se abria, ya se salio del menu
                nuevo.close();
                return;
            }
            clip = nuevo;
        }
        nuevo.addLineListener(evento -> {
            if (evento.getType() == LineEvent.Type.STOP) {
                synchronized (EfectoReparto.class) {
                    if (clip == nuevo) {
                        clip = null;
                        nuevo.close();
                    }
                }
            }
        });
        nuevo.start();
    }
}
