package juego.pantallas;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.ConfiguracionPartido;
import motor.Escena;
import ui.Texto;

/**
 * Pantalla previa a cada partido: se elige a cuantos puntos se juega (15 o 30)
 * y si se juega con flor. Despues arranca la EscenaPartida con esa configuracion.
 */
public class EscenaConfiguracion extends Escena {

    private final ModoPartido modo;
    private final Escena retorno;
    private final String nombreRival;

    // Lo elegido hasta ahora (por defecto: a 15 y sin flor, como el truco mas comun)
    private int puntos = ConfiguracionPartido.A_15;
    private boolean conFlor = false;

    private final Boton boton15 = new Boton("A 15 PUNTOS", "1", Estilo.AZUL, 186, 196, 206, 44);
    private final Boton boton30 = new Boton("A 30 PUNTOS", "2", Estilo.AZUL, 408, 196, 206, 44);
    private final Boton botonSinFlor = new Boton("SIN FLOR", null, Estilo.CARAMELO, 186, 348, 206, 44);
    private final Boton botonConFlor = new Boton("CON FLOR", null, Estilo.CARAMELO, 408, 348, 206, 44);
    private final Boton botonJugar = new Boton("¡A JUGAR!", "Enter", Estilo.VERDE, 408, 506, 242, 48);
    private final Boton botonVolver = new Boton("VOLVER", "Esc", Estilo.MAGENTA, 150, 506, 242, 48);
    private int mouseX = -1;
    private int mouseY = -1;

    /**
     * @param modo    gira o torneo: decide el rival y que pasa con el resultado
     * @param retorno a donde vuelve el boton VOLVER (el mapa de la gira o la llave)
     */
    public EscenaConfiguracion(ModoPartido modo, Escena retorno) {
        this.modo = modo;
        this.retorno = retorno;
        this.nombreRival = modo.crearRival().getNombreEstrategia();
    }

    @Override
    public void actualizar(double dt) {
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();

        if (getEntrada().fuePresionada(KeyEvent.VK_1) || (click && boton15.contiene(mouseX, mouseY))) {
            puntos = ConfiguracionPartido.A_15;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_2) || (click && boton30.contiene(mouseX, mouseY))) {
            puntos = ConfiguracionPartido.A_30;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_F)) {
            conFlor = !conFlor;
        }
        if (click && botonSinFlor.contiene(mouseX, mouseY)) conFlor = false;
        if (click && botonConFlor.contiene(mouseX, mouseY)) conFlor = true;

        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER) || (click && botonJugar.contiene(mouseX, mouseY))) {
            getContexto().irA(new EscenaPartida(modo, new ConfiguracionPartido(puntos, conFlor)));
        } else if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE) || (click && botonVolver.contiene(mouseX, mouseY))) {
            getContexto().irA(retorno != null ? retorno : new EscenaMenu());
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        double centroX = ancho / 2.0;

        Estilo.fondo(g, ancho, alto);
        Estilo.titulo(g, "Antes de repartir", centroX, 62, 36);
        Texto.centrado(g, modo.getTitulo() + "  ·  Rival: " + nombreRival, centroX, 92, 13,
                Estilo.TEXTO_SUAVE);

        Estilo.panel(g, 150, 116, 500, 360);

        Texto.centrado(g, "¿A CUANTOS PUNTOS?", centroX, 176, 14, Estilo.DORADO);
        boton15.dibujarOpcion(g, puntos == ConfiguracionPartido.A_15);
        boton30.dibujarOpcion(g, puntos == ConfiguracionPartido.A_30);
        String ayudaPuntos = (puntos == ConfiguracionPartido.A_15)
                ? "Partido corto. La falta envido vale lo que le falta al que va ganando."
                : "Con malas y buenas: si los dos estan en las malas, la falta envido gana el partido.";
        Texto.centrado(g, ayudaPuntos, centroX, 266, 12, Estilo.TEXTO_SUAVE);

        Texto.centrado(g, "¿SE JUEGA CON FLOR?", centroX, 328, 14, Estilo.DORADO);
        botonSinFlor.dibujarOpcion(g, !conFlor);
        botonConFlor.dibujarOpcion(g, conFlor);
        String ayudaFlor = conFlor
                ? "Tres cartas del mismo palo: suma 3 (flor contra flor, 4) y esa ronda no hay envido."
                : "Tres cartas del mismo palo cuentan solo para el envido.";
        Texto.centrado(g, ayudaFlor, centroX, 418, 12, Estilo.TEXTO_SUAVE);
        Texto.centrado(g, "Teclas: 1 / 2 para los puntos  ·  F para la flor", centroX, 452, 11,
                Estilo.TEXTO_SUAVE);

        botonVolver.dibujar(g, botonVolver.contiene(mouseX, mouseY), true);
        botonJugar.dibujar(g, botonJugar.contiene(mouseX, mouseY), true);

        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }
}
