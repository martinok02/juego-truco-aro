package juego.reglas;

public class Retruco extends Canto {
    @Override
    public String getNombre() {
        return "Retruco";
    }

    @Override
    public int puntosAceptado() {
        return 3;
    }

    @Override
    public int puntosRechazado() {
        return 2;
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
        return new ValeCuatro();
    }

    @Override
    public int getRango() {
        return 2;
    }
}
