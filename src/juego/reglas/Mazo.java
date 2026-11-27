package juego.reglas;

import estructuras.Lista;
import estructuras.MiLista;
import java.util.Random;

public class Mazo {

    private final Lista<Carta> cartas;
    private final Random azar;

    public Mazo() {
        this.cartas = new MiLista<>();
        this.azar = new Random();
        inicializarMazo();
    }

    private void inicializarMazo() {
        cartas.vaciar();
        agregarPalo(Palo.ESPADA);
        agregarPalo(Palo.BASTO);
        agregarPalo(Palo.ORO);
        agregarPalo(Palo.COPA);
    }

    /** Las 10 cartas de un palo: del 1 al 12 sin los 8 y 9 (mazo de 40 cartas, sin arreglos). */
    private void agregarPalo(Palo palo) {
        for (int numero = 1; numero <= 12; numero++) {
            if (numero != 8 && numero != 9) {
                cartas.agregar(new Carta(numero, palo));
            }
        }
    }

    public void mezclar() {
        inicializarMazo();
        // Algoritmo de Fisher-Yates sobre MiLista
        for (int i = cartas.tamanio() - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            Carta tempI = cartas.obtener(i);
            Carta tempJ = cartas.obtener(j);
            cartas.eliminar(i);
            cartas.insertar(i, tempJ);
            cartas.eliminar(j);
            cartas.insertar(j, tempI);
        }
    }

    public Lista<Carta> repartirMano() {
        Lista<Carta> mano = new MiLista<>();
        for (int i = 0; i < 3; i++) {
            if (!cartas.estaVacia()) {
                Carta c = cartas.obtener(0);
                cartas.eliminar(0);
                mano.agregar(c);
            }
        }
        return mano;
    }
}
