package persistencia;

import estructuras.Cola;
import estructuras.Lista;
import estructuras.MiCola;
import estructuras.MiLista;

/**
 * Avisos sobre los archivos de datos/ para mostrar en pantalla (archivo que falta, linea
 * danada, datos reemplazados por los iniciales...).
 *
 * - Cola: los avisos se muestran en el orden en que aparecieron.
 * - Lista de ya registrados: el mismo aviso no se repite cada vez que se vuelve a leer
 *   el archivo (por ejemplo, cada vez que se entra al mapa).
 */
public final class Avisos {

    private static final Cola<String> pendientes = new MiCola<>();
    private static final Lista<String> registrados = new MiLista<>();

    private Avisos() {
    }

    public static void registrar(String aviso) {
        if (aviso == null || registrados.indiceDe(aviso) >= 0) {
            return;
        }
        registrados.agregar(aviso);
        pendientes.encolar(aviso);
        System.err.println("Aviso: " + aviso);
    }

    public static boolean hayPendientes() {
        return !pendientes.estaVacia();
    }

    public static String siguiente() {
        return pendientes.desencolar();
    }

    /** Para las pruebas: cuantos avisos distintos se registraron en total. */
    public static int cantidadRegistrados() {
        return registrados.tamanio();
    }

    /** Para las pruebas: si se registro algun aviso que contenga el texto. */
    public static boolean huboAvisoCon(String texto) {
        for (int i = 0; i < registrados.tamanio(); i++) {
            if (registrados.obtener(i).contains(texto)) {
                return true;
            }
        }
        return false;
    }
}
