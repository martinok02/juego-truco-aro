package verificador;

import estructuras.MiCola;
import estructuras.MiLista;
import estructuras.MiPila;
import juego.pruebas.PruebaEstructuras;

public class PruebaMisEstructuras {

    // Punto de entrada
    public static void main(String[] args) {

        System.out.println("=== 1. Verificador del Kit Base (Lista, Pila, Cola) ===");
        VerificadorEstructuras.verificarLista(new MiLista<Integer>());
        VerificadorEstructuras.verificarPila(new MiPila<Integer>());
        VerificadorEstructuras.verificarCola(new MiCola<Integer>());
        System.out.println("=== Todas las estructuras del kit verificadas con exito! ===\n");

        System.out.println("=== 2. Verificador de Estructuras No Lineales y Algoritmos ===");
        PruebaEstructuras.ejecutarTodas();
    }
}
