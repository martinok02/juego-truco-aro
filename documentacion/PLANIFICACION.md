# Planificación TP3: Game Jam Java II · "TrucoAro"

## Contexto
- **Consigna vigente:** `Enunciado_Game_Jam_Java_II_Semana.docx.pdf` (está en la raíz). El PDF de `TPJuego/docs` es el de la Jam I; de ahí salen el kit y la Lista, Pila y Cola que se reutilizan.
- **Plazo:** 1 semana. Equipo de 4 con los roles A, B, C y D del enunciado (todavía sin repartir).
- **Avance del compañero (`TPJuego/`):**
  - Las estructuras están bien hechas.
  - **La partida se traba.** Con esto hoy no se aprueba el requisito "jugable de principio a fin".
  - **Markov, Trie, BFS/DFS y la llave casi no influyen en el juego.** Un árbol, grafo o modelo que no cumple una función real no suma en su criterio, y eso pone en juego cerca de 40 puntos.
- **Objetivo:** cerrar cada casillero de la sección 4 y maximizar la rúbrica, sin rehacer lo que ya está bien.

## Lo que aprendimos de la carpeta de la cátedra (ajusta prioridades)
- **Rúbrica real de la Jam I** (`Programacion/TPGrupalProgramacion-main/docs/Rubrica.pdf`): el profe no corrigió con la tabla del enunciado sino con una práctica → Compila 10 · **Funcionamiento y reglas 25** · Estructuras 15 · Arquitectura 15 · Física 15 · **Interfaz/UX 10** · **Trabajo en equipo 10 (GitHub colaborativo, código comentado)** · **Itch.io 5**. Sacaron 85: perdieron en UX (5/10), arquitectura (12/15) y física.
  → Para la Jam II es probable algo parecido: **que la partida no se trabe y se entienda sin explicación pesa más que agregar features**.
- El grupo ya trabaja con **GitHub** y subió la Jam I a **itch.io** → repetir: repo con commits de cada uno en su módulo, y subir el build.
- Las opiniones del grupo piden código **comentado** y sentían presión por la IA → **cambios chicos que todos puedan defender**, nada de reescrituras grandes.
- **Fecha:** el TPI cae el **30/11 o 01/12/2026** según comisión (cronograma) → hoy 24/11 quedan ~6 días.
- El `TPJuego` de `Programacion/` es idéntico al de `TP3/` (misma versión).

## Recorte de alcance (para no hacer de más)
- **Imprescindible:** compilar, B1/B2, Markov en partida, Trie con entrada del jugador, BFS/ciclos en el mapa, llave que avance, sacar arreglos fuera de TADs, excepciones propias, 5 tests faltantes, informe y diagrama.
- **Si sobra tiempo:** Retruco/ValeCuatro/Real/Falta Envido completos, rival que canta envido, alternar mano, Dijkstra con ABB.
- **Descartado:** BPE, rehacer pantallas o el motor.

## Estado actual vs. requisitos de aprobación

| Requisito (sección 4) | Estado | Qué falta |
|---|---|---|
| Compila de cero | ⚠️ | `compilar.bat` y `compilar.sh` arman `fuentes.txt` con rutas sin comillas. La carpeta tiene espacios ("Segundo año"), así que `javac` falla. |
| Jugable de principio a fin | ❌ | Ver los bugs B1 y B2 más abajo. |
| ArbolBinario, ABB y 4 recorridos que devuelven Lista | ✅ | `arboles/ArbolBinario.java`, `arboles/ABB.java` (eliminación con 2 hijos, in-orden con `MiPila`). |
| Grafo con BFS, DFS y Dijkstra que decide algo | ⚠️ | Dijkstra decide la ruta y el costo en fichas (`EscenaMapa`), pero busca el mínimo con `int[]`/`boolean[]` y no con una lista de prioridad propia. BFS, DFS y `tieneCiclo` solo se usan en las pruebas. |
| Markov de orden k desde archivo, visible en pantalla | ⚠️ | Funciona en `EscenaLaboratorioMarkov`. En `EscenaPartida:77` y `EscenaFin:33` se crea un `new Trie()` vacío, así que siempre sale el texto de respaldo "truco quiero". El corpus `datos/dichos.txt` es muy chico. |
| Trie con coincidencia más larga sobre entrada del jugador | ❌ | El Trie está completo (`tokens/Trie.java`), pero el jugador nunca escribe texto. |
| Persistencia: 4 archivos, árbol reconstruido, tolerancia a errores | ⚠️ | Hay 5 archivos, try-with-resources y `Respaldo`. Falta mostrar los errores en pantalla, y una línea dañada hoy invalida el archivo entero en progreso y llave. |
| Lista, Pila y Cola con función real | ✅ | Lista: mazo, manos y adyacencias. Pila: in-orden del ranking. Cola: niveles de la llave. Conviene reforzar la Pila con un "deshacer". |
| PruebaEstructuras con ≥15 checks y los casos pedidos | ⚠️ | Hay 55 checks. Faltan: árbol de un nodo, grafo sin aristas, camino inexistente, Markov sin sucesores y tokenizar sin coincidencia. La prueba pisa `datos/llave.txt`. |
| Informe Big-O | ⚠️ | Existe `documentacion/01_*.html`, pero afirma cosas que el código no hace (BFS en el juego, `EstrategiaArriesgada`). No compara salidas de Markov con distintos k. |
| POO: abstracta, interfaz, encapsulamiento | ⚠️ | `Canto`: 4 de sus 6 subclases nunca se crean. `EstrategiaArriesgada` no se usa. **No hay excepciones propias.** `RepositorioProgreso.Progreso` tiene atributos públicos. `Partido` y `ClaveRanking` no validan. |
| Restricciones (sin `java.util` ni `[]` fuera de los TAD) | ⚠️ | Hay arreglos en `EscenaMapa:20-22`, `EscenaMenu:13`, `Mazo:20-21`, `RepositorioCorpus:48`, `Respaldo:24` y en `Grafo.java` (BFS, DFS, Dijkstra, ciclos). Cada uno resta 10 puntos. |

**Bugs que rompen la partida (`juego/pantallas/EscenaPartida.java`):**
- **B1.** Cuando el rival gana una baza, sale jugando (`procesarSalidaRival`). Cuando el jugador responde, `procesarTurnoRival` elige otra carta y pisa la anterior. Así el rival se queda sin cartas en la baza 3, `resolverBaza` sale sin hacer nada y el juego se traba.
- **B2.** Si el rival canta truco después de que el jugador tiró, al responder "Quiero" el rival nunca juega su carta. La próxima carta del jugador pisa a la anterior y el jugador se queda sin cartas.
- **Faltan reglas:** no hay retruco, vale cuatro, real envido ni falta envido. El rival nunca canta envido. El jugador siempre es mano.

## Diseño propuesto: que cada estructura decida algo del Truco

| Estructura | Función real en el juego |
|---|---|
| **Árbol (llave)** | Torneo eliminatorio de 8 clubes. Al ganar o perder un partido se llama a `setGanador` y avanza la ronda. Se guarda en `llave.txt` (preorden con `#`) y se reconstruye al abrir. Las hojas se recorren por niveles con `MiCola`. |
| **ABB** | Ranking ordenado por `ClaveRanking`, que acumula partidas y no pisa los datos. También es la tabla de transiciones de Markov. Opcional: Dijkstra con un ABB por distancia. |
| **Grafo** | Mapa de clubes del Litoral. Dijkstra calcula el costo del viaje en fichas. BFS muestra qué sedes son alcanzables con las fichas actuales. DFS o la detección de ciclos habilita una "gira circular" con bonus. Las aristas se dibujan desde el grafo, no fijas en el código. |
| **Markov** | El rival "habla" en cada canto. Se crea un solo modelo compartido, con un Trie cargado, y se amplía el corpus a unos 60–100 dichos. Opcionalmente, el k depende de la dificultad de la sede. |
| **Trie** | Cantos escritos por el jugador: un campo de texto en la partida ("quiero retruco", "real envido") que se tokeniza por coincidencia más larga para obtener la acción. Debe tener sugerencias por prefijo y aceptar tanto la tecla como el texto. |
| **Lista, Pila, Cola** | Lista: mazo, manos y adyacencias. Pila: historial de cantos de la mano (Truco → Retruco → Vale Cuatro, para validar el siguiente nivel) y el in-orden iterativo. Cola: turnos y niveles de la llave. |

## Reparto de módulos (4 integrantes)

| Rol | Paquetes | Tareas concretas |
|---|---|---|
| **A. Árboles y trie** | `arboles`, `tokens` | Integrar la llave al estado del juego (avance de ronda y ganador). Excepción propia para un nodo inexistente. Tokenizar los cantos del jugador con el Trie. Tests: árbol de un nodo, trie sin coincidencia. `PruebaArboles` en verde. |
| **B. Grafos y persistencia** | `grafos`, `persistencia` | Dijkstra con una lista de prioridad propia y sin arreglos (marcas y distancias en una `MiLista` o en `Vertice`). Usar BFS y ciclos en el mapa. Tolerar líneas dañadas sin descartar el archivo entero. Errores visibles en pantalla (`RepositorioX.getAdvertencias()` → HUD). Tests: grafo sin aristas, camino inexistente. Ranking acumulativo. |
| **C. Markov y tokens** | `markov`, `tokens` (corpus) | Un `ModeloMarkov` compartido con un Trie cargado. Corpus ampliado. Arreglar la estrategia de "reinicio". Usar el parámetro `motivo`. Tabla comparando k = 1 a 4 para el informe. Test: Markov sin sucesores. |
| **D. Juego y TADs** | `juego.reglas`, `juego.pantallas`, `estructuras` | Arreglar B1 y B2 con una máquina de estados de la mano. Escalera de cantos vía `Canto.siguienteNivel()` y `puntosRechazado()` (polimorfismo real). Usar `EstrategiaArriesgada`. Alternar la mano. Quitar arreglos de `Mazo`, `EscenaMenu` y `EscenaMapa`. Encapsular `Progreso` y validar `Partido`. |

**Tareas comunes (cualquiera):**
- Arreglar `compilar.bat` y `compilar.sh` para que pongan comillas en las rutas. Se hace el día 1, y es lo primero.
- Borrar `provisorio/`, `ListaBase` y `validarIdiceSeguro`, y revertir `motor/Mundo.java`, que es del kit y no se modifica.
- Revisar los imports de `java.util` con grep.

## Cronograma (alineado a los hitos de la sección 7)

| Día | Objetivo | Responsables |
|---|---|---|
| **1** | Acta de contrato: métodos públicos de árbol, grafo y modelo; formato de cada archivo; política de repetidos (ABB y ranking); estrategia cuando no hay sucesores. Arreglar los scripts de compilación. Arreglar B1 y B2 para que la partida se juegue completa. Avisar al docente la temática. | Todos; D se encarga de B1 y B2 |
| **2** | Árbol y `PruebaArboles` en verde (ya casi lo están). Llave integrada a la partida: ganar avanza de ronda. Verificador de Lista, Pila y Cola en verde. | A, D |
| **3** | Markov compartido y visible en la partida y en el fin. Corpus ampliado. ABB con eliminación usado en el ranking. | C, A |
| **4** | Dijkstra con una lista de prioridad propia. BFS y ciclos aplicados al mapa. Aristas dibujadas desde el grafo. Entrada de texto en la partida tokenizada con el Trie. Escalera de cantos completa. | B, A, D |
| **5** | Persistencia robusta: errores en pantalla, líneas dañadas. Probar borrando o editando cada archivo de `datos/`. Excepciones propias. Quitar arreglos fuera de los TAD. | B, todos |
| **6** | Congelamiento. `PruebaEstructuras` con los 5 casos que faltan, sin tocar `datos/` reales (usar un archivo temporal). Informe (máx. 6 páginas): formatos, Big-O corregido, comparación de k en Markov, decisiones sobre repetidos y empates. Diagrama de clases. Corregir `LEEME_PRIMERO.md` y los HTML. | Todos |
| **7** | Ensayo de la demo: cerrar y reabrir el juego, borrar un archivo. Ensayo de las preguntas de defensa cruzadas. Armar el zip con `src` y `datos/` y la línea por integrante. Subir el build a itch.io como en la Jam I. | Todos |

**Durante toda la semana:** repo en GitHub con un commit por cambio y cada integrante en su módulo; comentar cada método nuevo (el profe lo evalúa).

## Verificación (antes de cada integración y al final)
1. `compilar.bat` desde cero en esta carpeta con espacios y luego `ejecutar.bat juego.Main`.
2. `ejecutar.bat verificador.PruebaMisEstructuras`: el verificador y PruebaEstructuras deben quedar en verde.
3. Partida completa a 15 puntos: probar cada combinación de bazas, truco a mitad de baza, envido, pardas e irse al mazo, sin que se trabe.
4. Persistencia: cerrar y reabrir el juego y comprobar que la llave, el ranking y el progreso se conservan. Borrar cada archivo de `datos/` y ver que el juego arranca igual. Romper una línea y ver que aparece el aviso en pantalla.
5. Grep final:
   - Ningún `import java.util.` salvo `Random`/`Iterator`, ni `java.nio` ni `Serializable`.
   - Ningún `[]` fuera de `estructuras/` y `tokens/NodoTrie`.
   - Ningún `instanceof` en `juego/`.

