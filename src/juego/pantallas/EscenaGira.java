package juego.pantallas;

import estructuras.Lista;
import estructuras.MiLista;
import grafos.Grafo;
import grafos.ResultadoDijkstra;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import juego.reglas.Etapa;
import juego.reglas.Gira;
import motor.Escena;
import persistencia.Avisos;
import persistencia.RepositorioGira;
import persistencia.RepositorioMapa;
import ui.Texto;

/**
 * Mapa de la gira por Argentina.
 * - El arbol de la gira (Gira) dice el orden de las provincias, cual se juega y las vidas.
 * - El grafo de rutas (datos/mapa.txt) dice POR DONDE se viaja: entre una provincia y la
 *   siguiente, el recorrido es el camino minimo de Dijkstra (puede pasar por otras provincias).
 * - La camara arranca mostrando el pais y hace zoom sobre la provincia actual; al ganar, un
 *   marcador recorre la ruta hasta la provincia siguiente y la camara lo sigue.
 */
public class EscenaGira extends Escena {

    // Ventana del mapa en pantalla y escala (pixeles por grado de longitud/latitud con zoom 1)
    private static final int VISTA_X = 16;
    private static final int VISTA_Y = 64;
    private static final int VISTA_W = 508;
    private static final int VISTA_H = 470;
    private static final double K = 13;
    private static final double ZOOM_LEJOS = 1.0;
    private static final double ZOOM_CERCA = 2.6;
    private static final double DURACION_VIAJE = 2.4;

    /** Donde esta cada provincia (centro aproximado, en grados). */
    private static class Ubicacion {
        private final String clave;
        private final double x; // coordenadas del mapa (ya convertidas de grados)
        private final double y;

        Ubicacion(String clave, double longitud, double latitud) {
            this.clave = clave;
            this.x = mapaX(longitud);
            this.y = mapaY(latitud);
        }
    }

    private final Gira gira;
    private final Grafo<String> rutas;
    private final Lista<Ubicacion> ubicaciones = new MiLista<>();
    private final Path2D continente = new Path2D.Double();
    private final Path2D tierraDelFuego = new Path2D.Double();
    private final Path2D islas = new Path2D.Double(); // Islas Malvinas (Gran Malvina y Soledad) e Isla de los Estados

    // Cartel de mensaje (por ejemplo "¡Completaste el Noreste!")
    private String mensaje;
    private double tiempoMensaje = 5;

    // Camara
    private double camX;
    private double camY;
    private double zoom;
    private double objetivoX;
    private double objetivoY;
    private double objetivoZoom;
    private boolean vistaCompleta = false;
    private double tiempo = 0;

    // Viaje animado entre la provincia ganada y la siguiente (camino de Dijkstra)
    private Lista<String> viaje = new MiLista<>();
    private double progresoViaje = 1;

    // Lo que el grafo dice del viaje a la provincia actual (se calcula una vez por provincia)
    private String claveViajeRevisado = null;
    private boolean conVueltaRevisado = true; // deteccion de ciclos (formanCiclo)
    private int tramosRevisados = 0;          // BFS por niveles (tramosHasta)

    private final Boton botonJugar = new Boton("JUGAR", "Enter", Estilo.VERDE, 536, 546, 248, 42);
    private final Boton botonVista = new Boton("VER ARGENTINA", "V", Estilo.AZUL, 16, 546, 250, 42);
    private final Boton botonMenu = new Boton("MENU", "Esc", Estilo.MAGENTA, 274, 546, 250, 42);
    private int mouseX = -1;
    private int mouseY = -1;
    private String provinciaBajoMouse = null;

    /**
     * @param gira          la gira (ya cargada o recien creada)
     * @param recienGanada  provincia que se acaba de ganar (para animar el viaje), o null
     * @param mensaje       texto para el cartel al entrar (puede ser vacio)
     */
    public EscenaGira(Gira gira, Etapa recienGanada, String mensaje) {
        this.gira = gira;
        this.rutas = RepositorioMapa.cargarMapa();
        this.mensaje = (mensaje == null) ? "" : mensaje;
        cargarUbicaciones();
        armarSilueta();
        verificarMapa();

        // Camara: arranca viendo todo el pais...
        camX = mapaX(-63.5);
        camY = mapaY(-38.5);
        zoom = ZOOM_LEJOS;
        objetivoX = camX;
        objetivoY = camY;
        objetivoZoom = zoom;

        // ...y si se acaba de ganar una provincia, se prepara el viaje hasta la siguiente
        Etapa actual = gira.getProvinciaActual();
        if (recienGanada != null && actual != null) {
            ResultadoDijkstra<String> ruta = rutas.dijkstra(recienGanada.getClave(), actual.getClave());
            if (ruta.existeCamino()) {
                viaje = ruta.getCamino();
                progresoViaje = 0;
                Ubicacion desde = ubicacion(recienGanada.getClave());
                camX = desde.x;
                camY = desde.y;
                zoom = ZOOM_CERCA;
            }
        }
    }

    // ------------------------------------------------------------------ geografia

    private static double mapaX(double longitud) {
        return (longitud + 74) * K;
    }

    private static double mapaY(double latitud) {
        return (-21 - latitud) * K;
    }

    private void cargarUbicaciones() {
        ubicaciones.agregar(new Ubicacion("formosa", -60.0, -24.8));
        ubicaciones.agregar(new Ubicacion("chaco", -60.8, -26.5));
        ubicaciones.agregar(new Ubicacion("corrientes", -57.9, -28.8));
        ubicaciones.agregar(new Ubicacion("misiones", -54.7, -26.9));
        ubicaciones.agregar(new Ubicacion("jujuy", -65.8, -23.1));
        ubicaciones.agregar(new Ubicacion("salta", -64.6, -24.9));
        ubicaciones.agregar(new Ubicacion("tucuman", -65.3, -27.0));
        ubicaciones.agregar(new Ubicacion("catamarca", -67.3, -27.9));
        ubicaciones.agregar(new Ubicacion("santiago del estero", -63.3, -27.9));
        ubicaciones.agregar(new Ubicacion("cordoba", -64.0, -31.6));
        ubicaciones.agregar(new Ubicacion("santa fe", -61.0, -30.6));
        ubicaciones.agregar(new Ubicacion("entre rios", -59.2, -32.1));
        ubicaciones.agregar(new Ubicacion("buenos aires", -60.0, -36.4));
    }

    private Ubicacion ubicacion(String clave) {
        for (int i = 0; i < ubicaciones.tamanio(); i++) {
            if (ubicaciones.obtener(i).clave.equals(clave)) return ubicaciones.obtener(i);
        }
        return new Ubicacion(clave, -63.5, -38.5); // provincia sin ubicacion conocida: al centro
    }

    /** Silueta aproximada de Argentina (puntos de longitud y latitud reales, simplificados). */
    private void armarSilueta() {
        // Sin arreglos: los puntos se agregan uno por uno
        p(-66.2, -21.8, true); p(-64.3, -22.8, false); p(-62.8, -22.0, false); p(-61.0, -23.3, false);
        p(-58.6, -24.9, false); p(-57.6, -25.4, false); p(-58.6, -27.2, false); p(-56.4, -27.4, false);
        p(-55.6, -27.0, false); p(-54.6, -25.6, false); p(-53.6, -26.2, false); p(-53.8, -27.2, false);
        p(-56.0, -28.6, false); p(-57.6, -30.2, false); p(-58.1, -32.3, false); p(-58.4, -33.9, false);
        p(-58.4, -34.6, false); p(-57.3, -35.5, false); p(-56.7, -36.4, false); p(-57.5, -38.0, false);
        p(-59.8, -38.8, false); p(-62.3, -38.8, false); p(-62.1, -40.0, false); p(-64.8, -40.8, false);
        p(-65.1, -42.0, false); p(-63.6, -42.6, false); p(-65.0, -43.2, false); p(-65.6, -45.0, false);
        p(-67.6, -46.0, false); p(-65.9, -47.8, false); p(-68.0, -50.0, false); p(-69.0, -51.6, false);
        p(-68.4, -52.3, false); p(-71.0, -52.0, false); p(-72.3, -51.6, false); p(-73.0, -50.5, false);
        p(-72.3, -48.0, false); p(-71.7, -46.0, false); p(-71.6, -44.0, false); p(-71.8, -42.0, false);
        p(-71.4, -40.0, false); p(-71.0, -37.0, false); p(-70.4, -35.0, false); p(-69.9, -33.0, false);
        p(-70.0, -31.0, false); p(-69.3, -29.0, false); p(-68.5, -27.0, false); p(-68.3, -25.0, false);
        p(-67.0, -23.5, false);
        continente.closePath();
        tierraDelFuego.moveTo(mapaX(-68.6), mapaY(-52.6));
        tierraDelFuego.lineTo(mapaX(-65.2), mapaY(-54.7));
        tierraDelFuego.lineTo(mapaX(-68.6), mapaY(-54.9));
        tierraDelFuego.closePath();

        // Islas Malvinas: Gran Malvina (oeste)...
        isla(-61.35, -51.25, true); isla(-60.55, -51.22, false); isla(-59.95, -51.35, false);
        isla(-59.75, -51.65, false); isla(-60.05, -51.95, false); isla(-60.60, -52.20, false);
        isla(-61.15, -51.95, false); isla(-61.40, -51.60, false);
        islas.closePath();
        // ...e Isla Soledad (este), separadas por el estrecho de San Carlos
        isla(-59.35, -51.40, true); isla(-58.90, -51.30, false); isla(-58.30, -51.35, false);
        isla(-57.75, -51.55, false); isla(-57.80, -51.85, false); isla(-58.35, -52.05, false);
        isla(-58.90, -52.30, false); isla(-59.20, -52.05, false); isla(-59.40, -51.75, false);
        islas.closePath();
        // Isla de los Estados, al este de Tierra del Fuego
        isla(-64.80, -54.68, true); isla(-63.80, -54.72, false); isla(-64.60, -54.86, false);
        islas.closePath();
    }

    private void isla(double longitud, double latitud, boolean primero) {
        if (primero) {
            islas.moveTo(mapaX(longitud), mapaY(latitud));
        } else {
            islas.lineTo(mapaX(longitud), mapaY(latitud));
        }
    }

    private void p(double longitud, double latitud, boolean primero) {
        if (primero) {
            continente.moveTo(mapaX(longitud), mapaY(latitud));
        } else {
            continente.lineTo(mapaX(longitud), mapaY(latitud));
        }
    }

    // Mapa -> pantalla con la camara actual
    private double pantallaX(double x) {
        return VISTA_X + VISTA_W / 2.0 + (x - camX) * zoom;
    }

    private double pantallaY(double y) {
        return VISTA_Y + VISTA_H / 2.0 + (y - camY) * zoom;
    }

    // ------------------------------------------------------------------ logica

    @Override
    public void actualizar(double dt) {
        tiempo += dt;
        tiempoMensaje -= dt;
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();

        moverCamara(dt);
        provinciaBajoMouse = buscarProvinciaBajoMouse();

        Etapa actual = gira.getProvinciaActual();
        if (getEntrada().fuePresionada(KeyEvent.VK_V) || (click && botonVista.contiene(mouseX, mouseY))) {
            vistaCompleta = !vistaCompleta;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_ESCAPE) || (click && botonMenu.contiene(mouseX, mouseY))) {
            getContexto().irA(new EscenaMenu());
            return;
        }
        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER) || (click && botonJugar.contiene(mouseX, mouseY))) {
            if (gira.isCompleta()) {
                RepositorioGira.borrar();
                getContexto().irA(new EscenaGira(Gira.nueva(), null, "Empieza una gira nueva desde Formosa."));
            } else {
                jugar();
            }
            return;
        }
        if (click && provinciaBajoMouse != null) {
            elegirProvincia(provinciaBajoMouse);
        }
    }

    private void jugar() {
        if (!viajeConVuelta()) {
            // El grafo decide: sin ciclo entre las dos provincias, viajar seria quedar varado
            mostrar("Desde " + gira.getProvinciaAnterior().getNombre() + " no hay ruta de ida y vuelta a "
                    + gira.getProvinciaActual().getNombre() + " (revisa datos/mapa.txt).");
            return;
        }
        getContexto().irA(new EscenaConfiguracion(new ModoGira(gira), this));
    }

    // ------------------------------------------------------------------ grafo: BFS y ciclos

    /**
     * Al abrir el mapa se revisa con BFS que todas las provincias de la gira se puedan alcanzar
     * desde la primera. Si mapa.txt quedo sin rutas hasta alguna, se avisa en pantalla.
     */
    private void verificarMapa() {
        Lista<Etapa> provincias = gira.provincias();
        if (provincias.estaVacia()) {
            return;
        }
        Lista<String> alcanzables = rutas.bfs(provincias.obtener(0).getClave());
        for (int i = 1; i < provincias.tamanio(); i++) {
            Etapa provincia = provincias.obtener(i);
            if (alcanzables.indiceDe(provincia.getClave()) < 0) {
                Avisos.registrar("mapa.txt: no hay ninguna ruta hasta " + provincia.getNombre() + ".");
            }
        }
    }

    /**
     * Deteccion de ciclos aplicada a la gira: solo se viaja a la provincia actual si desde la
     * ultima ganada se puede ir Y volver (las dos estan en un mismo ciclo dirigido del grafo).
     * Una ruta solo de ida dejaria al gaucho varado, asi que el viaje no se permite.
     */
    private boolean viajeConVuelta() {
        Etapa anterior = gira.getProvinciaAnterior();
        Etapa actual = gira.getProvinciaActual();
        if (anterior == null || actual == null) {
            return true; // la gira arranca aca: no hay viaje
        }
        if (!actual.getClave().equals(claveViajeRevisado)) {
            claveViajeRevisado = actual.getClave();
            conVueltaRevisado = rutas.formanCiclo(anterior.getClave(), actual.getClave());
            tramosRevisados = rutas.tramosHasta(anterior.getClave(), actual.getClave());
        }
        return conVueltaRevisado;
    }

    /** Menor cantidad de tramos (escalas + 1) del viaje actual, con BFS por niveles. */
    private int tramosDelViaje() {
        viajeConVuelta(); // asegura que el calculo este hecho para la provincia actual
        return gira.getProvinciaAnterior() == null ? 0 : tramosRevisados;
    }

    /** Click en una provincia: solo se juega la actual; las otras explican por que no. */
    private void elegirProvincia(String clave) {
        Etapa elegida = etapa(clave);
        if (elegida == null) return;
        if (gira.sePuedeJugar(elegida)) {
            jugar();
        } else if (elegida.isCompletada()) {
            mostrar("Ya ganaste en " + elegida.getNombre() + ".");
        } else {
            mostrar("Primero tenes que ganar en " + gira.getProvinciaActual().getNombre() + ".");
        }
    }

    private void mostrar(String texto) {
        mensaje = texto;
        tiempoMensaje = 3;
    }

    private Etapa etapa(String clave) {
        Lista<Etapa> provincias = gira.provincias();
        for (int i = 0; i < provincias.tamanio(); i++) {
            if (provincias.obtener(i).getClave().equals(clave)) return provincias.obtener(i);
        }
        return null;
    }

    /** La camara se acerca suavemente a su objetivo (viaje en curso, provincia actual o pais entero). */
    private void moverCamara(double dt) {
        Etapa actual = gira.getProvinciaActual();
        if (progresoViaje < 1) {
            progresoViaje = Math.min(1, progresoViaje + dt / DURACION_VIAJE);
            objetivoX = puntoDelViajeX(progresoViaje);
            objetivoY = puntoDelViajeY(progresoViaje);
            objetivoZoom = ZOOM_CERCA;
        } else if (vistaCompleta || actual == null || tiempo < 0.7) {
            objetivoX = mapaX(-63.5);
            objetivoY = mapaY(-38.5);
            objetivoZoom = ZOOM_LEJOS;
        } else {
            Ubicacion u = ubicacion(actual.getClave());
            objetivoX = u.x;
            objetivoY = u.y;
            objetivoZoom = ZOOM_CERCA;
        }
        double suavizado = Math.min(1, dt * 2.6);
        camX += (objetivoX - camX) * suavizado;
        camY += (objetivoY - camY) * suavizado;
        zoom += (objetivoZoom - zoom) * suavizado;
    }

    // Posicion sobre la ruta del viaje (polilinea), con t de 0 a 1
    private double puntoDelViajeX(double t) {
        return puntoDelViaje(t, true);
    }

    private double puntoDelViajeY(double t) {
        return puntoDelViaje(t, false);
    }

    private double puntoDelViaje(double t, boolean enX) {
        double total = largoViaje();
        double objetivo = total * t;
        double recorrido = 0;
        for (int i = 0; i < viaje.tamanio() - 1; i++) {
            Ubicacion a = ubicacion(viaje.obtener(i));
            Ubicacion b = ubicacion(viaje.obtener(i + 1));
            double tramo = Math.hypot(b.x - a.x, b.y - a.y);
            if (recorrido + tramo >= objetivo || i == viaje.tamanio() - 2) {
                double f = tramo == 0 ? 1 : Math.min(1, (objetivo - recorrido) / tramo);
                return enX ? a.x + (b.x - a.x) * f : a.y + (b.y - a.y) * f;
            }
            recorrido += tramo;
        }
        Ubicacion ultima = ubicacion(viaje.obtener(viaje.tamanio() - 1));
        return enX ? ultima.x : ultima.y;
    }

    private double largoViaje() {
        double total = 0;
        for (int i = 0; i < viaje.tamanio() - 1; i++) {
            Ubicacion a = ubicacion(viaje.obtener(i));
            Ubicacion b = ubicacion(viaje.obtener(i + 1));
            total += Math.hypot(b.x - a.x, b.y - a.y);
        }
        return total;
    }

    private String buscarProvinciaBajoMouse() {
        for (int i = 0; i < ubicaciones.tamanio(); i++) {
            Ubicacion u = ubicaciones.obtener(i);
            double dx = mouseX - pantallaX(u.x);
            double dy = mouseY - pantallaY(u.y);
            if (dx * dx + dy * dy <= 14 * 14 && mouseX < VISTA_X + VISTA_W) return u.clave;
        }
        return null;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        Estilo.fondo(g, ancho, alto);
        Estilo.titulo(g, "Gira por Argentina", 160, 46, 28);

        dibujarMapa(g);
        dibujarPanel(g);

        Etapa actual = gira.getProvinciaActual();
        boolean puede = actual != null && progresoViaje >= 1 && viajeConVuelta();
        Boton jugar = (actual == null)
                ? new Boton("NUEVA GIRA", "Enter", Estilo.CARAMELO, 536, 546, 248, 42)
                : new Boton("JUGAR", "Enter", Estilo.VERDE, 536, 546, 248, 42); // la provincia se ve en el panel
        jugar.dibujar(g, jugar.contiene(mouseX, mouseY), puede || actual == null);
        Boton vista = new Boton(vistaCompleta ? "ACERCAR" : "VER ARGENTINA", "V", Estilo.AZUL, 16, 546, 250, 42);
        vista.dibujar(g, vista.contiene(mouseX, mouseY), true);
        botonMenu.dibujar(g, botonMenu.contiene(mouseX, mouseY), true);

        if (!mensaje.isEmpty() && tiempoMensaje > 0) {
            boolean grande = mensaje.startsWith("¡Completaste") || mensaje.contains("completaste la gira");
            Estilo.pildora(g, Estilo.recortar(g, mensaje, grande ? 15 : 13, 480), VISTA_X + VISTA_W / 2.0, VISTA_Y + 30,
                    grande ? 15 : 13, grande ? Estilo.DORADO : new Color(10, 14, 30, 225),
                    grande ? new Color(40, 30, 10) : Estilo.TEXTO);
        }
        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }

    private void dibujarMapa(Graphics2D g) {
        // Marco y "mar"
        Estilo.panel(g, VISTA_X - 4, VISTA_Y - 4, VISTA_W + 8, VISTA_H + 8);
        Shape recorteAnterior = g.getClip();
        g.clipRect(VISTA_X, VISTA_Y, VISTA_W, VISTA_H);
        g.setPaint(new GradientPaint(0, VISTA_Y, new Color(22, 46, 92), 0, VISTA_Y + VISTA_H, new Color(12, 26, 58)));
        g.fillRect(VISTA_X, VISTA_Y, VISTA_W, VISTA_H);

        // Silueta del pais: se dibuja en coordenadas del mapa con la transformacion de la camara
        AffineTransform original = g.getTransform();
        g.translate(VISTA_X + VISTA_W / 2.0, VISTA_Y + VISTA_H / 2.0);
        g.scale(zoom, zoom);
        g.translate(-camX, -camY);
        g.setPaint(new GradientPaint(0, 0, new Color(214, 196, 150), 0, (float) mapaY(-55), new Color(170, 150, 110)));
        g.fill(continente);
        g.fill(tierraDelFuego);
        g.fill(islas);
        g.setStroke(new BasicStroke((float) (2.0 / zoom)));
        g.setColor(new Color(255, 245, 220, 200));
        g.draw(continente);
        g.draw(tierraDelFuego);
        g.draw(islas);
        g.setTransform(original);

        // Nombre de las Malvinas debajo de las islas (si estan a la vista)
        double malvinasX = pantallaX(mapaX(-59.5));
        double malvinasY = pantallaY(mapaY(-52.25));
        if (malvinasX > VISTA_X && malvinasX < VISTA_X + VISTA_W && malvinasY > VISTA_Y && malvinasY < VISTA_Y + VISTA_H) {
            Texto.centrado(g, "ISLAS MALVINAS", malvinasX, malvinasY + 10 + 6 * zoom, 9, new Color(246, 237, 211, 220));
            Texto.centrado(g, "(Argentina)", malvinasX, malvinasY + 22 + 6 * zoom, 8, new Color(190, 207, 197, 200));
        }

        dibujarRecorrido(g);
        dibujarProvincias(g);
        dibujarMarcadorDeViaje(g);
        g.setClip(recorteAnterior);
    }

    /** Tramos de la gira: entre cada provincia y la siguiente, la ruta de Dijkstra por el grafo. */
    private void dibujarRecorrido(Graphics2D g) {
        Lista<Etapa> provincias = gira.provincias();
        for (int i = 1; i < provincias.tamanio(); i++) {
            Etapa desde = provincias.obtener(i - 1);
            Etapa hasta = provincias.obtener(i);
            ResultadoDijkstra<String> ruta = rutas.dijkstra(desde.getClave(), hasta.getClave());
            if (!ruta.existeCamino()) continue;
            boolean hecho = hasta.isCompletada() || (gira.sePuedeJugar(hasta) && progresoViaje >= 1 && desde.isCompletada());
            Lista<String> camino = ruta.getCamino();
            for (int j = 0; j < camino.tamanio() - 1; j++) {
                Ubicacion a = ubicacion(camino.obtener(j));
                Ubicacion b = ubicacion(camino.obtener(j + 1));
                if (hecho) {
                    g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(Estilo.DORADO);
                    g.drawLine((int) pantallaX(a.x), (int) pantallaY(a.y), (int) pantallaX(b.x), (int) pantallaY(b.y));
                } else {
                    lineaPunteada(g, pantallaX(a.x), pantallaY(a.y), pantallaX(b.x), pantallaY(b.y));
                }
            }
        }
    }

    /** Linea de puntos (sin arreglos de "dash": se dibujan circulitos a distancia fija). */
    private void lineaPunteada(Graphics2D g, double x1, double y1, double x2, double y2) {
        double largo = Math.hypot(x2 - x1, y2 - y1);
        g.setColor(new Color(255, 255, 255, 150));
        for (double d = 0; d <= largo; d += 9) {
            double f = largo == 0 ? 0 : d / largo;
            g.fillOval((int) (x1 + (x2 - x1) * f) - 2, (int) (y1 + (y2 - y1) * f) - 2, 4, 4);
        }
    }

    private void dibujarProvincias(Graphics2D g) {
        Etapa actual = gira.getProvinciaActual();
        Etapa regionActual = gira.getRegionActual();
        Lista<Etapa> provincias = gira.provincias();
        for (int i = 0; i < provincias.tamanio(); i++) {
            Etapa e = provincias.obtener(i);
            Ubicacion u = ubicacion(e.getClave());
            double x = pantallaX(u.x);
            double y = pantallaY(u.y);
            boolean esActual = e.equals(actual);
            int r = esActual ? 13 : 10;
            if (esActual) {
                // Aro que "late" sobre la provincia que se juega
                double pulso = 4 + 3 * Math.sin(tiempo * 4);
                g.setStroke(new BasicStroke(3f));
                g.setColor(new Color(246, 200, 92, 180));
                g.drawOval((int) (x - r - pulso), (int) (y - r - pulso), (int) ((r + pulso) * 2), (int) ((r + pulso) * 2));
                g.setColor(Estilo.DORADO);
            } else if (e.isCompletada()) {
                g.setColor(Estilo.VERDE);
            } else {
                g.setColor(new Color(110, 116, 140));
            }
            g.fillOval((int) (x - r), (int) (y - r), r * 2, r * 2);
            g.setStroke(new BasicStroke(e.getClave().equals(provinciaBajoMouse) ? 3f : 2f));
            g.setColor(Color.WHITE);
            g.drawOval((int) (x - r), (int) (y - r), r * 2, r * 2);
            if (e.isCompletada()) {
                tilde(g, x, y);
            } else if (!esActual) {
                candado(g, x, y);
            }
            boolean mostrarNombre = esActual || zoom > 1.8 || e.getClave().equals(provinciaBajoMouse)
                    || gira.regionDe(e).equals(regionActual);
            if (mostrarNombre) {
                Estilo.pildora(g, e.getNombre().toUpperCase(), x, y + r + 12, 9, new Color(10, 14, 30, 210),
                        esActual ? Estilo.DORADO : Estilo.TEXTO);
            }
        }
    }

    private void tilde(Graphics2D g, double x, double y) {
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine((int) x - 5, (int) y, (int) x - 1, (int) y + 4);
        g.drawLine((int) x - 1, (int) y + 4, (int) x + 5, (int) y - 4);
    }

    private void candado(Graphics2D g, double x, double y) {
        g.setColor(new Color(235, 235, 245));
        g.fillRect((int) x - 4, (int) y - 1, 8, 6);
        g.setStroke(new BasicStroke(1.6f));
        g.drawArc((int) x - 3, (int) y - 6, 6, 8, 0, 180);
    }

    /** El jugador viajando por la ruta hacia la provincia siguiente. */
    private void dibujarMarcadorDeViaje(Graphics2D g) {
        if (progresoViaje >= 1 || viaje.tamanio() < 2) return;
        double x = pantallaX(puntoDelViajeX(progresoViaje));
        double y = pantallaY(puntoDelViajeY(progresoViaje));
        Estilo.avatar(g, x, y, 12, "V", Estilo.AZUL);
    }

    /** Panel derecho: region actual, sus provincias, vidas y recorrido. */
    private void dibujarPanel(Graphics2D g) {
        int x = 536;
        Estilo.panel(g, x, 60, 248, 478);
        Etapa actual = gira.getProvinciaActual();
        Etapa region = gira.getRegionActual();
        if (region == null) {
            Estilo.trofeo(g, x + 124, 130, 1.0, Estilo.DORADO);
            Texto.centrado(g, "¡GIRA COMPLETA!", x + 124, 200, 16, Estilo.DORADO);
            Texto.centrado(g, "Ganaste en las " + gira.provincias().tamanio() + " provincias.", x + 124, 226, 12, Estilo.TEXTO_SUAVE);
            return;
        }
        Lista<Etapa> provincias = gira.provinciasDe(region);
        Texto.izquierda(g, "REGION " + (gira.regiones().indiceDe(region) + 1) + " DE " + gira.regiones().tamanio(), x + 16, 86, 10, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, region.getNombre().toUpperCase(), x + 16, 108, 15, Estilo.DORADO);

        // Vidas de la region
        Texto.izquierda(g, "VIDAS", x + 16, 136, 10, Estilo.TEXTO_SUAVE);
        for (int i = 0; i < Gira.VIDAS_POR_REGION; i++) {
            corazon(g, x + 66 + i * 26, 132, i < gira.getVidas());
        }

        // Provincias de la region en orden
        for (int i = 0; i < provincias.tamanio(); i++) {
            Etapa e = provincias.obtener(i);
            int y = 168 + i * 32;
            boolean esActual = e.equals(actual);
            g.setColor(esActual ? new Color(246, 200, 92, 50) : new Color(255, 255, 255, 10));
            g.fillRoundRect(x + 10, y - 13, 228, 26, 13, 13);
            Color color = e.isCompletada() ? Estilo.VERDE : (esActual ? Estilo.DORADO : new Color(110, 116, 140));
            g.setColor(color);
            g.fillOval(x + 18, y - 8, 16, 16);
            if (e.isCompletada()) tilde(g, x + 26, y);
            else if (!esActual) candado(g, x + 26, y);
            Texto.izquierda(g, e.getNombre(), x + 42, y + 5, 12, esActual ? Estilo.DORADO : Estilo.TEXTO);
            String estado = e.isCompletada() ? "ganada" : (esActual ? "se juega" : "bloqueada");
            Texto.derecha(g, estado, x + 230, y + 5, 10, Estilo.TEXTO_SUAVE);
        }

        // Recorrido desde la ultima provincia ganada (camino minimo en el grafo)
        int yRuta = 180 + provincias.tamanio() * 32;
        Etapa anterior = gira.getProvinciaAnterior();
        Texto.izquierda(g, "RECORRIDO (DIJKSTRA)", x + 16, yRuta, 10, Estilo.TEXTO_SUAVE);
        if (anterior == null) {
            Texto.izquierda(g, "La gira arranca en " + actual.getNombre() + ".", x + 16, yRuta + 20, 11, Estilo.TEXTO);
        } else {
            ResultadoDijkstra<String> ruta = rutas.dijkstra(anterior.getClave(), actual.getClave());
            if (ruta.existeCamino()) {
                StringBuilder sb = new StringBuilder();
                Lista<String> camino = ruta.getCamino();
                for (int i = 0; i < camino.tamanio(); i++) {
                    if (i > 0) sb.append(" > ");
                    sb.append(nombreCorto(camino.obtener(i)));
                }
                Lista<String> renglones = Estilo.envolver(g, sb.toString(), 11, 216, 3);
                for (int i = 0; i < renglones.tamanio(); i++) {
                    Texto.izquierda(g, renglones.obtener(i), x + 16, yRuta + 20 + i * 16, 11, Estilo.TEXTO);
                }
                int yKm = yRuta + 24 + renglones.tamanio() * 16;
                Texto.izquierda(g, (ruta.getDistanciaTotal() * 100) + " km", x + 16, yKm, 11, Estilo.DORADO);
                // BFS: menor cantidad de tramos; ciclo: si se puede volver (si no, el viaje se bloquea)
                int tramos = tramosDelViaje();
                Texto.izquierda(g, tramos + (tramos == 1 ? " tramo" : " tramos") + " (BFS)", x + 92, yKm, 10, Estilo.TEXTO_SUAVE);
                boolean conVuelta = viajeConVuelta();
                Texto.izquierda(g, conVuelta ? "Ida y vuelta: hay regreso (ciclo)" : "Sin ruta de regreso: viaje bloqueado",
                        x + 16, yKm + 18, 10, conVuelta ? Estilo.TEXTO_SUAVE : new Color(255, 140, 160));
            } else {
                Texto.izquierda(g, "No hay ruta en mapa.txt.", x + 16, yRuta + 20, 11, new Color(255, 140, 160));
            }
        }

        // Regiones de la gira
        Lista<Etapa> regiones = gira.regiones();
        for (int i = 0; i < regiones.tamanio(); i++) {
            Etapa r = regiones.obtener(i);
            boolean hecha = gira.regionCompleta(r);
            String nombre = r.getNombre().contains("(") ? r.getNombre().substring(r.getNombre().indexOf('(') + 1, r.getNombre().indexOf(')')) : r.getNombre().toUpperCase();
            Estilo.pildora(g, nombre, x + 46 + i * 78, 510, 10, hecha ? Estilo.VERDE : (r.equals(region) ? Estilo.CARAMELO : new Color(70, 80, 110)), Estilo.TEXTO);
        }
    }

    private String nombreCorto(String clave) {
        Etapa e = etapa(clave);
        String nombre = (e == null) ? clave : e.getNombre();
        return nombre.equals("Santiago del Estero") ? "Sgo. del Estero" : nombre;
    }

    /** Corazon de vida: dos circulos y un triangulo (lleno si la vida esta, vacio si se perdio). */
    private void corazon(Graphics2D g, double cx, double cy, boolean lleno) {
        Path2D forma = new Path2D.Double();
        forma.moveTo(cx, cy + 8);
        forma.curveTo(cx - 12, cy, cx - 8, cy - 9, cx, cy - 4);
        forma.curveTo(cx + 8, cy - 9, cx + 12, cy, cx, cy + 8);
        forma.closePath();
        if (lleno) {
            g.setColor(new Color(226, 60, 80));
            g.fill(forma);
        }
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(lleno ? new Color(255, 170, 180) : new Color(150, 156, 180));
        g.draw(forma);
    }
}
