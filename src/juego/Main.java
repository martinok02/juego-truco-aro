package juego;

import juego.audio.EfectoReparto;
import juego.audio.MusicaTango;
import juego.pantallas.EscenaMenu;
import juego.ventana.OpcionesPantalla;
import juego.ventana.VentanaJuego;
import persistencia.RepositorioOpciones;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando TrucoAro...");
        MusicaTango.iniciar();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EfectoReparto.detener();
            MusicaTango.detener();
        }, "cerrar-audio"));
        // Ventana propia (usa el Motor del kit) con la resolucion y el modo guardados en datos/opciones.txt
        OpcionesPantalla opciones = RepositorioOpciones.cargar();
        VentanaJuego.abrir("TrucoAro", new EscenaMenu(), opciones.getResolucion().getAncho(),
                opciones.getResolucion().getAlto(), opciones.isPantallaCompleta());
    }
}
