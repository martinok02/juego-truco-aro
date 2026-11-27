package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Boton interactivo con apariencia pixel-art y una indicacion opcional de tecla.
 *
 * Encapsulamiento: los atributos son privados y el constructor valida que el boton
 * tenga texto y un tamanio positivo; asi no puede existir un boton invisible o sin
 * area para hacer click.
 */
public class Boton {

    private final String texto;
    private final String tecla;
    private final Color color;
    private int x;
    private int y;
    private final int ancho;
    private final int alto;

    public Boton(String texto, String tecla, Color color, int x, int y, int ancho, int alto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El boton necesita un texto.");
        }
        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("El tamanio del boton debe ser positivo.");
        }
        if (color == null) {
            throw new IllegalArgumentException("El boton necesita un color.");
        }
        this.texto = texto;
        this.tecla = tecla; // puede ser null: boton solo de mouse
        this.color = color;
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    /** Devuelve true si el punto (px, py) cae dentro del boton (para hover y click). */
    public boolean contiene(int px, int py) {
        return px >= x && px <= x + ancho && py >= y && py <= y + alto;
    }

    public void setPosicion(int nuevoX, int nuevoY) {
        this.x = nuevoX;
        this.y = nuevoY;
    }

    public String getTexto() {
        return texto;
    }

    /** Dibujo normal: habilitado y sin resaltar. */
    public void dibujar(Graphics2D g) {
        dibujarPixel(g, false, true, true, false);
    }

    /**
     * "resaltado" = el mouse esta encima; "habilitado" = la accion se puede realizar.
     */
    public void dibujar(Graphics2D g, boolean resaltado, boolean habilitado) {
        dibujarPixel(g, resaltado, habilitado, true, false);
    }

    public void dibujarOpcion(Graphics2D g, boolean seleccionada) {
        dibujarPixel(g, seleccionada, true, false, true);
    }

    /** Apariencia usada por el menu y por las opciones seleccionables. */
    public void dibujarOpcionMenu(Graphics2D g, boolean seleccionada) {
        dibujarPixel(g, seleccionada, true, false, true);
    }

    private void dibujarPixel(Graphics2D g, boolean resaltado, boolean habilitado, boolean mostrarTecla,
                              boolean seleccionable) {
        Graphics2D pixel = (Graphics2D) g.create();
        pixel.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int corte = Math.min(7, alto / 4);
        Color base = habilitado ? color : new Color(83, 91, 112);
        if (seleccionable && !resaltado) base = base.darker();
        if (resaltado && habilitado) base = base.brighter();
        Path2D sombra = formaPixel(x + 3, y + 4, ancho, alto, corte);
        pixel.setColor(new Color(5, 14, 50));
        pixel.fill(sombra);

        Path2D borde = formaPixel(x, y, ancho, alto, corte);
        pixel.setColor(new Color(7, 17, 58));
        pixel.fill(borde);

        Path2D cara = formaPixel(x + 3, y + 2, ancho - 6, alto - 8, Math.max(2, corte - 2));
        pixel.setColor(base);
        pixel.fill(cara);

        if (seleccionable) {
            int marcaX = x + 12;
            int marcaY = y + alto / 2 - 7;
            pixel.setColor(new Color(7, 17, 58));
            pixel.fillRect(marcaX, marcaY, 14, 14);
            pixel.setColor(resaltado ? Estilo.DORADO : new Color(126, 137, 153));
            if (resaltado) {
                pixel.fillRect(marcaX + 3, marcaY + 7, 3, 3);
                pixel.fillRect(marcaX + 6, marcaY + 10, 3, 3);
                pixel.fillRect(marcaX + 9, marcaY + 7, 3, 3);
                pixel.fillRect(marcaX + 11, marcaY + 4, 3, 3);
            } else {
                pixel.fillRect(marcaX + 3, marcaY + 3, 8, 8);
            }
        }

        pixel.setColor(habilitado ? base.brighter() : new Color(154, 160, 174));
        pixel.fillRect(x + corte + 3, y + 4, ancho - (corte + 3) * 2, 2);
        pixel.setColor(base.darker());
        pixel.fillRect(x + 6, y + alto - 11, ancho - 12, 4);
        pixel.setColor(new Color(20, 42, 105));
        pixel.fillRect(x + corte, y + alto - 6, ancho - corte * 2, 3);

        int anchoTecla = mostrarTecla && tecla != null ? Math.max(24, tecla.length() * Math.max(6, alto / 4) + 10) : 0;
        int espacioEtiqueta = ancho - 16 - (anchoTecla == 0 ? 0 : anchoTecla + 8);
        String etiqueta = texto.toUpperCase(java.util.Locale.ROOT);
        int tam = Math.max(10, Math.min(21, alto / 2));
        pixel.setFont(new Font(Font.MONOSPACED, Font.BOLD, tam));
        FontMetrics metricas = pixel.getFontMetrics();
        while (tam > 10 && metricas.stringWidth(etiqueta) > espacioEtiqueta) {
            pixel.setFont(new Font(Font.MONOSPACED, Font.BOLD, --tam));
            metricas = pixel.getFontMetrics();
        }
        int centroEtiqueta = x + (ancho - anchoTecla - (anchoTecla == 0 ? 0 : 8)) / 2;
        int tx = centroEtiqueta - metricas.stringWidth(etiqueta) / 2;
        int ty = y + (alto - metricas.getHeight()) / 2 + metricas.getAscent() - 1;
        pixel.setColor(new Color(5, 20, 48));
        pixel.drawString(etiqueta, tx + 1, ty + 1);
        Color colorTexto = !habilitado ? new Color(41, 46, 60)
                : (seleccionable && !resaltado ? new Color(230, 239, 242) : new Color(8, 30, 66));
        pixel.setColor(colorTexto);
        pixel.drawString(etiqueta, tx, ty);

        if (anchoTecla > 0) {
            int cajaX = x + ancho - anchoTecla - 10;
            int cajaY = y + Math.max(4, (alto - 22) / 2);
            pixel.setColor(new Color(7, 17, 58));
            pixel.fillRect(cajaX, cajaY, anchoTecla, Math.min(22, alto - 8));
            pixel.setColor(base.brighter());
            pixel.fillRect(cajaX + 2, cajaY + 2, anchoTecla - 4, Math.min(2, alto - 12));
            pixel.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.max(9, Math.min(11, alto / 3))));
            FontMetrics teclaMetricas = pixel.getFontMetrics();
            String teclaEtiqueta = tecla.toUpperCase(java.util.Locale.ROOT);
            int teclaX = cajaX + (anchoTecla - teclaMetricas.stringWidth(teclaEtiqueta)) / 2;
            int teclaY = cajaY + (Math.min(22, alto - 8) - teclaMetricas.getHeight()) / 2
                    + teclaMetricas.getAscent();
            pixel.setColor(Color.WHITE);
            pixel.drawString(teclaEtiqueta, teclaX, teclaY);
        }
        pixel.dispose();
    }

    private Path2D formaPixel(int x, int y, int ancho, int alto, int corte) {
        Path2D forma = new Path2D.Double();
        forma.moveTo(x + corte, y);
        forma.lineTo(x + ancho - corte, y);
        forma.lineTo(x + ancho - corte, y + 2);
        forma.lineTo(x + ancho - 2, y + 2);
        forma.lineTo(x + ancho - 2, y + corte);
        forma.lineTo(x + ancho, y + corte);
        forma.lineTo(x + ancho, y + alto - corte);
        forma.lineTo(x + ancho - 2, y + alto - corte);
        forma.lineTo(x + ancho - 2, y + alto - 2);
        forma.lineTo(x + ancho - corte, y + alto - 2);
        forma.lineTo(x + ancho - corte, y + alto);
        forma.lineTo(x + corte, y + alto);
        forma.lineTo(x + corte, y + alto - 2);
        forma.lineTo(x + 2, y + alto - 2);
        forma.lineTo(x + 2, y + alto - corte);
        forma.lineTo(x, y + alto - corte);
        forma.lineTo(x, y + corte);
        forma.lineTo(x + 2, y + corte);
        forma.lineTo(x + 2, y + 2);
        forma.lineTo(x + corte, y + 2);
        forma.closePath();
        return forma;
    }
}
