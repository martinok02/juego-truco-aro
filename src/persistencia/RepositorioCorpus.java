package persistencia;

import estructuras.Lista;
import markov.ModeloMarkov;
import tokens.Trie;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Lee el corpus de dichos del rival (datos/dichos.txt) y entrena el modelo de Markov.
 *
 * Formato del archivo: una frase por linea, en texto libre. Las lineas vacias y las que
 * empiezan con '#' (comentarios, que agrupan las frases por situacion) se ignoran.
 * Cada frase se tokeniza con el Trie (tokenizarFrase): las expresiones del vocabulario del
 * truco ("vale cuatro", "me voy al mazo", "no quiero"...) quedan como un solo token y el
 * resto de las palabras quedan sueltas. La primera palabra (o expresion) de cada frase es su
 * "semilla": el juego pide frases que empiecen con "truco", "quiero", "toma", etc.
 *
 * Si el archivo no existe o no se puede leer, se entrena con un corpus base escrito aca.
 */
public class RepositorioCorpus {

    private static final String RUTA_DICHOS = "datos/dichos.txt";

    /** Crea un modelo de orden k con el vocabulario del truco y lo entrena con el corpus. */
    public static ModeloMarkov crearModelo(int k) {
        ModeloMarkov modelo = new ModeloMarkov(k);
        Trie trie = new Trie();
        cargarVocabulario(trie);
        entrenarDesdeArchivo(modelo, trie);
        return modelo;
    }

    /**
     * Vocabulario del truco: expresiones que deben quedar como UN token aunque tengan varias
     * palabras. Tambien son las semillas con las que el juego pide frases.
     */
    public static void cargarVocabulario(Trie trie) {
        trie.insertar("truco");
        trie.insertar("retruco");
        trie.insertar("vale cuatro");
        trie.insertar("envido");
        trie.insertar("real envido");
        trie.insertar("falta envido");
        trie.insertar("el envido esta primero");
        trie.insertar("flor");
        trie.insertar("contraflor");
        trie.insertar("quiero");
        trie.insertar("no quiero");
        trie.insertar("me voy al mazo");
        trie.insertar("al mazo");
        trie.insertar("mazo");
        trie.insertar("paso");
        trie.insertar("son buenas");
        trie.insertar("ahi va");
        trie.insertar("que suerte");
        trie.insertar("me ganaste");
        trie.insertar("se termino");
        trie.insertar("ancho de espadas");
        trie.insertar("ancho de bastos");
        trie.insertar("siete de oro");
        trie.insertar("siete de espadas");
        trie.insertar("la mano");
        trie.insertar("a ver");
        // Expresiones gauchescas (estilo Martin Fierro) que tienen que quedar enteras
        trie.insertar("al compas de la viguela");
        trie.insertar("gaucho de ley");
        trie.insertar("la pampa");
        trie.insertar("la pampa entera");
        trie.insertar("dende chico");
        trie.insertar("el pingo");
        trie.insertar("la payada");
    }

    public static void entrenarDesdeArchivo(ModeloMarkov modelo, Trie trie) {
        if (modelo == null || trie == null) return;
        if (trie.estaVacio()) {
            cargarVocabulario(trie); // sin vocabulario las expresiones se partirian en palabras
        }

        File archivo = new File(RUTA_DICHOS);
        if (!archivo.exists()) {
            Avisos.registrar("dichos.txt no existe: el rival habla con el corpus base.");
            entrenarDefaults(modelo, trie);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                Lista<String> tokens = trie.tokenizarFrase(linea);
                if (!tokens.estaVacia()) {
                    modelo.entrenarSecuencia(tokens);
                }
            }
        } catch (Exception e) {
            Avisos.registrar("No se pudo leer dichos.txt: " + e.getMessage() + ". Se usa el corpus base.");
            entrenarDefaults(modelo, trie);
        }

        if (modelo.cantidadEstados() == 0) {
            Avisos.registrar("dichos.txt no tiene ninguna frase: se usa el corpus base.");
            entrenarDefaults(modelo, trie);
        }
    }

    /** Corpus base (si falta datos/dichos.txt): al menos una frase por cada semilla que usa el juego. */
    private static void entrenarDefaults(ModeloMarkov modelo, Trie trie) {
        // Corpus base al estilo del Martin Fierro: una frase por cada semilla que usa el juego
        entrenar(modelo, trie, "buenas, aqui me siento a jugar al compas de la viguela");
        entrenar(modelo, trie, "envido, que tengo los tantos como estrellas tiene el cielo");
        entrenar(modelo, trie, "real envido, que la pampa entera me presta los tantos");
        entrenar(modelo, trie, "falta envido, que el gaucho juega todo como juega la vida");
        entrenar(modelo, trie, "truco, que el gaucho bravo no le teme a la partida");
        entrenar(modelo, trie, "retruco, que el que se asusta del truco no merece la baraja");
        entrenar(modelo, trie, "vale cuatro, que naides me ha visto achicar en la payada");
        entrenar(modelo, trie, "quiero, que el gaucho de ley no le dice no a la partida");
        entrenar(modelo, trie, "no quiero, que el gaucho prudente sabe cuando callar");
        entrenar(modelo, trie, "me voy al mazo, que el gaucho sabio no pelea contra el destino");
        entrenar(modelo, trie, "toma, que ansi se juega en la pampa");
        entrenar(modelo, trie, "que suerte, pero el gaucho no se rinde en la primera");
        entrenar(modelo, trie, "me ganaste, paisano, y el gaucho sabe reconocer al mejor");
        entrenar(modelo, trie, "se termino la partida y el gaucho se lleva la gloria");
    }

    private static void entrenar(ModeloMarkov modelo, Trie trie, String frase) {
        modelo.entrenarSecuencia(trie.tokenizarFrase(frase));
    }
}
