package arboles;

import estructuras.Cola;
import estructuras.Lista;
import estructuras.MiCola;
import estructuras.MiLista;

/**
 * TAD Arbol Binario generico.
 * Cumple con los requerimientos de la catedra:
 * - Metodos recursivos con caso base explicito: buscar, altura, cantidadNodos, cantidadHojas.
 * - Recorridos pre, in, post y por niveles devolviendo Lista propia (sin imprimir por consola).
 * - Recorrido por niveles e insercion por niveles utilizando Cola propia.
 * - Sin uso de java.util ni arreglos fuera de TADs.
 */
public class ArbolBinario<T> {

    private NodoBinario<T> raiz;

    public ArbolBinario() {
        this.raiz = null;
    }

    public ArbolBinario(NodoBinario<T> raiz) {
        this.raiz = raiz;
    }

    public boolean estaVacio() {
        return this.raiz == null;
    }

    public NodoBinario<T> getRaiz() {
        return raiz;
    }

    public void setRaiz(NodoBinario<T> raiz) {
        this.raiz = raiz;
    }

    public void vaciar() {
        this.raiz = null;
    }

    /**
     * Inserta un elemento en el primer hueco disponible por niveles (arbol binario completo).
     * Utiliza la Cola propia.
     */
    public void insertar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se puede insertar un elemento nulo.");
        }
        NodoBinario<T> nuevo = new NodoBinario<>(elemento);
        if (estaVacio()) {
            this.raiz = nuevo;
            return;
        }

        Cola<NodoBinario<T>> cola = new MiCola<>();
        cola.encolar(this.raiz);

        while (!cola.estaVacia()) {
            NodoBinario<T> actual = cola.desencolar();

            if (actual.getIzquierdo() == null) {
                actual.setIzquierdo(nuevo);
                return;
            } else {
                cola.encolar(actual.getIzquierdo());
            }

            if (actual.getDerecho() == null) {
                actual.setDerecho(nuevo);
                return;
            } else {
                cola.encolar(actual.getDerecho());
            }
        }
    }

    /**
     * Busqueda recursiva de un elemento por equals.
     */
    public boolean buscar(T elemento) {
        if (elemento == null || estaVacio()) {
            return false;
        }
        return buscarRecursivo(this.raiz, elemento);
    }

    private boolean buscarRecursivo(NodoBinario<T> nodo, T elemento) {
        if (nodo == null) {
            return false; // Caso base 1: subarbol vacio
        }
        if (nodo.getDato().equals(elemento)) {
            return true; // Caso base 2: elemento encontrado
        }
        // Paso recursivo: buscar en rama izquierda o rama derecha
        return buscarRecursivo(nodo.getIzquierdo(), elemento) || buscarRecursivo(nodo.getDerecho(), elemento);
    }

    /**
     * Altura del arbol calculada de forma recursiva.
     * Caso base: arbol vacio = 0.
     */
    public int altura() {
        return alturaRecursiva(this.raiz);
    }

    private int alturaRecursiva(NodoBinario<T> nodo) {
        if (nodo == null) {
            return 0; // Caso base
        }
        int altIzq = alturaRecursiva(nodo.getIzquierdo());
        int altDer = alturaRecursiva(nodo.getDerecho());
        return 1 + (altIzq > altDer ? altIzq : altDer);
    }

    /**
     * Cantidad total de nodos calculada recursivamente.
     */
    public int cantidadNodos() {
        return cantidadNodosRecursivo(this.raiz);
    }

    private int cantidadNodosRecursivo(NodoBinario<T> nodo) {
        if (nodo == null) {
            return 0; // Caso base
        }
        return 1 + cantidadNodosRecursivo(nodo.getIzquierdo()) + cantidadNodosRecursivo(nodo.getDerecho());
    }

    /**
     * Cantidad de hojas (nodos sin hijos) calculada recursivamente.
     */
    public int cantidadHojas() {
        return cantidadHojasRecursivo(this.raiz);
    }

    private int cantidadHojasRecursivo(NodoBinario<T> nodo) {
        if (nodo == null) {
            return 0; // Caso base 1: vacio
        }
        if (nodo.esHoja()) {
            return 1; // Caso base 2: es hoja
        }
        return cantidadHojasRecursivo(nodo.getIzquierdo()) + cantidadHojasRecursivo(nodo.getDerecho());
    }

    // ==========================================
    // RECORRIDOS (Devuelven Lista propia)
    // ==========================================

    public Lista<T> preOrden() {
        Lista<T> resultado = new MiLista<>();
        preOrdenRecursivo(this.raiz, resultado);
        return resultado;
    }

    private void preOrdenRecursivo(NodoBinario<T> nodo, Lista<T> lista) {
        if (nodo == null) {
            return; // Caso base
        }
        lista.agregar(nodo.getDato());
        preOrdenRecursivo(nodo.getIzquierdo(), lista);
        preOrdenRecursivo(nodo.getDerecho(), lista);
    }

    public Lista<T> inOrden() {
        Lista<T> resultado = new MiLista<>();
        inOrdenRecursivo(this.raiz, resultado);
        return resultado;
    }

    private void inOrdenRecursivo(NodoBinario<T> nodo, Lista<T> lista) {
        if (nodo == null) {
            return; // Caso base
        }
        inOrdenRecursivo(nodo.getIzquierdo(), lista);
        lista.agregar(nodo.getDato());
        inOrdenRecursivo(nodo.getDerecho(), lista);
    }

    public Lista<T> postOrden() {
        Lista<T> resultado = new MiLista<>();
        postOrdenRecursivo(this.raiz, resultado);
        return resultado;
    }

    private void postOrdenRecursivo(NodoBinario<T> nodo, Lista<T> lista) {
        if (nodo == null) {
            return; // Caso base
        }
        postOrdenRecursivo(nodo.getIzquierdo(), lista);
        postOrdenRecursivo(nodo.getDerecho(), lista);
        lista.agregar(nodo.getDato());
    }

    /**
     * Recorrido por niveles (anchura / BFS en arbol) utilizando MiCola.
     */
    public Lista<T> porNiveles() {
        Lista<T> resultado = new MiLista<>();
        if (estaVacio()) {
            return resultado;
        }

        Cola<NodoBinario<T>> cola = new MiCola<>();
        cola.encolar(this.raiz);

        while (!cola.estaVacia()) {
            NodoBinario<T> actual = cola.desencolar();
            resultado.agregar(actual.getDato());

            if (actual.getIzquierdo() != null) {
                cola.encolar(actual.getIzquierdo());
            }
            if (actual.getDerecho() != null) {
                cola.encolar(actual.getDerecho());
            }
        }

        return resultado;
    }
}
