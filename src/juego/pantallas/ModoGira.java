package juego.pantallas;

import juego.reglas.Etapa;
import juego.reglas.EstrategiaArriesgada;
import juego.reglas.EstrategiaConservadora;
import juego.reglas.EstrategiaMentirosa;
import juego.reglas.Gira;
import juego.reglas.IARival;
import motor.Escena;
import persistencia.RepositorioGira;

/** Partido de la gira: se juega la provincia actual; ganar avanza y perder cuesta una vida. */
public class ModoGira implements ModoPartido {

    private final Gira gira;
    private final Etapa provincia;
    private Etapa ganada = null;   // para animar el recorrido al volver al mapa
    private String mensaje = "";

    public ModoGira(Gira gira) {
        if (gira == null || gira.isCompleta()) {
            throw new IllegalArgumentException("No hay provincia para jugar en la gira.");
        }
        this.gira = gira;
        this.provincia = gira.getProvinciaActual();
    }

    @Override
    public String getTitulo() {
        return "Gira  ·  " + provincia.getNombre() + "  ·  " + gira.regionDe(provincia).getNombre();
    }

    @Override
    public String getNombreRival() {
        return "Rival de " + provincia.getNombre();
    }

    @Override
    public PerfilRival getPerfilRival() {
        int region = gira.regiones().indiceDe(gira.regionDe(provincia));
        if (region == 0) return PerfilRival.CONSERVADOR;
        if (region == 1) return PerfilRival.ARRIESGADO;
        return PerfilRival.MENTIROSO;
    }

    /** La dificultad sube con la region: el Noreste es prudente, el Noroeste arriesga y el Centro miente. */
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
        if (gano) {
            ganada = provincia;
            mensaje = gira.registrarVictoria();
        } else {
            mensaje = gira.registrarDerrota();
        }
        RepositorioGira.guardar(gira);
        return mensaje;
    }

    @Override
    public String registrarAbandono() {
        mensaje = "Abandonaste en " + provincia.getNombre() + ". " + gira.registrarDerrota();
        RepositorioGira.guardar(gira);
        return mensaje;
    }

    @Override
    public String getAvisoAbandono() {
        return "Abandonar el partido te descuenta una vida (te quedan " + gira.getVidas() + ").";
    }

    @Override
    public Escena escenaDeVuelta() {
        return new EscenaGira(gira, ganada, mensaje);
    }
}
