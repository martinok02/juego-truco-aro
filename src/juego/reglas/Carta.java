package juego.reglas;

public class Carta implements Comparable<Carta> {

    private final Palo palo;
    private final int numero;
    private final int poder;
    private final int valorEnvido;

    public Carta(int numero, Palo palo) {
        if (palo == null) {
            throw new ReglaInvalidaException("El palo no puede ser nulo.");
        }
        if (numero < 1 || numero > 12 || numero == 8 || numero == 9) {
            throw new ReglaInvalidaException("Numero de carta espanola invalido: " + numero);
        }
        this.numero = numero;
        this.palo = palo;
        this.poder = calcularPoder(numero, palo);
        this.valorEnvido = (numero >= 10) ? 0 : numero;
    }

    public Palo getPalo() {
        return palo;
    }

    public int getNumero() {
        return numero;
    }

    public int getPoder() {
        return poder;
    }

    public int getValorEnvido() {
        return valorEnvido;
    }

    private static int calcularPoder(int num, Palo p) {
        if (num == 1 && p == Palo.ESPADA) return 14;
        if (num == 1 && p == Palo.BASTO)  return 13;
        if (num == 7 && p == Palo.ESPADA) return 12;
        if (num == 7 && p == Palo.ORO)    return 11;
        if (num == 3) return 10;
        if (num == 2) return 9;
        if (num == 1) return 8; // 1 de oro o copa
        if (num == 12) return 7;
        if (num == 11) return 6;
        if (num == 10) return 5;
        if (num == 7) return 4; // 7 de basto o copa
        if (num == 6) return 3;
        if (num == 5) return 2;
        if (num == 4) return 1;
        return 0;
    }

    @Override
    public int compareTo(Carta otra) {
        if (otra == null) return 1;
        return Integer.compare(this.poder, otra.poder);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Carta c = (Carta) obj;
        return numero == c.numero && palo == c.palo;
    }

    @Override
    public int hashCode() {
        return 31 * numero + palo.hashCode();
    }

    @Override
    public String toString() {
        return numero + " de " + palo.name().toLowerCase();
    }
}
