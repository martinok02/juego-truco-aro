package juego.pruebas;

import static juego.pruebas.MazoPreparado.c;

import juego.reglas.Accion;
import juego.reglas.ConfiguracionPartido;
import juego.reglas.Lado;
import juego.reglas.MesaTruco;
import juego.reglas.Palo;

/**
 * Pruebas de las reglas del truco (MesaTruco) contra el reglamento de Wikipedia:
 * pardas, mano, escalera del envido, truco / retruco / vale cuatro, falta envido y flor.
 * Cada prueba reparte cartas conocidas con MazoPreparado.
 *
 * Ejecutar: ejecutar.bat juego.pruebas.PruebaReglas
 */
public class PruebaReglas {

    private static final Lado J = Lado.JUGADOR;
    private static final Lado R = Lado.RIVAL;

    private static int superadas = 0;
    private static int total = 0;

    private static void afirmar(boolean condicion, String nombre) {
        total++;
        if (condicion) {
            superadas++;
            System.out.println("  [OK] " + nombre);
        } else {
            System.err.println("  [FALLO] " + nombre);
        }
    }

    private static MesaTruco mesa(MazoPreparado mazo, Lado mano) {
        return mesa(mazo, mano, 15, false);
    }

    private static MesaTruco mesa(MazoPreparado mazo, Lado mano, int puntos, boolean flor) {
        return new MesaTruco(new ConfiguracionPartido(puntos, flor), mano, mazo);
    }

    /** Juega la ronda actual tirando siempre la primera carta de la mano de quien tenga el turno. */
    private static void jugarRonda(MesaTruco mesa) {
        int ronda = mesa.getNumeroRonda();
        for (int i = 0; i < 6 && ronda == mesa.getNumeroRonda() && !mesa.isTerminado(); i++) {
            mesa.jugarCarta(mesa.getTurno(), 0);
        }
    }

    public static void ejecutarTodas() {
        System.out.println("==================================================");
        System.out.println("   PRUEBAS DE REGLAS DEL TRUCO (MesaTruco)        ");
        System.out.println("==================================================");
        probarPardasYMano();
        probarEnvido();
        probarTruco();
        probarFlor();
        probarCantosEscritos();
        probarGira();
        probarTorneo();
        System.out.println("==================================================");
        System.out.println("Resultado: " + superadas + " de " + total + " pruebas pasadas.");
        System.out.println("==================================================");
    }

    private static void probarPardasYMano() {
        System.out.println("\n--- 1. Bazas, pardas y mano ---");

        // Gana uno cada uno y la tercera es parda: gana el que gano la primera
        MesaTruco m = mesa(new MazoPreparado()
                .mano(c(3, Palo.ESPADA), c(4, Palo.COPA), c(5, Palo.ORO))
                .mano(c(4, Palo.BASTO), c(7, Palo.COPA), c(5, Palo.COPA)), J);
        jugarRonda(m);
        afirmar(m.getPuntos(J) == 1 && m.getPuntos(R) == 0, "Primera tuya, segunda del rival, tercera parda: gana quien hizo primera");
        afirmar(m.getManoDeLaRonda() == R, "La mano pasa al rival en la ronda siguiente");
        afirmar(m.getBazasRondaAnterior().tamanio() == 3 && m.getBazasRonda().estaVacia(),
                "Las tres bazas quedan guardadas como 'ronda anterior' y la nueva empieza vacia");
        afirmar(m.getBazasRondaAnterior().obtener(0).getCarta(J).getNumero() == 3
                        && m.getBazasRondaAnterior().obtener(0).getGanador() == J
                        && m.getBazasRondaAnterior().obtener(2).esParda()
                        && m.getBazasRondaAnterior().obtener(2).getSalio() == R,
                "Cada baza recuerda las cartas, quien salio y quien gano (la tercera fue parda y salio el rival)");

        // Primera ganada y segunda parda: termina en la segunda
        m = mesa(new MazoPreparado()
                .mano(c(3, Palo.ESPADA), c(4, Palo.COPA), c(5, Palo.ORO))
                .mano(c(4, Palo.BASTO), c(4, Palo.ORO), c(6, Palo.COPA)), J);
        jugarRonda(m);
        afirmar(m.getPuntos(J) == 1 && m.getNumeroRonda() == 2, "Primera tuya y segunda parda: ganas sin jugar la tercera");

        // Primera parda: define la segunda
        m = mesa(new MazoPreparado()
                .mano(c(4, Palo.COPA), c(5, Palo.ORO), c(6, Palo.ESPADA))
                .mano(c(4, Palo.ORO), c(7, Palo.COPA), c(6, Palo.COPA)), J);
        jugarRonda(m);
        afirmar(m.getPuntos(R) == 1 && m.getPuntos(J) == 0, "Primera parda: gana quien gana la segunda");

        // Parda, parda: define la tercera
        m = mesa(new MazoPreparado()
                .mano(c(4, Palo.COPA), c(5, Palo.ORO), c(3, Palo.ESPADA))
                .mano(c(4, Palo.ORO), c(5, Palo.COPA), c(2, Palo.BASTO)), J);
        jugarRonda(m);
        afirmar(m.getPuntos(J) == 1, "Dos pardas: gana quien gana la tercera");

        // Tres pardas: gana la mano
        m = mesa(new MazoPreparado()
                .mano(c(4, Palo.COPA), c(5, Palo.ORO), c(6, Palo.ESPADA))
                .mano(c(4, Palo.ORO), c(5, Palo.COPA), c(6, Palo.COPA)), R);
        jugarRonda(m);
        afirmar(m.getPuntos(R) == 1 && m.getPuntos(J) == 0, "Tres pardas: gana la mano");
    }

    private static MazoPreparado manosEnvido() {
        // Jugador: 7 y 6 de copa = 33 de envido. Rival: sin pareja, su mejor carta es un 6.
        return new MazoPreparado()
                .mano(c(7, Palo.COPA), c(6, Palo.COPA), c(4, Palo.ORO))
                .mano(c(4, Palo.ESPADA), c(5, Palo.BASTO), c(6, Palo.ORO));
    }

    private static void probarEnvido() {
        System.out.println("\n--- 2. Envido ---");

        MesaTruco m = mesa(manosEnvido(), J);
        afirmar(m.puntosEnvido(J) == 33 && m.puntosEnvido(R) == 6, "Calculo: pareja suma 20 + cartas; sin pareja, la mejor carta");
        m.ejecutar(J, Accion.ENVIDO);
        m.ejecutar(R, Accion.NO_QUIERO);
        afirmar(m.getPuntos(J) == 1, "Envido no querido: 1 punto");

        m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.ENVIDO);
        m.ejecutar(R, Accion.ENVIDO);
        afirmar(!m.puedeHacer(J, Accion.ENVIDO) && m.puedeHacer(J, Accion.REAL_ENVIDO),
                "Despues de envido-envido no hay un tercer envido, pero si real envido");
        m.ejecutar(J, Accion.REAL_ENVIDO);
        m.ejecutar(R, Accion.NO_QUIERO);
        afirmar(m.getPuntos(J) == 4, "Envido, envido, real envido no querido: 4 puntos");

        m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.ENVIDO);
        m.ejecutar(R, Accion.REAL_ENVIDO);
        m.ejecutar(J, Accion.QUIERO);
        afirmar(m.getPuntos(J) == 5, "Envido, real envido querido: 5 puntos al de mas tanto");

        // Empate de envido: gana la mano (el rival es mano y canta)
        m = mesa(new MazoPreparado()
                .mano(c(7, Palo.COPA), c(6, Palo.COPA), c(4, Palo.ORO))
                .mano(c(7, Palo.BASTO), c(6, Palo.BASTO), c(4, Palo.ESPADA)), R);
        m.ejecutar(R, Accion.ENVIDO);
        m.ejecutar(J, Accion.QUIERO);
        afirmar(m.getPuntos(R) == 2 && m.getPuntos(J) == 0, "Empate de envido (33 a 33): gana la mano");

        m = mesa(manosEnvido(), J);
        afirmar(m.puntosFaltaEnvido(J) == 15, "Falta envido a 15 con 0 a 0: vale 15");
        m.ejecutar(J, Accion.FALTA_ENVIDO);
        m.ejecutar(R, Accion.QUIERO);
        afirmar(m.isTerminado() && m.getGanadorPartido() == J, "Falta envido querida y ganada a 0 a 0: gana el partido");

        m = mesa(manosEnvido(), J, 30, false);
        afirmar(m.puntosFaltaEnvido(R) == 30, "A 30, los dos en las malas: la falta envido gana el partido");

        // Solo se canta antes de tirar la propia carta y en la primera baza
        m = mesa(manosEnvido(), J);
        m.jugarCarta(J, 0);
        afirmar(!m.puedeHacer(J, Accion.ENVIDO) || m.quienActua() != J, "Despues de tirar, el jugador ya no puede cantar envido");
        afirmar(m.puedeHacer(R, Accion.ENVIDO), "El pie todavia puede cantar envido antes de tirar");
        m.jugarCarta(R, 0);
        Lado siguiente = m.quienActua();
        afirmar(!m.puedeHacer(siguiente, Accion.ENVIDO), "En la segunda baza ya no hay envido");
    }

    private static void probarTruco() {
        System.out.println("\n--- 3. Truco, retruco y vale cuatro ---");

        MesaTruco m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.TRUCO);
        m.ejecutar(R, Accion.RETRUCO);
        m.ejecutar(J, Accion.NO_QUIERO);
        afirmar(m.getPuntos(R) == 2, "Retruco no querido: 2 puntos para quien lo canto");

        m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.TRUCO);
        m.ejecutar(R, Accion.RETRUCO);
        m.ejecutar(J, Accion.VALE_CUATRO);
        m.ejecutar(R, Accion.NO_QUIERO);
        afirmar(m.getPuntos(J) == 3, "Vale cuatro no querido: 3 puntos");

        m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.TRUCO);
        m.ejecutar(R, Accion.QUIERO);
        afirmar(!m.puedeHacer(J, Accion.RETRUCO), "Quien canto el truco no puede subirse a si mismo");
        afirmar(!m.puedeHacer(J, Accion.ENVIDO), "Con el truco querido ya no se puede cantar envido");
        m.jugarCarta(J, 0);
        afirmar(m.puedeHacer(R, Accion.RETRUCO), "Quien quiso el truco tiene el quiero para el retruco");
        m.ejecutar(R, Accion.QUIERO); // no hace nada: no hay nada que querer
        afirmar(m.getTrucoPropuesto() == null, "QUIERO sin canto pendiente se rechaza");

        m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.TRUCO);
        afirmar(m.puedeHacer(R, Accion.ENVIDO), "\"El envido esta primero\": se puede responder un truco con envido");
        m.ejecutar(R, Accion.ENVIDO);
        m.ejecutar(J, Accion.QUIERO);
        afirmar(m.getPuntos(J) == 2 && m.quienActua() == R && m.puedeHacer(R, Accion.QUIERO),
                "Resuelto el envido, el rival vuelve a responder el truco");

        m = mesa(manosEnvido(), J);
        m.ejecutar(J, Accion.TRUCO);
        m.ejecutar(R, Accion.QUIERO);
        m.ejecutar(J, Accion.MAZO);
        afirmar(m.getPuntos(R) == 2, "Irse al mazo con el truco querido: el otro cobra 2");

        m = mesa(new MazoPreparado()
                .mano(c(3, Palo.ESPADA), c(4, Palo.COPA), c(5, Palo.ORO))
                .mano(c(4, Palo.BASTO), c(7, Palo.COPA), c(5, Palo.COPA)), J);
        m.ejecutar(J, Accion.TRUCO);
        m.ejecutar(R, Accion.QUIERO);
        jugarRonda(m);
        afirmar(m.getPuntos(J) == 2, "Ronda ganada con truco querido: 2 puntos");
    }

    private static void probarFlor() {
        System.out.println("\n--- 4. Flor (opcional) ---");

        // Jugador con flor de espadas (1 + 5 + 6 + 20 = 32)
        MesaTruco m = mesa(new MazoPreparado()
                .mano(c(1, Palo.ESPADA), c(5, Palo.ESPADA), c(6, Palo.ESPADA))
                .mano(c(4, Palo.BASTO), c(7, Palo.COPA), c(5, Palo.COPA)), J, 15, true);
        afirmar(m.getPuntos(J) == 3, "Con flor: la flor suma 3 al repartir");
        afirmar(!m.puedeHacer(J, Accion.ENVIDO), "Si hay flor, esa ronda no hay envido");

        m = mesa(new MazoPreparado()
                .mano(c(1, Palo.ESPADA), c(5, Palo.ESPADA), c(6, Palo.ESPADA))
                .mano(c(7, Palo.ORO), c(6, Palo.ORO), c(5, Palo.ORO)), J, 15, true);
        afirmar(m.getPuntos(R) == 4 && m.getPuntos(J) == 0, "Flor contra flor: 4 puntos a la mejor (38 contra 32)");

        m = mesa(new MazoPreparado()
                .mano(c(1, Palo.ESPADA), c(5, Palo.ESPADA), c(6, Palo.ESPADA))
                .mano(c(4, Palo.BASTO), c(7, Palo.COPA), c(5, Palo.COPA)), J, 15, false);
        afirmar(m.getPuntos(J) == 0 && m.puedeHacer(J, Accion.ENVIDO), "Sin flor: tres del mismo palo no suman y se juega el envido");

        boolean rechazado = false;
        try {
            new ConfiguracionPartido(20, false);
        } catch (IllegalArgumentException e) {
            rechazado = true;
        }
        afirmar(rechazado, "La configuracion solo acepta partidos a 15 o a 30");
    }

    private static void probarCantosEscritos() {
        System.out.println("\n--- 5. Cantos escritos (InterpreteCantos con Trie) ---");
        juego.reglas.InterpreteCantos interprete = new juego.reglas.InterpreteCantos();
        estructuras.Lista<Accion> respondiendoTruco = new estructuras.MiLista<>();
        respondiendoTruco.agregar(Accion.QUIERO);
        respondiendoTruco.agregar(Accion.NO_QUIERO);
        respondiendoTruco.agregar(Accion.RETRUCO);

        afirmar(interprete.interpretar("Quiero retruco!", respondiendoTruco) == Accion.RETRUCO,
                "\"quiero retruco\" se lee como un solo canto (coincidencia mas larga): RETRUCO");
        afirmar(interprete.interpretar("no quiero", respondiendoTruco) == Accion.NO_QUIERO,
                "\"no quiero\" no se confunde con \"quiero\"");
        afirmar(interprete.interpretar("real envido", respondiendoTruco) == null
                        && interprete.reconocer("real envido") == Accion.REAL_ENVIDO,
                "Un canto que se entiende pero no se puede hacer ahora no se ejecuta");
        afirmar(interprete.reconocer("hola che") == null, "Texto sin cantos: no se reconoce nada");
        afirmar(interprete.reconocer("bueno, me voy al mazo") == Accion.MAZO, "\"me voy al mazo\" dentro de una frase: MAZO");
        afirmar(interprete.sugerencias("re").indiceDe("real envido") >= 0 && interprete.sugerencias("re").indiceDe("retruco") >= 0,
                "Sugerencias por prefijo: \"re\" sugiere real envido y retruco");

        // Con la mesa real: el texto se convierte en la jugada
        MesaTruco m = mesa(manosEnvido(), J);
        Accion escrita = interprete.interpretar("envido", m.accionesDisponibles(J));
        m.ejecutar(J, escrita);
        afirmar(m.isEnvidoPendiente(), "Escribir \"envido\" en tu turno canta envido en la mesa");
    }

    private static void probarGira() {
        System.out.println("\n--- 6. Gira por Argentina (arbol hijo-izquierdo / hermano-derecho) ---");
        juego.reglas.Gira gira = juego.reglas.Gira.nueva();
        estructuras.Lista<juego.reglas.Etapa> provincias = gira.provincias();
        afirmar(provincias.tamanio() == 13 && gira.regiones().tamanio() == 3, "La gira tiene 13 provincias en 3 regiones");
        afirmar(gira.recorrido().obtener(0).getNombre().equals("Argentina")
                        && gira.recorrido().obtener(1).getClave().equals("noreste")
                        && gira.recorrido().obtener(2).getClave().equals("formosa")
                        && provincias.obtener(4).getClave().equals("jujuy"),
                "El pre-orden da el orden de la gira: Argentina, Noreste, Formosa, ... y despues Jujuy");
        afirmar(gira.getProvinciaActual().getClave().equals("formosa") && !gira.sePuedeJugar(provincias.obtener(1)),
                "Se arranca en Formosa y no se puede saltear a Chaco");

        gira.registrarVictoria();
        afirmar(gira.getProvinciaActual().getClave().equals("chaco"), "Ganar Formosa habilita Chaco");
        String derrota = gira.registrarDerrota();
        afirmar(gira.getVidas() == 5 && gira.getProvinciaActual().getClave().equals("chaco") && derrota.contains("5"),
                "Perder cuesta una vida y se vuelve a jugar la misma provincia");

        for (int i = 0; i < 5; i++) gira.registrarDerrota();
        afirmar(gira.getVidas() == juego.reglas.Gira.VIDAS_POR_REGION && gira.getProvinciaActual().getClave().equals("formosa"),
                "Sin vidas, la region vuelve a empezar desde Formosa (y se recuperan las 6 vidas)");

        gira.registrarDerrota(); // queda con 5
        gira.registrarVictoria();
        gira.registrarVictoria();
        gira.registrarVictoria();
        String region = gira.registrarVictoria();
        afirmar(region.startsWith("¡Completaste el Noreste") && gira.getVidas() == 6
                        && gira.getProvinciaActual().getClave().equals("jujuy"),
                "Completar el Noreste da el mensaje, renueva las vidas y pasa a Jujuy");

        // Guardar y volver a cargar (archivo temporal: no toca datos/)
        try {
            java.io.File archivo = java.io.File.createTempFile("prueba_gira_", ".txt");
            archivo.deleteOnExit();
            new java.io.File(archivo.getAbsolutePath() + ".bak").deleteOnExit();
            gira.registrarDerrota();
            persistencia.RepositorioGira.guardar(gira, archivo);
            juego.reglas.Gira cargada = persistencia.RepositorioGira.cargar(archivo);
            afirmar(cargada.getProvinciaActual().getClave().equals("jujuy") && cargada.getVidas() == 5
                            && cargada.getArbol().cantidadNodos() == 17,
                    "La gira se guarda en pre-orden y se reconstruye (17 nodos, Jujuy, 5 vidas)");
            try (java.io.Writer w = new java.io.FileWriter(archivo)) {
                w.write("vidas=4\nE;PAIS;argentina;Argentina;no\nE;REGION;noreste;Noreste;talvez\n");
            }
            juego.reglas.Gira rota = persistencia.RepositorioGira.cargar(archivo);
            afirmar(rota.getProvinciaActual().getClave().equals("formosa")
                            && persistencia.Avisos.huboAvisoCon(archivo.getName() + ", linea 3"),
                    "Gira con una linea danada: se avisa y empieza una gira nueva");
        } catch (java.io.IOException e) {
            afirmar(false, "No se pudo usar un archivo temporal: " + e.getMessage());
        }
    }

    private static void probarTorneo() {
        System.out.println("\n--- 7. Torneo (llave eliminatoria) ---");
        java.util.Random azar = new java.util.Random(3);
        juego.reglas.Torneo torneo = juego.reglas.Torneo.desdeLlave(persistencia.RepositorioLlave.crearLlavePorDefecto(), azar);
        afirmar(torneo.getJugador().equals("Rodri") && torneo.getRivalActual().equals("Tano")
                        && torneo.getPartidoDelJugador().getRonda() == 1,
                "El jugador arranca en cuartos contra Tano");
        juego.pantallas.ModoTorneo modo = new juego.pantallas.ModoTorneo(torneo);
        afirmar(modo.getPerfilRival() == juego.pantallas.ModoPartido.PerfilRival.CONSERVADOR
                        && modo.crearRival().getNombreEstrategia().equals("Gaucho Prudente"),
                "Cuartos: rival prudente con perfil propio");
        torneo.registrarResultado(true);
        estructuras.Lista<juego.reglas.Partido> partidos = torneo.getLlave().porNiveles();
        afirmar(torneo.getPartidoDelJugador() == partidos.obtener(1) && partidos.obtener(1).getJugadorA().equals("Rodri")
                        && !partidos.obtener(1).getJugadorB().equals("-") && partidos.obtener(2).getJugadorA().length() > 1,
                "Ganar cuartos: los ganadores suben a semifinales (los otros partidos se resuelven solos)");
        modo = new juego.pantallas.ModoTorneo(torneo);
        afirmar(modo.getPerfilRival() == juego.pantallas.ModoPartido.PerfilRival.ARRIESGADO
                        && modo.crearRival().getNombreEstrategia().equals("Gaucho Picante"),
                "Semifinal: rival arriesgado con perfil propio");
        torneo.registrarResultado(true);
        afirmar(torneo.getPartidoDelJugador() == partidos.obtener(0), "Ganar la semifinal lleva a la final");
        modo = new juego.pantallas.ModoTorneo(torneo);
        afirmar(modo.getPerfilRival() == juego.pantallas.ModoPartido.PerfilRival.MENTIROSO
                        && modo.crearRival().getNombreEstrategia().equals("Gaucho Tahur (Mentiroso)"),
                "Final: rival tahur con perfil propio");
        torneo.registrarResultado(true);
        afirmar(torneo.isCampeon() && torneo.isTerminado(), "Ganar la final: campeon");

        juego.reglas.Torneo otro = juego.reglas.Torneo.desdeLlave(persistencia.RepositorioLlave.crearLlavePorDefecto(), azar);
        String eliminado = otro.registrarResultado(false);
        afirmar(otro.isEliminado() && otro.getPartidoDelJugador() == null && eliminado.contains("eliminado"),
                "Perder en cuartos: quedas eliminado");
    }

    public static void main(String[] args) {
        ejecutarTodas();
    }
}
