package persistencia;

import arboles.ArbolBinario;
import arboles.NodoBinario;
import estructuras.Lista;
import juego.reglas.Etapa;
import juego.reglas.Gira;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Progreso de la gira por Argentina (datos/gira.txt): el arbol de la gira en pre-orden.
 *
 * Formato documentado:
 * - Primera linea util: "vidas=N" (vidas que quedan en la region actual, 1 a 6).
 * - Despues, el arbol en PRE-ORDEN: cada nodo es "E;tipo;clave;nombre;ganada" y un hijo nulo
 *   es "#". Hijo izquierdo = primer hijo; hijo derecho = siguiente hermano. Ejemplo:
 *     vidas=5
 *     E;PAIS;argentina;Argentina;no
 *     E;REGION;noreste;Noreste (NEA);no
 *     E;PROVINCIA;formosa;Formosa;si
 *     #
 *     E;PROVINCIA;chaco;Chaco;no
 *     ...
 * - Las lineas que empiezan con "# " son comentarios.
 * Si una linea esta danada o el arbol no tiene las provincias de la gira, se avisa en
 * pantalla y se empieza una gira nueva (un arbol en pre-orden no se puede reparar salteando
 * lineas: cada una define la forma de lo que sigue).
 */
public class RepositorioGira {

    private static final String RUTA = "datos/gira.txt";

    public static boolean existe() {
        return new File(RUTA).exists();
    }

    public static Gira cargar() {
        return cargar(new File(RUTA));
    }

    public static Gira cargar(File archivo) {
        if (!archivo.exists()) {
            return Gira.nueva(); // primera vez: no es un error, no se avisa
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
            Lector lector = new Lector(reader, archivo.getName());
            int vidas = leerVidas(lector);
            NodoBinario<Etapa> raiz = leerNodo(lector);
            if (raiz == null) {
                Avisos.registrar(archivo.getName() + " no tiene la gira. Empieza una gira nueva.");
                return Gira.nueva();
            }
            Gira gira = new Gira(new ArbolBinario<>(raiz), vidas);
            if (!mismasProvincias(gira, Gira.nueva())) {
                Avisos.registrar(archivo.getName() + ": la gira no tiene las provincias esperadas. Empieza una gira nueva.");
                return Gira.nueva();
            }
            return gira;
        } catch (DatoDanadoException e) {
            Avisos.registrar(e.getMessage() + ". Empieza una gira nueva.");
        } catch (IllegalArgumentException | IOException e) {
            Avisos.registrar("No se pudo leer " + archivo.getName() + ": " + e.getMessage() + ". Empieza una gira nueva.");
        }
        return Gira.nueva();
    }

    /** Las provincias (claves y orden) tienen que ser las de la gira oficial. */
    private static boolean mismasProvincias(Gira a, Gira b) {
        Lista<Etapa> pa = a.provincias();
        Lista<Etapa> pb = b.provincias();
        if (pa.tamanio() != pb.tamanio()) return false;
        for (int i = 0; i < pa.tamanio(); i++) {
            if (!pa.obtener(i).getClave().equals(pb.obtener(i).getClave())) return false;
        }
        return a.regiones().tamanio() == b.regiones().tamanio();
    }

    /** Lee lineas contando su numero (para los avisos). */
    private static class Lector {
        private final BufferedReader reader;
        private final String archivo;
        private int numero = 0;

        Lector(BufferedReader reader, String archivo) {
            this.reader = reader;
            this.archivo = archivo;
        }

        String siguiente() throws IOException {
            String linea = reader.readLine();
            while (linea != null) {
                numero++;
                String t = linea.trim();
                if (!t.isEmpty() && !t.startsWith("# ")) return t;
                linea = reader.readLine();
            }
            return null;
        }
    }

    private static int leerVidas(Lector lector) throws IOException, DatoDanadoException {
        String linea = lector.siguiente();
        if (linea == null || !linea.startsWith("vidas=")) {
            throw new DatoDanadoException(lector.archivo, lector.numero, "se esperaba \"vidas=N\"");
        }
        try {
            int vidas = Integer.parseInt(linea.substring(6).trim());
            if (vidas < 1 || vidas > Gira.VIDAS_POR_REGION) {
                throw new NumberFormatException();
            }
            return vidas;
        } catch (NumberFormatException e) {
            throw new DatoDanadoException(lector.archivo, lector.numero, "vidas invalidas (1 a " + Gira.VIDAS_POR_REGION + ")");
        }
    }

    /** Pre-orden recursivo. Caso base: "#" o fin de archivo = subarbol vacio. */
    private static NodoBinario<Etapa> leerNodo(Lector lector) throws IOException, DatoDanadoException {
        String linea = lector.siguiente();
        if (linea == null || linea.equals("#")) {
            return null;
        }
        NodoBinario<Etapa> nodo = new NodoBinario<>(leerEtapa(linea, lector.archivo, lector.numero));
        nodo.setIzquierdo(leerNodo(lector));
        nodo.setDerecho(leerNodo(lector));
        return nodo;
    }

    private static Etapa leerEtapa(String linea, String archivo, int numero) throws DatoDanadoException {
        String[] p = linea.split(";"); // unico uso de arreglo permitido: split al leer una linea
        if (p.length != 5 || !p[0].equals("E")) {
            throw new DatoDanadoException(archivo, numero, "se esperaba \"#\" o \"E;tipo;clave;nombre;ganada\"");
        }
        try {
            Etapa etapa = new Etapa(Etapa.Tipo.valueOf(p[1].trim()), p[2].trim(), p[3].trim());
            String ganada = p[4].trim();
            if (!ganada.equals("si") && !ganada.equals("no")) {
                throw new DatoDanadoException(archivo, numero, "ganada debe ser si o no");
            }
            etapa.setCompletada(ganada.equals("si"));
            return etapa;
        } catch (IllegalArgumentException e) { // tipo desconocido o etapa imposible
            throw new DatoDanadoException(archivo, numero, "etapa invalida (" + e.getMessage() + ")");
        }
    }

    public static void guardar(Gira gira) {
        guardar(gira, new File(RUTA));
    }

    public static void guardar(Gira gira, File archivo) {
        Respaldo.prepararParaEscribir(archivo);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
            writer.write("# Gira por Argentina: vidas y arbol en pre-orden (izq = primer hijo, der = siguiente hermano)\n");
            writer.write("vidas=" + gira.getVidas() + "\n");
            escribirNodo(gira.getArbol().getRaiz(), writer);
        } catch (IOException e) {
            Avisos.registrar("No se pudo guardar " + archivo.getName() + ": " + e.getMessage());
        }
    }

    private static void escribirNodo(NodoBinario<Etapa> nodo, BufferedWriter writer) throws IOException {
        if (nodo == null) {
            writer.write("#\n");
            return;
        }
        Etapa e = nodo.getDato();
        writer.write("E;" + e.getTipo().name() + ";" + e.getClave() + ";" + e.getNombre() + ";" + (e.isCompletada() ? "si" : "no") + "\n");
        escribirNodo(nodo.getIzquierdo(), writer);
        escribirNodo(nodo.getDerecho(), writer);
    }

    /** Borra la gira guardada (para "Nueva gira"). */
    public static void borrar() {
        File archivo = new File(RUTA);
        if (archivo.exists()) {
            Respaldo.crearCopiaSiExiste(archivo);
            if (!archivo.delete()) {
                Avisos.registrar("No se pudo borrar " + archivo.getName() + ".");
            }
        }
    }
}
