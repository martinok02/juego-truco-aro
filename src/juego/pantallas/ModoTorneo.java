package juego.pantallas;

import juego.reglas.EstrategiaArriesgada;
import juego.reglas.EstrategiaConservadora;
import juego.reglas.EstrategiaMentirosa;
import juego.reglas.IARival;
import juego.reglas.Partido;
import juego.reglas.Torneo;
import motor.Escena;
import persistencia.RepositorioLlave;

/** Partido del torneo: ganar sube en la llave; perder (o abandonar) te elimina. */
public class ModoTorneo implements ModoPartido {

    private final Torneo torneo;
    private final Partido partido;
    private String mensaje = "";

    public ModoTorneo(Torneo torneo) {
        if (torneo == null || torneo.getPartidoDelJugador() == null) {
            throw new IllegalArgumentException("No hay partido del torneo para jugar.");
        }
        this.torneo = torneo;
        this.partido = torneo.getPartidoDelJugador();
    }

    @Override
    public String getTitulo() {
        return "Torneo  ·  " + Torneo.nombreRonda(partido.getRonda()) + "  ·  vs " + torneo.getRivalActual();
    }

    @Override
    public String getNombreRival() {
        return torneo.getRivalActual();
    }

    @Override
    public PerfilRival getPerfilRival() {
        if (partido.getRonda() == 1) return PerfilRival.CONSERVADOR;
        if (partido.getRonda() == 2) return PerfilRival.ARRIESGADO;
        if (partido.getRonda() == 3) return PerfilRival.MENTIROSO;
        throw new IllegalStateException("Ronda de torneo desconocida: " + partido.getRonda());
    }

    /** Cuartos: prudente; semifinal: arriesgado; final: mentiroso. */
    @Override
    public IARival crearRival() {
        switch (getPerfilRival()) {
            case CONSERVADOR: return new IARival(new EstrategiaConservadora());
            case ARRIESGADO: return new IARival(new EstrategiaArriesgada());
            case MENTIROSO: return new IARival(new EstrategiaMentirosa());
            default: throw new IllegalStateException("Perfil de rival desconocido.");
        }
    }

    @Override
    public String registrarResultado(boolean gano) {
        mensaje = torneo.registrarResultado(gano);
        RepositorioLlave.guardarLlave(torneo.getLlave());
        return mensaje;
    }

    @Override
    public String registrarAbandono() {
        mensaje = "Abandonaste el partido. " + torneo.registrarResultado(false);
        RepositorioLlave.guardarLlave(torneo.getLlave());
        return mensaje;
    }

    @Override
    public String getAvisoAbandono() {
        return "Abandonar el partido cuenta como derrota: quedas eliminado del torneo.";
    }

    @Override
    public Escena escenaDeVuelta() {
        return new EscenaLlave(torneo, mensaje);
    }
}
