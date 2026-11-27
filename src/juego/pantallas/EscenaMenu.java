package juego.pantallas;

import estructuras.Lista;
import estructuras.MiLista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.audio.EfectoReparto;
import juego.audio.MusicaTango;
import juego.reglas.Carta;
import juego.reglas.Etapa;
import juego.reglas.Gira;
import juego.reglas.Palo;
import motor.Escena;
import persistencia.RepositorioGira;
import ui.Texto;

/**
 * Menu principal: Gira, Torneo, Clasificatoria, Opciones, Demo Markov y Salir.
 * Si hay una gira guardada, "Gira" pregunta si continuarla o empezar una nueva.
 */
public class EscenaMenu extends Escena {

    // Opciones del menu como botones (Lista propia, sin arreglos). El indice es la opcion.
    private final Lista<Boton> opciones = new MiLista<>();
    private final Lista<Boton> opcionesGira = new MiLista<>();
    private boolean eligiendoGira = false;
    private Gira giraGuardada = null;

    // Cartas decorativas del titulo (el "ancho de espadas" y el "ancho de bastos")
    private final Carta cartaIzquierda = new Carta(1, Palo.ESPADA);
    private final Carta cartaDerecha = new Carta(1, Palo.BASTO);

    private int seleccionActual = 0;

    public EscenaMenu() {
        agregar(opciones, "Gira", 214);
        agregar(opciones, "Torneo", 214);
        agregar(opciones, "Clasificatoria", 214);
        agregar(opciones, "Opciones", 214);
        agregar(opciones, "Demo Markov", 214);
        agregar(opciones, "Salir", 214);
        agregar(opcionesGira, "Continuar gira", 250);
        agregar(opcionesGira, "Nueva gira", 250);
        agregar(opcionesGira, "Volver", 250);
    }

    @Override
    public void alEntrar() {
        EfectoReparto.reproducir();
    }

    @Override
    public void alSalir() {
        EfectoReparto.detener();
    }

    private void agregar(Lista<Boton> lista, String texto, int yInicial) {
        int indice = lista.tamanio();
        lista.agregar(new Boton(texto, null, Estilo.AZUL, 270, yInicial + indice * 54, 260, 42));
    }

    private Lista<Boton> visibles() {
        return eligiendoGira ? opcionesGira : opciones;
    }

    @Override
    public void actualizar(double dt) {
        Lista<Boton> lista = visibles();
        int cantidad = lista.tamanio();
        if (getEntrada().fuePresionada(KeyEvent.VK_UP) || getEntrada().fuePresionada(KeyEvent.VK_W)) {
            seleccionActual = (seleccionActual - 1 + cantidad) % cantidad;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_DOWN) || getEntrada().fuePresionada(KeyEvent.VK_S)) {
            seleccionActual = (seleccionActual + 1) % cantidad;
        }
        if (eligiendoGira && getEntrada().fuePresionada(KeyEvent.VK_ESCAPE)) {
            volverAlMenu();
            return;
        }

        // Mouse: pasar por encima selecciona, click confirma
        int mx = getEntrada().getMouseX();
        int my = getEntrada().getMouseY();
        boolean clickEnOpcion = false;
        for (int i = 0; i < cantidad; i++) {
            if (lista.obtener(i).contiene(mx, my)) {
                seleccionActual = i;
                clickEnOpcion = getEntrada().huboClick();
            }
        }
        if (clickEnOpcion || getEntrada().fuePresionada(KeyEvent.VK_ENTER) || getEntrada().fuePresionada(KeyEvent.VK_SPACE)) {
            if (eligiendoGira) {
                ejecutarOpcionGira(seleccionActual);
            } else {
                ejecutarOpcion(seleccionActual);
            }
        }
    }

    private void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 0: // Gira: si hay una guardada se pregunta si continuarla
                giraGuardada = RepositorioGira.existe() ? RepositorioGira.cargar() : null;
                if (giraGuardada != null && giraGuardada.tieneProgreso()) {
                    eligiendoGira = true;
                    seleccionActual = 0;
                } else {
                    getContexto().irA(new EscenaGira(Gira.nueva(), null, "La gira arranca en Formosa. ¡Suerte!"));
                }
                break;
            case 1: // Torneo
                getContexto().irA(EscenaLlave.desdeArchivo());
                break;
            case 2: // Clasificatoria (ranking)
                getContexto().irA(new EscenaRanking(this));
                break;
            case 3: // Opciones
                getContexto().irA(new EscenaOpciones(this));
                break;
            case 4: // Demo Markov
                getContexto().irA(new EscenaLaboratorioMarkov(this));
                break;
            default: // Salir
                getContexto().salir();
                break;
        }
    }

    private void ejecutarOpcionGira(int opcion) {
        if (opcion == 0) {
            getContexto().irA(new EscenaGira(giraGuardada, null, ""));
        } else if (opcion == 1) {
            RepositorioGira.borrar();
            getContexto().irA(new EscenaGira(Gira.nueva(), null, "La gira arranca en Formosa. ¡Suerte!"));
        } else {
            volverAlMenu();
        }
    }

    private void volverAlMenu() {
        eligiendoGira = false;
        seleccionActual = 0;
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();

        Estilo.fondo(g, ancho, alto);

        // Cartas del titulo a los costados, inclinadas
        DibujoCarta.frente(g, cartaIzquierda, 86, 112, 80, -0.28);
        DibujoCarta.frente(g, cartaDerecha, 714, 112, 80, 0.28);

        Estilo.titulo(g, "TrucoAro", ancho / 2.0, 98, 48);
        Estilo.pildora(g, eligiendoGira ? "GIRA POR ARGENTINA" : "EL TRUCO DE TODO EL PAIS", ancho / 2.0, 138, 14,
                Estilo.CARAMELO, Estilo.TEXTO);

        Lista<Boton> lista = visibles();
        int altoPanel = 30 + lista.tamanio() * 54;
        int yPanel = (eligiendoGira ? 250 : 214) - 22;
        Estilo.panel(g, 246, yPanel, 308, altoPanel);
        for (int i = 0; i < lista.tamanio(); i++) {
            lista.obtener(i).dibujarOpcionMenu(g, i == seleccionActual);
        }
        if (eligiendoGira && giraGuardada != null) {
            Etapa actual = giraGuardada.getProvinciaActual();
            String detalle = (actual == null) ? "Tu gira guardada esta completa."
                    : "Tu gira va por " + actual.getNombre() + " (" + giraGuardada.getRegionActual().getNombre() + "), "
                    + giraGuardada.getVidas() + " vidas.";
            Texto.centrado(g, detalle, ancho / 2.0, 214, 12, Estilo.DORADO);
        }

        Texto.centrado(g, "Flechas o mouse: elegir  ·  Enter: confirmar  ·  F6: musica "
                + (MusicaTango.estaActiva() ? "ON" : "OFF"), ancho / 2.0, alto - 14, 11,
                new Color(178, 192, 222, 200));
        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }
}
