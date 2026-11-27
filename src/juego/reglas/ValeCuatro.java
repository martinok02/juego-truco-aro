package juego.reglas;

public class ValeCuatro extends Canto {
    @Override
    public String getNombre() {
        return "Vale Cuatro";
    }

    @Override
    public int puntosAceptado() {
        return 4;
    }

    @Override
    public int puntosRechazado() {
        return 3;
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
        return null; // Nivel maximo de apuesta en truco
    }

    @Override
    public int getRango() {
        return 3;
    }
}
