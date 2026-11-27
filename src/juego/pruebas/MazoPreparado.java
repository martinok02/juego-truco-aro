package juego.pruebas;

import estructuras.Cola;
import estructuras.Lista;
import estructuras.MiCola;
import estructuras.MiLista;
import juego.reglas.Carta;
import juego.reglas.Mazo;
import juego.reglas.Palo;

/**
 * Mazo para pruebas: reparte manos que se le cargan antes, en orden (Cola: la primera
 * mano cargada es la primera que sale). Cuando se terminan, se comporta como un mazo comun.
 *
 * Herencia y polimorfismo: MesaTruco recibe un Mazo y no sabe si es este o el comun;
 * llama a mezclar() y repartirMano() y cada clase responde a su manera.
 */
public class MazoPreparado extends Mazo {

    private final Cola<Lista<Carta>> manos = new MiCola<>();

    /** Carga una mano de tres cartas. Orden de reparto: primero el jugador, despues el rival. */
    public MazoPreparado mano(Carta a, Carta b, Carta c) {
        Lista<Carta> mano = new MiLista<>();
        mano.agregar(a);
        mano.agregar(b);
        mano.agregar(c);
        manos.encolar(mano);
        return this;
    }

    @Override
    public void mezclar() {
        if (manos.estaVacia()) {
            super.mezclar();
        }
    }

    @Override
    public Lista<Carta> repartirMano() {
        if (manos.estaVacia()) {
            return super.repartirMano();
        }
        return manos.desencolar();
    }

    /** Atajo para escribir cartas en las pruebas: c(1, ESPADA) = ancho de espadas. */
    public static Carta c(int numero, Palo palo) {
        return new Carta(numero, palo);
    }
}
