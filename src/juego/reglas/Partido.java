package juego.reglas;

/**
 * Representa un enfrentamiento en la llave del torneo eliminatorio.
 * Se almacena en los nodos del ArbolBinario<Partido>.
 */
public class Partido {

    private final int id;
    private final int ronda;
    private final String sede;
    private String jugadorA;
    private String jugadorB;
    private String ganador;
    private int puntosA;
    private int puntosB;
    private boolean jugado;

    public Partido(int id, int ronda, String sede, String jugadorA, String jugadorB) {
        // La llave tiene 7 partidos en 3 rondas (cuartos = 1, semis = 2, final = 3)
        if (id < 1 || id > 7) {
            throw new ReglaInvalidaException("El id del partido va de 1 a 7: " + id);
        }
        if (ronda < 1 || ronda > 3) {
            throw new ReglaInvalidaException("La ronda va de 1 (cuartos) a 3 (final): " + ronda);
        }
        if (sede == null || sede.trim().isEmpty()) {
            throw new ReglaInvalidaException("El partido necesita una sede.");
        }
        this.id = id;
        this.ronda = ronda;
        this.sede = sede.trim();
        this.jugadorA = normalizarJugador(jugadorA);
        this.jugadorB = normalizarJugador(jugadorB);
        this.ganador = "-";
        this.puntosA = 0;
        this.puntosB = 0;
        this.jugado = false;
    }

    public int getId() {
        return id;
    }

    public int getRonda() {
        return ronda;
    }

    public String getSede() {
        return sede;
    }

    public String getJugadorA() {
        return jugadorA;
    }

    public void setJugadorA(String jugadorA) {
        this.jugadorA = normalizarJugador(jugadorA);
    }

    /** Un lugar vacio de la llave se guarda como "-" (todavia no se sabe quien juega). */
    private static String normalizarJugador(String jugador) {
        return (jugador == null || jugador.trim().isEmpty()) ? "-" : jugador.trim();
    }

    public String getJugadorB() {
        return jugadorB;
    }

    public void setJugadorB(String jugadorB) {
        this.jugadorB = normalizarJugador(jugadorB);
    }

    public String getGanador() {
        return ganador;
    }

    /** El ganador tiene que ser uno de los dos que jugaron el partido. */
    public void setGanador(String ganador) {
        if (ganador == null || (!ganador.equals(jugadorA) && !ganador.equals(jugadorB))) {
            throw new ReglaInvalidaException("El ganador (" + ganador + ") no jugo este partido: "
                    + jugadorA + " vs " + jugadorB);
        }
        this.ganador = ganador;
        this.jugado = true;
    }

    public int getPuntosA() {
        return puntosA;
    }

    public void setPuntosA(int puntosA) {
        if (puntosA < 0) throw new ReglaInvalidaException("Los puntos no pueden ser negativos.");
        this.puntosA = puntosA;
    }

    public int getPuntosB() {
        return puntosB;
    }

    public void setPuntosB(int puntosB) {
        if (puntosB < 0) throw new ReglaInvalidaException("Los puntos no pueden ser negativos.");
        this.puntosB = puntosB;
    }

    public boolean isJugado() {
        return jugado;
    }

    public void setJugado(boolean jugado) {
        this.jugado = jugado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Partido p = (Partido) obj;
        return id == p.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        String estado = jugado ? ("Ganador: " + ganador) : "Pendiente";
        return String.format("[R%d %s] %s vs %s (%s)", ronda, sede, jugadorA, jugadorB, estado);
    }
}
