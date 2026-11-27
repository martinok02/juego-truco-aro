package juego.reglas;

import estructuras.Lista;

/**
 * Cerebro del rival: decide UNA jugada por vez (cantar, responder o tirar una carta)
 * usando la EstrategiaRival que le toco (Conservadora, Arriesgada o Mentirosa).
 *
 * Polimorfismo: esta clase no sabe que estrategia tiene; le pregunta a la interfaz
 * (quiereEnvido, quiereTruco, elegirCarta) y cada estrategia responde a su manera.
 * Solo usa lo que MesaTruco permite (accionesDisponibles), asi nunca hace trampa.
 */
public class IARival {

    private static final Lado YO = Lado.RIVAL;

    private final EstrategiaRival estrategia;

    public IARival(EstrategiaRival estrategia) {
        if (estrategia == null) {
            throw new ReglaInvalidaException("El rival necesita una estrategia.");
        }
        this.estrategia = estrategia;
    }

    public String getNombreEstrategia() {
        return estrategia.getNombreEstrategia();
    }

    /**
     * Hace una jugada si le toca. Devuelve la accion cantada, o null si tiro una carta
     * (o si no le tocaba).
     */
    public Accion jugar(MesaTruco mesa) {
        if (mesa.quienActua() != YO) {
            return null;
        }
        Accion decidida = decidir(mesa);
        if (decidida != null && mesa.ejecutar(YO, decidida)) {
            return decidida;
        }
        // Sin canto (o no se pudo): tira carta si puede; si no, la primera accion valida
        if (mesa.puedeJugarCarta(YO)) {
            tirarCarta(mesa);
            return null;
        }
        Lista<Accion> posibles = mesa.accionesDisponibles(YO);
        if (!posibles.estaVacia()) {
            Accion primera = posibles.obtener(0);
            mesa.ejecutar(YO, primera);
            return primera;
        }
        return null;
    }

    private Accion decidir(MesaTruco mesa) {
        Lista<Accion> posibles = mesa.accionesDisponibles(YO);
        Lista<Carta> mano = mesa.getCartas(YO);
        int envido = mesa.puntosEnvido(YO);
        boolean soyMano = mesa.getManoDeLaRonda() == YO;
        int bazas = mesa.bazasGanadas(YO);

        // 1) Me cantaron envido: subo con mucho, si no quiero o no quiero
        if (mesa.isEnvidoPendiente()) {
            if (envido >= 32 && tiene(posibles, Accion.FALTA_ENVIDO)) return Accion.FALTA_ENVIDO;
            if (envido >= 30 && tiene(posibles, Accion.REAL_ENVIDO)) return Accion.REAL_ENVIDO;
            boolean quiere = estrategia.quiereEnvido(envido, mesa.getPuntosEnvidoEnJuego(), soyMano);
            return quiere ? Accion.QUIERO : Accion.NO_QUIERO;
        }

        // 2) Me cantaron truco (o retruco...): "el envido esta primero", subir, querer o no
        Canto propuesto = mesa.getTrucoPropuesto();
        if (propuesto != null) {
            if (tiene(posibles, Accion.ENVIDO) && envido >= 27
                    && estrategia.quiereEnvido(envido, 2, soyMano)) {
                return Accion.ENVIDO;
            }
            boolean quiere = estrategia.quiereTruco(mano, propuesto.puntosAceptado(), bazas);
            Accion subir = accionDeTruco(posibles);
            if (quiere && subir != null && manoFuerte(mano, bazas)) return subir;
            return quiere ? Accion.QUIERO : Accion.NO_QUIERO;
        }

        // 3) Es mi turno: envido antes de tirar, truco si me da la mano, y si no, carta
        if (tiene(posibles, Accion.ENVIDO) && envido >= 26 && estrategia.quiereEnvido(envido, 2, soyMano)) {
            return (envido >= 31 && tiene(posibles, Accion.REAL_ENVIDO)) ? Accion.REAL_ENVIDO : Accion.ENVIDO;
        }
        Accion truco = accionDeTruco(posibles);
        if (truco != null && manoFuerte(mano, bazas)
                && estrategia.quiereTruco(mano, mesa.getValorRonda() + 1, bazas)) {
            return truco;
        }
        return null; // tirar carta
    }

    private void tirarCarta(MesaTruco mesa) {
        Lista<Carta> mano = mesa.getCartas(YO);
        Carta elegida = estrategia.elegirCarta(mano, mesa.getCartaEnMesa(Lado.JUGADOR), mesa.getBazaActual());
        int indice = (elegida == null) ? -1 : mano.indiceDe(elegida);
        mesa.jugarCarta(YO, indice >= 0 ? indice : 0);
    }

    /** "Mano fuerte": un 7 de oro o mejor, o ya gano una baza y tiene un 2 o mejor. */
    private boolean manoFuerte(Lista<Carta> mano, int bazasGanadas) {
        for (int i = 0; i < mano.tamanio(); i++) {
            int poder = mano.obtener(i).getPoder();
            if (poder >= 11 || (bazasGanadas >= 1 && poder >= 9)) {
                return true;
            }
        }
        return false;
    }

    private Accion accionDeTruco(Lista<Accion> posibles) {
        if (tiene(posibles, Accion.TRUCO)) return Accion.TRUCO;
        if (tiene(posibles, Accion.RETRUCO)) return Accion.RETRUCO;
        if (tiene(posibles, Accion.VALE_CUATRO)) return Accion.VALE_CUATRO;
        return null;
    }

    private boolean tiene(Lista<Accion> acciones, Accion accion) {
        return acciones.indiceDe(accion) >= 0;
    }
}
