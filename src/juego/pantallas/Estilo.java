package juego.pantallas;

import estructuras.Lista;
import estructuras.MiLista;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import ui.Texto;

/**
 * Estilo visual comun a todas las pantallas: paleta de colores, fondo, paneles,
 * globos de dialogo, avatares, fosforos del tanteador y trofeo.
 *
 * Se centraliza aca para que todas las escenas se vean iguales y para no repetir
 * codigo de dibujo en cada una. Es una clase utilitaria: no se instancia.
 * No usa arreglos: los degradados son lineales (GradientPaint) y las formas se
 * arman con Path2D, que no necesita int[] como Polygon.
 */
public final class Estilo {

    // Paleta de pulperia: verde profundo, celeste patrio, cuero y papel crema.
    public static final Color FONDO_ARRIBA = new Color(28, 58, 62);
    public static final Color FONDO_ABAJO = new Color(12, 31, 35);
    public static final Color PANEL = new Color(30, 59, 61, 220);
    public static final Color PANEL_BORDE = new Color(212, 174, 105, 150);
    public static final Color AZUL = new Color(80, 164, 196);
    public static final Color CARAMELO = new Color(177, 112, 62);
    public static final Color MAGENTA = new Color(170, 68, 72);
    public static final Color VERDE = new Color(62, 137, 92);
    public static final Color DORADO = new Color(240, 190, 92);
    public static final Color CREMA = new Color(246, 237, 211);
    public static final Color TEXTO = Color.WHITE;
    public static final Color TEXTO_SUAVE = new Color(190, 207, 197);

    private Estilo() {
    }

    /** Activa el suavizado de bordes y de texto (sin esto todo se ve "serruchado"). */
    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Fondo de pulperia en degradado, con naipes y soles patrios apenas visibles. */
    public static void fondo(Graphics2D g, int ancho, int alto) {
        suavizar(g);
        g.setPaint(new GradientPaint(0, 0, FONDO_ARRIBA, 0, alto, FONDO_ABAJO));
        g.fillRect(0, 0, ancho, alto);

        // Siluetas de cartas inclinadas y una guarda tenue para enmarcar la escena.
        AffineTransform original = g.getTransform();
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 9; i++) {
            double x = (i * 97 + 40) % ancho;
            double y = (i * 173 + 60) % alto;
            g.translate(x, y);
            g.rotate((i % 2 == 0 ? 1 : -1) * (0.2 + i * 0.07));
            g.setColor(new Color(255, 255, 255, 10));
            g.fillRoundRect(-45, -68, 90, 136, 12, 12);
            g.setColor(new Color(255, 255, 255, 16));
            g.drawRoundRect(-45, -68, 90, 136, 12, 12);
            g.setTransform(original);
        }
        g.setColor(new Color(240, 190, 92, 24));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(12, 12, ancho - 24, alto - 24, 18, 18);
        solPatrio(g, ancho - 58, 58, 25, new Color(240, 190, 92, 25));
    }

    /** Sello solar vectorial, usado como detalle patrio en el fondo y en las cartas. */
    public static void solPatrio(Graphics2D g, double centroX, double centroY, double radio, Color color) {
        AffineTransform original = g.getTransform();
        g.setColor(color);
        g.setStroke(new BasicStroke((float) Math.max(1, radio / 12)));
        for (int i = 0; i < 12; i++) {
            double angulo = i * Math.PI / 6;
            double interior = radio * 1.2;
            double exterior = radio * 1.55;
            g.drawLine((int) (centroX + Math.cos(angulo) * interior),
                    (int) (centroY + Math.sin(angulo) * interior),
                    (int) (centroX + Math.cos(angulo) * exterior),
                    (int) (centroY + Math.sin(angulo) * exterior));
        }
        g.fillOval((int) (centroX - radio), (int) (centroY - radio),
                (int) (radio * 2), (int) (radio * 2));
        g.setColor(new Color(91, 61, 28, color.getAlpha()));
        g.fillOval((int) (centroX - radio * 0.28), (int) (centroY - radio * 0.28),
                (int) (radio * 0.56), (int) (radio * 0.56));
        g.setTransform(original);
    }

    /** Panel redondeado semitransparente con sombra suave. */
    public static void panel(Graphics2D g, int x, int y, int ancho, int alto) {
        panel(g, x, y, ancho, alto, PANEL, PANEL_BORDE);
    }

    /** Sobrecarga: mismo panel pero con colores propios (por ejemplo, la final dorada). */
    public static void panel(Graphics2D g, int x, int y, int ancho, int alto, Color relleno, Color borde) {
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(x + 3, y + 4, ancho, alto, 20, 20);
        g.setColor(relleno);
        g.fillRoundRect(x, y, ancho, alto, 20, 20);
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(borde);
        g.drawRoundRect(x, y, ancho, alto, 20, 20);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 239, 195, 32));
        g.drawRoundRect(x + 5, y + 5, ancho - 10, alto - 10, 16, 16);
    }

    /** Titulo grande con letra con serifa, dorado y con sombra. */
    public static void titulo(Graphics2D g, String texto, double centroX, double y, int tamanio) {
        g.setFont(new Font(Font.SERIF, Font.BOLD, tamanio));
        FontMetrics fm = g.getFontMetrics();
        float x = (float) (centroX - fm.stringWidth(texto) / 2.0);
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString(texto, x + 2, (float) y + 3);
        g.setColor(DORADO);
        g.drawString(texto, x, (float) y);
    }

    /** Etiqueta en forma de pildora que se ajusta al largo del texto. */
    public static void pildora(Graphics2D g, String texto, double centroX, double centroY, int tamanio, Color fondo, Color colorTexto) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanio));
        int anchoTexto = g.getFontMetrics().stringWidth(texto);
        int alto = tamanio + 12;
        int ancho = anchoTexto + 26;
        int x = (int) (centroX - ancho / 2.0);
        int y = (int) (centroY - alto / 2.0);
        g.setColor(fondo);
        g.fillRoundRect(x, y, ancho, alto, alto, alto);
        Texto.centrado(g, texto, centroX, centroY + tamanio * 0.36, tamanio, colorTexto);
    }

    /** Corta el texto con "..." si no entra en el ancho disponible (para globos y paneles). */
    public static String recortar(Graphics2D g, String texto, int tamanio, int anchoMaximo) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanio));
        FontMetrics fm = g.getFontMetrics();
        if (texto == null) return "";
        if (fm.stringWidth(texto) <= anchoMaximo) return texto;
        String recortado = texto;
        while (recortado.length() > 1 && fm.stringWidth(recortado + "...") > anchoMaximo) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }
        return recortado + "...";
    }

    /**
     * Parte el texto en renglones que entren en el ancho dado (cortando entre palabras).
     * Si sobran palabras despues de "maxRenglones", el ultimo renglon termina en "...".
     */
    public static Lista<String> envolver(Graphics2D g, String texto, int tamanio, int anchoMaximo, int maxRenglones) {
        Lista<String> renglones = new MiLista<>();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanio));
        FontMetrics fm = g.getFontMetrics();
        String restante = (texto == null) ? "" : texto.trim();
        while (!restante.isEmpty() && renglones.tamanio() < maxRenglones) {
            if (fm.stringWidth(restante) <= anchoMaximo) {
                renglones.agregar(restante);
                return renglones;
            }
            // Cortar en el ultimo espacio que todavia entra
            int corte = -1;
            int espacio = restante.indexOf(' ');
            while (espacio > 0 && fm.stringWidth(restante.substring(0, espacio)) <= anchoMaximo) {
                corte = espacio;
                espacio = restante.indexOf(' ', espacio + 1);
            }
            if (corte < 0 || renglones.tamanio() == maxRenglones - 1) {
                renglones.agregar(recortar(g, restante, tamanio, anchoMaximo));
                return renglones;
            }
            renglones.agregar(restante.substring(0, corte));
            restante = restante.substring(corte + 1).trim();
        }
        return renglones;
    }

    /** Globo de dialogo color crema con una "colita" que apunta a quien habla. */
    public static void globo(Graphics2D g, int x, int y, int ancho, int alto, double colaX, double colaY) {
        Path2D cola = new Path2D.Double();
        double baseX = Math.max(x + 18, Math.min(x + ancho - 18, colaX));
        double baseY = colaY < y ? y + 2 : (colaY > y + alto ? y + alto - 2 : y + alto / 2.0);
        if (colaY >= y && colaY <= y + alto) {
            // La cola sale por el costado izquierdo
            baseX = x + 2;
            cola.moveTo(baseX, baseY - 9);
            cola.lineTo(colaX, colaY);
            cola.lineTo(baseX, baseY + 9);
        } else {
            cola.moveTo(baseX - 9, baseY);
            cola.lineTo(colaX, colaY);
            cola.lineTo(baseX + 9, baseY);
        }
        cola.closePath();

        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(x + 3, y + 4, ancho, alto, 22, 22);
        g.setColor(CREMA);
        g.fillRoundRect(x, y, ancho, alto, 22, 22);
        g.fill(cola);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(177, 112, 62, 150));
        g.drawRoundRect(x, y, ancho, alto, 22, 22);
    }

    /** Avatar redondo con la inicial del jugador y un aro blanco. */
    public static void avatar(Graphics2D g, double centroX, double centroY, int radio, String nombre, Color color) {
        int x = (int) (centroX - radio);
        int y = (int) (centroY - radio);
        g.setPaint(new GradientPaint(x, y, color.brighter(), x, y + radio * 2, color.darker()));
        g.fillOval(x, y, radio * 2, radio * 2);
        g.setStroke(new BasicStroke(3f));
        g.setColor(Color.WHITE);
        g.drawOval(x, y, radio * 2, radio * 2);
        String inicial = (nombre == null || nombre.isEmpty()) ? "?" : nombre.substring(0, 1).toUpperCase();
        Texto.centrado(g, inicial, centroX, centroY + radio * 0.38, (int) (radio * 1.05), Color.WHITE);
    }

    /** Moneda dorada (para las fichas del jugador). */
    public static void moneda(Graphics2D g, double centroX, double centroY, int radio) {
        int x = (int) (centroX - radio);
        int y = (int) (centroY - radio);
        g.setPaint(new GradientPaint(x, y, new Color(255, 226, 120), x, y + radio * 2, new Color(205, 140, 30)));
        g.fillOval(x, y, radio * 2, radio * 2);
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(150, 95, 20));
        g.drawOval(x + 3, y + 3, radio * 2 - 6, radio * 2 - 6);
    }

    /**
     * Tanteador criollo con fosforos: cada grupo de 5 puntos es un cuadrado (4 fosforos)
     * cruzado por una diagonal. Asi se anota el truco "a mano" en la mesa.
     */
    public static void fosforos(Graphics2D g, int x, int y, int puntos) {
        int lado = 18;
        int separacion = 26;
        int dibujados = Math.min(puntos, 30); // hasta 6 grupos (partido a 30)
        for (int grupo = 0; grupo * 5 < dibujados; grupo++) {
            int enGrupo = Math.min(5, dibujados - grupo * 5);
            int gx = x + grupo * separacion;
            if (enGrupo >= 1) fosforo(g, gx, y, gx + lado, y);                 // arriba
            if (enGrupo >= 2) fosforo(g, gx + lado, y, gx + lado, y + lado);   // derecha
            if (enGrupo >= 3) fosforo(g, gx + lado, y + lado, gx, y + lado);   // abajo
            if (enGrupo >= 4) fosforo(g, gx, y + lado, gx, y);                 // izquierda
            if (enGrupo >= 5) fosforo(g, gx, y, gx + lado, y + lado);          // diagonal
        }
    }

    /** Un fosforo: palito color madera con la cabeza roja en la punta final. */
    private static void fosforo(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(236, 206, 150));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(new Color(210, 50, 40));
        g.fillOval(x2 - 3, y2 - 3, 6, 6);
    }

    /** Trofeo (copa con asas y base). La escala 1 mide unos 60 px de alto. */
    public static void trofeo(Graphics2D g, double centroX, double centroY, double escala, Color color) {
        AffineTransform original = g.getTransform();
        g.translate(centroX, centroY);
        g.scale(escala, escala);
        g.setStroke(new BasicStroke(5f));
        g.setColor(color.darker());
        g.draw(new Arc2D.Double(-30, -24, 20, 22, 90, 180, Arc2D.OPEN));  // asa izquierda
        g.draw(new Arc2D.Double(10, -24, 20, 22, 270, 180, Arc2D.OPEN));  // asa derecha
        g.setPaint(new GradientPaint(-20, -30, color.brighter(), 20, 10, color.darker()));
        g.fill(new Arc2D.Double(-22, -48, 44, 56, 180, 180, Arc2D.CHORD)); // copa
        g.fillRect(-4, 8, 8, 12);                                            // pie
        g.fill(new RoundRectangle2D.Double(-16, 18, 32, 9, 6, 6));         // base
        g.setTransform(original);
    }
}
