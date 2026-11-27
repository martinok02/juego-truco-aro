package markov;

import arboles.ABB;
import estructuras.Lista;
import estructuras.MiLista;
import java.util.Random;

/**
 * Modelo de Cadenas de Markov de orden k parametrizable (k = 1 a 4).
 * Cumple con los requerimientos de la catedra:
 * - Tabla de transiciones almacenada en ABB propio (ABB<EstadoMarkov>).
 * - Orden k configurable entre 1 y 4.
 * - Sucesores con repetidos intencionales para reflejar probabilidades.
 * - Control de calidad: nunca genera vacio; bitacora explicita ante callejones sin salida.
 * - Sin uso de java.util prohibido (solo Random).
 */
public class ModeloMarkov {

    private final int k;
    private final ABB<EstadoMarkov> tablaEstados;
    private final Lista<String> estadosIniciales;
    private final Random azar;

    public static final String TOKEN_FIN = "<FIN>";

    public ModeloMarkov(int k) {
        if (k < 1 || k > 4) {
            throw new IllegalArgumentException("El orden k de Markov debe estar entre 1 y 4. Recibido: " + k);
        }
        this.k = k;
        this.tablaEstados = new ABB<>();
        this.estadosIniciales = new MiLista<>();
        this.azar = new Random();
    }

    public int getK() {
        return k;
    }

    public int cantidadEstados() {
        return tablaEstados.tamanio();
    }

    /**
     * Entrena el modelo con una secuencia de tokens de una oracion/dicho.
     */
    public void entrenarSecuencia(Lista<String> tokens) {
        if (tokens == null || tokens.tamanio() < 1) {
            return;
        }

        // Construir estado inicial con hasta k tokens
        StringBuilder sbInicial = new StringBuilder();
        int cantInicial = Math.min(k, tokens.tamanio());
        for (int i = 0; i < cantInicial; i++) {
            if (i > 0) sbInicial.append("|");
            sbInicial.append(tokens.obtener(i));
        }
        String claveInicial = sbInicial.toString();
        estadosIniciales.agregar(claveInicial);

        // Ventana deslizante de tamano k
        for (int i = 0; i < tokens.tamanio(); i++) {
            // Construir clave de estado actual con los ultimos min(k, i+1) tokens
            StringBuilder sb = new StringBuilder();
            int inicioVentana = Math.max(0, i - k + 1);
            for (int j = inicioVentana; j <= i; j++) {
                if (j > inicioVentana) sb.append("|");
                sb.append(tokens.obtener(j));
            }
            String claveActual = sb.toString();

            // Determinar sucesor
            String sucesor;
            if (i + 1 < tokens.tamanio()) {
                sucesor = tokens.obtener(i + 1);
            } else {
                sucesor = TOKEN_FIN;
            }

            // Buscar o crear estado en el ABB
            EstadoMarkov buscado = tablaEstados.buscar(new EstadoMarkov(claveActual));
            if (buscado == null) {
                buscado = new EstadoMarkov(claveActual);
                tablaEstados.insertar(buscado);
            }
            buscado.agregarSucesor(sucesor);
        }
    }

    /**
     * Genera una secuencia de tokens con control de calidad y bitacora, desde un inicio al azar.
     */
    public ResultadoGeneracion generar(int maxTokens) {
        return generar(maxTokens, null);
    }

    /**
     * Sobrecarga: genera una frase que EMPIEZA con el token "semilla" (por ejemplo "truco" o
     * "no quiero"), eligiendo al azar entre los inicios del corpus que arrancan asi. Sirve para
     * que el rival diga algo que tenga que ver con lo que esta pasando en la mesa. Si ninguna
     * frase del corpus empieza con esa semilla, se anota en la bitacora y se arranca al azar.
     */
    public ResultadoGeneracion generar(int maxTokens, String semilla) {
        Lista<String> resultadoTokens = new MiLista<>();
        Lista<String> bitacora = new MiLista<>();

        if (tablaEstados.estaVacio() || estadosIniciales.estaVacia()) {
            bitacora.agregar("Modelo vacio: se utiliza fallback por defecto.");
            resultadoTokens.agregar("truco");
            resultadoTokens.agregar("quiero");
            return new ResultadoGeneracion("truco quiero", resultadoTokens, bitacora);
        }

        // Seleccionar estado inicial: entre los que empiezan con la semilla, o cualquiera
        String claveActual = elegirInicio(semilla, bitacora);

        // Descomponer tokens del estado inicial
        Lista<String> partes = partesDeClave(claveActual);
        for (int i = 0; i < partes.tamanio(); i++) {
            resultadoTokens.agregar(partes.obtener(i));
        }

        boolean terminoSola = false; // por token de fin o por falta de sucesores
        while (resultadoTokens.tamanio() < maxTokens) {
            EstadoMarkov estado = tablaEstados.buscar(new EstadoMarkov(claveActual));

            if (estado != null && estado.tieneSucesores()) {
                Lista<String> sucesores = estado.getSucesores();
                int idxSucesor = azar.nextInt(sucesores.tamanio());
                String elegido = sucesores.obtener(idxSucesor);

                if (elegido.equals(TOKEN_FIN)) {
                    bitacora.agregar("Fin de secuencia alcanzado por token de corte.");
                    terminoSola = true;
                    break;
                }

                resultadoTokens.agregar(elegido);

                // Actualizar ventana deslizante para el siguiente estado
                claveActual = actualizarClaveVentana(claveActual, elegido);
            } else {
                // Callejón sin salida: control de calidad escalonado
                bitacora.agregar("Estado sin sucesores: [" + claveActual + "]");

                // 1. Intentar recortar a k-1
                if (claveActual.contains("|")) {
                    claveActual = claveActual.substring(claveActual.indexOf("|") + 1);
                    bitacora.agregar("Retroceso a contexto menor: [" + claveActual + "]");
                    continue;
                }

                // 2. Ni con un solo token hay sucesores: se cierra la frase como si viniera el
                //    token de fin (antes se "reiniciaba" pero se cortaba igual en la linea siguiente)
                bitacora.agregar("Sin sucesores ni con contexto minimo: se cierra la frase con token de fin.");
                terminoSola = true;
                break;
            }
        }

        // Largo maximo: si la frase no termino sola, se avisa (bitacora y "..." al mostrarla).
        // Excepcion: si lo unico que podia seguir era el token de fin, la frase ya estaba completa.
        boolean cortadaPorLargo = false;
        if (!terminoSola && resultadoTokens.tamanio() >= maxTokens) {
            EstadoMarkov ultimo = tablaEstados.buscar(new EstadoMarkov(claveActual));
            if (ultimo == null || !soloPuedeTerminar(ultimo)) {
                cortadaPorLargo = true;
                bitacora.agregar("Se alcanzo el largo maximo (" + maxTokens + " tokens): la frase se corta y se marca con \"...\".");
            }
        }

        // Garantizar que nunca quede vacio
        if (resultadoTokens.estaVacia()) {
            bitacora.agregar("Garantia de calidad: resultado vacio sustituido por fallback.");
            resultadoTokens.agregar("truco");
            resultadoTokens.agregar("quiero");
        }

        // Unir tokens en texto final
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < resultadoTokens.tamanio(); i++) {
            if (i > 0) texto.append(" ");
            texto.append(resultadoTokens.obtener(i));
        }

        return new ResultadoGeneracion(texto.toString(), resultadoTokens, bitacora, cortadaPorLargo);
    }

    /** true si el unico sucesor posible del estado es el token de fin (la frase ya termino). */
    private boolean soloPuedeTerminar(EstadoMarkov estado) {
        Lista<String> sucesores = estado.getSucesores();
        for (int i = 0; i < sucesores.tamanio(); i++) {
            if (!sucesores.obtener(i).equals(TOKEN_FIN)) {
                return false;
            }
        }
        return true;
    }

    private String actualizarClaveVentana(String claveVieja, String nuevoToken) {
        Lista<String> partes = partesDeClave(claveVieja);
        StringBuilder sb = new StringBuilder();
        int inicio = (partes.tamanio() >= k) ? 1 : 0;
        for (int i = inicio; i < partes.tamanio(); i++) {
            sb.append(partes.obtener(i)).append("|");
        }
        sb.append(nuevoToken);
        return sb.toString();
    }

    /** Separa una clave "tok1|tok2" en sus tokens (con la Lista propia, sin split ni arreglos). */
    private Lista<String> partesDeClave(String clave) {
        Lista<String> partes = new MiLista<>();
        int desde = 0;
        int barra = clave.indexOf('|');
        while (barra >= 0) {
            partes.agregar(clave.substring(desde, barra));
            desde = barra + 1;
            barra = clave.indexOf('|', desde);
        }
        partes.agregar(clave.substring(desde));
        return partes;
    }

    /** Estado inicial al azar; si hay semilla, solo entre los que empiezan con ese token. */
    private String elegirInicio(String semilla, Lista<String> bitacora) {
        if (semilla != null && !semilla.trim().isEmpty()) {
            String buscado = semilla.trim().toLowerCase();
            Lista<String> candidatos = new MiLista<>();
            for (int i = 0; i < estadosIniciales.tamanio(); i++) {
                String inicio = estadosIniciales.obtener(i);
                if (partesDeClave(inicio).obtener(0).equals(buscado)) {
                    candidatos.agregar(inicio);
                }
            }
            if (!candidatos.estaVacia()) {
                return candidatos.obtener(azar.nextInt(candidatos.tamanio()));
            }
            bitacora.agregar("Ninguna frase empieza con [" + buscado + "]: se arranca desde un inicio al azar.");
        }
        return estadosIniciales.obtener(azar.nextInt(estadosIniciales.tamanio()));
    }
}
