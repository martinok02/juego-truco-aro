package juego.reglas;

import estructuras.Lista;

public class EstrategiaConservadora implements EstrategiaRival {

    @Override
    public String getNombreEstrategia() {
        return "Gaucho Prudente";
    }

    @Override
    public boolean quiereEnvido(int misPuntosEnvido, int puntosEnJuego, boolean soyMano) {
        int limite = soyMano ? 27 : 29;
        return misPuntosEnvido >= limite;
    }

    @Override
    public boolean quiereTruco(Lista<Carta> cartasEnMano, int puntosEnJuego, int bazasGanadas) {
        if (bazasGanadas >= 1) return true;
        // Solo acepta si tiene al menos un 3 o mejor (poder >= 10)
        for (int i = 0; i < cartasEnMano.tamanio(); i++) {
            if (cartasEnMano.obtener(i).getPoder() >= 10) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Carta elegirCarta(Lista<Carta> cartasEnMano, Carta cartaRivalEnMesa, int bazaActual) {
        if (cartasEnMano.estaVacia()) return null;

        if (cartaRivalEnMesa == null) {
            // Sale jugando la mas baja para tantear
            Carta menor = cartasEnMano.obtener(0);
            for (int i = 1; i < cartasEnMano.tamanio(); i++) {
                if (cartasEnMano.obtener(i).getPoder() < menor.getPoder()) {
                    menor = cartasEnMano.obtener(i);
                }
            }
            return menor;
        }

        // Si ya jugo el rival: buscar la menor que le gane
        Carta mejorOpcion = null;
        for (int i = 0; i < cartasEnMano.tamanio(); i++) {
            Carta c = cartasEnMano.obtener(i);
            if (c.getPoder() > cartaRivalEnMesa.getPoder()) {
                if (mejorOpcion == null || c.getPoder() < mejorOpcion.getPoder()) {
                    mejorOpcion = c;
                }
            }
        }

        // Si ninguna le gana, tirar la mas baja que tenga
        if (mejorOpcion == null) {
            mejorOpcion = cartasEnMano.obtener(0);
            for (int i = 1; i < cartasEnMano.tamanio(); i++) {
                if (cartasEnMano.obtener(i).getPoder() < mejorOpcion.getPoder()) {
                    mejorOpcion = cartasEnMano.obtener(i);
                }
            }
        }

        return mejorOpcion;
    }
}
