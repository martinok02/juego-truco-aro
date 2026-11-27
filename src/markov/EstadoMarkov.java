package markov;

import estructuras.Lista;
import estructuras.MiLista;

/**
 * Representa un estado en la cadena de Markov de orden k.
 * La clave es el estado compuesto (los ultimos k tokens concatenados).
 * Mantiene la lista de sucesores con repetidos intencionales para reflejar frecuencias.
 */
public class EstadoMarkov implements Comparable<EstadoMarkov> {

    private final String claveEstado;
    private final Lista<String> sucesores;

    public EstadoMarkov(String claveEstado) {
        if (claveEstado == null) {
            throw new IllegalArgumentException("La clave del estado no puede ser nula.");
        }
        this.claveEstado = claveEstado;
        this.sucesores = new MiLista<>();
    }

    public String getClaveEstado() {
        return claveEstado;
    }

    public Lista<String> getSucesores() {
        return sucesores;
    }

    public void agregarSucesor(String token) {
        if (token != null) {
            sucesores.agregar(token);
        }
    }

    public boolean tieneSucesores() {
        return !sucesores.estaVacia();
    }

    @Override
    public int compareTo(EstadoMarkov otro) {
        if (otro == null) {
            return 1;
        }
        return this.claveEstado.compareTo(otro.claveEstado);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EstadoMarkov that = (EstadoMarkov) obj;
        return claveEstado.equals(that.claveEstado);
    }

    @Override
    public int hashCode() {
        return claveEstado.hashCode();
    }
}
