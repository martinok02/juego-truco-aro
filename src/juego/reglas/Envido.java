package juego.reglas;

public class Envido extends Canto {
    @Override
    public String getNombre() {
        return "Envido";
    }

    @Override
    public int puntosAceptado() {
        return 2;
    }

    @Override
    public int puntosRechazado() {
        return 1;
    }

    @Override
    public boolean esTruco() {
        return false;
    }

    @Override
    public boolean esEnvido() {
        return true;
    }

    @Override
    public Canto siguienteNivel() {
        return new RealEnvido();
    }

    @Override
    public int getRango() {
        return 1;
    }
}
