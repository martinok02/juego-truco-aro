package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import persistencia.Avisos;
import ui.Texto;

/**
 * Cartel arriba de la pantalla con los avisos de los archivos de datos/ (archivo que falta,
 * linea danada...). Muestra uno por vez, unos segundos cada uno, en el orden en que llegaron.
 * Todas las escenas lo dibujan al final, para que quede encima de todo.
 */
final class Notificaciones {

    private static final long DURACION_MS = 4500;

    private static String actual = null;
    private static long visibleHasta = 0;

    private Notificaciones() {
    }

    static void dibujar(Graphics2D g, int ancho) {
        long ahora = System.currentTimeMillis();
        if (actual == null || ahora > visibleHasta) {
            if (Avisos.hayPendientes()) {
                actual = Avisos.siguiente();
                visibleHasta = ahora + DURACION_MS;
            } else {
                actual = null;
            }
        }
        if (actual == null) {
            return;
        }
        String texto = Estilo.recortar(g, actual, 12, ancho - 150);
        int alto = 30;
        int x = 40;
        int y = 8;
        int w = ancho - 80;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x + 2, y + 3, w, alto, alto, alto);
        g.setColor(new Color(176, 98, 22, 240));
        g.fillRoundRect(x, y, w, alto, alto, alto);
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(255, 210, 140));
        g.drawRoundRect(x, y, w, alto, alto, alto);
        Texto.izquierda(g, "DATOS", x + 16, y + 20, 11, new Color(255, 225, 170));
        Texto.izquierda(g, texto, x + 70, y + 20, 12, Color.WHITE);
    }
}
