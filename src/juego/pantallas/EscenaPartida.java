package juego.pantallas;

import estructuras.Lista;
import estructuras.MiLista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import juego.audio.MusicaTango;
import juego.reglas.Accion;
import juego.reglas.BazaJugada;
import juego.reglas.Canto;
import juego.reglas.Carta;
import juego.reglas.ConfiguracionPartido;
import juego.reglas.IARival;
import juego.reglas.InterpreteCantos;
import juego.reglas.Lado;
import juego.reglas.MesaTruco;
import juego.pantallas.ModoPartido.PerfilRival;
import markov.ModeloMarkov;
import markov.ResultadoGeneracion;
import motor.Escena;
import persistencia.RepositorioCorpus;
import persistencia.RepositorioProgreso;
import persistencia.RepositorioProgreso.Progreso;
import ui.Texto;

/**
 * Pantalla del partido. Las reglas viven en MesaTruco (juego.reglas); esta escena solo:
 * - dibuja la mesa, las cartas, el tanteador y los botones de lo que se puede cantar;
 * - traduce clicks y teclas en jugadas del jugador;
 * - le da al rival (IARival) un momento para "pensar" antes de cada jugada;
 * - muestra los eventos de la mesa en orden, uno por vez.
 */
public class EscenaPartida extends Escena {

    private static final double PAUSA_RIVAL = 1.0;          // segundos que "piensa" el rival
    private static final double PAUSA_EVENTO = 2.4;         // segundos que se lee cada mensaje
    private static final double PAUSA_EVENTO_RAPIDA = 1.6;  // si hay varios en fila, un poco menos cada uno
    private static final double LECTURA_MINIMA = 1.6;       // el rival no juega hasta que se leyo el ultimo mensaje
    private static final double PAUSA_CIERRE = 3.2;         // la mesa de la mano terminada queda a la vista
    private static final double PAUSA_FINAL = 3.5;          // antes de ir a la pantalla de fin
    private static final double APARICION = 0.3;            // segundos de la animacion de entrada de un mensaje
    private static final int MAX_CRONICA = 6;               // mensajes que guarda la cronica de la mesa

    private final ModoPartido modo;      // gira o torneo: que pasa con el resultado y a donde se vuelve
    private final MesaTruco mesa;
    private final IARival rival;

    // Markov: lo que dice el rival. Se genera con una "semilla" segun lo que pasa en la mesa
    // (al repartir "buenas", al cantar "truco", al ganar una baza "toma", etc.)
    private final ModeloMarkov modeloMarkov;
    private String fraseRival = "";
    private int rondaConFrase = 0;
    private int bazasComentadas = 0;

    private String mensajeMesa = "";
    private double tiempoMensaje = 0;  // cuanto hace que se muestra el mensaje actual (animacion y lectura)
    private final Lista<String> cronica = new MiLista<>(); // ultimos mensajes, el mas nuevo al final
    private double esperaRival = 0;
    private double esperaEvento = 0;
    private double esperaFinal = 0;

    // Cierre de mano: la mesa ya repartio la ronda nueva, pero la pantalla sigue mostrando como
    // termino la anterior (sus bazas y quien se la llevo) hasta que pasa la pausa o el jugador sigue.
    private int rondaMostrada = 1;
    private boolean cerrandoRonda = false;
    private double tiempoCierre = 0;
    private boolean partidoCerrado = false;

    // Interfaz: botones de las acciones disponibles (se arman de nuevo en cada cuadro)
    private final Lista<Accion> accionesVisibles = new MiLista<>();
    private final Lista<Boton> botonesAccion = new MiLista<>();
    private final Boton botonPausa = new Boton("PAUSA", "Esc", new Color(70, 86, 132), 640, 16, 144, 34);

    // Pausa: Esc abre un menu con OPCIONES y SALIR; salir pide confirmacion porque abandonar cuesta
    private boolean enPausa = false;
    private boolean confirmandoSalida = false;
    private final Boton botonOpciones = new Boton("OPCIONES", null, Estilo.AZUL, 290, 250, 220, 46);
    private final Boton botonSalirPausa = new Boton("SALIR", null, Estilo.MAGENTA, 290, 310, 220, 46);
    private final Boton botonConfirmar = new Boton("SI, ABANDONAR", null, Estilo.MAGENTA, 196, 330, 200, 46);
    private final Boton botonCancelar = new Boton("NO, SEGUIR", null, Estilo.VERDE, 404, 330, 200, 46);
    private int mouseX = -1;
    private int mouseY = -1;
    private int cartaBajoMouse = -1;

    // Cantos escritos: con Enter se abre un cuadro de texto ("quiero retruco", "me voy al mazo"...)
    // que el InterpreteCantos tokeniza con el Trie y convierte en una Accion.
    private final InterpreteCantos interprete = new InterpreteCantos();
    private boolean escribiendo = false;
    private final StringBuilder textoCanto = new StringBuilder();

    // Geometria de la mano del jugador (abanico abajo a la izquierda; a la derecha van los botones)
    private static final int MANO_CENTRO_X = 272;
    private static final int MANO_Y = 512;
    private static final int MANO_SEPARACION = 104;
    private static final int CARTA_MANO_ANCHO = 92;

    // Geometria de la mesa: una columna por baza; el rival arriba y el jugador abajo
    private static final int COLUMNA_X = 316;
    private static final int COLUMNA_SEPARACION = 84;
    private static final int FILA_RIVAL_Y = 240;
    private static final int FILA_JUGADOR_Y = 330;
    private static final int FILAS_X = 236;
    private static final int CARTA_MESA_ANCHO = 62;
    private static final Color MARCO_JUGADOR = Estilo.AZUL;
    private static final Color MARCO_RIVAL = new Color(226, 96, 140);
    private static final BufferedImage SALON_PIXEL = crearSalonPixel();
    private static final BufferedImage MESA_PIXEL = crearMesaPixel();
    private static final BufferedImage RIVAL_CONSERVADOR_PIXEL = crearRivalPixel(PerfilRival.CONSERVADOR);
    private static final BufferedImage RIVAL_ARRIESGADO_PIXEL = crearRivalPixel(PerfilRival.ARRIESGADO);
    private static final BufferedImage RIVAL_MENTIROSO_PIXEL = crearRivalPixel(PerfilRival.MENTIROSO);
    private static final BufferedImage RETRATO_CONSERVADOR_PIXEL = recortarRetrato(RIVAL_CONSERVADOR_PIXEL);
    private static final BufferedImage RETRATO_ARRIESGADO_PIXEL = recortarRetrato(RIVAL_ARRIESGADO_PIXEL);
    private static final BufferedImage RETRATO_MENTIROSO_PIXEL = recortarRetrato(RIVAL_MENTIROSO_PIXEL);

    public EscenaPartida(ModoPartido modo, ConfiguracionPartido configuracion) {
        if (modo == null) {
            throw new IllegalArgumentException("El partido necesita un modo (gira o torneo).");
        }
        this.modo = modo;
        this.rival = modo.crearRival();
        // Primera mano al azar entre los dos; despues alterna sola
        Lado primeraMano = (System.nanoTime() % 2 == 0) ? Lado.JUGADOR : Lado.RIVAL;
        this.mesa = new MesaTruco(configuracion, primeraMano);

        this.modeloMarkov = RepositorioCorpus.crearModelo(2);
    }

    // ------------------------------------------------------------------ logica de la escena

    @Override
    public void actualizar(double dt) {
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();

        if (enPausa) {
            actualizarPausa(click);
            return; // en pausa no corre nada: ni el rival ni los mensajes
        }
        if (escribiendo) {
            escribirCanto();
        } else if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE) || (click && botonPausa.contiene(mouseX, mouseY))) {
            enPausa = true;
            confirmandoSalida = false;
            return;
        }

        avanzarEventos(dt);
        if (!cerrandoRonda && mesa.getNumeroRonda() != rondaMostrada) {
            empezarCierreDeRonda();
        }
        if (cerrandoRonda) {
            actualizarCierreDeRonda(dt, click);
            return; // mientras se mira como termino la mano, nadie juega
        }
        comentarLaMesa();

        if (mesa.isTerminado()) {
            esperaFinal += dt;
            if (!mesa.hayEventos() && esperaEvento <= 0 && esperaFinal >= PAUSA_FINAL) {
                cerrarPartido();
            }
            return;
        }

        armarBotones();
        cartaBajoMouse = buscarCartaBajoMouse();

        if (mesa.quienActua() == Lado.RIVAL) {
            turnoDelRival(dt);
        } else {
            esperaRival = 0;
            if (!escribiendo) {
                turnoDelJugador(click);
            }
        }
    }

    /**
     * Cuadro de texto del canto: letras A-Z y espacio se agregan, Retroceso borra, flecha derecha completa
     * con la primera sugerencia del Trie, Enter canta y Esc cancela.
     */
    private void escribirCanto() {
        if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE)) {
            escribiendo = false;
            return;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER)) {
            escribiendo = false;
            cantarLoEscrito();
            return;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_BACK_SPACE) && textoCanto.length() > 0) {
            textoCanto.deleteCharAt(textoCanto.length() - 1);
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_RIGHT)) { // Tab no llega: Swing lo usa para mover el foco
            Lista<String> sugerencias = interprete.sugerencias(textoCanto.toString());
            if (!sugerencias.estaVacia()) {
                textoCanto.setLength(0);
                textoCanto.append(sugerencias.obtener(0));
            }
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_SPACE) && textoCanto.length() < 40) {
            textoCanto.append(' ');
        }
        for (int tecla = KeyEvent.VK_A; tecla <= KeyEvent.VK_Z; tecla++) { // VK_A..VK_Z == 'A'..'Z'
            if (getEntrada().fuePresionada(tecla) && textoCanto.length() < 40) {
                textoCanto.append((char) Character.toLowerCase(tecla));
            }
        }
    }

    private void cantarLoEscrito() {
        String texto = textoCanto.toString().trim();
        textoCanto.setLength(0);
        if (texto.isEmpty()) {
            return;
        }
        if (mesa.quienActua() != Lado.JUGADOR) {
            mostrarMensaje("Espera tu turno para cantar.");
            return;
        }
        Accion accion = interprete.interpretar(texto, mesa.accionesDisponibles(Lado.JUGADOR));
        if (accion != null) {
            mesa.ejecutar(Lado.JUGADOR, accion);
            return;
        }
        Accion reconocida = interprete.reconocer(texto);
        mostrarMensaje((reconocida == null)
                ? "No entendi \"" + texto + "\". Proba con: truco, envido, quiero, me voy al mazo..."
                : "Ahora no se puede cantar " + reconocida.getTexto() + ".");
    }

    /**
     * Muestra los eventos de la mesa en el orden en que pasaron, dejando que cada uno se lea
     * (un poco mas rapido si se acumulan). Durante el cierre de una mano se frena antes del
     * aviso "Ronda N": la ronda nueva se anuncia recien cuando se levanta la mesa.
     */
    private void avanzarEventos(double dt) {
        tiempoMensaje += dt;
        esperaEvento -= dt;
        if (esperaEvento > 0 || !mesa.hayEventos()) {
            return;
        }
        if (cerrandoRonda && esAvisoDeRondaNueva(mesa.verSiguienteEvento())) {
            return;
        }
        String texto = mesa.siguienteEvento();
        mostrarMensaje(texto);
        esperaEvento = mesa.hayEventos() ? PAUSA_EVENTO_RAPIDA : PAUSA_EVENTO;
    }

    private boolean esAvisoDeRondaNueva(String evento) {
        return evento != null && evento.startsWith("Ronda ");
    }

    /** Pone un mensaje en la pildora central (con su animacion) y lo anota en la cronica. */
    private void mostrarMensaje(String texto) {
        mensajeMesa = texto;
        tiempoMensaje = 0;
        esperaEvento = Math.max(esperaEvento, PAUSA_EVENTO_RAPIDA);
        cronica.agregar(texto);
        while (cronica.tamanio() > MAX_CRONICA) {
            cronica.eliminar(0);
        }
    }

    /** La mano termino: se congela la mesa como quedo y el rival comenta el resultado. */
    private void empezarCierreDeRonda() {
        cerrandoRonda = true;
        tiempoCierre = 0;
        escribiendo = false;
        hablarAmbiental(mesa.getGanadorUltimaRonda() == Lado.RIVAL ? "toma" : "que suerte");
    }

    /**
     * La mesa queda a la vista hasta que pasa la pausa y se leyeron los mensajes de la mano
     * (o hasta que el jugador hace click / Espacio / Enter para seguir).
     */
    private void actualizarCierreDeRonda(double dt, boolean click) {
        tiempoCierre += dt;
        boolean saltear = click || getEntrada().fuePresionada(KeyEvent.VK_SPACE)
                || getEntrada().fuePresionada(KeyEvent.VK_ENTER);
        boolean quedanDeLaMano = mesa.hayEventos() && !esAvisoDeRondaNueva(mesa.verSiguienteEvento());
        boolean listo = tiempoCierre >= PAUSA_CIERRE && esperaEvento <= 0 && !quedanDeLaMano;
        if (saltear && tiempoCierre > 0.4 || listo) {
            cerrandoRonda = false;
            rondaMostrada = mesa.getNumeroRonda();
            esperaRival = 0;
            esperaEvento = 0; // el aviso de la ronda nueva sale enseguida
        }
    }

    /** El rival comenta al empezar cada ronda y cada vez que se resuelve una baza. */
    private void comentarLaMesa() {
        if (rondaConFrase != mesa.getNumeroRonda()) {
            rondaConFrase = mesa.getNumeroRonda();
            bazasComentadas = 0;
            hablarAmbiental("buenas");
            return;
        }
        Lista<BazaJugada> bazas = mesa.getBazasRonda();
        if (bazas.tamanio() > bazasComentadas) {
            bazasComentadas = bazas.tamanio();
            BazaJugada ultima = bazas.obtener(bazas.tamanio() - 1);
            if (!ultima.esParda()) {
                hablarAmbiental(ultima.getGanador() == Lado.RIVAL ? "toma" : "que suerte");
            }
        }
    }

    /**
     * Genera con Markov una frase que empieza con la semilla y la pone en el globo del rival.
     * La semilla es el token con el que arranca la frase; el resto lo decide el modelo.
     * Si la frase se corto por largo, termina en "..." en lugar de "." (no se corta sin avisar).
     */
    private void hablar(String semilla) {
        ResultadoGeneracion gen = modeloMarkov.generar(12, semilla);
        decirRival(gen.getTextoParaMostrar());
    }

    /** Comentario del rival al repartir o al terminar una baza: siempre generado con Markov. */
    private void hablarAmbiental(String semilla) {
        hablar(semilla);
    }

    /** Lo que dice el rival queda en su globo de dialogo. */
    private void decirRival(String texto) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        fraseRival = texto;
    }

    /**
     * Cuando el rival canta o responde, su frase sale de Markov empezando con ese canto
     * ("truco ...", "no quiero ...", "me voy al mazo ..."), con el corpus de estilo gauchesco.
     */
    private void hablarJugada(Accion accion) {
        hablar(accion == Accion.MAZO ? "me voy al mazo" : accion.getTexto().toLowerCase());
    }

    /**
     * El rival espera a que se lean los mensajes y "piensa" un momento antes de jugar
     * (piensa mientras se lee el ultimo mensaje, asi no se suman las dos esperas).
     */
    private void turnoDelRival(double dt) {
        if (mesa.hayEventos()) {
            return;
        }
        esperaRival += dt;
        if (esperaRival < PAUSA_RIVAL || tiempoMensaje < LECTURA_MINIMA) {
            return;
        }
        esperaRival = 0;
        Accion dicha = rival.jugar(mesa);
        if (dicha != null) {
            hablarJugada(dicha);
        }
    }

    private void turnoDelJugador(boolean click) {
        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER)) {
            escribiendo = true;
            textoCanto.setLength(0);
            return;
        }
        // Tirar carta: teclas 1-3 o click sobre la carta
        int cartaElegida = -1;
        if (getEntrada().fuePresionada(KeyEvent.VK_1)) cartaElegida = 0;
        if (getEntrada().fuePresionada(KeyEvent.VK_2)) cartaElegida = 1;
        if (getEntrada().fuePresionada(KeyEvent.VK_3)) cartaElegida = 2;
        if (click && cartaBajoMouse >= 0) cartaElegida = cartaBajoMouse;
        if (cartaElegida >= 0 && mesa.jugarCarta(Lado.JUGADOR, cartaElegida)) {
            return;
        }

        // Cantos y respuestas: la tecla de cada accion (E, R, F, T, Q, N, M) o click en su boton
        for (int i = 0; i < accionesVisibles.tamanio(); i++) {
            Accion accion = accionesVisibles.obtener(i);
            boolean porTecla = getEntrada().fuePresionada(accion.getTecla().charAt(0)); // VK_A..VK_Z == 'A'..'Z'
            boolean porClick = click && botonesAccion.obtener(i).contiene(mouseX, mouseY);
            if (porTecla || porClick) {
                mesa.ejecutar(Lado.JUGADOR, accion);
                return;
            }
        }
    }

    /** Crea un boton por cada accion que la mesa permite ahora, en una grilla de 2 columnas. */
    private void armarBotones() {
        accionesVisibles.vaciar();
        botonesAccion.vaciar();
        Lista<Accion> disponibles = mesa.accionesDisponibles(Lado.JUGADOR);
        for (int i = 0; i < disponibles.tamanio() && i < 6; i++) {
            Accion accion = disponibles.obtener(i);
            int x = 470 + (i % 2) * 160;
            int y = 456 + (i / 2) * 44;
            accionesVisibles.agregar(accion);
            botonesAccion.agregar(new Boton(accion.getTexto(), accion.getTecla(), colorDe(accion), x, y, 152, 38));
        }
    }

    private Color colorDe(Accion accion) {
        if (accion.esEnvido()) return Estilo.CARAMELO;
        if (accion.esTruco()) return Estilo.AZUL;
        if (accion == Accion.QUIERO) return Estilo.VERDE;
        if (accion == Accion.NO_QUIERO) return Estilo.MAGENTA;
        return new Color(112, 76, 120); // al mazo
    }

    /** Menu de pausa: OPCIONES y SALIR (con confirmacion). Esc vuelve al partido. */
    private void actualizarPausa(boolean click) {
        if (confirmandoSalida) {
            if (click && botonConfirmar.contiene(mouseX, mouseY) || getEntrada().fuePresionada(KeyEvent.VK_S)) {
                String resultado = modo.registrarAbandono();
                partidoCerrado = true;
                mensajeMesa = resultado;
                getContexto().irA(modo.escenaDeVuelta());
            } else if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE) || getEntrada().fuePresionada(KeyEvent.VK_N)
                    || (click && botonCancelar.contiene(mouseX, mouseY))) {
                confirmandoSalida = false;
            }
            return;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE)) {
            enPausa = false;
        } else if (click && botonOpciones.contiene(mouseX, mouseY) || getEntrada().fuePresionada(KeyEvent.VK_O)) {
            getContexto().irA(new EscenaOpciones(this)); // al volver, el partido sigue en pausa
        } else if (click && botonSalirPausa.contiene(mouseX, mouseY) || getEntrada().fuePresionada(KeyEvent.VK_S)) {
            confirmandoSalida = true;
        }
    }

    private void cerrarPartido() {
        if (partidoCerrado) return;
        partidoCerrado = true;
        boolean ganoJugador = mesa.getGanadorPartido() == Lado.JUGADOR;
        if (ganoJugador) {
            Progreso progreso = RepositorioProgreso.cargarProgreso();
            progreso.sumarPuntos(10);
            RepositorioProgreso.guardarProgreso(progreso);
        }
        String resultado = modo.registrarResultado(ganoJugador); // gira o torneo: cada uno sabe que hacer
        getContexto().irA(new EscenaFin(ganoJugador, mesa.getPuntos(Lado.JUGADOR), mesa.getPuntos(Lado.RIVAL), modo, resultado));
    }

    // ------------------------------------------------------------------ geometria de la mano

    private int buscarCartaBajoMouse() {
        if (!mesa.puedeJugarCarta(Lado.JUGADOR)) return -1;
        Lista<Carta> mano = mesa.getCartas(Lado.JUGADOR);
        for (int i = 0; i < mano.tamanio(); i++) {
            double cx = centroCartaManoX(i, mano.tamanio());
            double cy = centroCartaManoY(i, mano.tamanio());
            if (Math.abs(mouseX - cx) <= CARTA_MANO_ANCHO / 2.0
                    && Math.abs(mouseY - cy) <= CARTA_MANO_ANCHO * DibujoCarta.PROPORCION / 2.0) {
                return i;
            }
        }
        return -1;
    }

    private double desplazamientoEnMano(int indice, int cantidad) {
        return indice - (cantidad - 1) / 2.0;
    }

    private double centroCartaManoX(int indice, int cantidad) {
        return MANO_CENTRO_X + desplazamientoEnMano(indice, cantidad) * MANO_SEPARACION;
    }

    private double centroCartaManoY(int indice, int cantidad) {
        return MANO_Y + Math.abs(desplazamientoEnMano(indice, cantidad)) * 8;
    }

    private double anguloCartaMano(int indice, int cantidad) {
        return desplazamientoEnMano(indice, cantidad) * 0.12;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();

        dibujarPixelArt(g, SALON_PIXEL, 0, 0, ancho, alto);
        dibujarRivalPixel(g);
        dibujarMesa(g);
        dibujarTanteador(g);
        dibujarRival(g);
        dibujarEstadoRonda(g);
        dibujarCartasEnMesa(g);
        dibujarCantoPendiente(g);
        dibujarManoJugador(g);
        dibujarBotones(g);
        dibujarCronica(g);
        dibujarMensajeMesa(g);
        if (cerrandoRonda) {
            dibujarCartelCierre(g);
        }

        if (enPausa) {
            dibujarPausa(g, ancho, alto);
        }
        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }

    /** Menu de pausa (o la confirmacion de salida) sobre la mesa oscurecida. */
    private void dibujarPausa(Graphics2D g, int ancho, int alto) {
        g.setColor(new Color(5, 8, 20, 190));
        g.fillRect(0, 0, ancho, alto);
        if (confirmandoSalida) {
            Estilo.panel(g, 170, 190, 460, 210);
            Estilo.titulo(g, "¿Abandonar el partido?", 400, 240, 26);
            Lista<String> renglones = Estilo.envolver(g, modo.getAvisoAbandono(), 13, 420, 2);
            for (int i = 0; i < renglones.tamanio(); i++) {
                Texto.centrado(g, renglones.obtener(i), 400, 278 + i * 20, 13, Estilo.CREMA);
            }
            botonConfirmar.dibujar(g, botonConfirmar.contiene(mouseX, mouseY), true);
            botonCancelar.dibujar(g, botonCancelar.contiene(mouseX, mouseY), true);
            return;
        }
        Estilo.panel(g, 250, 170, 300, 230);
        Estilo.titulo(g, "Pausa", 400, 222, 32);
        botonOpciones.dibujar(g, botonOpciones.contiene(mouseX, mouseY), true);
        botonSalirPausa.dibujar(g, botonSalirPausa.contiene(mouseX, mouseY), true);
        Texto.centrado(g, "F6: musica " + (MusicaTango.estaActiva() ? "ON" : "OFF")
                + "  ·  Esc para volver", 400, 386, 11, Estilo.TEXTO_SUAVE);
    }

    /** Mesa de pulperia en pixel art, con paño verde y borde de madera. */
    private void dibujarMesa(Graphics2D g) {
        dibujarPixelArt(g, MESA_PIXEL, 80, 166, 640, 296);
    }

    private void dibujarRivalPixel(Graphics2D g) {
        dibujarPixelArt(g, imagenRival(), 296, 18, 208, 224);
    }

    private BufferedImage imagenRival() {
        switch (modo.getPerfilRival()) {
            case CONSERVADOR: return RIVAL_CONSERVADOR_PIXEL;
            case ARRIESGADO: return RIVAL_ARRIESGADO_PIXEL;
            case MENTIROSO: return RIVAL_MENTIROSO_PIXEL;
            default: throw new IllegalStateException("Perfil de rival desconocido.");
        }
    }

    private BufferedImage retratoRival() {
        switch (modo.getPerfilRival()) {
            case CONSERVADOR: return RETRATO_CONSERVADOR_PIXEL;
            case ARRIESGADO: return RETRATO_ARRIESGADO_PIXEL;
            case MENTIROSO: return RETRATO_MENTIROSO_PIXEL;
            default: throw new IllegalStateException("Perfil de rival desconocido.");
        }
    }

    private static void dibujarPixelArt(Graphics2D g, BufferedImage imagen,
            int x, int y, int ancho, int alto) {
        Graphics2D pixel = (Graphics2D) g.create();
        try {
            pixel.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            pixel.drawImage(imagen, x, y, ancho, alto, null);
        } finally {
            pixel.dispose();
        }
    }

    private static BufferedImage crearMesaPixel() {
        BufferedImage imagen = new BufferedImage(160, 74, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setColor(new Color(17, 15, 16, 180));
            g.fillOval(2, 8, 156, 63);
            g.setColor(new Color(43, 24, 19));
            g.fillOval(0, 0, 160, 68);
            g.setColor(new Color(115, 59, 34));
            g.fillOval(3, 3, 154, 62);
            g.setColor(new Color(157, 91, 48));
            g.fillOval(6, 5, 148, 57);

            g.setClip(new Ellipse2D.Double(7, 6, 146, 54));
            g.setColor(new Color(129, 70, 38));
            g.fillRect(7, 13, 146, 2);
            g.setColor(new Color(174, 105, 57));
            g.fillRect(7, 22, 146, 2);
            g.setColor(new Color(141, 77, 41));
            g.fillRect(7, 31, 146, 2);
            g.setColor(new Color(188, 117, 65));
            g.fillRect(7, 40, 146, 2);
            g.setColor(new Color(102, 52, 31));
            g.fillRect(52, 7, 2, 13);
            g.fillRect(102, 48, 2, 12);
            g.setClip(null);

            g.setColor(new Color(75, 43, 27));
            g.fillOval(15, 13, 130, 42);
            g.setColor(new Color(201, 155, 83));
            g.fillOval(18, 15, 124, 38);
            g.setColor(new Color(28, 78, 54));
            g.fillOval(20, 17, 120, 34);
            g.setColor(new Color(39, 101, 67));
            g.fillOval(23, 19, 114, 30);
            g.setColor(new Color(73, 132, 83));
            g.drawOval(24, 20, 112, 28);

            g.setColor(new Color(205, 166, 103));
            for (int x = 34; x <= 126; x += 8) {
                g.fillRect(x, 21, 2, 1);
                g.fillRect(x, 47, 2, 1);
            }
        } finally {
            g.dispose();
        }
        return imagen;
    }

    private static BufferedImage crearSalonPixel() {
        BufferedImage imagen = new BufferedImage(200, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setColor(new Color(33, 25, 30));
            g.fillRect(0, 0, 200, 150);
            g.setColor(new Color(39, 28, 34));
            g.fillRect(0, 0, 200, 78);
            for (int x = 5; x < 200; x += 16) {
                g.setColor((x / 16) % 2 == 0 ? new Color(43, 30, 36) : new Color(36, 27, 33));
                g.fillRect(x, 6, 5, 68);
            }

            g.setColor(new Color(18, 16, 20));
            g.fillRect(0, 0, 200, 7);
            g.setColor(new Color(117, 77, 48));
            g.fillRect(0, 7, 200, 2);
            dibujarCuadroSalon(g, 14, 19);
            dibujarCuadroSalon(g, 154, 19);
            dibujarApliqueSalon(g, 69, 29);
            dibujarApliqueSalon(g, 131, 29);

            g.setColor(new Color(76, 43, 34));
            g.fillRect(0, 77, 200, 27);
            g.setColor(new Color(140, 91, 52));
            g.fillRect(0, 77, 200, 2);
            g.setColor(new Color(36, 26, 28));
            g.fillRect(0, 81, 200, 2);
            for (int x = 4; x < 200; x += 24) {
                g.setColor(new Color(104, 60, 42));
                g.fillRect(x, 84, 2, 18);
                g.setColor(new Color(48, 31, 29));
                g.fillRect(x + 2, 84, 18, 2);
            }
            g.setColor(new Color(28, 22, 24));
            g.fillRect(0, 103, 200, 47);
            for (int y = 110; y < 150; y += 10) {
                g.setColor(new Color(51, 35, 31));
                g.fillRect(0, y, 200, 2);
                for (int x = ((y / 10) % 2 == 0) ? 0 : 20; x < 200; x += 40) {
                    g.setColor(new Color(43, 30, 28));
                    g.fillRect(x, y + 2, 2, 8);
                }
            }

            g.setColor(new Color(58, 24, 34));
            g.fillRect(31, 123, 138, 27);
            g.setColor(new Color(125, 78, 47));
            g.fillRect(31, 123, 138, 2);
            g.fillRect(31, 148, 138, 2);
            g.fillRect(31, 123, 2, 27);
            g.fillRect(167, 123, 2, 27);
            g.setColor(new Color(91, 42, 48));
            g.fillRect(39, 131, 122, 11);
            g.setColor(new Color(126, 84, 51));
            g.fillRect(63, 135, 74, 2);
        } finally {
            g.dispose();
        }
        return imagen;
    }

    private static void dibujarCuadroSalon(Graphics2D g, int x, int y) {
        g.setColor(new Color(133, 92, 48));
        g.fillRect(x, y, 32, 42);
        g.setColor(new Color(45, 28, 27));
        g.fillRect(x + 3, y + 3, 26, 36);
        g.setColor(new Color(69, 39, 40));
        g.fillRect(x + 5, y + 5, 22, 32);
        g.setColor(new Color(185, 143, 89));
        g.fillRect(x + 14, y + 11, 6, 8);
        g.setColor(new Color(29, 26, 28));
        g.fillRect(x + 10, y + 18, 14, 14);
        g.fillRect(x + 7, y + 26, 20, 7);
    }

    private static void dibujarApliqueSalon(Graphics2D g, int x, int y) {
        g.setColor(new Color(115, 77, 44));
        g.fillRect(x + 2, y + 7, 4, 14);
        g.fillRect(x, y + 15, 8, 3);
        g.setColor(new Color(222, 169, 79));
        g.fillRect(x + 2, y + 2, 4, 7);
        g.setColor(new Color(250, 208, 112));
        g.fillRect(x + 3, y + 3, 2, 4);
    }

    private static BufferedImage crearRivalPixel(PerfilRival perfil) {
        BufferedImage imagen = new BufferedImage(52, 56, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        try {
            Color contorno = new Color(24, 22, 24);
            Color traje;
            Color luzTraje;
            Color piel;
            Color sombraPiel;
            Color corbata;
            Color sombrero;
            Color bandaSombrero;
            switch (perfil) {
                case CONSERVADOR:
                    traje = new Color(34, 37, 43);
                    luzTraje = new Color(57, 59, 62);
                    piel = new Color(218, 158, 111);
                    sombraPiel = new Color(163, 101, 76);
                    corbata = new Color(139, 43, 44);
                    sombrero = new Color(48, 49, 53);
                    bandaSombrero = new Color(139, 43, 44);
                    break;
                case ARRIESGADO:
                    traje = new Color(75, 35, 43);
                    luzTraje = new Color(119, 53, 56);
                    piel = new Color(228, 166, 115);
                    sombraPiel = new Color(166, 99, 72);
                    corbata = new Color(207, 151, 53);
                    sombrero = new Color(92, 59, 38);
                    bandaSombrero = new Color(207, 151, 53);
                    break;
                case MENTIROSO:
                    traje = new Color(27, 46, 48);
                    luzTraje = new Color(49, 76, 73);
                    piel = new Color(200, 137, 95);
                    sombraPiel = new Color(141, 83, 66);
                    corbata = new Color(49, 104, 73);
                    sombrero = new Color(28, 33, 42);
                    bandaSombrero = new Color(183, 142, 60);
                    break;
                default:
                    throw new IllegalArgumentException("Perfil de rival desconocido: " + perfil);
            }
            Color blanco = new Color(240, 232, 210);
            Color oro = new Color(218, 173, 79);

            g.setColor(contorno);
            g.fillRect(12, 32, 28, 24);
            g.fillRect(7, 36, 38, 20);
            g.fillRect(5, 41, 42, 15);
            g.setColor(traje);
            g.fillRect(13, 34, 26, 22);
            g.fillRect(8, 38, 36, 18);
            g.fillRect(6, 43, 40, 13);
            g.setColor(luzTraje);
            g.fillRect(10, 40, 2, 13);
            g.fillRect(40, 40, 2, 13);
            g.fillRect(15, 36, 2, 7);
            g.fillRect(35, 36, 2, 7);

            g.setColor(sombraPiel);
            g.fillRect(21, 28, 11, 9);
            g.setColor(piel);
            g.fillRect(23, 27, 8, 8);
            g.setColor(blanco);
            g.fillRect(20, 33, 14, 8);
            g.setColor(contorno);
            g.fillRect(19, 32, 5, 4);
            g.fillRect(30, 32, 5, 4);
            g.setColor(traje);
            g.fillRect(17, 35, 7, 12);
            g.fillRect(30, 35, 7, 12);
            g.setColor(new Color(100, 103, 105));
            g.fillRect(22, 35, 2, 9);
            g.fillRect(30, 35, 2, 9);
            g.setColor(corbata);
            g.fillRect(25, 35, 5, 13);
            g.fillRect(24, 47, 7, 2);
            g.setColor(oro);
            g.fillRect(26, 42, 3, 2);
            g.fillRect(34, 43, 3, 2);
            g.setColor(blanco);
            g.fillRect(14, 42, 3, 2);

            g.setColor(contorno);
            g.fillRect(15, 16, 22, 18);
            g.fillRect(13, 19, 26, 11);
            g.setColor(sombraPiel);
            g.fillRect(16, 17, 20, 16);
            g.setColor(piel);
            g.fillRect(18, 18, 17, 14);
            g.setColor(new Color(237, 187, 137));
            g.fillRect(20, 19, 11, 2);
            g.setColor(sombraPiel);
            g.fillRect(14, 22, 4, 7);
            g.fillRect(35, 22, 4, 7);
            g.setColor(contorno);
            g.fillRect(18, 23, 5, 2);
            g.fillRect(30, 23, 5, 2);
            g.fillRect(20, 26, 3, 2);
            g.fillRect(31, 26, 3, 2);
            g.fillRect(23, 29, 8, 2);
            g.fillRect(22, 28, 2, 2);
            g.fillRect(30, 28, 2, 2);
            g.setColor(new Color(91, 53, 43));
            g.fillRect(22, 30, 9, 2);

            g.setColor(contorno);
            g.fillRect(11, 15, 30, 4);
            g.fillRect(15, 10, 22, 7);
            g.fillRect(18, 8, 16, 3);
            g.setColor(sombrero);
            g.fillRect(13, 14, 26, 3);
            g.fillRect(17, 10, 18, 5);
            g.setColor(bandaSombrero);
            g.fillRect(17, 14, 18, 2);
            g.setColor(new Color(93, 95, 98));
            g.fillRect(21, 10, 2, 2);
            g.fillRect(29, 10, 2, 2);
            if (perfil == PerfilRival.ARRIESGADO) {
                g.setColor(new Color(198, 71, 55));
                g.fillRect(35, 8, 3, 7);
                g.fillRect(37, 6, 2, 4);
            } else if (perfil == PerfilRival.MENTIROSO) {
                g.setColor(contorno);
                g.fillRect(17, 22, 8, 4);
                g.fillRect(28, 22, 8, 4);
                g.fillRect(24, 23, 5, 2);
                g.setColor(new Color(89, 107, 108));
                g.fillRect(18, 23, 6, 2);
                g.fillRect(29, 23, 6, 2);
            }
        } finally {
            g.dispose();
        }
        return imagen;
    }

    private static BufferedImage recortarRetrato(BufferedImage imagen) {
        return imagen.getSubimage(8, 7, 36, 36);
    }

    /** Tanteador: a cuantos se juega, puntos con fosforos y si hay flor. */
    private void dibujarTanteador(Graphics2D g) {
        ConfiguracionPartido cfg = mesa.getConfiguracion();
        Estilo.panel(g, 16, 14, 196, 138);
        Texto.izquierda(g, "A " + cfg.getPuntosObjetivo() + (cfg.isConFlor() ? "  ·  CON FLOR" : "  ·  SIN FLOR"),
                30, 32, 10, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, "VOS", 30, 52, 13, Estilo.TEXTO);
        Texto.derecha(g, String.valueOf(mesa.getPuntos(Lado.JUGADOR)), 198, 52, 14, Estilo.DORADO);
        Estilo.fosforos(g, 32, 60, mesa.getPuntos(Lado.JUGADOR));
        Texto.izquierda(g, "RIVAL", 30, 100, 13, new Color(255, 150, 185));
        Texto.derecha(g, String.valueOf(mesa.getPuntos(Lado.RIVAL)), 198, 100, 14, Estilo.DORADO);
        Estilo.fosforos(g, 32, 108, mesa.getPuntos(Lado.RIVAL));
        if (cfg.seJuegaConMalasYBuenas()) {
            Texto.derecha(g, "15 = buenas", 198, 145, 9, Estilo.TEXTO_SUAVE);
        }
    }

    /** Avatar del rival, globo con lo que dice y sus cartas boca abajo. */
    private void dibujarRival(Graphics2D g) {
        g.setColor(new Color(31, 27, 24));
        g.fillRect(231, 19, 62, 62);
        g.setColor(Estilo.DORADO);
        g.drawRect(231, 19, 62, 62);
        dibujarPixelArt(g, retratoRival(), 237, 25, 50, 50);
        if (mesa.getManoDeLaRonda() == Lado.RIVAL && !cerrandoRonda) {
            Estilo.pildora(g, "MANO", 262, 90, 9, Estilo.DORADO, new Color(40, 30, 10));
        }
        Estilo.globo(g, 304, 6, 318, 66, 290, 50);
        Texto.izquierda(g, Estilo.recortar(g, modo.getNombreRival().toUpperCase() + "  ·  " + rival.getNombreEstrategia().toUpperCase(), 10, 290),
                320, 24, 10, new Color(150, 96, 40));
        Lista<String> renglones = Estilo.envolver(g, fraseRival, 13, 290, 2);
        for (int i = 0; i < renglones.tamanio(); i++) {
            Texto.izquierda(g, renglones.obtener(i), 320, 44 + i * 16, 13, new Color(30, 30, 40));
        }

        int cantidad = cerrandoRonda ? 0 : mesa.getCartas(Lado.RIVAL).tamanio(); // la mano nueva todavia no se muestra
        for (int i = 0; i < cantidad; i++) {
            double k = i - (cantidad - 1) / 2.0;
            DibujoCarta.dorso(g, 400 + k * 24, 190 + Math.abs(k) * 3, 30, k * 0.14 + Math.PI);
        }
    }

    /** Ronda, baza, bazas ganadas y cuanto vale la ronda (arriba a la derecha). */
    private void dibujarEstadoRonda(Graphics2D g) {
        botonPausa.dibujar(g, botonPausa.contiene(mouseX, mouseY), true);
        if (cerrandoRonda) {
            Lista<BazaJugada> bazas = mesa.getBazasRondaAnterior();
            Estilo.pildora(g, "RONDA " + rondaMostrada + "  ·  TERMINADA", 712, 72, 11,
                    new Color(10, 14, 30, 190), Estilo.DORADO);
            Estilo.pildora(g, "VOS " + contarBazas(bazas, Lado.JUGADOR) + "  ·  RIVAL " + contarBazas(bazas, Lado.RIVAL),
                    712, 100, 11, new Color(10, 14, 30, 150), Estilo.TEXTO_SUAVE);
            return;
        }
        Estilo.pildora(g, "RONDA " + mesa.getNumeroRonda() + "  ·  BAZA " + mesa.getBazaActual() + " DE 3", 712, 72, 11,
                new Color(10, 14, 30, 190), Estilo.TEXTO);
        Estilo.pildora(g, "VOS " + mesa.bazasGanadas(Lado.JUGADOR) + "  ·  RIVAL " + mesa.bazasGanadas(Lado.RIVAL), 712, 100, 11,
                new Color(10, 14, 30, 150), Estilo.TEXTO_SUAVE);
        Canto truco = mesa.getTrucoVigente();
        String valor = (truco == null) ? "LA RONDA VALE 1" : truco.getNombre().toUpperCase() + ": VALE " + truco.puntosAceptado();
        Estilo.pildora(g, valor, 712, 128, 10, truco == null ? new Color(10, 14, 30, 150) : Estilo.AZUL, Estilo.TEXTO);
    }

    /**
     * Cartas jugadas en la ronda: cada baza en su columna (1, 2, 3), la del rival arriba y la
     * propia abajo, cada una con el marco de su color. Quedan en la mesa toda la ronda y,
     * debajo, se marca quien gano cada baza. Cuando la mano termina, sus cartas quedan un momento
     * (cierre de mano) y despues la mesa arranca vacia para la ronda nueva.
     */
    private void dibujarCartasEnMesa(Graphics2D g) {
        Lista<BazaJugada> bazas = cerrandoRonda ? mesa.getBazasRondaAnterior() : mesa.getBazasRonda();
        Carta propia = cerrandoRonda ? null : mesa.getCartaEnMesa(Lado.JUGADOR);
        Carta delRival = cerrandoRonda ? null : mesa.getCartaEnMesa(Lado.RIVAL);
        float opacidad = 1f;
        if (bazas.estaVacia() && propia == null && delRival == null) {
            return; // ronda nueva: la mesa arranca limpia
        }

        Estilo.pildora(g, "RIVAL", FILAS_X, FILA_RIVAL_Y, 10, MARCO_RIVAL, Estilo.TEXTO);
        Estilo.pildora(g, "VOS", FILAS_X, FILA_JUGADOR_Y, 10, MARCO_JUGADOR, Estilo.TEXTO);

        for (int i = 0; i < bazas.tamanio(); i++) {
            BazaJugada baza = bazas.obtener(i);
            dibujarColumna(g, i, baza.getCarta(Lado.JUGADOR), baza.getCarta(Lado.RIVAL), baza.getSalio(), opacidad);
            dibujarResultadoBaza(g, i, baza, opacidad);
        }
        if (propia != null || delRival != null) {
            // Baza en juego: todavia falta la carta de uno de los dos
            dibujarColumna(g, bazas.tamanio(), propia, delRival, mesa.getSalioEnBaza(), 1f);
        }
    }

    /** Una columna de la mesa: la carta que salio primero queda abajo y la otra encima. */
    private void dibujarColumna(Graphics2D g, int columna, Carta propia, Carta delRival, Lado salio, float opacidad) {
        double x = COLUMNA_X + columna * COLUMNA_SEPARACION;
        double inclinacion = (columna - 1) * 0.05;
        if (salio == Lado.RIVAL) {
            cartaDeFila(g, delRival, x, FILA_RIVAL_Y, inclinacion + 0.04, opacidad, MARCO_RIVAL);
            cartaDeFila(g, propia, x, FILA_JUGADOR_Y, inclinacion - 0.04, opacidad, MARCO_JUGADOR);
        } else {
            cartaDeFila(g, propia, x, FILA_JUGADOR_Y, inclinacion - 0.04, opacidad, MARCO_JUGADOR);
            cartaDeFila(g, delRival, x, FILA_RIVAL_Y, inclinacion + 0.04, opacidad, MARCO_RIVAL);
        }
    }

    private void cartaDeFila(Graphics2D g, Carta carta, double x, double y, double angulo, float opacidad, Color marco) {
        if (carta != null) {
            DibujoCarta.frente(g, carta, x, y, CARTA_MESA_ANCHO, angulo, opacidad, marco);
        }
    }

    /** Debajo de cada baza resuelta: quien la gano (o PARDA). */
    private void dibujarResultadoBaza(Graphics2D g, int columna, BazaJugada baza, float opacidad) {
        double x = COLUMNA_X + columna * COLUMNA_SEPARACION;
        int alfa = (int) (230 * opacidad);
        if (baza.esParda()) {
            Estilo.pildora(g, "PARDA", x, 393, 10, new Color(110, 116, 140, alfa), Estilo.TEXTO);
        } else if (baza.getGanador() == Lado.JUGADOR) {
            Estilo.pildora(g, "GANASTE", x, 393, 10, conAlfa(MARCO_JUGADOR, alfa), Estilo.TEXTO);
        } else {
            Estilo.pildora(g, "GANO RIVAL", x, 393, 10, conAlfa(MARCO_RIVAL, alfa), Estilo.TEXTO);
        }
    }

    private Color conAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    /** Cartel del canto que espera respuesta: quien canto y que. */
    private void dibujarCantoPendiente(Graphics2D g) {
        Canto canto = mesa.isEnvidoPendiente() ? mesa.getUltimoCantoEnvido() : mesa.getTrucoPropuesto();
        if (canto == null) return;
        if (mesa.quienActua() == Lado.JUGADOR) {
            Estilo.pildora(g, "EL RIVAL CANTA ¡" + canto.getNombre().toUpperCase() + "!", 400, 170, 16, Estilo.MAGENTA, Estilo.TEXTO);
        } else {
            Estilo.pildora(g, "CANTASTE ¡" + canto.getNombre().toUpperCase() + "!", 400, 170, 15, Estilo.AZUL, Estilo.TEXTO);
        }
    }

    /** Mano del jugador en abanico; la carta bajo el mouse "se levanta". */
    private void dibujarManoJugador(Graphics2D g) {
        if (cerrandoRonda) {
            return; // las cartas nuevas se reparten cuando se levanta la mesa
        }
        Lista<Carta> mano = mesa.getCartas(Lado.JUGADOR);
        boolean puedeTirar = mesa.puedeJugarCarta(Lado.JUGADOR);
        for (int i = 0; i < mano.tamanio(); i++) {
            double cx = centroCartaManoX(i, mano.tamanio());
            double cy = centroCartaManoY(i, mano.tamanio()) + ((i == cartaBajoMouse) ? -18 : 0);
            DibujoCarta.frente(g, mano.obtener(i), cx, cy, CARTA_MANO_ANCHO, anguloCartaMano(i, mano.tamanio()));
            if (puedeTirar) {
                Estilo.pildora(g, String.valueOf(i + 1), cx, cy + 62, 10, new Color(10, 14, 30, 200), Estilo.DORADO);
            }
        }
        if (mesa.getManoDeLaRonda() == Lado.JUGADOR) {
            Estilo.pildora(g, "SOS MANO", 60, 470, 10, Estilo.DORADO, new Color(40, 30, 10));
        }
        Estilo.pildora(g, "TU ENVIDO: " + mesa.puntosEnvido(Lado.JUGADOR), 60, 500, 11,
                new Color(10, 14, 30, 190), Estilo.CREMA);
    }

    /** Botones de lo que se puede cantar ahora, o un aviso si le toca al rival. */
    private void dibujarBotones(Graphics2D g) {
        if (mesa.isTerminado()) {
            Estilo.pildora(g, "FIN DEL PARTIDO", 627, 520, 13, Estilo.DORADO, new Color(40, 30, 10));
            return;
        }
        if (cerrandoRonda) {
            dibujarEsperaCierre(g);
            return;
        }
        if (mesa.quienActua() == Lado.RIVAL) {
            Estilo.pildora(g, "EL RIVAL ESTA PENSANDO...", 627, 520, 12, new Color(10, 14, 30, 200), Estilo.TEXTO_SUAVE);
            return;
        }
        if (escribiendo) {
            dibujarCuadroCanto(g);
            return;
        }
        for (int i = 0; i < botonesAccion.tamanio(); i++) {
            Boton b = botonesAccion.obtener(i);
            b.dibujar(g, b.contiene(mouseX, mouseY), true);
        }
        Texto.centrado(g, "[Enter] escribir el canto", 627, 446, 11, Estilo.TEXTO_SUAVE);
    }

    /** Pildora central con el ultimo mensaje de la mesa: entra subiendo y aclarandose. */
    private void dibujarMensajeMesa(Graphics2D g) {
        if (mensajeMesa.isEmpty()) {
            return;
        }
        double t = curvaSuave(Math.min(1, tiempoMensaje / APARICION));
        double y = 424 + (1 - t) * 10;
        String texto = Estilo.recortar(g, mensajeMesa, 13, 540);
        Estilo.pildora(g, texto, 400, y + 3, 13, new Color(0, 0, 0, (int) (70 * t)), new Color(0, 0, 0, 0)); // sombra
        Estilo.pildora(g, texto, 400, y, 13, new Color(10, 14, 30, (int) (225 * t)), conAlfa(Estilo.CREMA, (int) (255 * t)));
    }

    /**
     * Cronica de la mesa (a la derecha): los ultimos mensajes, para releer lo que paso aunque
     * los avisos se sucedan. El mas viejo arriba y apagado; el mas nuevo abajo, resaltado.
     */
    private void dibujarCronica(Graphics2D g) {
        if (cronica.estaVacia()) {
            return;
        }
        final int x = 590;
        final int ancho = 194;
        final int tope = 150;
        final int altoMaximo = 252;
        final int interlineado = 13;
        final int separacion = 7;
        final int encabezado = 32;

        // Renglones de cada mensaje, del mas nuevo al mas viejo, mientras entren en el panel
        Lista<Lista<String>> bloques = new MiLista<>();
        int altoTexto = 0;
        for (int i = cronica.tamanio() - 1; i >= 0; i--) {
            Lista<String> renglones = Estilo.envolver(g, cronica.obtener(i), 10, ancho - 30, 3);
            int alto = renglones.tamanio() * interlineado + separacion;
            if (encabezado + altoTexto + alto > altoMaximo) {
                break;
            }
            bloques.agregar(renglones);
            altoTexto += alto;
        }

        Estilo.panel(g, x, tope, ancho, encabezado + altoTexto + 4, new Color(10, 14, 30, 170), Estilo.PANEL_BORDE);
        Texto.izquierda(g, "CRONICA DE LA MESA", x + 14, tope + 20, 9, Estilo.DORADO);
        int y = tope + encabezado + 8;
        for (int b = bloques.tamanio() - 1; b >= 0; b--) { // b = 0 es el mas nuevo
            Lista<String> renglones = bloques.obtener(b);
            Color color;
            if (b == 0) {
                double t = curvaSuave(Math.min(1, tiempoMensaje / APARICION));
                color = conAlfa(Estilo.CREMA, (int) (110 + 145 * t));
                g.setColor(conAlfa(Estilo.DORADO, (int) (255 * t)));
                g.fillOval(x + 8, y - 6, 5, 5);
            } else {
                color = conAlfa(Estilo.TEXTO_SUAVE, Math.max(80, 210 - (b - 1) * 32));
            }
            for (int k = 0; k < renglones.tamanio(); k++) {
                Texto.izquierda(g, renglones.obtener(k), x + 18, y, 10, color);
                y += interlineado;
            }
            y += separacion;
        }
    }

    /** Cartel sobre la mesa al terminar una mano: quien se la llevo y cuantos puntos sumo. */
    private void dibujarCartelCierre(Graphics2D g) {
        Lado ganador = mesa.getGanadorUltimaRonda();
        if (ganador == null) {
            return;
        }
        int puntos = mesa.getPuntosUltimaRonda();
        double t = curvaSuave(Math.min(1, tiempoCierre / 0.35));
        String texto = (ganador == Lado.JUGADOR ? "¡TE LLEVASTE LA MANO!" : "LA MANO ES DEL RIVAL")
                + "   +" + puntos + (puntos == 1 ? " PUNTO" : " PUNTOS");
        Color fondo = (ganador == Lado.JUGADOR) ? MARCO_JUGADOR : MARCO_RIVAL;
        double y = 178 - (1 - t) * 14;
        Estilo.pildora(g, texto, 400, y + 4, 16, new Color(0, 0, 0, (int) (90 * t)), new Color(0, 0, 0, 0)); // sombra
        Estilo.pildora(g, texto, 400, y, 16, conAlfa(fondo, (int) (240 * t)), conAlfa(Color.WHITE, (int) (255 * t)));
    }

    /** Abajo a la derecha, mientras se mira la mano terminada: cuanto falta para repartir. */
    private void dibujarEsperaCierre(Graphics2D g) {
        Estilo.pildora(g, "FIN DE LA MANO", 627, 486, 13, Estilo.DORADO, new Color(40, 30, 10));
        double avance = Math.min(1, tiempoCierre / PAUSA_CIERRE);
        int bx = 547;
        int by = 512;
        int bw = 160;
        int bh = 6;
        g.setColor(new Color(10, 14, 30, 200));
        g.fillRoundRect(bx, by, bw, bh, bh, bh);
        g.setColor(Estilo.DORADO);
        g.fillRoundRect(bx, by, (int) (bw * avance), bh, bh, bh);
        Texto.centrado(g, "Click o Espacio para repartir", 627, 540, 11, Estilo.TEXTO_SUAVE);
    }

    /** Bazas que gano un lado en una lista de bazas jugadas. */
    private int contarBazas(Lista<BazaJugada> bazas, Lado lado) {
        int ganadas = 0;
        for (int i = 0; i < bazas.tamanio(); i++) {
            if (bazas.obtener(i).getGanador() == lado) {
                ganadas++;
            }
        }
        return ganadas;
    }

    /** Curva de animacion: arranca y termina despacio (0..1 -> 0..1). */
    private static double curvaSuave(double t) {
        return t * t * (3 - 2 * t);
    }

    /** Cuadro de texto del canto con las sugerencias del Trie para lo que se lleva escrito. */
    private void dibujarCuadroCanto(Graphics2D g) {
        Estilo.panel(g, 470, 440, 312, 150);
        Texto.izquierda(g, "ESCRIBI TU CANTO", 486, 462, 11, Estilo.DORADO);
        g.setColor(new Color(10, 14, 30, 220));
        g.fillRoundRect(484, 472, 284, 34, 12, 12);
        boolean cursorVisible = (System.currentTimeMillis() / 400) % 2 == 0;
        Texto.izquierda(g, textoCanto + (cursorVisible ? "|" : ""), 496, 495, 15, Estilo.TEXTO);

        Lista<String> sugerencias = interprete.sugerencias(textoCanto.toString());
        StringBuilder lista = new StringBuilder();
        for (int i = 0; i < sugerencias.tamanio() && i < 3; i++) {
            if (i > 0) lista.append("  ·  ");
            lista.append(sugerencias.obtener(i));
        }
        String textoSugerencias = sugerencias.estaVacia() ? "(sin sugerencias)" : lista.toString();
        Texto.izquierda(g, Estilo.recortar(g, textoSugerencias, 11, 280), 486, 528, 11, Estilo.CREMA);
        Texto.izquierda(g, "Enter canta  ·  Flecha der. completa  ·  Esc cancela", 486, 574, 10, Estilo.TEXTO_SUAVE);
    }
}
