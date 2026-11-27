package arboles;

import estructuras.Lista;
import estructuras.MiLista;
import estructuras.MiPila;
import estructuras.Pila;

/**
 * Arbol Binario de Busqueda (ABB) generico para tipos que implementan Comparable.
 * Cumple con los requisitos de la catedra:
 * - Insercion ordenada recursiva.
 * - Busqueda eficiente O(h).
 * - Eliminacion completa de nodo (hoja, un hijo, dos hijos con sucesor in-orden).
 * - Recorrido in-orden ITERATIVO utilizando Pila propia (MiPila).
 * - Metodos auxiliares obtenerMin y eliminarMin para poda de ranking.
 */
public class ABB<T extends Comparable<T>> {

    private NodoABB<T> raiz;
    private int cantidadNodos;

    public ABB() {
        this.raiz = null;
        this.cantidadNodos = 0;
    }

    public boolean estaVacio() {
        return this.raiz == null;
    }

    public int tamanio() {
        return this.cantidadNodos;
    }

    public void vaciar() {
        this.raiz = null;
        this.cantidadNodos = 0;
    }

    public NodoABB<T> getRaiz() {
        return raiz;
    }

    /**
     * Insercion en el ABB respetando el ordenamiento.
     */
    public void insertar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se puede insertar un elemento nulo en el ABB.");
        }
        this.raiz = insertarRecursivo(this.raiz, elemento);
    }

    private NodoABB<T> insertarRecursivo(NodoABB<T> actual, T elemento) {
        if (actual == null) {
            cantidadNodos++;
            return new NodoABB<>(elemento);
        }

        int comparacion = elemento.compareTo(actual.getDato());
        if (comparacion < 0) {
            actual.setIzquierdo(insertarRecursivo(actual.getIzquierdo(), elemento));
        } else if (comparacion > 0) {
            actual.setDerecho(insertarRecursivo(actual.getDerecho(), elemento));
        } else {
            // Elemento duplicado: se actualiza el dato existente
            actual.setDato(elemento);
        }
        return actual;
    }

    /**
     * Busqueda binaria en el ABB.
     */
    public boolean contiene(T elemento) {
        return buscar(elemento) != null;
    }

    public T buscar(T elemento) {
        if (elemento == null || estaVacio()) {
            return null;
        }
        return buscarRecursivo(this.raiz, elemento);
    }

    private T buscarRecursivo(NodoABB<T> actual, T elemento) {
        if (actual == null) {
            return null;
        }
        int comparacion = elemento.compareTo(actual.getDato());
        if (comparacion == 0) {
            return actual.getDato();
        } else if (comparacion < 0) {
            return buscarRecursivo(actual.getIzquierdo(), elemento);
        } else {
            return buscarRecursivo(actual.getDerecho(), elemento);
        }
    }

    /**
     * Eliminacion de nodo en el ABB considerando los 3 casos:
     * 1. Nodo hoja
     * 2. Nodo con un solo hijo
     * 3. Nodo con dos hijos (reemplaza por el minimo del subarbol derecho)
     */
    public boolean eliminar(T elemento) {
        if (elemento == null || estaVacio()) {
            return false;
        }
        int cantidadAntes = this.cantidadNodos;
        this.raiz = eliminarRecursivo(this.raiz, elemento);
        return this.cantidadNodos < cantidadAntes;
    }

    private NodoABB<T> eliminarRecursivo(NodoABB<T> actual, T elemento) {
        if (actual == null) {
            return null; // Elemento no encontrado
        }

        int comparacion = elemento.compareTo(actual.getDato());
        if (comparacion < 0) {
            actual.setIzquierdo(eliminarRecursivo(actual.getIzquierdo(), elemento));
        } else if (comparacion > 0) {
            actual.setDerecho(eliminarRecursivo(actual.getDerecho(), elemento));
        } else {
            // Encontrado el nodo a eliminar
            cantidadNodos--;

            // Caso 1 y 2: Sin hijo izquierdo o sin hijo derecho
            if (actual.getIzquierdo() == null) {
                return actual.getDerecho();
            }
            if (actual.getDerecho() == null) {
                return actual.getIzquierdo();
            }

            // Caso 3: Dos hijos.
            // Buscamos el menor del subarbol derecho (sucesor in-orden)
            T menorSubarbolDerecho = encontrarMinimo(actual.getDerecho());
            actual.setDato(menorSubarbolDerecho);
            // Incrementamos temporalmente porque la llamada recursiva lo decrementara
            cantidadNodos++;
            actual.setDerecho(eliminarRecursivo(actual.getDerecho(), menorSubarbolDerecho));
        }
        return actual;
    }

    private T encontrarMinimo(NodoABB<T> actual) {
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual.getDato();
    }

    /**
     * Devuelve el menor elemento del ABB.
     */
    public T obtenerMin() {
        if (estaVacio()) {
            return null;
        }
        return encontrarMinimo(this.raiz);
    }

    /**
     * Elimina el menor elemento del ABB (utilizado para podar el top 10).
     */
    public T eliminarMin() {
        if (estaVacio()) {
            return null;
        }
        T min = obtenerMin();
        eliminar(min);
        return min;
    }

    /**
     * RECORRIDO IN-ORDEN ITERATIVO.
     * Exigido por la catedra: se realiza utilizando una Pila propia (MiPila) sin recursion.
     * Devuelve una Lista propia con los elementos ordenados ascendentemente.
     */
    public Lista<T> inOrdenIterativo() {
        Lista<T> resultado = new MiLista<>();
        if (estaVacio()) {
            return resultado;
        }

        Pila<NodoABB<T>> pila = new MiPila<>();
        NodoABB<T> actual = this.raiz;

        while (actual != null || !pila.estaVacia()) {
            // Descender todo lo posible a la izquierda
            while (actual != null) {
                pila.apilar(actual);
                actual = actual.getIzquierdo();
            }

            // Desapilar y visitar nodo
            actual = pila.desapilar();
            resultado.agregar(actual.getDato());

            // Moverse al subarbol derecho
            actual = actual.getDerecho();
        }

        return resultado;
    }

    /**
     * Recorrido in-orden recursivo.
     */
    public Lista<T> inOrden() {
        Lista<T> resultado = new MiLista<>();
        inOrdenRecursivo(this.raiz, resultado);
        return resultado;
    }

    private void inOrdenRecursivo(NodoABB<T> nodo, Lista<T> lista) {
        if (nodo == null) {
            return;
        }
        inOrdenRecursivo(nodo.getIzquierdo(), lista);
        lista.agregar(nodo.getDato());
        inOrdenRecursivo(nodo.getDerecho(), lista);
    }

    public int altura() {
        return alturaRecursiva(this.raiz);
    }

    private int alturaRecursiva(NodoABB<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        int altIzq = alturaRecursiva(nodo.getIzquierdo());
        int altDer = alturaRecursiva(nodo.getDerecho());
        return 1 + (altIzq > altDer ? altIzq : altDer);
    }
}
