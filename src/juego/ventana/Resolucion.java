package juego.ventana;

import estructuras.Lista;
import estructuras.MiLista;

/** Tamanio de la ventana en pixeles. Todas son 4:3, como el juego (800x600 logicos). */
public final class Resolucion {

    private final int ancho;
    private final int alto;

    private Resolucion(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
    }

    /** Las resoluciones que ofrece la pantalla de Opciones. */
    public static Lista<Resolucion> disponibles() {
        Lista<Resolucion> lista = new MiLista<>();
        lista.agregar(new Resolucion(800, 600));
        lista.agregar(new Resolucion(1024, 768));
        lista.agregar(new Resolucion(1280, 960));
        lista.agregar(new Resolucion(1600, 1200));
        return lista;
    }

    /** Busca una resolucion disponible; devuelve null si no es una de las ofrecidas. */
    public static Resolucion buscar(int ancho, int alto) {
        Lista<Resolucion> lista = disponibles();
        for (int i = 0; i < lista.tamanio(); i++) {
            if (lista.obtener(i).ancho == ancho && lista.obtener(i).alto == alto) {
                return lista.obtener(i);
            }
        }
        return null;
    }

    public static Resolucion porDefecto() {
        return disponibles().obtener(0);
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (otro == null || getClass() != otro.getClass()) return false;
        Resolucion r = (Resolucion) otro;
        return ancho == r.ancho && alto == r.alto;
    }

    @Override
    public int hashCode() {
        return ancho * 31 + alto;
    }

    @Override
    public String toString() {
        return ancho + " x " + alto;
    }
}
