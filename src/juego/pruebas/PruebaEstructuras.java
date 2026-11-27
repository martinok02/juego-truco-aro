package juego.pruebas;

import arboles.ABB;
import arboles.ArbolBinario;
import estructuras.Cola;
import estructuras.Lista;
import estructuras.MiCola;
import estructuras.MiLista;
import estructuras.MiPila;
import estructuras.Pila;
import grafos.Grafo;
import grafos.ResultadoDijkstra;
import markov.ModeloMarkov;
import markov.ResultadoGeneracion;
import tokens.Trie;

/**
 * Bateria integral de pruebas unitarias para todas las estructuras de datos propias.
 * Cubre las 20 comprobaciones exigidas por la rubrica.
 */
public class PruebaEstructuras {

    private static int pruebasSuperadas = 0;
    private static int totalPruebas = 0;

    private static void afirmar(boolean condicion, String nombrePrueba) {
        totalPruebas++;
        if (condicion) {
            pruebasSuperadas++;
            System.out.println("  [OK] " + nombrePrueba);
        } else {
            System.err.println("  [FALLO] " + nombrePrueba);
        }
    }

    public static void ejecutarTodas() {
        System.out.println("==================================================");
        System.out.println("   INICIANDO BATERIA DE PRUEBAS DE ESTRUCTURAS    ");
        System.out.println("==================================================");

        probarTADsLineales();
        probarArbolBinario();
        probarABB();
        probarTrie();
        probarGrafo();
        probarMarkov();
        probarPersistencia();

        System.out.println("==================================================");
        System.out.println("Resultado: " + pruebasSuperadas + " de " + totalPruebas + " pruebas pasadas.");
        if (pruebasSuperadas == totalPruebas) {
            System.out.println("TODAS LAS PRUEBAS PASARON EXITOSAMENTE (100%).");
        } else {
            System.err.println("ATENCION: Existen pruebas que no pasaron.");
        }
        System.out.println("==================================================");
    }

    private static void probarTADsLineales() {
        System.out.println("\n--- 1. TADs Lineales (MiLista, MiPila, MiCola) ---");

        // MiLista
        Lista<String> lista = new MiLista<>();
        afirmar(lista.estaVacia(), "MiLista nueva esta vacia");
        lista.agregar("A");
        lista.agregar("B");
        lista.agregar("C");
        afirmar(lista.tamanio() == 3, "MiLista tamanio correcto");
        afirmar(lista.obtener(1).equals("B"), "MiLista obtener elemento intermedio");
        lista.eliminar(1);
        afirmar(lista.tamanio() == 2 && lista.obtener(1).equals("C"), "MiLista eliminar elemento");

        // MiPila
        Pila<Integer> pila = new MiPila<>();
        pila.apilar(10);
        pila.apilar(20);
        afirmar(pila.tope() == 20, "MiPila tope");
        afirmar(pila.desapilar() == 20, "MiPila desapilar LIFO");
        afirmar(pila.desapilar() == 10 && pila.estaVacia(), "MiPila vacia tras desapilar todo");

        // MiCola
        Cola<Integer> cola = new MiCola<>();
        cola.encolar(1);
        cola.encolar(2);
        afirmar(cola.frente() == 1, "MiCola frente");
        afirmar(cola.desencolar() == 1, "MiCola desencolar FIFO");
        afirmar(cola.desencolar() == 2 && cola.estaVacia(), "MiCola vacia tras desencolar todo");
    }

    private static void probarArbolBinario() {
        System.out.println("\n--- 2. Arbol Binario Generico ---");

        ArbolBinario<String> arbolVacio = new ArbolBinario<>();
        afirmar(arbolVacio.estaVacio(), "Arbol vacio");
        afirmar(arbolVacio.altura() == 0, "Altura de arbol vacio es 0");
        afirmar(arbolVacio.cantidadNodos() == 0, "Nodos de arbol vacio es 0");
        afirmar(arbolVacio.cantidadHojas() == 0, "Hojas de arbol vacio es 0");

        ArbolBinario<String> arbol = new ArbolBinario<>();
        // Insertar 7 nodos por niveles (arbol completo de 3 niveles)
        arbol.insertar("Final");       // Raiz
        arbol.insertar("Semi 1");      // Hijos de raiz
        arbol.insertar("Semi 2");
        arbol.insertar("Cuartos 1");   // Hojas
        arbol.insertar("Cuartos 2");
        arbol.insertar("Cuartos 3");
        arbol.insertar("Cuartos 4");

        afirmar(arbol.altura() == 3, "Altura arbol de 7 nodos es 3");
        afirmar(arbol.cantidadNodos() == 7, "Cantidad de nodos es 7");
        afirmar(arbol.cantidadHojas() == 4, "Cantidad de hojas es 4");
        afirmar(arbol.buscar("Semi 1"), "Buscar existente devuelve true");
        afirmar(!arbol.buscar("Inexistente"), "Buscar inexistente devuelve false");

        // Recorridos devolviendo Lista propia
        Lista<String> niveles = arbol.porNiveles();
        afirmar(niveles.tamanio() == 7 && niveles.obtener(0).equals("Final"), "Recorrido por niveles correcto");

        Lista<String> pre = arbol.preOrden();
        afirmar(pre.tamanio() == 7 && pre.obtener(0).equals("Final"), "Pre-orden correcto");

        Lista<String> in = arbol.inOrden();
        afirmar(in.tamanio() == 7, "In-orden devuelve lista con 7 elementos");

        Lista<String> post = arbol.postOrden();
        afirmar(post.tamanio() == 7 && post.obtener(6).equals("Final"), "Post-orden visita raiz al final");
    }

    private static void probarABB() {
        System.out.println("\n--- 3. Arbol Binario de Busqueda (ABB) ---");

        ABB<Integer> abb = new ABB<>();
        abb.insertar(50);
        abb.insertar(30);
        abb.insertar(70);
        abb.insertar(20);
        abb.insertar(40);
        abb.insertar(60);
        abb.insertar(80);

        afirmar(abb.tamanio() == 7, "ABB tamanio 7 tras inserciones");
        afirmar(abb.contiene(40), "ABB contiene elemento existente");
        afirmar(!abb.contiene(99), "ABB no contiene elemento inexistente");

        // Recorrido In-Orden Iterativo con Pila
        Lista<Integer> ordenado = abb.inOrdenIterativo();
        boolean estaOrdenado = true;
        for (int i = 0; i < ordenado.tamanio() - 1; i++) {
            if (ordenado.obtener(i) >= ordenado.obtener(i + 1)) {
                estaOrdenado = false;
                break;
            }
        }
        afirmar(estaOrdenado && ordenado.tamanio() == 7, "In-Orden Iterativo con Pila produce secuencia ordenada");

        // Eliminacion Caso 1: Hoja (20)
        boolean elimHoja = abb.eliminar(20);
        afirmar(elimHoja && !abb.contiene(20) && abb.tamanio() == 6, "Eliminar hoja (20)");

        // Eliminacion Caso 2: Un hijo (si eliminamos 70 despues de eliminar 80, o preparamos un nodo con 1 hijo)
        abb.insertar(25); // 30 tiene 25 y 40. Si eliminamos 25, 30 tiene solo 40
        abb.eliminar(40);
        afirmar(abb.eliminar(30) && !abb.contiene(30), "Eliminar nodo con un hijo (30)");

        // Eliminacion Caso 3: Dos hijos (50 - raiz)
        afirmar(abb.eliminar(50) && !abb.contiene(50), "Eliminar nodo con dos hijos (50 - raiz)");
        afirmar(abb.inOrdenIterativo().tamanio() == abb.tamanio(), "ABB mantiene consistencia tras eliminaciones");

        // Ranking acumulativo: el registro se saca del ABB y vuelve a entrar con los puntos sumados
        ABB<juego.reglas.ClaveRanking> ranking = new ABB<>();
        juego.reglas.ClaveRanking lola = new juego.reglas.ClaveRanking(24, "Lola", 4, 3);
        juego.reglas.ClaveRanking tano = new juego.reglas.ClaveRanking(18, "Tano", 4, 2);
        ranking.insertar(lola);
        ranking.insertar(tano);
        juego.reglas.ClaveRanking tanoNuevo = tano.sumarPartido(15, true);
        ranking.eliminar(tano);
        ranking.insertar(tanoNuevo);
        Lista<juego.reglas.ClaveRanking> orden = ranking.inOrdenIterativo();
        afirmar(ranking.tamanio() == 2 && orden.obtener(1).getNombre().equals("Tano")
                        && orden.obtener(1).getPuntaje() == 33 && orden.obtener(1).getPartidas() == 5
                        && orden.obtener(1).getVictorias() == 3,
                "Ranking: Tano suma 15 (33 pts, 5 partidas, 3 victorias) y pasa a Lola sin duplicarse");
        boolean imposible = false;
        try {
            new juego.reglas.ClaveRanking(10, "Pipa", 2, 5);
        } catch (IllegalArgumentException e) {
            imposible = true;
        }
        afirmar(imposible, "ClaveRanking rechaza mas victorias que partidas");
    }

    private static void probarTrie() {
        System.out.println("\n--- 4. Trie (Arbol de Prefijos y Tokenizador) ---");

        Trie trie = new Trie();
        trie.insertar("truco");
        trie.insertar("retruco");
        trie.insertar("vale cuatro");
        trie.insertar("envido");
        trie.insertar("real envido");
        trie.insertar("falta envido");
        trie.insertar("quiero");
        trie.insertar("no quiero");

        afirmar(trie.buscar("truco"), "Trie busqueda exacta existente");
        afirmar(!trie.buscar("flor"), "Trie busqueda exacta inexistente");
        afirmar(trie.comienzaCon("re"), "Trie prefijo 're' existe");
        afirmar(!trie.comienzaCon("xyz"), "Trie prefijo inexistente");

        // Tokenizacion por coincidencia mas larga
        Lista<String> tokens = trie.tokenizarPorCoincidenciaMasLarga("real envido quiero retruco");
        afirmar(tokens.tamanio() == 3, "Tokenizador identifico 3 tokens");
        afirmar(tokens.obtener(0).equals("real envido"), "Token 1: 'real envido' (coincidencia mas larga sobre 'envido')");
        afirmar(tokens.obtener(1).equals("quiero"), "Token 2: 'quiero'");
        afirmar(tokens.obtener(2).equals("retruco"), "Token 3: 'retruco'");

        // Trie sin coincidencias: ninguna palabra del texto esta en el vocabulario
        afirmar(trie.tokenizarPorCoincidenciaMasLarga("hola che").estaVacia(),
                "Trie sin coincidencias: el tokenizador estricto no devuelve tokens");

        // Tokenizador de frases (el del corpus de Markov)
        trie.insertar("me voy al mazo");
        Lista<String> frase = trie.tokenizarFrase("¡Me voy al MAZO, che!");
        afirmar(frase.tamanio() == 2 && frase.obtener(0).equals("me voy al mazo") && frase.obtener(1).equals("che"),
                "tokenizarFrase: la expresion queda como un token y la palabra desconocida no se pierde");
        afirmar(trie.tokenizarFrase("envidioso").obtener(0).equals("envidioso"),
                "tokenizarFrase: no corta palabras ('envidioso' no es 'envido' + 'so')");
    }

    private static void probarGrafo() {
        System.out.println("\n--- 5. Grafo Dirigido Ponderado y Dijkstra ---");

        Grafo<String> grafo = new Grafo<>();
        grafo.agregarArista("Resistencia", "Corrientes", 4);
        grafo.agregarArista("Corrientes", "Posadas", 6);
        grafo.agregarArista("Resistencia", "Formosa", 5);
        grafo.agregarArista("Formosa", "Posadas", 9);
        grafo.agregarArista("Posadas", "Resistencia", 8); // Ciclo

        afirmar(grafo.cantidadVertices() == 4, "Cantidad de vertices es 4");
        afirmar(grafo.cantidadAristas() == 5, "Cantidad de aristas es 5");

        // BFS y DFS
        Lista<String> bfs = grafo.bfs("Resistencia");
        afirmar(bfs.tamanio() == 4, "BFS recorre todos los alcanzables (4 vertices)");

        Lista<String> dfs = grafo.dfs("Resistencia");
        afirmar(dfs.tamanio() == 4, "DFS recorre todos los alcanzables (4 vertices)");

        // Dijkstra: camino de Resistencia a Posadas
        // Opcion 1: Resistencia -> Corrientes (4) -> Posadas (6) = 10
        // Opcion 2: Resistencia -> Formosa (5) -> Posadas (9) = 14
        ResultadoDijkstra<String> res = grafo.dijkstra("Resistencia", "Posadas");
        afirmar(res.existeCamino(), "Dijkstra encontro camino");
        afirmar(res.getDistanciaTotal() == 10, "Dijkstra distancia minima es 10 (via Corrientes)");
        afirmar(res.getCamino().tamanio() == 3, "Ruta contiene 3 paradas: Resistencia -> Corrientes -> Posadas");

        // Deteccion de ciclos
        afirmar(grafo.tieneCiclo(), "Grafo detecta ciclo (Resistencia -> Corrientes -> Posadas -> Resistencia)");

        // Dijkstra elige el camino mas barato aunque tenga mas tramos
        Grafo<String> rodeo = new Grafo<>();
        rodeo.agregarArista("A", "B", 10);
        rodeo.agregarArista("A", "C", 1);
        rodeo.agregarArista("C", "D", 1);
        rodeo.agregarArista("D", "B", 1);
        ResultadoDijkstra<String> barato = rodeo.dijkstra("A", "B");
        afirmar(barato.getDistanciaTotal() == 3 && barato.getCamino().tamanio() == 4,
                "Dijkstra prefiere A-C-D-B (3) antes que A-B directo (10)");

        // BFS por niveles: tramos minimos
        afirmar(grafo.tramosHasta("Resistencia", "Posadas") == 2, "BFS por niveles: Resistencia a Posadas son 2 tramos");
        afirmar(grafo.tramosHasta("Resistencia", "Resistencia") == 0, "BFS por niveles: de un vertice a si mismo, 0 tramos");

        // Camino inexistente: la arista es dirigida y no hay vuelta
        Grafo<String> idaSola = new Grafo<>();
        idaSola.agregarArista("Formosa", "Asuncion", 6);
        ResultadoDijkstra<String> sinVuelta = idaSola.dijkstra("Asuncion", "Formosa");
        afirmar(!sinVuelta.existeCamino() && sinVuelta.getCamino().estaVacia(),
                "Camino inexistente: Dijkstra informa que no hay ruta (arista dirigida)");
        afirmar(idaSola.tramosHasta("Asuncion", "Formosa") == -1, "Camino inexistente: BFS devuelve -1 tramos");
        afirmar(!idaSola.formanCiclo("Formosa", "Asuncion") && grafo.formanCiclo("Resistencia", "Posadas"),
                "formanCiclo: sin vuelta no hay ciclo; Resistencia y Posadas si estan en uno");
        afirmar(!idaSola.tieneCiclo(), "Grafo sin ciclos: tieneCiclo es false");

        // Grafo sin aristas
        Grafo<String> suelto = new Grafo<>();
        suelto.agregarVertice("Resistencia");
        suelto.agregarVertice("Corrientes");
        afirmar(suelto.cantidadAristas() == 0 && suelto.bfs("Resistencia").tamanio() == 1,
                "Grafo sin aristas: BFS solo visita el inicio");
        afirmar(!suelto.dijkstra("Resistencia", "Corrientes").existeCamino() && !suelto.tieneCiclo(),
                "Grafo sin aristas: no hay camino ni ciclos");

        // Lista de prioridad propia (la que usa Dijkstra)
        estructuras.ListaPrioridad<String> prioridad = new estructuras.ListaPrioridad<>();
        prioridad.encolar("lejos", 9);
        prioridad.encolar("cerca", 2);
        prioridad.encolar("medio", 5);
        prioridad.encolar("cerca2", 2);
        afirmar(prioridad.desencolarMin().getElemento().equals("cerca")
                        && prioridad.desencolarMin().getElemento().equals("cerca2")
                        && prioridad.desencolarMin().getElemento().equals("medio"),
                "ListaPrioridad: sale siempre la menor y, a igual prioridad, la que llego antes");
    }

    private static void probarMarkov() {
        System.out.println("\n--- 6. Cadenas de Markov (Orden k con Control de Calidad) ---");

        ModeloMarkov modelo = new ModeloMarkov(2);
        Lista<String> oracion1 = new MiLista<>();
        oracion1.agregar("truco");
        oracion1.agregar("quiero");
        oracion1.agregar("retruco");

        Lista<String> oracion2 = new MiLista<>();
        oracion2.agregar("truco");
        oracion2.agregar("no");
        oracion2.agregar("quiero");

        modelo.entrenarSecuencia(oracion1);
        modelo.entrenarSecuencia(oracion2);

        afirmar(modelo.cantidadEstados() > 0, "Modelo Markov entrenado contiene estados en ABB");

        ResultadoGeneracion gen = modelo.generar(5);
        afirmar(gen.getTextoGenerado() != null && !gen.getTextoGenerado().isEmpty(), "Markov nunca genera vacio");
        afirmar(gen.getTokens().tamanio() > 0, "Genera tokens validos");
        afirmar(gen.getTokens().tamanio() <= 5, "Markov respeta el maximo de tokens pedido");

        // Semilla: la frase arranca con el token pedido; si no existe, se avisa en la bitacora
        ResultadoGeneracion conSemilla = modelo.generar(5, "truco");
        afirmar(conSemilla.getTokens().obtener(0).equals("truco"), "Markov con semilla: la frase empieza con 'truco'");
        ResultadoGeneracion semillaRara = modelo.generar(5, "flor");
        afirmar(!semillaRara.getBitacora().estaVacia() && !semillaRara.getTextoGenerado().isEmpty(),
                "Markov con semilla inexistente: genera igual y lo anota en la bitacora");

        // Markov sin sucesores: modelo sin entrenar, no hay ningun estado del que partir
        ModeloMarkov vacio = new ModeloMarkov(2);
        ResultadoGeneracion sinSucesores = vacio.generar(5);
        afirmar(!sinSucesores.getTextoGenerado().isEmpty() && !sinSucesores.getBitacora().estaVacia(),
                "Markov sin sucesores: no devuelve vacio y deja constancia en la bitacora");

        // Largo maximo: una frase de 7 tokens pedida con maximo 4 se corta, pero avisando
        ModeloMarkov largo = new ModeloMarkov(2);
        Lista<String> frase = new MiLista<>();
        frase.agregar("truco");
        frase.agregar("que");
        frase.agregar("el");
        frase.agregar("gaucho");
        frase.agregar("bravo");
        frase.agregar("no");
        frase.agregar("teme");
        largo.entrenarSecuencia(frase);
        ResultadoGeneracion cortada = largo.generar(4, "truco");
        boolean anotada = false;
        for (int i = 0; i < cortada.getBitacora().tamanio(); i++) {
            anotada = anotada || cortada.getBitacora().obtener(i).contains("largo maximo");
        }
        afirmar(cortada.isCortadaPorLargo() && anotada && cortada.getTextoParaMostrar().endsWith("..."),
                "Markov cortado por largo: lo anota en la bitacora y la frase se muestra con \"...\"");
        ResultadoGeneracion completa = largo.generar(20, "truco");
        afirmar(!completa.isCortadaPorLargo() && completa.getTextoParaMostrar().equals("Truco que el gaucho bravo no teme."),
                "Markov que termina solo (token de fin): no se marca como cortado y cierra con punto");
    }

    private static void probarPersistencia() {
        System.out.println("\n--- 7. Persistencia con java.io, archivos ausentes y lineas danadas ---");

        // 1. Mapa: el real (o el base) y uno con lineas danadas
        Grafo<String> mapa = persistencia.RepositorioMapa.cargarMapa();
        afirmar(mapa.cantidadVertices() >= 4, "Persistencia: mapa cargado desde archivo o defaults con >= 4 vertices");
        java.io.File mapaRoto = escribirTemporal("mapa",
                "# prueba\nresistencia;corrientes;4\nsolo;dos\ncorrientes;posadas;seis\nposadas;formosa;-3\nposadas;encarnacion;5\n");
        Grafo<String> mapaParcial = persistencia.RepositorioMapa.cargarMapa(mapaRoto);
        afirmar(mapaParcial.cantidadAristas() == 2 && mapaParcial.dijkstra("posadas", "encarnacion").existeCamino(),
                "Mapa con 3 lineas danadas: se saltean y se cargan las 2 rutas validas");
        afirmar(persistencia.Avisos.huboAvisoCon(mapaRoto.getName() + ", linea 3")
                        && persistencia.Avisos.huboAvisoCon(mapaRoto.getName() + ", linea 4")
                        && persistencia.Avisos.huboAvisoCon(mapaRoto.getName() + ", linea 5"),
                "Mapa: cada linea danada queda avisada con su numero de linea");
        afirmar(persistencia.RepositorioMapa.cargarMapa(new java.io.File("no_existe_mapa.txt")).cantidadVertices() >= 6,
                "Mapa ausente: se usa el mapa base");

        // 2. Ranking: una fila buena, una imposible y una rota
        java.io.File rankingRoto = escribirTemporal("ranking", "# p;n;p;v\n30;Rodri;4;4\n10;Pipa;2;5\nbasura\n");
        ABB<juego.reglas.ClaveRanking> ranking = persistencia.RepositorioRanking.cargarRanking(rankingRoto);
        afirmar(ranking.tamanio() == 1 && persistencia.Avisos.huboAvisoCon(rankingRoto.getName() + ", linea 3"),
                "Ranking: la fila con mas victorias que partidas y la rota se saltean (queda 1)");
        afirmar(persistencia.RepositorioRanking.cargarRanking(new java.io.File("no_existe_ranking.txt")).estaVacio(),
                "Ranking ausente: arranca vacio");

        // 3. Llave del Torneo: guardar y reconstruir en pre-orden (en archivo temporal, no pisa datos/)
        ArbolBinario<juego.reglas.Partido> llaveOriginal = persistencia.RepositorioLlave.crearLlavePorDefecto();
        llaveOriginal.porNiveles().obtener(3).setGanador("Rodri");
        java.io.File llaveTemporal = archivoTemporal("llave");
        persistencia.RepositorioLlave.guardarLlave(llaveOriginal, llaveTemporal);
        ArbolBinario<juego.reglas.Partido> llaveCargada = persistencia.RepositorioLlave.cargarLlave(llaveTemporal);
        afirmar(llaveCargada.cantidadNodos() == llaveOriginal.cantidadNodos(), "Persistencia Llave: arbol reconstruido desde pre-orden conserva cantidad de nodos (7)");
        afirmar(llaveCargada.altura() == llaveOriginal.altura(), "Persistencia Llave: arbol reconstruido conserva altura (3)");
        afirmar(llaveCargada.porNiveles().obtener(3).isJugado()
                        && llaveCargada.porNiveles().obtener(3).getGanador().equals("Rodri"),
                "Persistencia Llave: el ganador de un partido sobrevive a guardar y cargar");
        persistencia.RepositorioLlave.guardarLlave(llaveOriginal, llaveTemporal);
        afirmar(new java.io.File(llaveTemporal.getAbsolutePath() + ".bak").exists(),
                "Respaldo: antes de sobrescribir se crea la copia .bak");

        java.io.File llaveRota = escribirTemporal("llave_rota",
                "# llave\nM;1;3;posadas;-;-;-\nM;2;2;corrientes;-;-;-\nM;4;uno;resistencia;Rodri;Tano;-\n#\n#\n");
        ArbolBinario<juego.reglas.Partido> llaveDefecto = persistencia.RepositorioLlave.cargarLlave(llaveRota);
        afirmar(llaveDefecto.cantidadNodos() == 7 && persistencia.Avisos.huboAvisoCon(llaveRota.getName() + ", linea 4"),
                "Llave con una linea danada: se avisa (linea 4) y se usa la llave inicial");
        java.io.File llaveCorta = escribirTemporal("llave_corta", "M;1;3;posadas;-;-;-\n#\n#\n");
        afirmar(persistencia.RepositorioLlave.cargarLlave(llaveCorta).cantidadNodos() == 7
                        && persistencia.Avisos.huboAvisoCon(llaveCorta.getName() + ": la llave tiene 1"),
                "Llave incompleta (1 partido): se avisa y se usa la llave inicial");

        // 4. Progreso: un numero mal escrito y una linea sin "=" no impiden leer el resto
        java.io.File progresoRoto = escribirTemporal("progreso", "club=formosa\nfichas=muchas\nbasura\nnombre=Lola\n");
        persistencia.RepositorioProgreso.Progreso prog = persistencia.RepositorioProgreso.cargarProgreso(progresoRoto);
        afirmar(prog.getClub().equals("formosa") && prog.getNombre().equals("Lola") && prog.getFichas() == 20,
                "Progreso con lineas danadas: se leen club y nombre; las fichas quedan en el valor inicial");
        afirmar(persistencia.Avisos.huboAvisoCon(progresoRoto.getName() + ", linea 2"),
                "Progreso: el numero mal escrito queda avisado");
        boolean negativo = false;
        try {
            prog.setFichas(-5);
        } catch (IllegalArgumentException e) {
            negativo = true;
        }
        afirmar(negativo, "Progreso: el setter no permite fichas negativas");
    }

    /** Archivo temporal vacio (se borra al terminar). */
    private static java.io.File archivoTemporal(String nombre) {
        try {
            java.io.File archivo = java.io.File.createTempFile("prueba_" + nombre + "_", ".txt");
            archivo.deleteOnExit();
            new java.io.File(archivo.getAbsolutePath() + ".bak").deleteOnExit();
            return archivo;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo crear un archivo temporal: " + e.getMessage());
        }
    }

    /** Archivo temporal con el contenido dado (para probar archivos danados sin tocar datos/). */
    private static java.io.File escribirTemporal(String nombre, String contenido) {
        java.io.File archivo = archivoTemporal(nombre);
        try (java.io.Writer w = new java.io.OutputStreamWriter(new java.io.FileOutputStream(archivo), "UTF-8")) {
            w.write(contenido);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo escribir el archivo temporal: " + e.getMessage());
        }
        return archivo;
    }

    public static void main(String[] args) {
        ejecutarTodas();
    }
}
