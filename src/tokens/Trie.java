package tokens;

import estructuras.Lista;
import estructuras.MiLista;

/**
 * TAD Trie (Arbol de Prefijos).
 * Cumple con los requerimientos de la catedra:
 * - Insercion O(L).
 * - Busqueda exacta O(L).
 * - Busqueda por prefijo O(L).
 * - Sugerencias por prefijo.
 * - Tokenizacion por coincidencia mas larga (Longest Prefix Match).
 * - Cero uso de colecciones java.util.
 */
public class Trie {

    private final NodoTrie raiz;
    private int cantidadPalabras;

    public Trie() {
        this.raiz = new NodoTrie();
        this.cantidadPalabras = 0;
    }

    public int getCantidadPalabras() {
        return cantidadPalabras;
    }

    public boolean estaVacio() {
        return cantidadPalabras == 0;
    }

    /**
     * Inserta una palabra en el Trie normalizandola a minusculas.
     */
    public void insertar(String palabra) {
        if (palabra == null || palabra.trim().isEmpty()) {
            return;
        }
        String p = palabra.trim().toLowerCase();
        NodoTrie actual = raiz;

        for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            NodoTrie hijo = actual.getHijo(c);
            if (hijo == null) {
                hijo = new NodoTrie();
                actual.setHijo(c, hijo);
            }
            actual = hijo;
        }

        if (!actual.esFinDePalabra()) {
            actual.setFinDePalabra(true);
            actual.setPalabraCompleta(p);
            cantidadPalabras++;
        }
    }

    /**
     * Busqueda exacta de una palabra.
     */
    public boolean buscar(String palabra) {
        if (palabra == null || estaVacio()) {
            return false;
        }
        String p = palabra.trim().toLowerCase();
        NodoTrie nodo = buscarNodo(p);
        return nodo != null && nodo.esFinDePalabra();
    }

    /**
     * Verifica si existe al menos una palabra que comience con el prefijo dado.
     */
    public boolean comienzaCon(String prefijo) {
        if (prefijo == null || estaVacio()) {
            return false;
        }
        String p = prefijo.trim().toLowerCase();
        return buscarNodo(p) != null;
    }

    private NodoTrie buscarNodo(String cadena) {
        NodoTrie actual = raiz;
        for (int i = 0; i < cadena.length(); i++) {
            char c = cadena.charAt(i);
            actual = actual.getHijo(c);
            if (actual == null) {
                return null;
            }
        }
        return actual;
    }

    /**
     * Devuelve todas las palabras almacenadas que comiencen con el prefijo dado.
     */
    public Lista<String> obtenerSugerencias(String prefijo) {
        Lista<String> resultado = new MiLista<>();
        if (prefijo == null) {
            return resultado;
        }
        String p = prefijo.trim().toLowerCase();
        NodoTrie nodoPrefijo = buscarNodo(p);

        if (nodoPrefijo != null) {
            recolectarPalabras(nodoPrefijo, resultado);
        }
        return resultado;
    }

    private void recolectarPalabras(NodoTrie nodo, Lista<String> resultado) {
        if (nodo == null) {
            return;
        }
        if (nodo.esFinDePalabra()) {
            resultado.agregar(nodo.getPalabraCompleta());
        }
        NodoTrie[] hijos = nodo.getHijos();
        for (int i = 0; i < hijos.length; i++) {
            if (hijos[i] != null) {
                recolectarPalabras(hijos[i], resultado);
            }
        }
    }

    /**
     * TOKENIZACION POR COINCIDENCIA MAS LARGA (Longest Prefix Match).
     * Toma una cadena (ej: "realenvidovale4" o "truco quiero") y busca secuencialmente
     * el token mas largo registrado en el Trie que comience en cada posicion.
     */
    public Lista<String> tokenizarPorCoincidenciaMasLarga(String texto) {
        Lista<String> tokens = new MiLista<>();
        if (texto == null || texto.isEmpty()) {
            return tokens;
        }

        String t = texto.toLowerCase();
        int n = t.length();
        int i = 0;

        while (i < n) {
            // Saltar espacios en blanco
            if (Character.isWhitespace(t.charAt(i))) {
                i++;
                continue;
            }

            NodoTrie actual = raiz;
            int ultimoFinIndex = -1;
            String tokenMasLargo = null;

            // Avanzar en el trie buscando el prefijo mas largo
            for (int j = i; j < n; j++) {
                char c = t.charAt(j);
                actual = actual.getHijo(c);
                if (actual == null) {
                    break;
                }
                if (actual.esFinDePalabra()) {
                    ultimoFinIndex = j;
                    tokenMasLargo = actual.getPalabraCompleta();
                }
            }

            if (tokenMasLargo != null) {
                tokens.agregar(tokenMasLargo);
                i = ultimoFinIndex + 1;
            } else {
                // Caracter sin coincidencia en el diccionario: avanzar 1 posicion
                i++;
            }
        }

        return tokens;
    }

    /**
     * TOKENIZACION DE FRASES (la que usa el corpus de Markov).
     * En cada palabra busca la expresion MAS LARGA del vocabulario que termine justo en un fin
     * de palabra (por ejemplo "vale cuatro" o "me voy al mazo" quedan como un solo token). Si no
     * hay ninguna, toma la palabra suelta. A diferencia de tokenizarPorCoincidenciaMasLarga:
     * - no se pierde ninguna palabra que no este en el vocabulario;
     * - no corta palabras por la mitad ("envidioso" no se lee como "envido" + "so").
     * Costo: O(n * L), con n el largo del texto y L el largo de la expresion mas larga.
     */
    public Lista<String> tokenizarFrase(String texto) {
        Lista<String> tokens = new MiLista<>();
        if (texto == null) {
            return tokens;
        }
        String t = normalizar(texto);
        int n = t.length();
        int i = 0;
        while (i < n) {
            if (t.charAt(i) == ' ') {
                i++;
                continue;
            }
            // Expresion mas larga del vocabulario que empiece aca y termine en fin de palabra
            NodoTrie actual = raiz;
            int fin = -1;
            String mejor = null;
            for (int j = i; j < n; j++) {
                actual = actual.getHijo(t.charAt(j));
                if (actual == null) {
                    break;
                }
                boolean terminaPalabra = (j + 1 == n) || t.charAt(j + 1) == ' ';
                if (actual.esFinDePalabra() && terminaPalabra) {
                    fin = j;
                    mejor = actual.getPalabraCompleta();
                }
            }
            if (mejor == null) {
                // No esta en el vocabulario: la palabra entera es el token
                fin = i;
                while (fin + 1 < n && t.charAt(fin + 1) != ' ') {
                    fin++;
                }
                mejor = t.substring(i, fin + 1);
            }
            tokens.agregar(mejor);
            i = fin + 1;
        }
        return tokens;
    }

    /**
     * Pasa a minusculas, saca tildes y cambia por espacios los signos ("¡Truco!" -> " truco ").
     * Asi "Quiero", "quiero" y "¡QUIERO!" son el mismo token. La enie se conserva.
     */
    public static String normalizar(String texto) {
        StringBuilder sb = new StringBuilder();
        String t = texto.toLowerCase();
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case 'á': c = 'a'; break;
                case 'é': c = 'e'; break;
                case 'í': c = 'i'; break;
                case 'ó': c = 'o'; break;
                case 'ú': case 'ü': c = 'u'; break;
                default: break;
            }
            sb.append(Character.isLetterOrDigit(c) ? c : ' ');
        }
        return sb.toString();
    }
}
