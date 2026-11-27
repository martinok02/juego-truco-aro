package juego.reglas;

import estructuras.Lista;

public class CalculadorEnvido {

    public static int calcular(Lista<Carta> cartas) {
        if (cartas == null || cartas.estaVacia()) {
            return 0;
        }

        int maximoPuntos = 0;

        // Probar todas las parejas posibles
        for (int i = 0; i < cartas.tamanio(); i++) {
            Carta c1 = cartas.obtener(i);
            if (c1.getValorEnvido() > maximoPuntos) {
                maximoPuntos = c1.getValorEnvido();
            }

            for (int j = i + 1; j < cartas.tamanio(); j++) {
                Carta c2 = cartas.obtener(j);
                if (c1.getPalo() == c2.getPalo()) {
                    int puntosPareja = 20 + c1.getValorEnvido() + c2.getValorEnvido();
                    if (puntosPareja > maximoPuntos) {
                        maximoPuntos = puntosPareja;
                    }
                }
            }
        }

        return maximoPuntos;
    }

    /** Flor: las tres cartas de la mano son del mismo palo. */
    public static boolean tieneFlor(Lista<Carta> cartas) {
        if (cartas == null || cartas.tamanio() != 3) {
            return false;
        }
        Palo palo = cartas.obtener(0).getPalo();
        return cartas.obtener(1).getPalo() == palo && cartas.obtener(2).getPalo() == palo;
    }

    /** Puntos de la flor: 20 + el valor de las tres cartas (los 10, 11 y 12 valen 0). */
    public static int calcularFlor(Lista<Carta> cartas) {
        if (!tieneFlor(cartas)) {
            return 0;
        }
        int suma = 20;
        for (int i = 0; i < cartas.tamanio(); i++) {
            suma += cartas.obtener(i).getValorEnvido();
        }
        return suma;
    }
}
