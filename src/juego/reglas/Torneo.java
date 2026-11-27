package juego.reglas;

import arboles.ArbolBinario;
import estructuras.Lista;
import java.util.Random;

/**
 * Torneo eliminatorio de 8 jugadores sobre la llave (ArbolBinario<Partido>, 7 partidos).
 *
 * Recorriendo el arbol por niveles queda: indice 0 = final, 1 y 2 = semifinales,
 * 3 a 6 = cuartos. En ese orden, el partido i "alimenta" a su padre (i - 1) / 2: el ganador
 * de un partido de indice impar pasa como jugador A del padre y el de indice par, como B.
 *
 * El jugador juega su partido; los demas partidos de la misma ronda se resuelven solos
 * (al azar) y los ganadores suben en el arbol. Si el jugador pierde, queda eliminado.
 */
public class Torneo {

    private final ArbolBinario<Partido> llave;
    private final String jugador;
    private final Random azar;

    public Torneo(ArbolBinario<Partido> llave, String jugador, Random azar) {
        if (llave == null || llave.cantidadNodos() != 7) {
            throw new ReglaInvalidaException("La llave del torneo tiene que tener 7 partidos.");
        }
        if (jugador == null || jugador.trim().isEmpty()) {
            throw new ReglaInvalidaException("El torneo necesita el nombre del jugador.");
        }
        this.llave = llave;
        this.jugador = jugador.trim();
        this.azar = (azar != null) ? azar : new Random();
    }

    /** El jugador es siempre el primero del primer partido de cuartos (indice 3 por niveles). */
    public static Torneo desdeLlave(ArbolBinario<Partido> llave, Random azar) {
        if (llave == null || llave.cantidadNodos() != 7) {
            throw new ReglaInvalidaException("La llave del torneo tiene que tener 7 partidos.");
        }
        return new Torneo(llave, llave.porNiveles().obtener(3).getJugadorA(), azar);
    }

    public ArbolBinario<Partido> getLlave() {
        return llave;
    }

    public String getJugador() {
        return jugador;
    }

    private Lista<Partido> partidos() {
        return llave.porNiveles(); // los mismos objetos del arbol: modificarlos modifica la llave
    }

    /** El partido que le toca al jugador (null si quedo eliminado o ya es campeon). */
    public Partido getPartidoDelJugador() {
        Lista<Partido> partidos = partidos();
        for (int i = partidos.tamanio() - 1; i >= 0; i--) {
            Partido p = partidos.obtener(i);
            if (!p.isJugado() && (p.getJugadorA().equals(jugador) || p.getJugadorB().equals(jugador))) {
                return p;
            }
        }
        return null;
    }

    public boolean isEliminado() {
        Lista<Partido> partidos = partidos();
        for (int i = 0; i < partidos.tamanio(); i++) {
            Partido p = partidos.obtener(i);
            boolean jugo = p.getJugadorA().equals(jugador) || p.getJugadorB().equals(jugador);
            if (p.isJugado() && jugo && !p.getGanador().equals(jugador)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCampeon() {
        Partido fin = partidos().obtener(0);
        return fin.isJugado() && fin.getGanador().equals(jugador);
    }

    public boolean isTerminado() {
        return isEliminado() || isCampeon();
    }

    /** El rival del jugador en su partido actual. */
    public String getRivalActual() {
        Partido p = getPartidoDelJugador();
        if (p == null) return "-";
        return p.getJugadorA().equals(jugador) ? p.getJugadorB() : p.getJugadorA();
    }

    /** "Cuartos de final", "Semifinal" o "Final" segun la ronda del partido. */
    public static String nombreRonda(int ronda) {
        switch (ronda) {
            case 1: return "Cuartos de final";
            case 2: return "Semifinal";
            default: return "Final";
        }
    }

    /**
     * Registra el resultado del partido del jugador, resuelve los otros partidos de esa ronda
     * y sube a los ganadores. Devuelve el mensaje para mostrar.
     */
    public String registrarResultado(boolean ganoJugador) {
        Partido propio = getPartidoDelJugador();
        if (propio == null) {
            return "El torneo ya termino para vos.";
        }
        int ronda = propio.getRonda();
        propio.setGanador(ganoJugador ? jugador : (propio.getJugadorA().equals(jugador) ? propio.getJugadorB() : propio.getJugadorA()));

        // Los demas partidos de esta ronda se resuelven solos
        Lista<Partido> partidos = partidos();
        for (int i = 0; i < partidos.tamanio(); i++) {
            Partido p = partidos.obtener(i);
            if (p.getRonda() == ronda && !p.isJugado() && !p.getJugadorA().equals("-") && !p.getJugadorB().equals("-")) {
                p.setGanador(azar.nextBoolean() ? p.getJugadorA() : p.getJugadorB());
            }
        }
        subirGanadores();

        if (!ganoJugador) {
            return "Quedaste eliminado en " + nombreRonda(ronda).toLowerCase() + ".";
        }
        if (isCampeon()) {
            return "¡Sos campeon del torneo!";
        }
        return "¡Pasaste a la " + nombreRonda(ronda + 1).toLowerCase() + "! Te toca contra " + getRivalActual() + ".";
    }

    /** Cada partido jugado lleva a su ganador al partido padre, (i - 1) / 2 en el orden por niveles. */
    private void subirGanadores() {
        Lista<Partido> partidos = partidos();
        for (int i = partidos.tamanio() - 1; i >= 1; i--) {
            Partido hijo = partidos.obtener(i);
            Partido padre = partidos.obtener((i - 1) / 2);
            if (hijo.isJugado() && !padre.isJugado()) {
                if (i % 2 == 1) {
                    padre.setJugadorA(hijo.getGanador());
                } else {
                    padre.setJugadorB(hijo.getGanador());
                }
            }
        }
    }
}
