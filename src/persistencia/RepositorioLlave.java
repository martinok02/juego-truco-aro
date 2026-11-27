package persistencia;

import arboles.ArbolBinario;
import arboles.NodoBinario;
import estructuras.Lista;
import estructuras.MiLista;
import juego.reglas.Partido;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Persistencia del Arbol Binario del Torneo (ArbolBinario<Partido>), datos/llave.txt.
 * Serializa y reconstruye el arbol de forma recursiva en formato Pre-Orden con marcadores '#'.
 *
 * Formato documentado:
 * - Cada nodo es una linea "M;id;ronda;sede;jugadorA;jugadorB;ganador" ("-" = vacio).
 * - Un hijo nulo es una linea "#". Las lineas que empiezan con "# " son comentarios.
 * - Orden: nodo, subarbol izquierdo, subarbol derecho (pre-orden). Ejemplo de una hoja:
 *     M;4;1;resistencia;Rodri;Tano;-
 *     #
 *     #
 * Lineas danadas: en un arbol en pre-orden cada linea define la forma de todo lo que sigue,
 * asi que no se puede saltear una sola sin desarmar el arbol. Si una linea esta danada, o
 * lo reconstruido no es la llave de 7 partidos (3 niveles), se avisa en pantalla y se usa
 * la llave inicial.
 */
public class RepositorioLlave {

    private static final String RUTA_LLAVE = "datos/llave.txt";

    public static void guardarLlave(ArbolBinario<Partido> arbol) {
        guardarLlave(arbol, new File(RUTA_LLAVE));
    }

    public static void guardarLlave(ArbolBinario<Partido> arbol, File archivo) {
        if (arbol == null) return;
        Respaldo.prepararParaEscribir(archivo);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
            writer.write("# Arbol del Torneo en Pre-Orden con marcadores '#'\n");
            guardarPreOrdenRecursivo(arbol.getRaiz(), writer);
        } catch (IOException e) {
            Avisos.registrar("No se pudo guardar " + archivo.getName() + ": " + e.getMessage());
        }
    }

    private static void guardarPreOrdenRecursivo(NodoBinario<Partido> nodo, BufferedWriter writer) throws IOException {
        if (nodo == null) {
            writer.write("#\n");
            return;
        }

        Partido p = nodo.getDato();
        writer.write(String.format("M;%d;%d;%s;%s;%s;%s\n",
                p.getId(), p.getRonda(), p.getSede(), p.getJugadorA(), p.getJugadorB(), p.getGanador()));

        guardarPreOrdenRecursivo(nodo.getIzquierdo(), writer);
        guardarPreOrdenRecursivo(nodo.getDerecho(), writer);
    }

    public static ArbolBinario<Partido> cargarLlave() {
        return cargarLlave(new File(RUTA_LLAVE));
    }

    /** Sobrecarga con el archivo a leer (las pruebas usan un archivo temporal). */
    public static ArbolBinario<Partido> cargarLlave(File archivo) {
        if (!archivo.exists()) {
            Avisos.registrar(archivo.getName() + " no existe: se arma la llave inicial del torneo.");
            return crearLlavePorDefecto();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
            Lector lector = new Lector(reader, archivo.getName());
            NodoBinario<Partido> raiz = reconstruirPreOrden(lector);
            if (raiz != null) {
                ArbolBinario<Partido> arbol = new ArbolBinario<>(raiz);
                if (arbol.cantidadNodos() == 7 && arbol.altura() == 3) {
                    return arbol;
                }
                Avisos.registrar(archivo.getName() + ": la llave tiene " + arbol.cantidadNodos()
                        + " partidos en vez de 7. Se usa la llave inicial.");
            } else {
                Avisos.registrar(archivo.getName() + " no tiene ningun partido. Se usa la llave inicial.");
            }
        } catch (DatoDanadoException e) {
            Avisos.registrar(e.getMessage() + ". Se usa la llave inicial.");
        } catch (IOException e) {
            Avisos.registrar("No se pudo leer " + archivo.getName() + ": " + e.getMessage() + ". Se usa la llave inicial.");
        }
        return crearLlavePorDefecto();
    }

    /** Lee lineas contando su numero, para que los avisos digan donde esta el problema. */
    private static class Lector {
        private final BufferedReader reader;
        private final String archivo;
        private int numero = 0;

        Lector(BufferedReader reader, String archivo) {
            this.reader = reader;
            this.archivo = archivo;
        }

        /** Siguiente linea util (saltea vacias y comentarios "# ..."), o null al final. */
        String siguiente() throws IOException {
            String linea = reader.readLine();
            while (linea != null) {
                numero++;
                String t = linea.trim();
                if (!t.isEmpty() && !t.startsWith("# ")) {
                    return t;
                }
                linea = reader.readLine();
            }
            return null;
        }
    }

    /**
     * Reconstruccion recursiva en pre-orden.
     * Caso base: "#" (o el fin del archivo) es un subarbol vacio y devuelve null.
     * Caso recursivo: una linea "M;..." crea el nodo y luego se leen su izquierdo y su derecho.
     */
    private static NodoBinario<Partido> reconstruirPreOrden(Lector lector) throws IOException, DatoDanadoException {
        String linea = lector.siguiente();
        if (linea == null || linea.equals("#")) {
            return null;
        }
        NodoBinario<Partido> nodo = new NodoBinario<>(leerPartido(linea, lector.archivo, lector.numero));
        nodo.setIzquierdo(reconstruirPreOrden(lector));
        nodo.setDerecho(reconstruirPreOrden(lector));
        return nodo;
    }

    private static Partido leerPartido(String linea, String archivo, int numero) throws DatoDanadoException {
        String[] p = linea.split(";"); // unico uso de arreglo permitido: split al leer una linea
        if (p.length != 7 || !p[0].trim().equals("M")) {
            throw new DatoDanadoException(archivo, numero, "se esperaba \"#\" o \"M;id;ronda;sede;jugadorA;jugadorB;ganador\"");
        }
        try {
            Partido partido = new Partido(Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()),
                    p[3].trim(), p[4].trim(), p[5].trim());
            String ganador = p[6].trim();
            if (!ganador.equals("-")) {
                partido.setGanador(ganador);
            }
            return partido;
        } catch (IllegalArgumentException e) { // numero mal escrito o partido imposible
            throw new DatoDanadoException(archivo, numero, "partido invalido (" + e.getMessage() + ")");
        }
    }

    public static ArbolBinario<Partido> crearLlavePorDefecto() {
        ArbolBinario<Partido> arbol = new ArbolBinario<>();
        // Insercion por niveles para un torneo de 8 jugadores (7 partidos)
        // Raiz: Final
        Partido finalP = new Partido(1, 3, "posadas", "-", "-");
        // Nivel 2: Semifinales
        Partido semi1 = new Partido(2, 2, "corrientes", "-", "-");
        Partido semi2 = new Partido(3, 2, "corrientes", "-", "-");
        // Nivel 1: Cuartos de final
        Partido c1 = new Partido(4, 1, "resistencia", "Rodri", "Tano");
        Partido c2 = new Partido(5, 1, "resistencia", "Lola", "Pipa");
        Partido c3 = new Partido(6, 1, "formosa", "Chino", "Negro");
        Partido c4 = new Partido(7, 1, "formosa", "Ana", "Beto");

        arbol.insertar(finalP);
        arbol.insertar(semi1);
        arbol.insertar(semi2);
        arbol.insertar(c1);
        arbol.insertar(c2);
        arbol.insertar(c3);
        arbol.insertar(c4);

        return arbol;
    }
}
