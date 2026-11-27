package persistencia;

/**
 * Excepcion propia: una linea de un archivo de datos/ no tiene el formato esperado
 * (faltan campos, un numero esta mal escrito, un valor es imposible...).
 *
 * Es "checked" (extiende Exception) a proposito: quien lee una linea esta obligado a
 * decidir que hacer si viene danada. Los repositorios la atrapan por linea, avisan en
 * pantalla (Avisos) y siguen con el resto del archivo.
 */
public class DatoDanadoException extends Exception {

    private final String archivo;
    private final int numeroLinea;

    public DatoDanadoException(String archivo, int numeroLinea, String motivo) {
        super(archivo + ", linea " + numeroLinea + ": " + motivo);
        this.archivo = archivo;
        this.numeroLinea = numeroLinea;
    }

    public String getArchivo() {
        return archivo;
    }

    public int getNumeroLinea() {
        return numeroLinea;
    }
}
