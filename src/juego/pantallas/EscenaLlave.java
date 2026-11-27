package juego.pantallas;

import estructuras.Lista;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.Partido;
import juego.reglas.Torneo;
import motor.Escena;
import persistencia.RepositorioLlave;
import ui.Texto;

/**
 * Torneo eliminatorio: la llave (arbol binario de 7 partidos) recorrida por niveles.
 * Se juega el partido que le toca al jugador; los demas se resuelven solos y los ganadores
 * suben en el arbol. Perder te elimina; ganar la final te hace campeon.
 */
public class EscenaLlave extends Escena {

    private static final int CAJA_ANCHO = 170;
    private static final int CAJA_ALTO = 64;

    private final Torneo torneo;
    private String mensaje;
    private double tiempoMensaje = 5;

    private final Boton botonMenu = new Boton("MENU", "Esc", Estilo.MAGENTA, 120, 538, 190, 42);
    private final Boton botonJugar = new Boton("JUGAR", "Enter", Estilo.VERDE, 330, 538, 350, 42);
    private int mouseX = -1;
    private int mouseY = -1;

    public EscenaLlave(Torneo torneo, String mensaje) {
        this.torneo = torneo;
        this.mensaje = (mensaje == null) ? "" : mensaje;
    }

    /** Torneo guardado en datos/llave.txt (o uno nuevo si no hay). */
    public static EscenaLlave desdeArchivo() {
        return new EscenaLlave(Torneo.desdeLlave(RepositorioLlave.cargarLlave(), null), "");
    }

    @Override
    public void actualizar(double dt) {
        tiempoMensaje -= dt;
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();

        if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE) || (click && botonMenu.contiene(mouseX, mouseY))) {
            getContexto().irA(new EscenaMenu());
        } else if (getEntrada().fuePresionada(KeyEvent.VK_ENTER) || (click && botonJugar.contiene(mouseX, mouseY))) {
            if (torneo.isTerminado()) {
                // Nuevo torneo: llave inicial, guardada
                RepositorioLlave.guardarLlave(RepositorioLlave.crearLlavePorDefecto());
                getContexto().irA(new EscenaLlave(Torneo.desdeLlave(RepositorioLlave.crearLlavePorDefecto(), null),
                        "Empieza un torneo nuevo. ¡Suerte en cuartos!"));
            } else {
                getContexto().irA(new EscenaConfiguracion(new ModoTorneo(torneo), this));
            }
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();

        Estilo.fondo(g, ancho, alto);
        Estilo.titulo(g, "Torneo", ancho / 2.0, 50, 34);
        Texto.centrado(g, "Llave eliminatoria  ·  arbol binario recorrido por niveles con Cola propia", ancho / 2.0, 76, 12,
                Estilo.TEXTO_SUAVE);

        // Partidos en orden por niveles: raiz = final, luego semis, luego cuartos
        Lista<Partido> partidos = torneo.getLlave().porNiveles();
        conectarCodo(g, 80, 120, 330, 170);
        conectarCodo(g, 80, 220, 330, 170);
        conectarCodo(g, 80, 340, 330, 390);
        conectarCodo(g, 80, 440, 330, 390);
        conectarCodo(g, 330, 170, 580, 280);
        conectarCodo(g, 330, 390, 580, 280);
        dibujarCajaPartido(g, 80, 120, partidos.obtener(3), "CUARTOS 1");
        dibujarCajaPartido(g, 80, 220, partidos.obtener(4), "CUARTOS 2");
        dibujarCajaPartido(g, 80, 340, partidos.obtener(5), "CUARTOS 3");
        dibujarCajaPartido(g, 80, 440, partidos.obtener(6), "CUARTOS 4");
        dibujarCajaPartido(g, 330, 170, partidos.obtener(1), "SEMIFINAL 1");
        dibujarCajaPartido(g, 330, 390, partidos.obtener(2), "SEMIFINAL 2");
        Estilo.trofeo(g, 580 + CAJA_ANCHO / 2.0, 232, 0.7, torneo.isCampeon() ? Estilo.DORADO : new Color(200, 170, 90));
        dibujarCajaPartido(g, 580, 280, partidos.obtener(0), "GRAN FINAL");

        // Estado del jugador
        String estado;
        if (torneo.isCampeon()) {
            estado = "¡CAMPEON DEL TORNEO!";
        } else if (torneo.isEliminado()) {
            estado = "Quedaste eliminado";
        } else {
            Partido propio = torneo.getPartidoDelJugador();
            estado = "Te toca: " + Torneo.nombreRonda(propio.getRonda()) + " vs " + torneo.getRivalActual();
        }
        Estilo.pildora(g, estado, 665, 470, 12, torneo.isCampeon() ? Estilo.DORADO : new Color(10, 14, 30, 210),
                torneo.isCampeon() ? new Color(40, 30, 10) : Estilo.TEXTO);

        Boton jugar = torneo.isTerminado()
                ? new Boton("NUEVO TORNEO", "Enter", Estilo.CARAMELO, 330, 538, 350, 42)
                : new Boton("JUGAR " + Torneo.nombreRonda(torneo.getPartidoDelJugador().getRonda()).toUpperCase(), "Enter",
                        Estilo.VERDE, 330, 538, 350, 42);
        jugar.dibujar(g, jugar.contiene(mouseX, mouseY), true);
        botonMenu.dibujar(g, botonMenu.contiene(mouseX, mouseY), true);

        if (!mensaje.isEmpty() && tiempoMensaje > 0) {
            Estilo.pildora(g, Estilo.recortar(g, mensaje, 13, 600), ancho / 2.0, 100, 13, Estilo.DORADO, new Color(40, 30, 10));
        }
        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }

    /** Linea en codo desde el borde derecho de una caja al borde izquierdo de la siguiente ronda. */
    private void conectarCodo(Graphics2D g, int x1, int y1, int x2, int y2) {
        int desdeX = x1 + CAJA_ANCHO;
        int desdeY = y1 + CAJA_ALTO / 2;
        int hastaY = y2 + CAJA_ALTO / 2;
        int medioX = (desdeX + x2) / 2;
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(150, 180, 235, 130));
        g.drawLine(desdeX, desdeY, medioX, desdeY);
        g.drawLine(medioX, desdeY, medioX, hastaY);
        g.drawLine(medioX, hastaY, x2, hastaY);
    }

    private void dibujarCajaPartido(Graphics2D g, int x, int y, Partido p, String etapa) {
        boolean esFinal = etapa.equals("GRAN FINAL"); // ojo: "SEMIFINAL" tambien contiene "FINAL"
        boolean delJugador = p.getJugadorA().equals(torneo.getJugador()) || p.getJugadorB().equals(torneo.getJugador());
        boolean seJuega = p == torneo.getPartidoDelJugador();
        Color borde = seJuega ? Estilo.DORADO : (p.isJugado() ? new Color(90, 220, 150) : (esFinal ? Estilo.DORADO : Estilo.PANEL_BORDE));
        Color relleno = seJuega ? new Color(84, 66, 30, 230) : (esFinal ? new Color(84, 66, 30, 200) : Estilo.PANEL);
        Estilo.panel(g, x, y, CAJA_ANCHO, CAJA_ALTO, relleno, borde);

        Texto.izquierda(g, etapa + "  ·  " + p.getSede().toUpperCase(), x + 12, y + 18, 9,
                esFinal || seJuega ? Estilo.DORADO : Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, Estilo.recortar(g, p.getJugadorA() + " vs " + p.getJugadorB(), 12, CAJA_ANCHO - 24),
                x + 12, y + 38, 12, delJugador ? Estilo.CREMA : Estilo.TEXTO);
        if (p.isJugado()) {
            boolean ganoJugador = p.getGanador().equals(torneo.getJugador());
            Texto.izquierda(g, "Gano: " + p.getGanador(), x + 12, y + 55, 11,
                    delJugador && !ganoJugador ? new Color(255, 140, 160) : new Color(120, 240, 170));
        } else {
            Texto.izquierda(g, seJuega ? "Te toca jugar" : "Pendiente", x + 12, y + 55, 11,
                    seJuega ? Estilo.DORADO : new Color(170, 178, 196));
        }
    }
}
