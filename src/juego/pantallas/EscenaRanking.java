package juego.pantallas;

import arboles.ABB;
import estructuras.Lista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.ClaveRanking;
import motor.Escena;
import persistencia.RepositorioRanking;
import ui.Texto;

public class EscenaRanking extends Escena {

    private final Escena retorno;
    private final ABB<ClaveRanking> rankingABB;
    private final Lista<ClaveRanking> listaRanking;

    private final Boton botonVolver = new Boton("VOLVER", "Esc", Estilo.MAGENTA, 300, 536, 200, 42);
    private int mouseX = -1;
    private int mouseY = -1;

    public EscenaRanking(Escena retorno) {
        this.retorno = retorno;
        this.rankingABB = RepositorioRanking.cargarRanking();
        // Recorrido In-Orden Iterativo exigido por la catedra utilizando MiPila
        this.listaRanking = rankingABB.inOrdenIterativo();
    }

    @Override
    public void actualizar(double dt) {
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick() && botonVolver.contiene(mouseX, mouseY);
        if (click || getEntrada().fuePresionada(KeyEvent.VK_ENTER) || getEntrada().fuePresionada(KeyEvent.VK_ESCAPE)) {
            getContexto().irA(retorno != null ? retorno : new EscenaMenu());
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();

        Estilo.fondo(g, ancho, alto);
        Estilo.titulo(g, "Clasificatoria", ancho / 2.0, 56, 34);
        Texto.centrado(g, "Top 10  ·  ABB con poda (eliminarMin) e in-orden iterativo con Pila propia", ancho / 2.0, 82, 12,
                Estilo.TEXTO_SUAVE);

        Estilo.panel(g, 60, 96, 680, 424);
        Texto.izquierda(g, "POS", 84, 124, 11, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, "JUGADOR", 150, 124, 11, Estilo.TEXTO_SUAVE);
        Texto.centrado(g, "PUNTOS", 450, 124, 11, Estilo.TEXTO_SUAVE);
        Texto.centrado(g, "VICTORIAS", 560, 124, 11, Estilo.TEXTO_SUAVE);
        Texto.centrado(g, "PARTIDAS", 666, 124, 11, Estilo.TEXTO_SUAVE);

        // listaRanking viene en orden ascendente (in-orden); se muestra de mayor a menor
        int total = listaRanking.tamanio();
        if (total == 0) {
            Texto.centrado(g, "Todavia no hay partidas registradas. ¡Juga la primera!", ancho / 2.0, 300, 15, Estilo.TEXTO_SUAVE);
        }
        for (int i = 0; i < total && i < 10; i++) {
            ClaveRanking c = listaRanking.obtener(total - 1 - i);
            int y = 136 + i * 37;
            g.setColor(new Color(255, 255, 255, i % 2 == 0 ? 16 : 7));
            g.fillRoundRect(74, y, 652, 32, 16, 16);
            dibujarPosicion(g, i, 98, y + 16);
            Texto.izquierda(g, c.getNombre(), 150, y + 21, 14, Estilo.TEXTO);
            Texto.centrado(g, String.valueOf(c.getPuntaje()), 450, y + 21, 15, Estilo.DORADO);
            Texto.centrado(g, String.valueOf(c.getVictorias()), 560, y + 21, 14, Estilo.TEXTO);
            Texto.centrado(g, String.valueOf(c.getPartidas()), 666, y + 21, 14, Estilo.TEXTO);
        }

        botonVolver.dibujar(g, botonVolver.contiene(mouseX, mouseY), true);

        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }

    /** Medalla de oro, plata o bronce para los tres primeros; numero simple para el resto. */
    private void dibujarPosicion(Graphics2D g, int indice, int cx, int cy) {
        Color medalla = null;
        if (indice == 0) medalla = new Color(246, 196, 60);
        if (indice == 1) medalla = new Color(196, 204, 220);
        if (indice == 2) medalla = new Color(205, 128, 60);
        if (medalla != null) {
            g.setColor(medalla);
            g.fillOval(cx - 13, cy - 13, 26, 26);
            Texto.centrado(g, String.valueOf(indice + 1), cx, cy + 5, 13, new Color(40, 30, 20));
        } else {
            Texto.centrado(g, String.valueOf(indice + 1), cx, cy + 5, 13, Estilo.TEXTO_SUAVE);
        }
    }
}
