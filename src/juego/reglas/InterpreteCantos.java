package juego.reglas;

import estructuras.Lista;
import tokens.Trie;

/**
 * Entiende lo que el jugador escribe en la mesa ("quiero retruco", "me voy al mazo",
 * "el envido esta primero"...) y lo convierte en una Accion.
 *
 * Usa un Trie con el vocabulario de cantos:
 * - tokenizarFrase busca la coincidencia MAS LARGA, asi "real envido" no se lee como
 *   "envido" y "no quiero" no se lee como "quiero";
 * - obtenerSugerencias (busqueda por prefijo) completa lo que se va escribiendo.
 */
public class InterpreteCantos {

    private final Trie vocabulario = new Trie();

    public InterpreteCantos() {
        vocabulario.insertar("envido");
        vocabulario.insertar("real envido");
        vocabulario.insertar("falta envido");
        vocabulario.insertar("el envido esta primero");
        vocabulario.insertar("truco");
        vocabulario.insertar("retruco");
        vocabulario.insertar("quiero retruco");
        vocabulario.insertar("vale cuatro");
        vocabulario.insertar("quiero vale cuatro");
        vocabulario.insertar("quiero");
        vocabulario.insertar("no quiero");
        vocabulario.insertar("mazo");
        vocabulario.insertar("al mazo");
        vocabulario.insertar("me voy al mazo");
    }

    /** Tokens del texto segun el vocabulario (coincidencia mas larga). */
    public Lista<String> tokenizar(String texto) {
        return vocabulario.tokenizarFrase(texto);
    }

    /** Cantos que empiezan con lo escrito hasta ahora (busqueda por prefijo en el Trie). */
    public Lista<String> sugerencias(String prefijo) {
        return vocabulario.obtenerSugerencias(Trie.normalizar(prefijo).trim());
    }

    /** Primer canto reconocido en el texto, se pueda hacer o no ahora (null si no hay ninguno). */
    public Accion reconocer(String texto) {
        Lista<String> tokens = tokenizar(texto);
        for (int i = 0; i < tokens.tamanio(); i++) {
            Accion accion = accionDeToken(tokens.obtener(i));
            if (accion != null) {
                return accion;
            }
        }
        return null;
    }

    /**
     * Primer canto del texto que este entre las acciones disponibles. Devuelve null si no
     * se entendio nada o si lo que se canto no se puede hacer en este momento.
     */
    public Accion interpretar(String texto, Lista<Accion> disponibles) {
        Lista<String> tokens = tokenizar(texto);
        for (int i = 0; i < tokens.tamanio(); i++) {
            Accion accion = accionDeToken(tokens.obtener(i));
            if (accion != null && disponibles.indiceDe(accion) >= 0) {
                return accion;
            }
        }
        return null;
    }

    /** A que Accion corresponde cada expresion del vocabulario. */
    public static Accion accionDeToken(String token) {
        switch (token) {
            case "envido":
            case "el envido esta primero": return Accion.ENVIDO;
            case "real envido":            return Accion.REAL_ENVIDO;
            case "falta envido":           return Accion.FALTA_ENVIDO;
            case "truco":                  return Accion.TRUCO;
            case "retruco":
            case "quiero retruco":         return Accion.RETRUCO;
            case "vale cuatro":
            case "quiero vale cuatro":     return Accion.VALE_CUATRO;
            case "quiero":                 return Accion.QUIERO;
            case "no quiero":              return Accion.NO_QUIERO;
            case "mazo":
            case "al mazo":
            case "me voy al mazo":         return Accion.MAZO;
            default:                       return null;
        }
    }
}
