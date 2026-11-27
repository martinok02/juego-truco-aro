package juego.pantallas;

import estructuras.Lista;
import estructuras.MiLista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import markov.ModeloMarkov;
import markov.ResultadoGeneracion;
import motor.Escena;
import persistencia.RepositorioCorpus;
import ui.Texto;

public class EscenaLaboratorioMarkov extends Escena {

    private final Escena retorno;
    private int kActual = 2;

    private ModeloMarkov modeloActual;
    private final Lista<String> ultimasMuestras = new MiLista<>();
    private final Lista<String> bitacoraMuestras = new MiLista<>();

    // Botones: uno por cada orden k (1 a 4) y los de abajo
    private final Lista<Boton> botonesK = new MiLista<>();
    private final Boton botonRegenerar = new Boton("REGENERAR", "G", Estilo.AZUL, 196, 536, 196, 42);
    private final Boton botonVolver = new Boton("VOLVER", "Esc", Estilo.MAGENTA, 408, 536, 196, 42);
    private int mouseX = -1;
    private int mouseY = -1;

    public EscenaLaboratorioMarkov(Escena retorno) {
        this.retorno = retorno;
        entrenarModelo(kActual);
        generarMuestras();
    }

    private void entrenarModelo(int k) {
        this.kActual = k;
        // Mismo vocabulario y corpus que usa el rival en la partida (RepositorioCorpus)
        this.modeloActual = RepositorioCorpus.crearModelo(k);
    }

    private void generarMuestras() {
        ultimasMuestras.vaciar();
        bitacoraMuestras.vaciar();

        for (int i = 0; i < 3; i++) {
            ResultadoGeneracion gen = modeloActual.generar(6);
            ultimasMuestras.agregar(gen.getTextoGenerado());
            if (!gen.getBitacora().estaVacia()) {
                for (int b = 0; b < gen.getBitacora().tamanio(); b++) {
                    bitacoraMuestras.agregar(gen.getBitacora().obtener(b));
                }
            }
        }
    }

    @Override
    public void actualizar(double dt) {
        mouseX = getEntrada().getMouseX();
        mouseY = getEntrada().getMouseY();
        boolean click = getEntrada().huboClick();

        // Orden k: teclas 1 a 4 o click en el boton (el boton i corresponde a k = i + 1)
        int kElegido = 0;
        if (getEntrada().fuePresionada(KeyEvent.VK_1)) kElegido = 1;
        if (getEntrada().fuePresionada(KeyEvent.VK_2)) kElegido = 2;
        if (getEntrada().fuePresionada(KeyEvent.VK_3)) kElegido = 3;
        if (getEntrada().fuePresionada(KeyEvent.VK_4)) kElegido = 4;
        for (int i = 0; i < botonesK().tamanio(); i++) {
            if (click && botonesK().obtener(i).contiene(mouseX, mouseY)) kElegido = i + 1;
        }
        if (kElegido > 0) {
            entrenarModelo(kElegido);
            generarMuestras();
        }

        if (getEntrada().fuePresionada(KeyEvent.VK_G) || getEntrada().fuePresionada(KeyEvent.VK_SPACE)
                || (click && botonRegenerar.contiene(mouseX, mouseY))) {
            generarMuestras();
        }

        if (getEntrada().fuePresionada(KeyEvent.VK_ENTER) || getEntrada().fuePresionada(KeyEvent.VK_ESCAPE)
                || (click && botonVolver.contiene(mouseX, mouseY))) {
            getContexto().irA(retorno != null ? retorno : new EscenaMenu());
        }
    }

    /** Crea los botones de k la primera vez que se piden (el constructor ya entrena el modelo). */
    private Lista<Boton> botonesK() {
        if (botonesK.estaVacia()) {
            for (int k = 1; k <= 4; k++) {
                botonesK.agregar(new Boton("k = " + k, null, Estilo.AZUL, 250 + (k - 1) * 88, 104, 78, 34));
            }
        }
        return botonesK;
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();

        Estilo.fondo(g, ancho, alto);
        Estilo.titulo(g, "Laboratorio Markov", ancho / 2.0, 52, 34);
        Texto.centrado(g, "Modelo de orden k sobre ABB propio  ·  entrenado con datos/dichos.txt", ancho / 2.0, 78, 12,
                Estilo.TEXTO_SUAVE);

        // Selector de k
        Texto.izquierda(g, "ORDEN", 176, 127, 12, Estilo.TEXTO_SUAVE);
        for (int i = 0; i < botonesK().tamanio(); i++) {
            botonesK().obtener(i).dibujarOpcion(g, i + 1 == kActual);
        }
        Estilo.pildora(g, modeloActual.cantidadEstados() + " estados en el ABB", 676, 121, 11,
                new Color(10, 14, 30, 190), Estilo.DORADO);

        // Frases generadas: la "IA criolla" habla en globos
        Estilo.panel(g, 40, 156, 720, 212);
        Texto.izquierda(g, "FRASES GENERADAS CON k = " + kActual, 60, 182, 12, Estilo.DORADO);
        Estilo.avatar(g, 84, 280, 26, "IA", new Color(150, 60, 160));
        for (int i = 0; i < ultimasMuestras.tamanio(); i++) {
            int y = 198 + i * 54;
            Estilo.globo(g, 130, y, 610, 42, 114, y + 21);
            String frase = "\"" + ultimasMuestras.obtener(i).toUpperCase() + "\"";
            Texto.izquierda(g, Estilo.recortar(g, frase, 15, 580), 148, y + 27, 15, new Color(30, 30, 40));
        }

        // Bitacora de control de calidad (callejones sin salida y retrocesos del modelo)
        Estilo.panel(g, 40, 380, 720, 140);
        Texto.izquierda(g, "BITACORA DE CONTROL DE CALIDAD", 60, 406, 12, Estilo.CARAMELO);
        if (bitacoraMuestras.estaVacia()) {
            Texto.izquierda(g, "Sin retrocesos: todas las transiciones tuvieron sucesor.", 60, 436, 13, Estilo.TEXTO_SUAVE);
        } else {
            for (int i = 0; i < Math.min(3, bitacoraMuestras.tamanio()); i++) {
                Texto.izquierda(g, Estilo.recortar(g, "› " + bitacoraMuestras.obtener(i), 12, 680), 60, 436 + i * 26, 12,
                        new Color(255, 196, 150));
            }
        }

        botonRegenerar.dibujar(g, botonRegenerar.contiene(mouseX, mouseY), true);
        botonVolver.dibujar(g, botonVolver.contiene(mouseX, mouseY), true);

        Notificaciones.dibujar(g, getContexto().getAncho()); // avisos de datos/ encima de todo
    }
}
