package juego.reglas;

import arboles.ArbolBinario;
import arboles.NodoBinario;
import estructuras.Lista;
import estructuras.MiLista;

/**
 * La gira por Argentina: un arbol general (pais -> regiones -> provincias) guardado como
 * ARBOL BINARIO con la representacion "hijo izquierdo = primer hijo, hijo derecho = siguiente
 * hermano". Asi:
 *
 *            Argentina
 *            /
 *         Noreste ------------------> Noroeste --------> Centro
 *          /                            /                  /
 *     Formosa -> Chaco -> ...       Jujuy -> Salta ...   Cordoba -> ...
 *
 * El recorrido PRE-ORDEN de ese arbol da exactamente el orden de la gira:
 * Argentina, Noreste, Formosa, Chaco, Corrientes, Misiones, Noroeste, Jujuy, ...
 *
 * Reglas: solo se puede jugar la primera provincia no ganada (no se saltean puestos).
 * Cada region da 6 vidas; perder o abandonar un partido descuenta una y se vuelve a jugar la
 * misma provincia. Si las vidas llegan a 0, la region entera vuelve a empezar.
 */
public class Gira {

    public static final int VIDAS_POR_REGION = 6;

    private final ArbolBinario<Etapa> arbol;
    private int vidas;

    public Gira(ArbolBinario<Etapa> arbol, int vidas) {
        if (arbol == null || arbol.estaVacio()) {
            throw new ReglaInvalidaException("La gira necesita un arbol con etapas.");
        }
        if (vidas < 1 || vidas > VIDAS_POR_REGION) {
            throw new ReglaInvalidaException("Las vidas van de 1 a " + VIDAS_POR_REGION + ": " + vidas);
        }
        this.arbol = arbol;
        this.vidas = vidas;
    }

    /** Gira nueva: Noreste, Noroeste y Centro, sin ninguna provincia ganada. */
    public static Gira nueva() {
        NodoBinario<Etapa> argentina = nodo(Etapa.Tipo.PAIS, "argentina", "Argentina");
        NodoBinario<Etapa> nea = region("noreste", "Noreste (NEA)",
                "formosa", "Formosa", "chaco", "Chaco", "corrientes", "Corrientes", "misiones", "Misiones", null, null);
        NodoBinario<Etapa> noa = region("noroeste", "Noroeste (NOA)",
                "jujuy", "Jujuy", "salta", "Salta", "tucuman", "Tucuman", "catamarca", "Catamarca",
                "santiago del estero", "Santiago del Estero");
        NodoBinario<Etapa> centro = region("centro", "Centro",
                "cordoba", "Cordoba", "santa fe", "Santa Fe", "entre rios", "Entre Rios", "buenos aires", "Buenos Aires", null, null);
        argentina.setIzquierdo(nea); // primer hijo
        nea.setDerecho(noa);         // hermanos: las regiones en orden
        noa.setDerecho(centro);
        return new Gira(new ArbolBinario<>(argentina), VIDAS_POR_REGION);
    }

    private static NodoBinario<Etapa> nodo(Etapa.Tipo tipo, String clave, String nombre) {
        return new NodoBinario<>(new Etapa(tipo, clave, nombre));
    }

    /** Region con hasta 5 provincias encadenadas como hermanas (las claves null se ignoran). */
    private static NodoBinario<Etapa> region(String clave, String nombre,
                                             String c1, String n1, String c2, String n2, String c3, String n3,
                                             String c4, String n4, String c5, String n5) {
        NodoBinario<Etapa> region = nodo(Etapa.Tipo.REGION, clave, nombre);
        NodoBinario<Etapa> anterior = null;
        for (int i = 0; i < 5; i++) {
            String c = (i == 0) ? c1 : (i == 1) ? c2 : (i == 2) ? c3 : (i == 3) ? c4 : c5;
            String n = (i == 0) ? n1 : (i == 1) ? n2 : (i == 2) ? n3 : (i == 3) ? n4 : n5;
            if (c == null) {
                continue;
            }
            NodoBinario<Etapa> provincia = nodo(Etapa.Tipo.PROVINCIA, c, n);
            if (anterior == null) {
                region.setIzquierdo(provincia); // primera provincia = hijo izquierdo
            } else {
                anterior.setDerecho(provincia); // siguientes = hermanos (derecha)
            }
            anterior = provincia;
        }
        return region;
    }

    // ------------------------------------------------------------------ consultas

    public ArbolBinario<Etapa> getArbol() {
        return arbol;
    }

    public int getVidas() {
        return vidas;
    }

    /** Todas las etapas en el orden de la gira (pre-orden del arbol). */
    public Lista<Etapa> recorrido() {
        return arbol.preOrden();
    }

    /** Solo las provincias, en el orden en que se juegan. */
    public Lista<Etapa> provincias() {
        Lista<Etapa> todas = recorrido();
        Lista<Etapa> provincias = new MiLista<>();
        for (int i = 0; i < todas.tamanio(); i++) {
            if (todas.obtener(i).getTipo() == Etapa.Tipo.PROVINCIA) {
                provincias.agregar(todas.obtener(i));
            }
        }
        return provincias;
    }

    /** Las regiones en orden. */
    public Lista<Etapa> regiones() {
        Lista<Etapa> todas = recorrido();
        Lista<Etapa> regiones = new MiLista<>();
        for (int i = 0; i < todas.tamanio(); i++) {
            if (todas.obtener(i).getTipo() == Etapa.Tipo.REGION) {
                regiones.agregar(todas.obtener(i));
            }
        }
        return regiones;
    }

    /** La region de una provincia: la ultima region que aparece antes en el pre-orden. */
    public Etapa regionDe(Etapa provincia) {
        Lista<Etapa> todas = recorrido();
        Etapa region = null;
        for (int i = 0; i < todas.tamanio(); i++) {
            Etapa e = todas.obtener(i);
            if (e.getTipo() == Etapa.Tipo.REGION) region = e;
            if (e.equals(provincia)) return region;
        }
        return null;
    }

    /** Provincias de una region, en orden. */
    public Lista<Etapa> provinciasDe(Etapa region) {
        Lista<Etapa> resultado = new MiLista<>();
        Lista<Etapa> provincias = provincias();
        for (int i = 0; i < provincias.tamanio(); i++) {
            if (regionDe(provincias.obtener(i)).equals(region)) {
                resultado.agregar(provincias.obtener(i));
            }
        }
        return resultado;
    }

    /** La provincia que toca jugar: la primera no ganada (null si la gira esta completa). */
    public Etapa getProvinciaActual() {
        Lista<Etapa> provincias = provincias();
        for (int i = 0; i < provincias.tamanio(); i++) {
            if (!provincias.obtener(i).isCompletada()) {
                return provincias.obtener(i);
            }
        }
        return null;
    }

    /** La ultima provincia ganada (de donde viene el recorrido), o null si no hay ninguna. */
    public Etapa getProvinciaAnterior() {
        Etapa anterior = null;
        Lista<Etapa> provincias = provincias();
        for (int i = 0; i < provincias.tamanio() && provincias.obtener(i).isCompletada(); i++) {
            anterior = provincias.obtener(i);
        }
        return anterior;
    }

    public Etapa getRegionActual() {
        Etapa actual = getProvinciaActual();
        return actual == null ? null : regionDe(actual);
    }

    /** Solo se puede jugar la provincia actual: no se saltean puestos. */
    public boolean sePuedeJugar(Etapa provincia) {
        return provincia != null && provincia.equals(getProvinciaActual());
    }

    public boolean isCompleta() {
        return getProvinciaActual() == null;
    }

    public boolean regionCompleta(Etapa region) {
        Lista<Etapa> provincias = provinciasDe(region);
        for (int i = 0; i < provincias.tamanio(); i++) {
            if (!provincias.obtener(i).isCompletada()) return false;
        }
        return true;
    }

    /** Hay algo guardado que valga la pena continuar. */
    public boolean tieneProgreso() {
        return getProvinciaAnterior() != null || vidas < VIDAS_POR_REGION;
    }

    // ------------------------------------------------------------------ resultados

    /** Gano la provincia actual. Devuelve el mensaje para mostrar. */
    public String registrarVictoria() {
        Etapa provincia = getProvinciaActual();
        if (provincia == null) {
            return "La gira ya esta completa.";
        }
        Etapa region = regionDe(provincia);
        provincia.setCompletada(true);
        if (isCompleta()) {
            return "¡Ganaste en " + provincia.getNombre() + " y completaste la gira por Argentina!";
        }
        if (regionCompleta(region)) {
            vidas = VIDAS_POR_REGION;
            return "¡Completaste el " + region.getNombre() + "! Ahora vamos por el " + getRegionActual().getNombre() + ".";
        }
        return "¡Ganaste en " + provincia.getNombre() + "! Sigue " + getProvinciaActual().getNombre() + ".";
    }

    /** Perdio (o abandono) el partido de la provincia actual: una vida menos. */
    public String registrarDerrota() {
        Etapa provincia = getProvinciaActual();
        if (provincia == null) {
            return "La gira ya esta completa.";
        }
        vidas--;
        if (vidas > 0) {
            return "Perdiste una vida en " + provincia.getNombre() + ". Te quedan " + vidas + ".";
        }
        // Sin vidas: la region vuelve a empezar
        Etapa region = regionDe(provincia);
        Lista<Etapa> provincias = provinciasDe(region);
        for (int i = 0; i < provincias.tamanio(); i++) {
            provincias.obtener(i).setCompletada(false);
        }
        vidas = VIDAS_POR_REGION;
        return "Te quedaste sin vidas: el " + region.getNombre() + " empieza de nuevo desde "
                + provincias.obtener(0).getNombre() + ".";
    }
}
