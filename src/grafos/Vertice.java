package grafos;

import estructuras.Lista;
import estructuras.MiLista;

public class Vertice<T> {
    private final T dato;
    private final Lista<Arista<T>> adyacentes;

    public Vertice(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("El dato del vertice no puede ser nulo.");
        }
        this.dato = dato;
        this.adyacentes = new MiLista<>();
    }

    public T getDato() {
        return dato;
    }

    public Lista<Arista<T>> getAdyacentes() {
        return adyacentes;
    }

    public void agregarArista(Vertice<T> destino, int peso) {
        // Si ya existe la arista, actualizamos si el nuevo peso es menor
        for (int i = 0; i < adyacentes.tamanio(); i++) {
            Arista<T> actual = adyacentes.obtener(i);
            if (actual.getDestino().equals(destino)) {
                if (peso < actual.getPeso()) {
                    adyacentes.eliminar(i);
                    adyacentes.agregar(new Arista<>(destino, peso));
                }
                return;
            }
        }
        adyacentes.agregar(new Arista<>(destino, peso));
    }
}
