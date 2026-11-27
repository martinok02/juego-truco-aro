package estructuras;

/**
 * Lista de prioridad propia: una Lista ordenada por prioridad (de menor a mayor).
 * La usa Dijkstra para sacar siempre el vertice con menor distancia acumulada.
 *
 * Costo: encolar O(n) (recorre hasta encontrar su lugar), desencolarMin O(1) (siempre
 * esta adelante). Con los pocos vertices del mapa conviene mas que un monticulo: es
 * simple y no necesita arreglos.
 * A igual prioridad respeta el orden de llegada (el que entro antes sale antes).
 */
public class ListaPrioridad<T> {

    /** Elemento con su prioridad (por ejemplo: un vertice y su distancia). */
    public static class Entrada<T> {
        private final T elemento;
        private final int prioridad;

        public Entrada(T elemento, int prioridad) {
            this.elemento = elemento;
            this.prioridad = prioridad;
        }

        public T getElemento() {
            return elemento;
        }

        public int getPrioridad() {
            return prioridad;
        }
    }

    private final Lista<Entrada<T>> entradas = new MiLista<>();

    public void encolar(T elemento, int prioridad) {
        int posicion = 0;
        while (posicion < entradas.tamanio() && entradas.obtener(posicion).getPrioridad() <= prioridad) {
            posicion++;
        }
        entradas.insertar(posicion, new Entrada<>(elemento, prioridad));
    }

    /** Saca y devuelve la entrada de menor prioridad. */
    public Entrada<T> desencolarMin() {
        if (entradas.estaVacia()) {
            throw new IllegalStateException("No se puede desencolar: la lista de prioridad esta vacia.");
        }
        return entradas.eliminar(0);
    }

    /** Mira la entrada de menor prioridad sin sacarla. */
    public Entrada<T> verMin() {
        if (entradas.estaVacia()) {
            throw new IllegalStateException("No se puede consultar: la lista de prioridad esta vacia.");
        }
        return entradas.obtener(0);
    }

    public boolean estaVacia() {
        return entradas.estaVacia();
    }

    public int tamanio() {
        return entradas.tamanio();
    }
}
