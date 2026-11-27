package juego.pantallas;

import estructuras.Lista;
import estructuras.MiLista;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.ventana.OpcionesPantalla;
import juego.ventana.Resolucion;
import motor.Escena;
import persistencia.RepositorioOpciones;
import ui.Texto;

/**
 * Opciones de pantalla: resolucion y ventana / pantalla completa. Se abre desde el menu
 * principal y desde la pausa del partido; al volver, se regresa a la escena de origen.
 */
public class EscenaOpciones extends Escena {

    private final Escena retorno;
    private final OpcionesPantalla opciones;
    private final Lista<Resolucion> resoluciones = Resolucion.disponibles();
    private final Lista<Boton> botonesResolucion = new MiLista<>();
    private int seleccion;      // resolucion elegida (indice)
    private boolean completa;   // modo elegido

    private final Boton botonVentana = new Boton("VENTANA", null, Estilo.CARAMELO, 186, 330, 206, 44);
    private final Boton botonCompleta = new Boton("PANTALLA COMPLETA", null, Estilo.CARAMELO, 408, 330, 206, 44);
    private final Boton botonAplicar = new Boton("APLICAR", "Enter", Estilo.VERDE, 408, 506, 242, 48);
    private final Boton botonVolver = new Boton("VOLVER", "Esc", Estilo.MAGENTA, 150, 506, 242, 48);
    private int mouseX = -1;
    private int mouseY = -1;
    private String mensaje = "";

    public EscenaOpciones(Escena retorno) {
        this.retorno = retorno;
        this.opciones = RepositorioOpciones.cargar();
        this.seleccion = Math.max(0, resoluciones.indiceDe(opciones.getResolucion()));
        this.completa = opciones.isPantallaCompleta();
        for (int i = 0; i < resoluciones.tamanio(); i++) {
            int x = 186 + (i % 2) * 222;
            int y = 166 + (i / 2) * 56;
            botonesResolucion.agregar(new Boton(resoluciones.obtener(i).toString(), null, Estilo.AZUL, x, y, 206, 44));
        }
    }

    @Override
    public void actualizar(double dt) {
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();

        for (int i = 0; i < botonesResolucion.tamanio(); i++) {
            if (click && botonesResolucion.obtener(i).contiene(mouseX, mouseY)) seleccion = i;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_LEFT) || getEntrada().fuePresionada(KeyEvent.VK_UP)) {
            seleccion = (seleccion - 1 + resoluciones.tamanio()) % resoluciones.tamanio();
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_RIGHT) || getEntrada().fuePresionada(KeyEvent.VK_DOWN)) {
            seleccion = (seleccion + 1) % resoluciones.tamanio();
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_F)) completa = !completa;
        if (click && botonVentana.contiene(mouseX, mouseY)) completa = false;
        if (click && botonCompleta.contiene(mouseX, mouseY)) completa = true;

        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER) || (click && botonAplicar.contiene(mouseX, mouseY))) {
            opciones.setResolucion(resoluciones.obtener(seleccion));
            opciones.setPantallaCompleta(completa);
            RepositorioOpciones.guardar(opciones);
            opciones.aplicar();
            mensaje = "Opciones guardadas: " + resoluciones.obtener(seleccion) + (completa ? ", pantalla completa." : ", en ventana.");
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
        Estilo.titulo(g, "Opciones", centroX, 62, 36);
        Estilo.panel(g, 150, 100, 500, 380);

        Texto.centrado(g, "RESOLUCION", centroX, 148, 14, Estilo.DORADO);
        for (int i = 0; i < botonesResolucion.tamanio(); i++) {
            botonesResolucion.obtener(i).dibujarOpcion(g, i == seleccion);
        }
        Texto.centrado(g, "MODO", centroX, 312, 14, Estilo.DORADO);
        botonVentana.dibujarOpcion(g, !completa);
        botonCompleta.dibujarOpcion(g, completa);
        Texto.centrado(g, completa ? "Ocupa todo el monitor (sin bordes). La resolucion se ajusta sola."
                : "Ventana del tamanio elegido.", centroX, 400, 12, Estilo.TEXTO_SUAVE);
        Texto.centrado(g, "Flechas: resolucion  ·  F: ventana / pantalla completa", centroX, 430, 11, Estilo.TEXTO_SUAVE);
        if (!mensaje.isEmpty()) {
            Texto.centrado(g, mensaje, centroX, 462, 12, Estilo.CREMA);
        }

        botonVolver.dibujar(g, botonVolver.contiene(mouseX, mouseY), true);
        botonAplicar.dibujar(g, botonAplicar.contiene(mouseX, mouseY), true);
        Notificaciones.dibujar(g, ancho); // avisos de datos/ encima de todo
    }
}
