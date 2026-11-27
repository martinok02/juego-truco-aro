package arboles;

public class NodoABB<T extends Comparable<T>> {
    private T dato;
    private NodoABB<T> izquierdo;
    private NodoABB<T> derecho;

    public NodoABB(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("El dato del nodo ABB no puede ser nulo.");
        }
        this.dato = dato;
        this.izquierdo = null;
        this.derecho = null;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("El dato del nodo ABB no puede ser nulo.");
        }
        this.dato = dato;
    }

    public NodoABB<T> getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoABB<T> izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoABB<T> getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoABB<T> derecho) {
        this.derecho = derecho;
    }

    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }
}
