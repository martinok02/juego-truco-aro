package juego.reglas;

public class Truco extends Canto {
    @Override
    public String getNombre() {
        return "Truco";
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
        return true;
    }

    @Override
    public boolean esEnvido() {
        return false;
    }

    @Override
    public Canto siguienteNivel() {
        return new Retruco();
    }

    @Override
    public int getRango() {
        return 1;
    }
}
