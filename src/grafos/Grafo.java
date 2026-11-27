package grafos;

import estructuras.Cola;
import estructuras.Lista;
import estructuras.ListaPrioridad;
import estructuras.MiCola;
import estructuras.MiLista;
import estructuras.MiPila;
import estructuras.Pila;

/**
 * TAD Grafo Dirigido Ponderado.
 * Implementado con lista de adyacencia sobre MiLista.
 * Cumple con los requerimientos de la catedra:
 * - Recorrido BFS utilizando Cola propia (MiCola).
 * - Recorrido DFS recursivo.
 * - Algoritmo de Dijkstra para camino minimo con reconstruccion de ruta.
 * - Deteccion de ciclos dirigidos.
 * - Sin uso de librerias java.util.
 */
public class Grafo<T> {

    private final Lista<Vertice<T>> vertices;

    public Grafo() {
        this.vertices = new MiLista<>();
    }

    public int cantidadVertices() {
        return vertices.tamanio();
    }

    public Lista<T> obtenerVertices() {
        Lista<T> lista = new MiLista<>();
        for (int i = 0; i < vertices.tamanio(); i++) {
            lista.agregar(vertices.obtener(i).getDato());
        }
        return lista;
    }

    public Vertice<T> buscarVertice(T dato) {
        if (dato == null) {
            return null;
        }
        for (int i = 0; i < vertices.tamanio(); i++) {
            Vertice<T> v = vertices.obtener(i);
            if (v.getDato().equals(dato)) {
                return v;
            }
        }
        return null;
    }

    private int indiceVertice(T dato) {
        if (dato == null) {
            return -1;
        }
        for (int i = 0; i < vertices.tamanio(); i++) {
            if (vertices.obtener(i).getDato().equals(dato)) {
                return i;
            }
        }
        return -1;
    }

    public void agregarVertice(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("No se puede agregar un vertice nulo.");
        }
        if (buscarVertice(dato) == null) {
            vertices.agregar(new Vertice<>(dato));
        }
    }

    public void agregarArista(T origen, T destino, int peso) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Origen y destino no pueden ser nulos.");
        }
        Vertice<T> vOrigen = buscarVertice(origen);
        if (vOrigen == null) {
            vOrigen = new Vertice<>(origen);
            vertices.agregar(vOrigen);
        }
        Vertice<T> vDestino = buscarVertice(destino);
        if (vDestino == null) {
            vDestino = new Vertice<>(destino);
            vertices.agregar(vDestino);
        }
        vOrigen.agregarArista(vDestino, peso);
    }

    public int cantidadAristas() {
        int total = 0;
        for (int i = 0; i < vertices.tamanio(); i++) {
            total += vertices.obtener(i).getAdyacentes().tamanio();
        }
        return total;
    }

    // ==========================================
    // RECORRIDO BFS (Anchura con Cola propia)
    // ==========================================

    /**
     * Recorrido en anchura desde "inicio". Las marcas de "ya descubierto" se guardan en una
     * Lista propia (sin arreglos). Costo: O(V + E) visitas, y cada consulta de marca es O(V)
     * sobre la lista, asi que en total O(V * (V + E)); con los pocos clubes del mapa es nada.
     */
    public Lista<T> bfs(T inicio) {
        Lista<T> visitados = new MiLista<>();
        Vertice<T> vInicio = buscarVertice(inicio);
        if (vInicio == null) {
            return visitados;
        }
        Lista<T> descubiertos = new MiLista<>();
        Cola<Vertice<T>> cola = new MiCola<>();
        descubiertos.agregar(inicio);
        cola.encolar(vInicio);

        while (!cola.estaVacia()) {
            Vertice<T> actual = cola.desencolar();
            visitados.agregar(actual.getDato());
            Lista<Arista<T>> adyacentes = actual.getAdyacentes();
            for (int i = 0; i < adyacentes.tamanio(); i++) {
                Vertice<T> vecino = adyacentes.obtener(i).getDestino();
                if (descubiertos.indiceDe(vecino.getDato()) < 0) {
                    descubiertos.agregar(vecino.getDato());
                    cola.encolar(vecino);
                }
            }
        }
        return visitados;
    }

    /**
     * Cantidad minima de tramos (aristas) para ir de origen a destino, con BFS por niveles:
     * junto a cada vertice se encola su nivel. Devuelve 0 si son el mismo y -1 si no hay camino.
     */
    public int tramosHasta(T origen, T destino) {
        Vertice<T> vOrigen = buscarVertice(origen);
        if (vOrigen == null || buscarVertice(destino) == null) {
            return -1;
        }
        Lista<T> descubiertos = new MiLista<>();
        Cola<Vertice<T>> cola = new MiCola<>();
        Cola<Integer> niveles = new MiCola<>();
        descubiertos.agregar(origen);
        cola.encolar(vOrigen);
        niveles.encolar(0);

        while (!cola.estaVacia()) {
            Vertice<T> actual = cola.desencolar();
            int nivel = niveles.desencolar();
            if (actual.getDato().equals(destino)) {
                return nivel;
            }
            Lista<Arista<T>> adyacentes = actual.getAdyacentes();
            for (int i = 0; i < adyacentes.tamanio(); i++) {
                Vertice<T> vecino = adyacentes.obtener(i).getDestino();
                if (descubiertos.indiceDe(vecino.getDato()) < 0) {
                    descubiertos.agregar(vecino.getDato());
                    cola.encolar(vecino);
                    niveles.encolar(nivel + 1);
                }
            }
        }
        return -1;
    }

    // ==========================================
    // RECORRIDO DFS (Profundidad recursivo)
    // ==========================================

    public Lista<T> dfs(T inicio) {
        Lista<T> visitados = new MiLista<>();
        Vertice<T> vInicio = buscarVertice(inicio);
        if (vInicio != null) {
            dfsRecursivo(vInicio, visitados);
        }
        return visitados;
    }

    /** Caso base: si el vertice ya fue visitado, no se hace nada (corta los ciclos). */
    private void dfsRecursivo(Vertice<T> actual, Lista<T> visitados) {
        if (visitados.indiceDe(actual.getDato()) >= 0) {
            return;
        }
        visitados.agregar(actual.getDato());
        Lista<Arista<T>> ady = actual.getAdyacentes();
        for (int i = 0; i < ady.tamanio(); i++) {
            dfsRecursivo(ady.obtener(i).getDestino(), visitados);
        }
    }

    /** true si se puede llegar de origen a destino siguiendo las aristas (DFS). */
    public boolean hayCamino(T origen, T destino) {
        return dfs(origen).indiceDe(destino) >= 0;
    }

    /**
     * true si a y b estan en un mismo ciclo dirigido: se puede ir de a hasta b y volver.
     * En el juego decide si un viaje es posible: ir a un club sin vuelta seria quedar varado.
     */
    public boolean formanCiclo(T a, T b) {
        return hayCamino(a, b) && hayCamino(b, a);
    }

    // ==========================================
    // ALGORITMO DE DIJKSTRA (con ListaPrioridad propia)
    // ==========================================

    /** Lo que Dijkstra sabe de cada vertice alcanzado: su mejor distancia y desde donde llego. */
    private static class Registro<T> {
        private final T dato;
        private int distancia;
        private T anterior;

        Registro(T dato, int distancia, T anterior) {
            this.dato = dato;
            this.distancia = distancia;
            this.anterior = anterior;
        }
    }

    /**
     * Camino de menor costo de origen a destino.
     * - La ListaPrioridad entrega siempre el vertice pendiente con menor distancia.
     * - Cuando se mejora la distancia de un vertice se vuelve a encolar; la entrada vieja,
     *   al salir, se descarta porque el vertice ya esta cerrado ("eliminacion perezosa").
     * - La ruta se reconstruye hacia atras con una Pila propia y se da vuelta.
     * Costo: O(E) inserciones de O(V + E) cada una en la lista ordenada, mas O(V) por cada
     * busqueda de registro: O((V + E)^2) en el peor caso, despreciable para el mapa.
     */
    public ResultadoDijkstra<T> dijkstra(T origen, T destino) {
        if (buscarVertice(origen) == null || buscarVertice(destino) == null) {
            return new ResultadoDijkstra<>(-1, null, false);
        }
        Lista<Registro<T>> registros = new MiLista<>();
        Lista<T> cerrados = new MiLista<>();
        ListaPrioridad<T> pendientes = new ListaPrioridad<>();
        registros.agregar(new Registro<>(origen, 0, null));
        pendientes.encolar(origen, 0);

        while (!pendientes.estaVacia()) {
            ListaPrioridad.Entrada<T> entrada = pendientes.desencolarMin();
            T u = entrada.getElemento();
            if (cerrados.indiceDe(u) >= 0) {
                continue; // entrada vieja: ya se cerro con una distancia menor
            }
            cerrados.agregar(u);
            if (u.equals(destino)) {
                break;
            }
            Lista<Arista<T>> ady = buscarVertice(u).getAdyacentes();
            for (int i = 0; i < ady.tamanio(); i++) {
                Arista<T> arista = ady.obtener(i);
                T v = arista.getDestino().getDato();
                if (cerrados.indiceDe(v) >= 0) {
                    continue;
                }
                int nuevaDistancia = entrada.getPrioridad() + arista.getPeso();
                Registro<T> registro = buscarRegistro(registros, v);
                if (registro == null) {
                    registros.agregar(new Registro<>(v, nuevaDistancia, u));
                    pendientes.encolar(v, nuevaDistancia);
                } else if (nuevaDistancia < registro.distancia) {
                    registro.distancia = nuevaDistancia;
                    registro.anterior = u;
                    pendientes.encolar(v, nuevaDistancia);
                }
            }
        }

        Registro<T> final_ = buscarRegistro(registros, destino);
        if (final_ == null) {
            return new ResultadoDijkstra<>(-1, null, false);
        }

        // Reconstruccion de la ruta utilizando una Pila propia
        Pila<T> pilaRuta = new MiPila<>();
        T actual = destino;
        while (actual != null) {
            pilaRuta.apilar(actual);
            actual = buscarRegistro(registros, actual).anterior;
        }
        Lista<T> rutaFinal = new MiLista<>();
        while (!pilaRuta.estaVacia()) {
            rutaFinal.agregar(pilaRuta.desapilar());
        }
        return new ResultadoDijkstra<>(final_.distancia, rutaFinal, true);
    }

    private Registro<T> buscarRegistro(Lista<Registro<T>> registros, T dato) {
        for (int i = 0; i < registros.tamanio(); i++) {
            if (registros.obtener(i).dato.equals(dato)) {
                return registros.obtener(i);
            }
        }
        return null;
    }

    // ==========================================
    // DETECCION DE CICLOS DIRIGIDOS
    // ==========================================

    /**
     * true si el grafo tiene algun ciclo dirigido (DFS con colores). "En curso" (gris) son
     * los vertices de la rama que se esta recorriendo; "terminados" (negro), los ya cerrados.
     * Si se llega a un vertice gris, hay una arista hacia atras: ciclo.
     */
    public boolean tieneCiclo() {
        Lista<T> enCurso = new MiLista<>();
        Lista<T> terminados = new MiLista<>();
        for (int i = 0; i < vertices.tamanio(); i++) {
            Vertice<T> v = vertices.obtener(i);
            if (terminados.indiceDe(v.getDato()) < 0 && tieneCicloRecursivo(v, enCurso, terminados)) {
                return true;
            }
        }
        return false;
    }

    private boolean tieneCicloRecursivo(Vertice<T> u, Lista<T> enCurso, Lista<T> terminados) {
        enCurso.agregar(u.getDato());
        Lista<Arista<T>> ady = u.getAdyacentes();
        for (int i = 0; i < ady.tamanio(); i++) {
            Vertice<T> v = ady.obtener(i).getDestino();
            if (enCurso.indiceDe(v.getDato()) >= 0) {
                return true; // arista hacia atras: ciclo
            }
            if (terminados.indiceDe(v.getDato()) < 0 && tieneCicloRecursivo(v, enCurso, terminados)) {
                return true;
            }
        }
        enCurso.eliminar(enCurso.indiceDe(u.getDato()));
        terminados.agregar(u.getDato());
        return false;
    }

    /** Aristas de un vertice (para dibujar el mapa desde el grafo). */
    public Lista<Arista<T>> aristasDe(T dato) {
        Vertice<T> v = buscarVertice(dato);
        return v == null ? new MiLista<Arista<T>>() : v.getAdyacentes();
    }
}
