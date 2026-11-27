package juego.reglas;

/**
 * Clase abstracta base para los cantos y desafios del Truco.
 * Cumple con el requisito de POO: clase abstracta con subclases concretas
 * que sobreescriben metodos y aportan polimorfismo real.
 */
public abstract class Canto {

    public abstract String getNombre();

    public abstract int puntosAceptado();

    public abstract int puntosRechazado();

    public abstract boolean esTruco();

    public abstract boolean esEnvido();

    public abstract Canto siguienteNivel();

    /**
     * Escalon del canto dentro de su escalera: Truco 1, Retruco 2, Vale Cuatro 3;
     * Envido 1, Real Envido 2, Falta Envido 3. Despues de un canto solo se puede subir
     * a uno de rango mayor (salvo el "envido envido", que se permite una vez).
     */
    public abstract int getRango();

    /**
     * true si al quererlo NO se suman los puntos de los cantos anteriores sino que el
     * canto vale por si solo (la Falta Envido). Por defecto, false.
     */
    public boolean reemplazaAcumulado() {
        return false;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
