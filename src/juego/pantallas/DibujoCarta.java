package juego.pantallas;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import estructuras.Lista;
import estructuras.MiLista;
import juego.reglas.Carta;
import juego.reglas.Palo;
import persistencia.RepositorioRecursos;
import ui.Texto;

/**
 * Dibuja las cartas con las imagenes pixel art de datos/cartas (mazo "Spanish Deck - Pixel Art").
 * Las 40 imagenes se guardan en una Lista propia en el orden de los archivos: oros (1 a 10),
 * copas (11 a 20), espadas (21 a 30) y bastos (31 a 40), cada palo del 1 al 7 y del 10 al 12.
 * Si una imagen falta, la carta se dibuja simple (numero y palo) y el juego sigue.
 */
public final class DibujoCarta {

    public static final double PROPORCION = 113.0 / 73.0;

    private static final String RUTA_CARTAS = "datos/cartas/free_deck/";
    private static final int CARTAS_POR_PALO = 10;
    private static final Lista<BufferedImage> FRENTES = cargarFrentes();
    private static final BufferedImage DORSO = RepositorioRecursos.cargarImagen(RUTA_CARTAS + "back.PNG");

    private DibujoCarta() {
    }

    public static Color colorPalo(Palo palo) {
        switch (palo) {
            case ESPADA: return new Color(43, 116, 153);
            case COPA: return new Color(190, 53, 49);
            case ORO: return new Color(226, 174, 42);
            default: return new Color(54, 117, 70);
        }
    }

    public static void frente(Graphics2D g, Carta carta, double centroX, double centroY,
            double ancho, double angulo) {
        frente(g, carta, centroX, centroY, ancho, angulo, 1f);
    }

    public static void frente(Graphics2D g, Carta carta, double centroX, double centroY,
            double ancho, double angulo, float opacidad) {
        BufferedImage imagen = FRENTES.obtener(indiceImagen(carta));
        if (imagen == null) {
            dibujarSimple(g, carta.getNumero() + "", carta.getPalo().name(), colorPalo(carta.getPalo()),
                    centroX, centroY, ancho, angulo, opacidad);
            return;
        }
        dibujarImagen(g, imagen, centroX, centroY, ancho, angulo, opacidad);
    }

    public static void frente(Graphics2D g, Carta carta, double centroX, double centroY,
            double ancho, double angulo, float opacidad, Color marco) {
        AffineTransform transformacion = g.getTransform();
        Composite composicion = g.getComposite();
        try {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
            g.translate(centroX, centroY);
            g.rotate(angulo);
            double alto = ancho * PROPORCION;
            double borde = Math.max(2, ancho * 0.055);
            g.setColor(marco);
            g.fillRect((int) Math.floor(-ancho / 2 - borde),
                    (int) Math.floor(-alto / 2 - borde),
                    (int) Math.ceil(ancho + borde * 2),
                    (int) Math.ceil(alto + borde * 2));
        } finally {
            g.setComposite(composicion);
            g.setTransform(transformacion);
        }
        frente(g, carta, centroX, centroY, ancho, angulo, opacidad);
    }

    public static void dorso(Graphics2D g, double centroX, double centroY, double ancho, double angulo) {
        if (DORSO == null) {
            dibujarSimple(g, "", "", new Color(43, 80, 140), centroX, centroY, ancho, angulo, 1f);
            return;
        }
        dibujarImagen(g, DORSO, centroX, centroY, ancho, angulo, 1f);
    }

    private static void dibujarImagen(Graphics2D g, BufferedImage imagen, double centroX,
            double centroY, double ancho, double angulo, float opacidad) {
        AffineTransform transformacion = g.getTransform();
        Composite composicion = g.getComposite();
        RenderingHints pistas = g.getRenderingHints();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
            g.translate(centroX, centroY);
            g.rotate(angulo);
            int anchoImagen = (int) Math.round(ancho);
            int altoImagen = (int) Math.round(ancho * PROPORCION);
            g.drawImage(imagen, -anchoImagen / 2, -altoImagen / 2,
                    anchoImagen, altoImagen, null);
        } finally {
            g.setComposite(composicion);
            g.setTransform(transformacion);
            g.setRenderingHints(pistas);
        }
    }

    /** Carga las 40 imagenes en el orden de los archivos (1.PNG a 40.PNG). */
    private static Lista<BufferedImage> cargarFrentes() {
        Lista<BufferedImage> frentes = new MiLista<>();
        for (int archivo = 1; archivo <= 4 * CARTAS_POR_PALO; archivo++) {
            frentes.agregar(RepositorioRecursos.cargarImagen(RUTA_CARTAS + archivo + ".PNG"));
        }
        return frentes;
    }

    /**
     * Posicion de la carta en la lista: el palo marca el bloque de 10 y dentro del bloque
     * el 1..7 ocupan las posiciones 0..6 y el 10, 11 y 12 las posiciones 7, 8 y 9.
     */
    private static int indiceImagen(Carta carta) {
        int numero = carta.getNumero();
        int posicion = (numero <= 7) ? numero - 1 : numero - 3;
        return bloqueDelPalo(carta.getPalo()) * CARTAS_POR_PALO + posicion;
    }

    private static int bloqueDelPalo(Palo palo) {
        switch (palo) {
            case ORO: return 0;
            case COPA: return 1;
            case ESPADA: return 2;
            default: return 3; // BASTO
        }
    }

    /** Reemplazo si falta la imagen: rectangulo con el numero y el palo, del color del palo. */
    private static void dibujarSimple(Graphics2D g, String numero, String palo, Color color,
            double centroX, double centroY, double ancho, double angulo, float opacidad) {
        AffineTransform transformacion = g.getTransform();
        Composite composicion = g.getComposite();
        try {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
            g.translate(centroX, centroY);
            g.rotate(angulo);
            int w = (int) Math.round(ancho);
            int h = (int) Math.round(ancho * PROPORCION);
            g.setColor(numero.isEmpty() ? color : new Color(246, 237, 211));
            g.fillRect(-w / 2, -h / 2, w, h);
            g.setColor(color);
            g.drawRect(-w / 2, -h / 2, w - 1, h - 1);
            if (!numero.isEmpty()) {
                Texto.centrado(g, numero, 0, -h * 0.05, (int) Math.max(10, ancho * 0.32), color);
                Texto.centrado(g, palo, 0, h * 0.25, (int) Math.max(7, ancho * 0.12), color);
            }
        } finally {
            g.setComposite(composicion);
            g.setTransform(transformacion);
        }
    }
}
