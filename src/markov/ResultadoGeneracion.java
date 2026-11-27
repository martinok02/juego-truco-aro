package markov;

import estructuras.Lista;
import estructuras.MiLista;

/**
 * Lo que devuelve el modelo al generar: el texto, sus tokens, la bitacora de decisiones
 * (retrocesos de contexto, token de fin, respaldos) y si la frase se corto por llegar al
 * largo maximo pedido, para que quien la muestre lo indique (por ejemplo con "...").
 */
public class ResultadoGeneracion {
    private final String textoGenerado;
    private final Lista<String> tokens;
    private final Lista<String> bitacora;
    private final boolean cortadaPorLargo;

    public ResultadoGeneracion(String textoGenerado, Lista<String> tokens, Lista<String> bitacora) {
        this(textoGenerado, tokens, bitacora, false);
    }

    public ResultadoGeneracion(String textoGenerado, Lista<String> tokens, Lista<String> bitacora, boolean cortadaPorLargo) {
        this.textoGenerado = textoGenerado;
        this.tokens = tokens != null ? tokens : new MiLista<String>();
        this.bitacora = bitacora != null ? bitacora : new MiLista<String>();
        this.cortadaPorLargo = cortadaPorLargo;
    }

    public String getTextoGenerado() {
        return textoGenerado;
    }

    public Lista<String> getTokens() {
        return tokens;
    }

    public Lista<String> getBitacora() {
        return bitacora;
    }

    /** true si la frase no termino sola (token de fin) sino porque se llego al maximo de tokens. */
    public boolean isCortadaPorLargo() {
        return cortadaPorLargo;
    }

    /**
     * El texto listo para mostrar: con mayuscula inicial y cerrado con "." si la frase termino
     * sola, o con "..." si se corto por largo (asi nunca se corta sin avisar).
     */
    public String getTextoParaMostrar() {
        if (textoGenerado == null || textoGenerado.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(textoGenerado.charAt(0)) + textoGenerado.substring(1)
                + (cortadaPorLargo ? "..." : ".");
    }
}
