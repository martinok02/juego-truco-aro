package juego.reglas;

import estructuras.Lista;
import java.util.Random;

public class EstrategiaMentirosa implements EstrategiaRival {

    private final Random azar = new Random();

    @Override
    public String getNombreEstrategia() {
        return "Gaucho Tahur (Mentiroso)";
    }

    @Override
    public boolean quiereEnvido(int misPuntosEnvido, int puntosEnJuego, boolean soyMano) {
        if (misPuntosEnvido >= 28) return true;
        // Mentira en el 35% de los casos
        return azar.nextInt(100) < 35;
    }

    @Override
    public boolean quiereTruco(Lista<Carta> cartasEnMano, int puntosEnJuego, int bazasGanadas) {
        if (bazasGanadas >= 1) return true;
        // Mentira el 40% de las veces
        return azar.nextInt(100) < 40;
    }

    @Override
    public Carta elegirCarta(Lista<Carta> cartasEnMano, Carta cartaRivalEnMesa, int bazaActual) {
        if (cartasEnMano.estaVacia()) return null;

        // Mezcla estrategia: a veces guarda la mejor carta para el final
        if (cartaRivalEnMesa == null) {
            // Elige carta intermedia
            int idx = cartasEnMano.tamanio() / 2;
            return cartasEnMano.obtener(idx);
        }

        for (int i = 0; i < cartasEnMano.tamanio(); i++) {
            Carta c = cartasEnMano.obtener(i);
            if (c.getPoder() > cartaRivalEnMesa.getPoder()) {
                return c;
            }
        }

        return cartasEnMano.obtener(0);
    }
}
