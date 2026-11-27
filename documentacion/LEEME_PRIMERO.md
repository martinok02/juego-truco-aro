# Carpeta de Documentación Oficial · TrucoAro

Esta carpeta contiene la documentación integral de ingeniería de software, arquitectura, justificación pedagógica, guía de defensa oral y manual del juego.

---

## 📄 Archivos disponibles (Listos para imprimir a PDF o leer en navegador)

Cada documento tiene dos versiones:
1. **Versión `.html` interactiva y lista para PDF:** Abrila en cualquier navegador (Opera GX, Chrome, Edge) y podés presionar el botón violeta superior **"🖨️ Imprimir / Guardar en PDF"** o `Ctrl + P` para obtener un PDF con formato de imprenta profesional.
2. **Versión `.md` Markdown:** Para leer directamente en tu editor de código, GitHub o IDE.

---

### 1. [01_INFORME_TECNICO_OFICIAL.html](file:///D:/Descargas/Programacion/TPJuego/documentacion/01_INFORME_TECNICO_OFICIAL.html)
* **Contenido:**
  * Portada oficial de la UTN FRRe (Programación II, Cátedra Prof. Goya).
  * Justificación pedagógica de por qué cada estructura no lineal es insustituible.
  * Especificación técnica y formato de cada uno de los 5 archivos de persistencia (`datos/`).
  * Tabla maestra de complejidad temporal y espacial (Big-O).
  * Verificación de principios de POO (polimorfismo sin `instanceof`, encapsulamiento, clases abstractas e interfaces).

### 2. [02_GUIA_DEFENSA_ORAL_Y_PREGUNTAS_TRAMPA.html](file:///D:/Descargas/Programacion/TPJuego/documentacion/02_GUIA_DEFENSA_ORAL_Y_PREGUNTAS_TRAMPA.html)
* **Contenido:**
  * **Guion de 5 minutos cronometrado para la demo:** qué mostrar en el proyector minuto a minuto.
  * **Banco de 8 preguntas típicas y capciosas de los docentes** con sus respuestas técnicas exactas (explicación de la eliminación con 2 hijos en el ABB, por qué el in-orden es iterativo con Pila, reconstrucción de árbol con marcadores `#`, cómo se evita el soft-lock con viáticos, control de calidad en Markov).

### 3. [03_MANUAL_DEL_JUGADOR_Y_REGLAS.html](file:///D:/Descargas/Programacion/TPJuego/documentacion/03_MANUAL_DEL_JUGADOR_Y_REGLAS.html)
* **Contenido:**
  * Mapeo de controles de teclado para todas las pantallas (`1`, `2`, `3`, `E`, `T`, `Q`, `N`, `M`, `P`, `L`, `ESC`).
  * Jerarquía completa de las 40 cartas españolas (de mayor a menor poder).
  * Reglas del truco express (envido, truco, pardas/empates, mano a 15 puntos).
  * Mecánica de viaje y recarga de fichas en el mapa con Dijkstra.

### 4. [04_DIAGRAMAS_DE_CLASES_Y_ARQUITECTURA.html](file:///D:/Descargas/Programacion/TPJuego/documentacion/04_DIAGRAMAS_DE_CLASES_Y_ARQUITECTURA.html)
* **Contenido:**
  * Diagrama de dependencias entre paquetes (garantía de cero dependencias circulares).
  * Jerarquía de clases de cantos (`Canto` -> `Truco`, `Retruco`, `ValeCuatro`, `Envido`, `RealEnvido`, `FaltaEnvido`).
  * Jerarquía de interfaz de rivales (`EstrategiaRival` -> `Conservadora`, `Arriesgada`, `Mentirosa`).
  * Topología del árbol binario de la llave (7 partidos).
  * Topología del grafo de clubes del Litoral.

---

## 🖨️ Cómo generar los PDFs en 1 click

1. Abrí cualquiera de los archivos `.html` haciendo doble click en Windows.
2. Hacé click en el botón superior derecho **"🖨️ Imprimir / Guardar en PDF"** (o presioná `Ctrl + P`).
3. En la ventana de impresión, elegí **"Guardar como PDF"** (Save as PDF).
4. Guardalo en esta misma carpeta y te quedarán los PDFs oficiales listos para imprimir o adjuntar en la entrega final.
