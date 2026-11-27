package persistencia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Gestor de copias de seguridad (.bak).
 * Cumple con la consigna de generar backup antes de sobrescribir archivos criticos
 * utilizando streams basicos de java.io (sin java.nio).
 */
public class Respaldo {

    /**
     * Antes de escribir un archivo: crea la carpeta si no existe (por ejemplo, si se borro
     * datos/ entera) y hace la copia .bak del archivo anterior.
     */
    public static void prepararParaEscribir(File archivo) {
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            Avisos.registrar("No se pudo crear la carpeta " + carpeta.getName() + " para guardar los datos.");
        }
        crearCopiaSiExiste(archivo);
    }

    public static void crearCopiaSiExiste(File origen) {
        if (origen == null || !origen.exists() || !origen.isFile()) {
            return;
        }

        File destino = new File(origen.getAbsolutePath() + ".bak");
        // Se copia byte a byte: los Buffered*Stream ya agrupan las lecturas por dentro,
        // asi no hace falta un arreglo byte[] propio (los arreglos van solo dentro de los TADs).
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(origen));
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destino))) {
            int dato;
            while ((dato = in.read()) != -1) {
                out.write(dato);
            }
        } catch (IOException e) {
            Avisos.registrar("No se pudo crear la copia de seguridad de " + origen.getName() + ": " + e.getMessage());
        }
    }
}
