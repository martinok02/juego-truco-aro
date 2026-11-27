package juego.reglas;

public class RealEnvido extends Canto {
    @Override
    public String getNombre() {
        return "Real Envido";
    }

    @Override
    public int puntosAceptado() {
        return 3;
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
        return new FaltaEnvido();
    }

    @Override
    public int getRango() {
        return 2;
    }
}
