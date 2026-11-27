package juego.ventana;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import juego.audio.MusicaTango;
import motor.Escena;
import motor.Motor;

/**
 * Ventana del juego con resolucion configurable y pantalla completa.
 *
 * El kit trae motor.Ventana, que es de tamanio fijo y no se puede modificar. Esta clase
 * usa el mismo Motor del kit (su API publica) pero:
 * - el juego siempre se dibuja en 800x600 "logicos" y se ESCALA con una transformacion
 *   (no se estira una imagen, asi el texto queda nitido en cualquier resolucion);
 * - si la pantalla tiene otra proporcion, se centra con bandas negras a los costados;
 * - la posicion del mouse se convierte de pixeles reales a coordenadas logicas.
 */
public final class VentanaJuego {

    public static final int ANCHO_LOGICO = 800;
    public static final int ALTO_LOGICO = 600;
    private static final int MAXIMO_PASOS = 12;

    private static VentanaJuego instancia;

    private final Motor motor = new Motor(ANCHO_LOGICO, ALTO_LOGICO);
    private final String titulo;
    private final JPanel panel;
    private JFrame marco;
    private Timer reloj;
    private long ultimo;
    private double acumulado;

    // Escala y desplazamiento actuales (para dibujar y para convertir el mouse)
    private double escala = 1;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;

    private VentanaJuego(String titulo) {
        this.titulo = titulo;
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujar((Graphics2D) g);
            }
        };
        panel.setBackground(Color.BLACK);
        panel.setFocusable(true);
        panel.setFocusTraversalKeysEnabled(false); // asi Tab tambien llega al juego
        panel.addKeyListener(motor.getEntrada());
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    moverMouse(e);
                    motor.getEntrada().apretarMouse();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    motor.getEntrada().soltarMouse();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                moverMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                moverMouse(e);
            }
        };
        panel.addMouseListener(mouse);
        panel.addMouseMotionListener(mouse);
    }

    /** Abre el juego con la resolucion y el modo elegidos (desde main). */
    public static void abrir(String titulo, Escena inicial, int ancho, int alto, boolean pantallaCompleta) {
        SwingUtilities.invokeLater(() -> {
            instancia = new VentanaJuego(titulo);
            instancia.motor.iniciar(inicial);
            instancia.armarMarco(ancho, alto, pantallaCompleta);
            instancia.iniciarReloj();
        });
    }

    /**
     * Cambia la resolucion o el modo de pantalla mientras se juega (lo usa la pantalla de
     * Opciones). Si no hay ventana abierta (por ejemplo, en las pruebas) no hace nada.
     */
    public static void aplicar(int ancho, int alto, boolean pantallaCompleta) {
        if (instancia != null) {
            instancia.armarMarco(ancho, alto, pantallaCompleta);
        }
    }

    /** Crea (o vuelve a crear) el marco: con bordes y tamanio fijo, o sin bordes ocupando la pantalla. */
    private void armarMarco(int ancho, int alto, boolean pantallaCompleta) {
        GraphicsConfiguration monitor = null;
        if (marco != null) {
            monitor = marco.getGraphicsConfiguration();
            marco.remove(panel);
            marco.dispose(); // para cambiar los bordes hay que cerrar el marco
        }
        marco = (monitor == null) ? new JFrame(titulo) : new JFrame(titulo, monitor);
        marco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marco.add(panel);
        if (pantallaCompleta) {
            Rectangle pantalla = marco.getGraphicsConfiguration().getBounds();
            marco.setUndecorated(true);
            marco.setResizable(false);
            marco.setBounds(pantalla);
        } else {
            panel.setPreferredSize(new Dimension(ancho, alto));
            marco.setResizable(false);
            marco.pack();
            marco.setLocationRelativeTo(null);
        }
        marco.setVisible(true);
        panel.requestFocusInWindow();
    }

    private void iniciarReloj() {
        ultimo = System.nanoTime();
        reloj = new Timer(8, e -> tick());
        reloj.start();
    }

    /** Mismo bucle de paso fijo que el kit: varias actualizaciones a 120 por segundo y un dibujo. */
    private void tick() {
        long ahora = System.nanoTime();
        acumulado += Math.min((ahora - ultimo) / 1e9, 0.25);
        ultimo = ahora;
        int pasos = 0;
        while (acumulado >= Motor.PASO && pasos < MAXIMO_PASOS) {
            if (motor.getEntrada().fuePresionada(KeyEvent.VK_F6)) {
                MusicaTango.alternar();
            }
            motor.paso();
            acumulado -= Motor.PASO;
            pasos++;
        }
        if (pasos == MAXIMO_PASOS) {
            acumulado = 0;
        }
        if (motor.estaTerminado()) {
            reloj.stop();
            marco.dispose();
            System.exit(0);
        }
        panel.repaint();
    }

    /** Escala el juego al tamanio del panel manteniendo la proporcion 4:3. */
    private void dibujar(Graphics2D g) {
        int anchoPanel = panel.getWidth();
        int altoPanel = panel.getHeight();
        escala = Math.min(anchoPanel / (double) ANCHO_LOGICO, altoPanel / (double) ALTO_LOGICO);
        desplazamientoX = (anchoPanel - ANCHO_LOGICO * escala) / 2.0;
        desplazamientoY = (altoPanel - ALTO_LOGICO * escala) / 2.0;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        AffineTransform transformacion = new AffineTransform();
        transformacion.translate(desplazamientoX, desplazamientoY);
        transformacion.scale(escala, escala);
        g2.transform(transformacion);
        g2.clipRect(0, 0, ANCHO_LOGICO, ALTO_LOGICO);
        motor.dibujar(g2);
        g2.dispose();
    }

    /** Pixeles reales del panel -> coordenadas logicas 800x600 del juego. */
    private void moverMouse(MouseEvent e) {
        int x = (int) Math.round((e.getX() - desplazamientoX) / escala);
        int y = (int) Math.round((e.getY() - desplazamientoY) / escala);
        motor.getEntrada().moverMouse(x, y);
    }
}
