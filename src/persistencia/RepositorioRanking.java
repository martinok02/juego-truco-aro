package persistencia;

import arboles.ABB;
import estructuras.Lista;
import juego.reglas.ClaveRanking;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Ranking historico (datos/ranking.txt), guardado en un ABB ordenado por puntaje.
 *
 * Formato: "puntaje;nombre;partidas;victorias", una linea por jugador.
 *   30;Rodri;4;4
 * Repetidos: el ABB compara por puntaje y, a igual puntaje, por nombre; un jugador tiene
 * una sola fila (registrarPartido junta sus filas). Una linea danada (campos que faltan,
 * numeros mal escritos o valores imposibles) se avisa en pantalla y se saltea.
 */
public class RepositorioRanking {

    private static final String RUTA_ARCHIVO = "datos/ranking.txt";
    private static final int MAX_TOP = 10;

    public static ABB<ClaveRanking> cargarRanking() {
        return cargarRanking(new File(RUTA_ARCHIVO));
    }

    /** Sobrecarga con el archivo a leer (las pruebas usan un archivo temporal). */
    public static ABB<ClaveRanking> cargarRanking(File archivo) {
        ABB<ClaveRanking> abb = new ABB<>();
        if (!archivo.exists()) {
            Avisos.registrar(archivo.getName() + " no existe: el ranking empieza vacio.");
            return abb;
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
                    abb.insertar(leerLinea(linea, archivo.getName(), numero));
                    if (abb.tamanio() > MAX_TOP) {
                        abb.eliminarMin();
                    }
                } catch (DatoDanadoException e) {
                    Avisos.registrar(e.getMessage() + " (fila ignorada)");
                }
            }
        } catch (Exception e) {
            Avisos.registrar("No se pudo leer " + archivo.getName() + ": " + e.getMessage());
        }
        return abb;
    }

    private static ClaveRanking leerLinea(String linea, String archivo, int numero) throws DatoDanadoException {
        String[] p = linea.split(";"); // unico uso de arreglo permitido: split al leer una linea
        if (p.length != 4) {
            throw new DatoDanadoException(archivo, numero, "se esperaban 4 campos (puntaje;nombre;partidas;victorias) y hay " + p.length);
        }
        try {
            return new ClaveRanking(Integer.parseInt(p[0].trim()), p[1].trim(),
                    Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim()));
        } catch (IllegalArgumentException e) { // numero mal escrito o valores imposibles
            throw new DatoDanadoException(archivo, numero, "valores invalidos (" + e.getMessage() + ")");
        }
    }

    /**
     * Suma un partido al ranking y lo guarda. Si el jugador ya estaba, su registro se ELIMINA
     * del ABB (eliminacion de un nodo: el puntaje es la clave, asi que cambia de lugar) y se
     * vuelve a insertar con los puntos, partidas y victorias acumulados. Si el arbol pasa de
     * 10 registros, se poda el de menor puntaje (eliminarMin).
     */
    public static ABB<ClaveRanking> registrarPartido(String nombre, int puntos, boolean gano) {
        ABB<ClaveRanking> abb = cargarRanking();
        ClaveRanking nuevo = new ClaveRanking(Math.max(0, puntos), nombre, 1, gano ? 1 : 0);
        // Si el jugador ya estaba (en archivos viejos puede aparecer en varias filas), cada
        // registro suyo se elimina del ABB y se suma al nuevo: queda una sola fila por jugador.
        ClaveRanking anterior = buscarPorNombre(abb, nombre);
        while (anterior != null) {
            abb.eliminar(anterior);
            nuevo = new ClaveRanking(nuevo.getPuntaje() + anterior.getPuntaje(), nuevo.getNombre(),
                    nuevo.getPartidas() + anterior.getPartidas(), nuevo.getVictorias() + anterior.getVictorias());
            anterior = buscarPorNombre(abb, nombre);
        }
        abb.insertar(nuevo);
        while (abb.tamanio() > MAX_TOP) {
            abb.eliminarMin();
        }
        guardarRanking(abb);
        return abb;
    }

    /** El ABB esta ordenado por puntaje, no por nombre: se recorre in-orden para encontrarlo. */
    public static ClaveRanking buscarPorNombre(ABB<ClaveRanking> abb, String nombre) {
        Lista<ClaveRanking> todos = abb.inOrdenIterativo();
        for (int i = 0; i < todos.tamanio(); i++) {
            if (todos.obtener(i).getNombre().equalsIgnoreCase(nombre)) {
                return todos.obtener(i);
            }
        }
        return null;
    }

    public static void guardarRanking(ABB<ClaveRanking> abb) {
        guardarRanking(abb, new File(RUTA_ARCHIVO));
    }

    public static void guardarRanking(ABB<ClaveRanking> abb, File archivo) {
        if (abb == null) return;
        Respaldo.prepararParaEscribir(archivo);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
            writer.write("# puntaje;nombre;partidas;victorias\n");
            Lista<ClaveRanking> lista = abb.inOrdenIterativo();
            for (int i = 0; i < lista.tamanio(); i++) {
                ClaveRanking c = lista.obtener(i);
                writer.write(String.format("%d;%s;%d;%d\n", c.getPuntaje(), c.getNombre(), c.getPartidas(), c.getVictorias()));
            }
        } catch (Exception e) {
            Avisos.registrar("No se pudo guardar " + archivo.getName() + ": " + e.getMessage());
        }
    }
}
