package juego.reglas;

import estructuras.Lista;

public class EstrategiaArriesgada implements EstrategiaRival {

    @Override
    public String getNombreEstrategia() {
        return "Gaucho Picante";
    }

    @Override
    public boolean quiereEnvido(int misPuntosEnvido, int puntosEnJuego, boolean soyMano) {
        return misPuntosEnvido >= 24;
    }

    @Override
    public boolean quiereTruco(Lista<Carta> cartasEnMano, int puntosEnJuego, int bazasGanadas) {
        // Acepta si tiene cualquier carta media o si ya gano una baza
        if (bazasGanadas >= 1) return true;
        for (int i = 0; i < cartasEnMano.tamanio(); i++) {
            if (cartasEnMano.obtener(i).getPoder() >= 7) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Carta elegirCarta(Lista<Carta> cartasEnMano, Carta cartaRivalEnMesa, int bazaActual) {
        if (cartasEnMano.estaVacia()) return null;

        // Si sale primero, clava su carta mas alta para presionar
        if (cartaRivalEnMesa == null) {
            Carta mayor = cartasEnMano.obtener(0);
            for (int i = 1; i < cartasEnMano.tamanio(); i++) {
                if (cartasEnMano.obtener(i).getPoder() > mayor.getPoder()) {
                    mayor = cartasEnMano.obtener(i);
                }
            }
            return mayor;
        }

        // Si ya jugo el rival: mata con la mas fuerte disponible
        Carta mejorOpcion = null;
        for (int i = 0; i < cartasEnMano.tamanio(); i++) {
            Carta c = cartasEnMano.obtener(i);
            if (c.getPoder() > cartaRivalEnMesa.getPoder()) {
                if (mejorOpcion == null || c.getPoder() > mejorOpcion.getPoder()) {
                    mejorOpcion = c;
                }
            }
        }

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
