package juego.ventana;

/**
 * Opciones de pantalla del jugador: resolucion de la ventana y si se juega en pantalla completa.
 * El setter solo acepta las resoluciones que ofrece el juego (Resolucion.disponibles()).
 */
public class OpcionesPantalla {

    private Resolucion resolucion = Resolucion.porDefecto();
    private boolean pantallaCompleta = false;

    public Resolucion getResolucion() {
        return resolucion;
    }

    public void setResolucion(Resolucion resolucion) {
        if (resolucion == null || Resolucion.buscar(resolucion.getAncho(), resolucion.getAlto()) == null) {
            throw new IllegalArgumentException("Resolucion no disponible: " + resolucion);
        }
        this.resolucion = resolucion;
    }

    public boolean isPantallaCompleta() {
        return pantallaCompleta;
    }

    public void setPantallaCompleta(boolean pantallaCompleta) {
        this.pantallaCompleta = pantallaCompleta;
    }

    /** Lleva estas opciones a la ventana abierta. */
    public void aplicar() {
        VentanaJuego.aplicar(resolucion.getAncho(), resolucion.getAlto(), pantallaCompleta);
    }
}
