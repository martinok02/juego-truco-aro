package juego.reglas;

public class FaltaEnvido extends Canto {
    @Override
    public String getNombre() {
        return "Falta Envido";
    }

    @Override
    public int puntosAceptado() {
        // No se usa: su valor depende del tanteador ("lo que le falta al que va ganando")
        // y lo calcula MesaTruco.puntosFaltaEnvido() al resolver el envido.
        return 0;
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
        return null;
    }

    @Override
    public int getRango() {
        return 3;
    }

    /** La falta envido no se suma a lo cantado antes: vale "lo que falta" (lo calcula MesaTruco). */
    @Override
    public boolean reemplazaAcumulado() {
        return true;
    }
}
