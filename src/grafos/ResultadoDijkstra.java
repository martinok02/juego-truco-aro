package grafos;

import estructuras.Lista;
import estructuras.MiLista;

public class ResultadoDijkstra<T> {
    private final int distanciaTotal;
    private final Lista<T> camino;
    private final boolean existeCamino;

    public ResultadoDijkstra(int distanciaTotal, Lista<T> camino, boolean existeCamino) {
        this.distanciaTotal = distanciaTotal;
        this.camino = camino != null ? camino : new MiLista<T>();
        this.existeCamino = existeCamino;
    }

    public int getDistanciaTotal() {
        return distanciaTotal;
    }

    public Lista<T> getCamino() {
        return camino;
    }

    public boolean existeCamino() {
        return existeCamino;
    }
}
