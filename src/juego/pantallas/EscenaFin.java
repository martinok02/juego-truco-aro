package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import markov.ModeloMarkov;
import markov.ResultadoGeneracion;
import motor.Escena;
import persistencia.RepositorioCorpus;
import persistencia.RepositorioProgreso;
import persistencia.RepositorioProgreso.Progreso;
import persistencia.RepositorioRanking;
import ui.Texto;

public class EscenaFin extends Escena {

    private final boolean ganoJugador;
    private final int puntosJugador;
    private final int puntosRival;
    private final ModoPartido modo;     // gira o torneo: a donde se vuelve
    private final String resultado;     // lo que paso en la gira o el torneo ("Te quedan 4 vidas"...)
    private final String cronicaMarkov;

    // Botones (mouse o tecla)
    private final Boton botonSeguir = new Boton("CONTINUAR", "Enter", Estilo.AZUL, 130, 512, 180, 42);
    private final Boton botonRanking = new Boton("CLASIFICATORIA", "R", Estilo.CARAMELO, 318, 512, 196, 42);
    private final Boton botonMenu = new Boton("MENU", "Esc", Estilo.MAGENTA, 522, 512, 148, 42);
    private int mouseX = -1;
    private int mouseY = -1;

    public EscenaFin(boolean ganoJugador, int puntosJugador, int puntosRival, ModoPartido modo, String resultado) {
        this.ganoJugador = ganoJugador;
        this.puntosJugador = puntosJugador;
        this.puntosRival = puntosRival;
        this.modo = modo;
        this.resultado = (resultado == null) ? "" : resultado;

        // Cronica final con Markov: el rival habla segun como termino ("me ganaste..." / "se termino...")
        ModeloMarkov markov = RepositorioCorpus.crearModelo(2);
        ResultadoGeneracion gen = markov.generar(10, ganoJugador ? "me ganaste" : "se termino");
        this.cronicaMarkov = gen.getTextoParaMostrar(); // "." si termino sola, "..." si se corto por largo

        // Actualizar ranking en ABB
        actualizarRanking();
    }


    private void actualizarRanking() {
        // Acumula el partido en el registro del jugador (el partido puede ser a 15 o a 30)
        Progreso p = RepositorioProgreso.cargarProgreso();
        RepositorioRanking.registrarPartido(p.getNombre(), puntosJugador, ganoJugador);
    }

    @Override
    public void actualizar(double dt) {
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();
        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER) || (click && botonSeguir.contiene(mouseX, mouseY))) {
            getContexto().irA(modo != null ? modo.escenaDeVuelta() : new EscenaMenu());
        } else if (getEntrada().fuePresionada(KeyEvent.VK_R) || (click && botonRanking.contiene(mouseX, mouseY))) {
            getContexto().irA(new EscenaRanking(modo != null ? modo.escenaDeVuelta() : new EscenaMenu()));
        } else if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE) || getEntrada().fuePresionada(KeyEvent.VK_M)
                || (click && botonMenu.contiene(mouseX, mouseY))) {
            getContexto().irA(new EscenaMenu());
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        double centroX = ancho / 2.0;

        Estilo.fondo(g, ancho, alto);
        Estilo.panel(g, 110, 24, 580, 470);

        // Trofeo dorado si gano; gris si perdio
        Estilo.trofeo(g, centroX, 82, 1.0, ganoJugador ? Estilo.DORADO : new Color(140, 148, 165));
        if (ganoJugador) {
            Estilo.titulo(g, "¡Victoria criolla!", centroX, 152, 38);
            Texto.centrado(g, Estilo.recortar(g, resultado, 14, 540), centroX, 180, 14, Estilo.CREMA);
        } else {
            Estilo.titulo(g, "Derrota en la mesa", centroX, 152, 38);
            Texto.centrado(g, Estilo.recortar(g, resultado, 14, 540), centroX, 180, 14, Estilo.CREMA);
        }

        // Resultado: avatar, puntos y fosforos de cada uno
        dibujarJugador(g, 250, "VOS", puntosJugador, Estilo.AZUL);
        Texto.centrado(g, "vs", centroX, 262, 20, Estilo.TEXTO_SUAVE);
        dibujarJugador(g, 550, "RIVAL", puntosRival, new Color(150, 60, 160));

        // Cronica del partido generada por Markov, en un globo
        Estilo.globo(g, 160, 380, 480, 92, centroX, 360);
        Texto.centrado(g, "EL RIVAL DICE (Markov)", centroX, 404, 11, new Color(150, 96, 40));
        Texto.centrado(g, Estilo.recortar(g, "\"" + cronicaMarkov + "\"", 16, 440), centroX, 442, 16,
                new Color(30, 30, 40));

        botonSeguir.dibujar(g, botonSeguir.contiene(mouseX, mouseY), true);
        botonRanking.dibujar(g, botonRanking.contiene(mouseX, mouseY), true);
        botonMenu.dibujar(g, botonMenu.contiene(mouseX, mouseY), true);

        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }

    private void dibujarJugador(Graphics2D g, int centroX, String nombre, int puntos, Color color) {
        Estilo.avatar(g, centroX, 236, 30, nombre, color);
        Texto.centrado(g, nombre, centroX, 290, 13, Estilo.TEXTO_SUAVE);
        Texto.centrado(g, String.valueOf(puntos), centroX, 326, 30, Estilo.DORADO);
        int grupos = (Math.min(puntos, 30) + 4) / 5;            // grupos de 5 fosforos
        Estilo.fosforos(g, centroX - (grupos * 26 - 8) / 2, 340, puntos); // centrados
    }
}
