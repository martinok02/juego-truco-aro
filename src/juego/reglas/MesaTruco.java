package juego.reglas;

import estructuras.Cola;
import estructuras.Lista;
import estructuras.MiCola;
import estructuras.MiLista;
import estructuras.MiPila;
import estructuras.Pila;

/**
 * Motor de reglas del truco argentino mano a mano (reglamento de Wikipedia "Truco argentino").
 *
 * Lleva el partido completo: tanteador, quien es mano en cada ronda, las tres bazas con
 * sus pardas, el envido con su escalera, el truco con retruco y vale cuatro, y la flor
 * (si se eligio jugar con flor). No dibuja nada: la pantalla le pregunta que se puede hacer
 * (accionesDisponibles) y le avisa que hizo cada lado (ejecutar / jugarCarta).
 *
 * Estructuras propias que usa y por que:
 * - Pila de cantos del envido: el ULTIMO canto (el tope) decide que se puede subir despues
 *   y cuanto vale el "no quiero". Es exactamente un acceso LIFO.
 * - Cola de eventos: lo que pasa en la mesa (cantos, bazas, puntos) se encola y la pantalla
 *   lo muestra en el mismo orden en que ocurrio (FIFO), aunque pasen varias cosas juntas.
 * - Lista: las cartas de cada mano.
 *
 * Reglas implementadas (resumen):
 * - Mano: alterna en cada ronda; la mano tira primero y gana los empates de envido.
 * - Bazas y pardas: gana quien gana dos bazas. Si hay parda, define la otra baza; si se
 *   empardan dos, define la tercera; si se empardan las tres, gana la mano. Si ganan una
 *   cada uno y la tercera es parda, gana quien gano la primera. Tras una parda tira la mano.
 * - Envido: solo en la primera baza, antes de tirar la propia carta (o como "el envido esta
 *   primero" al responder un truco). Escalera: envido (una vez se puede repetir), real envido,
 *   falta envido. Querido: suma lo cantado (la falta vale "lo que falta"); no querido: suma
 *   todo menos el ultimo canto (o 1 si fue un solo canto). Una vez querido el truco no hay envido.
 * - Truco: truco (2, no querido 1), retruco (3 / 2), vale cuatro (4 / 3). Solo puede subir
 *   quien "tiene el quiero" (el ultimo que acepto). Al irse al mazo el otro cobra lo vigente.
 * - Falta envido: a 15, lo que le falta al que va ganando. A 30: si los dos estan en las malas
 *   gana el partido; si no, lo que le falta al que va ganando.
 * - Flor (opcional): 3 cartas del mismo palo. Se canta sola al repartir y anula el envido de
 *   esa ronda. Una sola flor: 3 puntos. Flor contra flor: 4 puntos a la mejor (empate: mano).
 */
public class MesaTruco {

    public static final int PUNTOS_FLOR = 3;
    public static final int PUNTOS_FLOR_CONTRA_FLOR = 4;

    private final ConfiguracionPartido configuracion;
    private final Mazo mazo;

    // Tanteador del partido
    private int puntosJugador = 0;
    private int puntosRival = 0;
    private Lado ganadorPartido = null;

    // Ronda (una "mano" de tres bazas)
    private int numeroRonda = 0;
    private Lado manoDeLaRonda;
    private Lado turno;
    private Lista<Carta> cartasJugador;
    private Lista<Carta> cartasRival;
    private Lista<Carta> repartoJugador; // las 3 cartas originales (para envido y flor)
    private Lista<Carta> repartoRival;
    private Carta mesaJugador;
    private Carta mesaRival;
    private Lado salioEnBaza;            // quien tiro primero en la baza en juego
    private Lista<BazaJugada> bazasRonda = new MiLista<>();     // bazas ya resueltas de esta ronda
    private Lista<BazaJugada> bazasRondaAnterior = new MiLista<>(); // para mostrar como termino la anterior
    private int jugadasJugador;
    private int jugadasRival;
    private int bazaActual;
    private int bazasJugadas;
    private Lado resultadoBaza1;         // null = parda
    private Lado resultadoBaza2;
    private Lado resultadoBaza3;

    // Envido
    private final Pila<Canto> cantosEnvido = new MiPila<>();
    private boolean envidoCerrado;       // ya se jugo (o no se puede jugar) en esta ronda
    private boolean envidoPendiente;     // alguien canto y el otro tiene que responder
    private Lado cantorEnvido;
    private int acumuladoEnvido;         // puntos si se quiere (sin contar la falta)
    private int noQuieroEnvido;          // puntos si no se quiere
    private int envidosSimples;          // para permitir "envido envido" una sola vez

    // Truco
    private Canto trucoVigente;          // nivel querido (null = la ronda vale 1)
    private Canto trucoPropuesto;        // canto esperando respuesta
    private Lado cantorTruco;
    private Lado duenioDelQuiero;        // el unico que puede subir el truco

    // Como termino la ultima ronda (la pantalla lo muestra un momento antes de levantar la mesa)
    private Lado ganadorUltimaRonda;
    private int puntosUltimaRonda;

    private final Cola<String> eventos = new MiCola<>();

    public MesaTruco(ConfiguracionPartido configuracion, Lado primeraMano) {
        this(configuracion, primeraMano, new Mazo());
    }

    /**
     * Sobrecarga que recibe el mazo desde afuera. El juego usa el mazo comun (mezclado al azar);
     * las pruebas le pasan un mazo "preparado" (una subclase de Mazo) para repartir cartas
     * conocidas y verificar cada regla.
     */
    public MesaTruco(ConfiguracionPartido configuracion, Lado primeraMano, Mazo mazo) {
        if (mazo == null) {
            throw new ReglaInvalidaException("El partido necesita un mazo.");
        }
        this.mazo = mazo;
        if (configuracion == null) {
            throw new ReglaInvalidaException("El partido necesita una configuracion.");
        }
        if (primeraMano == null) {
            throw new ReglaInvalidaException("Hay que indicar quien es mano en la primera ronda.");
        }
        this.configuracion = configuracion;
        this.manoDeLaRonda = primeraMano;
        iniciarRonda();
    }

    // ------------------------------------------------------------------ rondas

    private void iniciarRonda() {
        numeroRonda++;
        mazo.mezclar();
        cartasJugador = mazo.repartirMano();
        cartasRival = mazo.repartirMano();
        repartoJugador = copiar(cartasJugador);
        repartoRival = copiar(cartasRival);

        bazasRondaAnterior = bazasRonda;
        bazasRonda = new MiLista<>();
        salioEnBaza = null;
        mesaJugador = null;
        mesaRival = null;
        jugadasJugador = 0;
        jugadasRival = 0;
        bazaActual = 1;
        bazasJugadas = 0;
        resultadoBaza1 = null;
        resultadoBaza2 = null;
        resultadoBaza3 = null;
        turno = manoDeLaRonda;

        cantosEnvido.vaciar();
        envidoCerrado = false;
        envidoPendiente = false;
        cantorEnvido = null;
        acumuladoEnvido = 0;
        noQuieroEnvido = 0;
        envidosSimples = 0;

        trucoVigente = null;
        trucoPropuesto = null;
        cantorTruco = null;
        duenioDelQuiero = null;

        evento("Ronda " + numeroRonda + ": " + (manoDeLaRonda == Lado.JUGADOR ? "sos mano, tiras primero." : "el rival es mano."));
        if (configuracion.isConFlor()) {
            resolverFlor();
        }
    }

    private Lista<Carta> copiar(Lista<Carta> origen) {
        Lista<Carta> copia = new MiLista<>();
        for (int i = 0; i < origen.tamanio(); i++) {
            copia.agregar(origen.obtener(i));
        }
        return copia;
    }

    /** La flor se canta sola al repartir: suma sus puntos y anula el envido de la ronda. */
    private void resolverFlor() {
        boolean florJugador = CalculadorEnvido.tieneFlor(repartoJugador);
        boolean florRival = CalculadorEnvido.tieneFlor(repartoRival);
        if (!florJugador && !florRival) {
            return;
        }
        envidoCerrado = true;
        if (florJugador && florRival) {
            int fj = CalculadorEnvido.calcularFlor(repartoJugador);
            int fr = CalculadorEnvido.calcularFlor(repartoRival);
            Lado ganador = fj > fr ? Lado.JUGADOR : (fr > fj ? Lado.RIVAL : manoDeLaRonda);
            evento("¡Flor contra flor! Vos " + fj + " - rival " + fr + ". " + sujeto(ganador, "sumás ", "suma ")
                    + PUNTOS_FLOR_CONTRA_FLOR + ".");
            sumar(ganador, PUNTOS_FLOR_CONTRA_FLOR);
        } else {
            Lado conFlor = florJugador ? Lado.JUGADOR : Lado.RIVAL;
            evento("¡Flor! " + sujeto(conFlor, "tenés las tres del mismo palo y sumás ", "tiene las tres del mismo palo y suma ")
                    + PUNTOS_FLOR + ".");
            sumar(conFlor, PUNTOS_FLOR);
        }
    }

    /** Termina la ronda dando los puntos al ganador y, si el partido sigue, reparte otra. */
    private void terminarRonda(Lado ganador, int puntos) {
        ganadorUltimaRonda = ganador;
        puntosUltimaRonda = puntos;
        evento(sujeto(ganador, "ganás la ronda y sumás ", "gana la ronda y suma ") + puntos + (puntos == 1 ? " punto." : " puntos."));
        sumar(ganador, puntos);
        if (!isTerminado()) {
            manoDeLaRonda = manoDeLaRonda.otro();
            iniciarRonda();
        }
    }

    private void sumar(Lado lado, int puntos) {
        if (puntos <= 0 || isTerminado()) {
            return;
        }
        if (lado == Lado.JUGADOR) {
            puntosJugador += puntos;
        } else {
            puntosRival += puntos;
        }
        if (getPuntos(lado) >= configuracion.getPuntosObjetivo()) {
            ganadorPartido = lado;
            evento(sujeto(lado, "llegás a ", "llega a ") + configuracion.getPuntosObjetivo()
                    + (lado == Lado.JUGADOR ? " y ganás el partido." : " y gana el partido."));
        }
    }

    // ------------------------------------------------------------------ que se puede hacer

    /** Quien tiene que actuar ahora: el que debe responder un canto o, si no hay, el del turno. */
    public Lado quienActua() {
        if (isTerminado()) return null;
        if (envidoPendiente) return cantorEnvido.otro();
        if (trucoPropuesto != null) return cantorTruco.otro();
        return turno;
    }

    /** Lo que el lado puede cantar o responder ahora (vacia si no le toca). */
    public Lista<Accion> accionesDisponibles(Lado lado) {
        Lista<Accion> acciones = new MiLista<>();
        if (lado == null || lado != quienActua()) {
            return acciones;
        }
        if (envidoPendiente) {
            acciones.agregar(Accion.QUIERO);
            acciones.agregar(Accion.NO_QUIERO);
            agregarSubidasEnvido(acciones);
            return acciones;
        }
        if (trucoPropuesto != null) {
            acciones.agregar(Accion.QUIERO);
            acciones.agregar(Accion.NO_QUIERO);
            Canto subida = trucoPropuesto.siguienteNivel();
            if (subida != null) {
                acciones.agregar(accionDeTruco(subida));
            }
            if (puedeCantarEnvido(lado, true)) { // "el envido esta primero"
                agregarSubidasEnvido(acciones);
            }
            return acciones;
        }
        if (puedeCantarEnvido(lado, false)) {
            agregarSubidasEnvido(acciones);
        }
        Canto truco = proximoTruco(lado);
        if (truco != null) {
            acciones.agregar(accionDeTruco(truco));
        }
        acciones.agregar(Accion.MAZO);
        return acciones;
    }

    public boolean puedeHacer(Lado lado, Accion accion) {
        return accionesDisponibles(lado).indiceDe(accion) >= 0;
    }

    /** Se puede tirar carta cuando es tu turno y no hay ningun canto esperando respuesta. */
    public boolean puedeJugarCarta(Lado lado) {
        return !isTerminado() && !envidoPendiente && trucoPropuesto == null && turno == lado;
    }

    private boolean puedeCantarEnvido(Lado lado, boolean respondiendoTruco) {
        if (envidoCerrado || envidoPendiente || bazaActual != 1 || trucoVigente != null) {
            return false;
        }
        // Normalmente hay que cantarlo antes de tirar la propia carta; respondiendo un truco
        // en la primera baza vale aunque ya se haya tirado ("el envido esta primero").
        return respondiendoTruco || jugadas(lado) == 0;
    }

    /** Segun el ultimo canto (tope de la pila), que escalones de envido quedan. */
    private void agregarSubidasEnvido(Lista<Accion> acciones) {
        int rango = cantosEnvido.estaVacia() ? 0 : cantosEnvido.tope().getRango();
        if (rango == 0 || (rango == 1 && envidosSimples < 2)) acciones.agregar(Accion.ENVIDO);
        if (rango < 2) acciones.agregar(Accion.REAL_ENVIDO);
        if (rango < 3) acciones.agregar(Accion.FALTA_ENVIDO);
    }

    private Canto proximoTruco(Lado lado) {
        if (trucoPropuesto != null || envidoPendiente) return null;
        if (trucoVigente == null) return new Truco();
        if (duenioDelQuiero != lado) return null;
        return trucoVigente.siguienteNivel();
    }

    private Accion accionDeTruco(Canto canto) {
        switch (canto.getRango()) {
            case 1: return Accion.TRUCO;
            case 2: return Accion.RETRUCO;
            default: return Accion.VALE_CUATRO;
        }
    }

    // ------------------------------------------------------------------ acciones

    /** Ejecuta un canto o respuesta. Devuelve false (y no cambia nada) si no estaba permitido. */
    public boolean ejecutar(Lado lado, Accion accion) {
        if (accion == null || !puedeHacer(lado, accion)) {
            return false;
        }
        if (accion.esEnvido()) {
            cantarEnvido(lado, accion);
        } else if (accion.esTruco()) {
            cantarTruco(lado);
        } else if (accion == Accion.QUIERO) {
            querer(lado);
        } else if (accion == Accion.NO_QUIERO) {
            noQuerer(lado);
        } else {
            irseAlMazo(lado);
        }
        return true;
    }

    private void cantarEnvido(Lado lado, Accion accion) {
        Canto canto;
        if (accion == Accion.ENVIDO) {
            canto = new Envido();
            envidosSimples++;
        } else if (accion == Accion.REAL_ENVIDO) {
            canto = new RealEnvido();
        } else {
            canto = new FaltaEnvido();
        }
        // Si no lo quieren, se cobra todo lo cantado antes de este canto (o 1 si es el primero)
        noQuieroEnvido = cantosEnvido.estaVacia() ? 1 : acumuladoEnvido;
        if (!canto.reemplazaAcumulado()) {
            acumuladoEnvido += canto.puntosAceptado();
        }
        cantosEnvido.apilar(canto);
        cantorEnvido = lado;
        envidoPendiente = true;
        evento(sujeto(lado, "cantás", "canta") + " ¡" + canto.getNombre().toUpperCase() + "!");
    }

    private void cantarTruco(Lado lado) {
        if (trucoPropuesto != null) {
            // Responder subiendo ("quiero retruco") acepta el nivel anterior
            trucoVigente = trucoPropuesto;
            duenioDelQuiero = lado;
            trucoPropuesto = trucoVigente.siguienteNivel();
        } else {
            trucoPropuesto = (trucoVigente == null) ? new Truco() : trucoVigente.siguienteNivel();
        }
        cantorTruco = lado;
        evento(sujeto(lado, "cantás", "canta") + " ¡" + trucoPropuesto.getNombre().toUpperCase() + "!");
    }

    private void querer(Lado lado) {
        if (envidoPendiente) {
            resolverEnvido(true);
            return;
        }
        trucoVigente = trucoPropuesto;
        trucoPropuesto = null;
        duenioDelQuiero = lado;
        evento(sujeto(lado, "decís", "dice") + " QUIERO: la ronda vale " + trucoVigente.puntosAceptado() + ".");
    }

    private void noQuerer(Lado lado) {
        if (envidoPendiente) {
            resolverEnvido(false);
            return;
        }
        Canto rechazado = trucoPropuesto;
        trucoPropuesto = null;
        evento(sujeto(lado, "no querés", "no quiere") + " el " + rechazado.getNombre() + ".");
        terminarRonda(cantorTruco, rechazado.puntosRechazado());
    }

    private void irseAlMazo(Lado lado) {
        int puntos = (trucoVigente != null) ? trucoVigente.puntosAceptado() : 1;
        evento(sujeto(lado, "te vas al mazo.", "se va al mazo."));
        terminarRonda(lado.otro(), puntos);
    }

    private void resolverEnvido(boolean quiero) {
        envidoPendiente = false;
        envidoCerrado = true;
        if (!quiero) {
            evento(sujeto(cantorEnvido.otro(), "no querés", "no quiere") + ". " + sujeto(cantorEnvido, "sumás ", "suma ")
                    + noQuieroEnvido + ".");
            sumar(cantorEnvido, noQuieroEnvido);
            return;
        }
        int pj = puntosEnvido(Lado.JUGADOR);
        int pr = puntosEnvido(Lado.RIVAL);
        Lado ganador = pj > pr ? Lado.JUGADOR : (pr > pj ? Lado.RIVAL : manoDeLaRonda); // empate: gana la mano
        int puntos = cantosEnvido.tope().reemplazaAcumulado() ? puntosFaltaEnvido(ganador) : acumuladoEnvido;
        evento("Envido querido: vos " + pj + ", rival " + pr + (pj == pr ? " (gana la mano)" : "") + ". "
                + sujeto(ganador, "sumás ", "suma ") + puntos + ".");
        sumar(ganador, puntos);
    }

    /** Cuanto vale la falta envido para quien la gano, segun el tanteador. */
    public int puntosFaltaEnvido(Lado ganador) {
        int objetivo = configuracion.getPuntosObjetivo();
        boolean ambosEnMalas = puntosJugador < objetivo / 2 && puntosRival < objetivo / 2;
        if (configuracion.seJuegaConMalasYBuenas() && ambosEnMalas) {
            return objetivo - getPuntos(ganador); // en las malas, la falta gana el partido
        }
        return objetivo - Math.max(puntosJugador, puntosRival); // lo que le falta al que va ganando
    }

    /** Tira la carta "indice" de la mano del lado. Devuelve false si no podia jugar. */
    public boolean jugarCarta(Lado lado, int indice) {
        if (!puedeJugarCarta(lado)) {
            return false;
        }
        Lista<Carta> mano = getCartas(lado);
        if (indice < 0 || indice >= mano.tamanio()) {
            return false;
        }
        Carta carta = mano.eliminar(indice);
        if (getCartaEnMesa(lado.otro()) == null) {
            salioEnBaza = lado; // es la primera carta de la baza
        }
        if (lado == Lado.JUGADOR) {
            mesaJugador = carta;
            jugadasJugador++;
        } else {
            mesaRival = carta;
            jugadasRival++;
        }
        if (getCartaEnMesa(lado.otro()) == null) {
            turno = lado.otro();
            evento(sujeto(lado, "tirás el ", "tira el ") + carta + ".");
            return true;
        }
        resolverBaza();
        return true;
    }

    private void resolverBaza() {
        Lado ganador = null;
        if (mesaJugador.getPoder() > mesaRival.getPoder()) ganador = Lado.JUGADOR;
        if (mesaRival.getPoder() > mesaJugador.getPoder()) ganador = Lado.RIVAL;

        bazasJugadas++;
        if (bazasJugadas == 1) resultadoBaza1 = ganador;
        if (bazasJugadas == 2) resultadoBaza2 = ganador;
        if (bazasJugadas == 3) resultadoBaza3 = ganador;

        if (ganador == null) {
            evento("¡Parda! " + mesaJugador + " empata con " + mesaRival + ".");
        } else {
            Carta alta = ganador == Lado.JUGADOR ? mesaJugador : mesaRival;
            Carta baja = ganador == Lado.JUGADOR ? mesaRival : mesaJugador;
            evento(sujeto(ganador, "ganás la baza: ", "gana la baza: ") + alta + " mata al " + baja + ".");
        }
        bazasRonda.agregar(new BazaJugada(mesaJugador, mesaRival, salioEnBaza, ganador));
        mesaJugador = null;
        mesaRival = null;

        Lado ganadorRonda = ganadorDeLaRonda();
        if (ganadorRonda != null) {
            terminarRonda(ganadorRonda, trucoVigente != null ? trucoVigente.puntosAceptado() : 1);
            return;
        }
        bazaActual++;
        turno = (ganador != null) ? ganador : manoDeLaRonda; // tras una parda tira la mano
    }

    /** Aplica la tabla de bazas y pardas. Devuelve null si la ronda todavia no esta definida. */
    private Lado ganadorDeLaRonda() {
        int ganadasJugador = bazasGanadas(Lado.JUGADOR);
        int ganadasRival = bazasGanadas(Lado.RIVAL);
        if (ganadasJugador >= 2) return Lado.JUGADOR;
        if (ganadasRival >= 2) return Lado.RIVAL;
        if (bazasJugadas >= 2) {
            if (resultadoBaza1 == null && resultadoBaza2 != null) return resultadoBaza2; // parda y define la segunda
            if (resultadoBaza1 != null && resultadoBaza2 == null) return resultadoBaza1; // primera y parda
        }
        if (bazasJugadas == 3) {
            if (resultadoBaza3 != null) return resultadoBaza3;                // parda, parda y define la tercera
            return resultadoBaza1 != null ? resultadoBaza1 : manoDeLaRonda;  // una cada uno y parda: la primera; tres pardas: la mano
        }
        return null;
    }

    // ------------------------------------------------------------------ consultas

    public int bazasGanadas(Lado lado) {
        int ganadas = 0;
        if (bazasJugadas >= 1 && resultadoBaza1 == lado) ganadas++;
        if (bazasJugadas >= 2 && resultadoBaza2 == lado) ganadas++;
        if (bazasJugadas >= 3 && resultadoBaza3 == lado) ganadas++;
        return ganadas;
    }

    /** Puntos de envido con las tres cartas repartidas (aunque ya se haya tirado alguna). */
    public int puntosEnvido(Lado lado) {
        return CalculadorEnvido.calcular(lado == Lado.JUGADOR ? repartoJugador : repartoRival);
    }

    /** Lo que se lleva quien gane el envido pendiente si se quiere (sin contar la falta). */
    public int getPuntosEnvidoEnJuego() {
        return acumuladoEnvido;
    }

    public Canto getUltimoCantoEnvido() {
        return cantosEnvido.estaVacia() ? null : cantosEnvido.tope();
    }

    private int jugadas(Lado lado) {
        return lado == Lado.JUGADOR ? jugadasJugador : jugadasRival;
    }

    public Lista<Carta> getCartas(Lado lado) {
        return lado == Lado.JUGADOR ? cartasJugador : cartasRival;
    }

    public Carta getCartaEnMesa(Lado lado) {
        return lado == Lado.JUGADOR ? mesaJugador : mesaRival;
    }

    /** Quien tiro primero en la baza en juego (null si todavia no tiro nadie). */
    public Lado getSalioEnBaza() {
        return salioEnBaza;
    }

    /** Bazas ya resueltas de la ronda actual, en orden (la pantalla las deja en la mesa). */
    public Lista<BazaJugada> getBazasRonda() {
        return bazasRonda;
    }

    /** Bazas de la ronda anterior (para mostrar como termino hasta que se tire la primera carta). */
    public Lista<BazaJugada> getBazasRondaAnterior() {
        return bazasRondaAnterior;
    }

    public int getPuntos(Lado lado) {
        return lado == Lado.JUGADOR ? puntosJugador : puntosRival;
    }

    public ConfiguracionPartido getConfiguracion() {
        return configuracion;
    }

    public int getBazaActual() {
        return bazaActual;
    }

    public int getNumeroRonda() {
        return numeroRonda;
    }

    public Lado getManoDeLaRonda() {
        return manoDeLaRonda;
    }

    public Lado getTurno() {
        return turno;
    }

    public Canto getTrucoVigente() {
        return trucoVigente;
    }

    public Canto getTrucoPropuesto() {
        return trucoPropuesto;
    }

    public boolean isEnvidoPendiente() {
        return envidoPendiente;
    }

    public boolean isTerminado() {
        return ganadorPartido != null;
    }

    public Lado getGanadorPartido() {
        return ganadorPartido;
    }

    /** Cuantos puntos vale la ronda si se termina ahora por bazas. */
    public int getValorRonda() {
        return trucoVigente != null ? trucoVigente.puntosAceptado() : 1;
    }

    // Eventos de la mesa, en el orden en que pasaron (Cola: el primero en entrar es el primero en mostrarse)
    private void evento(String texto) {
        eventos.encolar(texto);
    }

    public boolean hayEventos() {
        return !eventos.estaVacia();
    }

    public String siguienteEvento() {
        return eventos.desencolar();
    }

    /** Mira el proximo evento sin sacarlo de la cola (null si no hay). */
    public String verSiguienteEvento() {
        return eventos.estaVacia() ? null : eventos.frente();
    }

    /** Quien gano la ultima ronda terminada (null si todavia no termino ninguna). */
    public Lado getGanadorUltimaRonda() {
        return ganadorUltimaRonda;
    }

    /** Puntos que se llevo el ganador de la ultima ronda terminada. */
    public int getPuntosUltimaRonda() {
        return puntosUltimaRonda;
    }

    /** Arma el sujeto con el verbo conjugado: "Vos ganás" / "El rival gana". */
    private String sujeto(Lado lado, String formaVos, String formaRival) {
        return lado == Lado.JUGADOR ? "Vos " + formaVos : "El rival " + formaRival;
    }
}
