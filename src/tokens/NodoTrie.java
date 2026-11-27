package tokens;

public class NodoTrie {
    private final NodoTrie[] hijos;
    private boolean esFinDePalabra;
    private String palabraCompleta;

    public NodoTrie() {
        // Arreglo directo para 256 caracteres ASCII extendido (permitido dentro del TAD)
        this.hijos = new NodoTrie[256];
        this.esFinDePalabra = false;
        this.palabraCompleta = null;
    }

    public NodoTrie getHijo(char c) {
        int index = (int) c;
        if (index < 0 || index >= hijos.length) {
            return null;
        }
        return hijos[index];
    }

    public void setHijo(char c, NodoTrie nodo) {
        int index = (int) c;
        if (index >= 0 && index < hijos.length) {
            hijos[index] = nodo;
        }
    }

    public boolean esFinDePalabra() {
        return esFinDePalabra;
    }

    public void setFinDePalabra(boolean esFin) {
        this.esFinDePalabra = esFin;
    }

    public String getPalabraCompleta() {
        return palabraCompleta;
    }

    public void setPalabraCompleta(String palabraCompleta) {
        this.palabraCompleta = palabraCompleta;
    }

    public NodoTrie[] getHijos() {
        return hijos;
    }
}
