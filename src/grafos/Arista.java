package grafos;

public class Arista<T> {
    private final Vertice<T> destino;
    private final int peso;

    public Arista(Vertice<T> destino, int peso) {
        if (destino == null) {
            throw new IllegalArgumentException("El destino de la arista no puede ser nulo.");
        }
        if (peso < 0) {
            throw new IllegalArgumentException("El peso de la arista debe ser mayor o igual a cero.");
        }
        this.destino = destino;
        this.peso = peso;
    }

    public Vertice<T> getDestino() {
        return destino;
    }

    public int getPeso() {
        return peso;
    }
}
