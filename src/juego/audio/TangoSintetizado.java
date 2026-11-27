package juego.audio;

import estructuras.Lista;
import estructuras.ListaPrioridad;
import estructuras.MiLista;
import persistencia.FuenteDeMuestras;

/**
 * Tango instrumental original (La menor, Mi siete, Re menor) generado nota por nota.
 *
 * En vez de calcular todo el sonido en un arreglo, las notas esperan en una ListaPrioridad
 * ordenada por el momento en que empiezan. Al pedir cada muestra, las notas que ya empezaron
 * pasan a una Lista de "activas", se suman, y las que terminaron se sacan.
 */
public class TangoSintetizado implements FuenteDeMuestras {

    private static final int MUESTRAS_POR_SEGUNDO = 22050;
    private static final double TEMPO = 80.0;
    private static final int COMPASES = 16;
    private static final double SEGUNDOS_POR_PULSO = 60.0 / TEMPO;

    /** Una nota: cuando empieza, cuanto dura, que frecuencia tiene y si es del bajo. */
    private static final class Nota {
        private final int inicio;
        private final int muestrasNota;
        private final int muestrasCola;
        private final double frecuencia;
        private final double volumen;
        private final boolean bajo;
        private final double duracionSegundos;

        Nota(double pulso, double duracionPulsos, int numeroMidi, double volumen, boolean bajo) {
            this.inicio = (int) Math.round(pulso * SEGUNDOS_POR_PULSO * MUESTRAS_POR_SEGUNDO);
            this.duracionSegundos = duracionPulsos * SEGUNDOS_POR_PULSO;
            this.muestrasNota = (int) (duracionSegundos * MUESTRAS_POR_SEGUNDO);
            this.muestrasCola = MUESTRAS_POR_SEGUNDO / 9;
            this.frecuencia = 440.0 * Math.pow(2.0, (numeroMidi - 69) / 12.0);
            this.volumen = volumen;
            this.bajo = bajo;
        }

        int fin() {
            return inicio + muestrasNota + muestrasCola;
        }

        /** Aporte de la nota en la muestra i (bajo: tono grave que se apaga; resto: con vibrato). */
        double valor(int i) {
            double tiempo = (i - inicio) / (double) MUESTRAS_POR_SEGUNDO;
            double envolvente = Math.min(1, tiempo / (bajo ? 0.008 : 0.028));
            if (i - inicio > muestrasNota) {
                envolvente *= Math.max(0, 1 - (i - inicio - muestrasNota) / (double) muestrasCola);
            }
            double fase = 2 * Math.PI * frecuencia * tiempo;
            double tono;
            if (bajo) {
                tono = Math.sin(fase) * 0.78 + Math.sin(fase * 2) * 0.16 + Math.sin(fase * 3) * 0.06;
                envolvente *= Math.exp(-3.2 * tiempo / duracionSegundos);
            } else {
                fase *= 1 + 0.004 * Math.sin(2 * Math.PI * 5.2 * tiempo);
                tono = Math.sin(fase) * 0.58 + Math.sin(fase * 1.006) * 0.18
                        + Math.sin(fase * 2) * 0.16 + Math.sin(fase * 3) * 0.06 + Math.sin(fase * 4) * 0.02;
                envolvente *= 0.84 + 0.16 * Math.sin(2 * Math.PI * 4.4 * tiempo);
            }
            return tono * envolvente * volumen;
        }
    }

    private final int totalMuestras = (int) (COMPASES * 2 * SEGUNDOS_POR_PULSO * MUESTRAS_POR_SEGUNDO);
    private final int fundido = MUESTRAS_POR_SEGUNDO / 12;
    private ListaPrioridad<Nota> pendientes;
    private final Lista<Nota> activas = new MiLista<>();
    private int actual;
    private double escala = 1;

    public TangoSintetizado() {
        // Primera pasada: se busca el pico para que la musica suene a un volumen parejo
        reiniciar();
        double pico = 0;
        for (int i = 0; i < totalMuestras; i++) {
            pico = Math.max(pico, Math.abs(mezclaCruda()));
        }
        escala = (pico == 0) ? 0 : 0.3 / pico;
        reiniciar();
    }

    @Override
    public int getMuestrasPorSegundo() {
        return MUESTRAS_POR_SEGUNDO;
    }

    @Override
    public int getCantidadMuestras() {
        return totalMuestras;
    }

    @Override
    public void reiniciar() {
        actual = 0;
        activas.vaciar();
        pendientes = new ListaPrioridad<>();
        componer();
    }

    @Override
    public double siguienteMuestra() {
        int i = actual;
        double valor = mezclaCruda() * escala;
        if (i < fundido) valor *= i / (double) fundido;                                  // entrada suave
        if (i >= totalMuestras - fundido) valor *= (totalMuestras - i) / (double) fundido; // salida suave
        return valor;
    }

    /** Suma las notas que suenan en la muestra actual y avanza una muestra. */
    private double mezclaCruda() {
        while (!pendientes.estaVacia() && pendientes.verMin().getPrioridad() <= actual) {
            activas.agregar(pendientes.desencolarMin().getElemento());
        }
        double suma = 0;
        for (int j = activas.tamanio() - 1; j >= 0; j--) {
            Nota nota = activas.obtener(j);
            if (actual >= nota.fin() || actual >= totalMuestras) {
                activas.eliminar(j);
            } else {
                suma += nota.valor(actual);
            }
        }
        actual++;
        return suma;
    }

    /** Las mismas notas del tango original: bajo, acordes a contratiempo y melodia. */
    private void componer() {
        for (int compas = 0; compas < COMPASES; compas++) {
            int acorde = acordeDe(compas % 8);
            int base = notaBajo(acorde);
            double inicioCompas = compas * 2.0;

            agregar(new Nota(inicioCompas, 0.72, base, 0.19, true));
            agregar(new Nota(inicioCompas + 1.0, 0.48, base + 7, 0.09, true));
            for (int pulso = 0; pulso < 2; pulso++) {
                for (int voz = 0; voz < 4; voz++) {
                    int nota = notaAcorde(acorde, voz);
                    if (nota >= 0) agregar(new Nota(inicioCompas + pulso + 0.5, 0.34, nota, 0.035, false));
                }
            }
            for (int paso = 0; paso < 4; paso++) {
                agregar(new Nota(inicioCompas + paso * 0.5, paso == 3 ? 0.42 : 0.39, notaMelodia(compas % 8, paso), 0.12, false));
            }
        }
    }

    private void agregar(Nota nota) {
        pendientes.encolar(nota, nota.inicio); // ordenadas por cuando empiezan
    }

    private static int acordeDe(int compas) {
        switch (compas) {
            case 1: case 4: case 7: return 1; // Mi siete
            case 3: case 6: return 2;         // Re menor
            default: return 0;                // La menor
        }
    }

    private static int notaBajo(int acorde) {
        switch (acorde) {
            case 1: return 40;
            case 2: return 38;
            default: return 45;
        }
    }

    private static int notaAcorde(int acorde, int voz) {
        if (acorde == 1) return elegir(voz, 52, 56, 59, 62);
        if (acorde == 2) return elegir(voz, 50, 53, 57, 60);
        return elegir(voz, 57, 60, 64, -1);
    }

    private static int notaMelodia(int compas, int paso) {
        switch (compas) {
            case 0: return elegir(paso, 69, 72, 76, 72);
            case 1: return elegir(paso, 71, 74, 76, 71);
            case 2: return elegir(paso, 72, 76, 81, 76);
            case 3: return elegir(paso, 65, 69, 74, 72);
            case 4: return elegir(paso, 71, 68, 65, 64);
            case 5: return elegir(paso, 69, 72, 76, 81);
            case 6: return elegir(paso, 67, 65, 64, 60);
            default: return elegir(paso, 74, 73, 72, 71);
        }
    }

    private static int elegir(int indice, int primera, int segunda, int tercera, int cuarta) {
        switch (indice) {
            case 0: return primera;
            case 1: return segunda;
            case 2: return tercera;
            default: return cuarta;
        }
    }
}
