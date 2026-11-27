package juego.reglas;

/**
 * Registro de puntaje para el Ranking Historico almacenado en el ABB.
 * Compara por puntaje ascendente para que eliminarMin() pode la peor puntuacion del top 10.
 * A igualdad de puntos desempata alfabeticamente por nombre.
 */
public class ClaveRanking implements Comparable<ClaveRanking> {

    private final int puntaje;
    private final String nombre;
    private final int partidas;
    private final int victorias;

    public ClaveRanking(int puntaje, String nombre, int partidas, int victorias) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaInvalidaException("El nombre no puede ser vacio.");
        }
        if (puntaje < 0) {
            throw new ReglaInvalidaException("El puntaje no puede ser negativo: " + puntaje);
        }
        if (partidas < 0 || victorias < 0 || victorias > partidas) {
            // Evita estados imposibles, como mas victorias que partidas jugadas
            throw new ReglaInvalidaException("Partidas y victorias invalidas: " + partidas + " / " + victorias);
        }
        this.puntaje = puntaje;
        this.nombre = nombre.trim();
        this.partidas = partidas;
        this.victorias = victorias;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPartidas() {
        return partidas;
    }

    public int getVictorias() {
        return victorias;
    }

    /**
     * Devuelve un registro NUEVO con un partido mas sumado (la clave es inmutable: como el
     * puntaje es la clave del ABB, para cambiarlo hay que sacar el registro viejo y meter este).
     */
    public ClaveRanking sumarPartido(int puntosDelPartido, boolean gano) {
        return new ClaveRanking(puntaje + Math.max(0, puntosDelPartido), nombre, partidas + 1, victorias + (gano ? 1 : 0));
    }

    @Override
    public int compareTo(ClaveRanking otra) {
        if (otra == null) return 1;
        if (this.puntaje != otra.puntaje) {
            return Integer.compare(this.puntaje, otra.puntaje);
        }
        return this.nombre.compareToIgnoreCase(otra.nombre);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClaveRanking that = (ClaveRanking) obj;
        return puntaje == that.puntaje && nombre.equalsIgnoreCase(that.nombre);
    }

    @Override
    public int hashCode() {
        return 31 * puntaje + nombre.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s - %d pts (%d vict / %d part)", nombre, puntaje, victorias, partidas);
    }
}
