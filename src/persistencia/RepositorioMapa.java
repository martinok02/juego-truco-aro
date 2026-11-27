package persistencia;

import grafos.Grafo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Mapa de viajes (datos/mapa.txt): grafo dirigido y ponderado.
 *
 * Formato: una arista por linea, "origen;destino;costo" (costo entero >= 0, en fichas).
 *   resistencia;corrientes;4
 * Las lineas con '#' son comentarios. Una linea danada (campos de mas o de menos, costo
 * que no es numero o es negativo) se avisa en pantalla y se saltea; el resto del mapa se
 * carga igual. Si no queda ninguna arista valida, o falta el archivo, se usa el mapa base.
 */
public class RepositorioMapa {

    private static final String RUTA_POR_DEFECTO = "datos/mapa.txt";

    public static Grafo<String> cargarMapa() {
        return cargarMapa(new File(RUTA_POR_DEFECTO));
    }

    public static Grafo<String> cargarMapa(File archivo) {
        Grafo<String> grafo = new Grafo<>();

        if (archivo == null || !archivo.exists()) {
            Avisos.registrar((archivo == null ? "mapa.txt" : archivo.getName()) + " no existe: se usa el mapa base de rutas.");
            cargarMapaPorDefecto(grafo);
            return grafo;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
            String linea;
            int numero = 0;
            while ((linea = reader.readLine()) != null) {
                numero++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                try {
                    leerArista(grafo, linea, archivo.getName(), numero);
                } catch (DatoDanadoException e) {
                    Avisos.registrar(e.getMessage() + " (ruta ignorada)");
                }
            }
        } catch (Exception e) {
            Avisos.registrar("No se pudo leer " + archivo.getName() + ": " + e.getMessage() + ". Se usa el mapa base.");
            grafo = new Grafo<>();
        }

        if (grafo.cantidadVertices() == 0) {
            Avisos.registrar(archivo.getName() + " no tiene ninguna ruta valida: se usa el mapa base.");
            cargarMapaPorDefecto(grafo);
        }
        return grafo;
    }

    private static void leerArista(Grafo<String> grafo, String linea, String archivo, int numero) throws DatoDanadoException {
        String[] partes = linea.split(";"); // unico uso de arreglo permitido: split al leer una linea
        if (partes.length != 3) {
            throw new DatoDanadoException(archivo, numero, "se esperaban 3 campos (origen;destino;costo) y hay " + partes.length);
        }
        String origen = partes[0].trim().toLowerCase();
        String destino = partes[1].trim().toLowerCase();
        if (origen.isEmpty() || destino.isEmpty()) {
            throw new DatoDanadoException(archivo, numero, "falta el origen o el destino");
        }
        try {
            grafo.agregarArista(origen, destino, Integer.parseInt(partes[2].trim()));
        } catch (IllegalArgumentException e) { // costo que no es numero o negativo
            throw new DatoDanadoException(archivo, numero, "costo invalido \"" + partes[2].trim() + "\"");
        }
    }

    /** Mapa base (si falta mapa.txt): rutas de ida y vuelta entre las provincias de la gira. */
    private static void cargarMapaPorDefecto(Grafo<String> grafo) {
        ruta(grafo, "formosa", "chaco", 2);
        ruta(grafo, "chaco", "corrientes", 1);
        ruta(grafo, "corrientes", "misiones", 3);
        ruta(grafo, "formosa", "salta", 7);
        ruta(grafo, "chaco", "salta", 8);
        ruta(grafo, "chaco", "santiago del estero", 6);
        ruta(grafo, "salta", "jujuy", 1);
        ruta(grafo, "salta", "tucuman", 3);
        ruta(grafo, "tucuman", "catamarca", 2);
        ruta(grafo, "tucuman", "santiago del estero", 2);
        ruta(grafo, "catamarca", "santiago del estero", 4);
        ruta(grafo, "santiago del estero", "cordoba", 4);
        ruta(grafo, "catamarca", "cordoba", 4);
        ruta(grafo, "cordoba", "santa fe", 3);
        ruta(grafo, "santa fe", "entre rios", 1);
        ruta(grafo, "santa fe", "buenos aires", 5);
        ruta(grafo, "entre rios", "buenos aires", 4);
        ruta(grafo, "cordoba", "buenos aires", 7);
        ruta(grafo, "chaco", "santa fe", 5);
        ruta(grafo, "corrientes", "entre rios", 6);
    }

    /** Ruta de ida y vuelta: dos aristas dirigidas con el mismo costo. */
    private static void ruta(Grafo<String> grafo, String a, String b, int costo) {
        grafo.agregarArista(a, b, costo);
        grafo.agregarArista(b, a, costo);
    }
}
