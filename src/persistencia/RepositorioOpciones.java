package persistencia;

import juego.ventana.OpcionesPantalla;
import juego.ventana.Resolucion;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Opciones de pantalla (datos/opciones.txt).
 *
 * Formato: "clave=valor" por linea.
 *   resolucion=1024x768
 *   pantalla_completa=no
 * Si falta el archivo se usa 800x600 en ventana. Una linea danada se avisa y se ignora.
 */
public class RepositorioOpciones {

    private static final String RUTA = "datos/opciones.txt";

    public static OpcionesPantalla cargar() {
        return cargar(new File(RUTA));
    }

    public static OpcionesPantalla cargar(File archivo) {
        OpcionesPantalla opciones = new OpcionesPantalla();
        if (!archivo.exists()) {
            return opciones; // primera vez: valores por defecto, sin aviso
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
                    leerLinea(opciones, linea, archivo.getName(), numero);
                } catch (DatoDanadoException e) {
                    Avisos.registrar(e.getMessage() + " (se usa el valor por defecto)");
                }
            }
        } catch (Exception e) {
            Avisos.registrar("No se pudo leer " + archivo.getName() + ": " + e.getMessage());
        }
        return opciones;
    }

    private static void leerLinea(OpcionesPantalla opciones, String linea, String archivo, int numero) throws DatoDanadoException {
        int igual = linea.indexOf('=');
        if (igual <= 0) {
            throw new DatoDanadoException(archivo, numero, "se esperaba clave=valor");
        }
        String clave = linea.substring(0, igual).trim().toLowerCase();
        String valor = linea.substring(igual + 1).trim().toLowerCase();
        if (clave.equals("resolucion")) {
            int x = valor.indexOf('x');
            Resolucion r = null;
            try {
                if (x > 0) {
                    r = Resolucion.buscar(Integer.parseInt(valor.substring(0, x).trim()),
                            Integer.parseInt(valor.substring(x + 1).trim()));
                }
            } catch (NumberFormatException e) {
                r = null;
            }
            if (r == null) {
                throw new DatoDanadoException(archivo, numero, "resolucion no disponible \"" + valor + "\"");
            }
            opciones.setResolucion(r);
        } else if (clave.equals("pantalla_completa")) {
            if (!valor.equals("si") && !valor.equals("no")) {
                throw new DatoDanadoException(archivo, numero, "pantalla_completa debe ser si o no");
            }
            opciones.setPantallaCompleta(valor.equals("si"));
        } else {
            throw new DatoDanadoException(archivo, numero, "clave desconocida \"" + clave + "\"");
        }
    }

    public static void guardar(OpcionesPantalla opciones) {
        File archivo = new File(RUTA);
        Respaldo.prepararParaEscribir(archivo);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
            writer.write("# Opciones de pantalla\n");
            writer.write("resolucion=" + opciones.getResolucion().getAncho() + "x" + opciones.getResolucion().getAlto() + "\n");
            writer.write("pantalla_completa=" + (opciones.isPantallaCompleta() ? "si" : "no") + "\n");
        } catch (Exception e) {
            Avisos.registrar("No se pudo guardar " + archivo.getName() + ": " + e.getMessage());
        }
    }
}
