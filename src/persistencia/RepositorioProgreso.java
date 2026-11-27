package persistencia;

import juego.reglas.ReglaInvalidaException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Progreso de la gira (datos/progreso.txt).
 *
 * Formato: una linea "clave=valor" por dato; las lineas con '#' son comentarios.
 *   club=resistencia
 *   fichas=20
 *   puntos=0
 *   ronda=1
 *   nombre=Rodri
 * Cada linea se lee por separado: si una esta danada (numero mal escrito, valor imposible,
 * clave desconocida) se avisa en pantalla y ese dato queda con su valor inicial; el resto
 * del archivo se usa igual.
 */
public class RepositorioProgreso {

    private static final String RUTA_PROGRESO = "datos/progreso.txt";

    /**
     * Estado de la gira. Atributos privados con setters que validan: no puede haber fichas
     * ni puntos negativos, ni un club o un nombre vacios, ni una ronda menor a 1.
     */
    public static class Progreso {
        private String club = "resistencia";
        private int fichas = 20;
        private int puntos = 0;
        private int ronda = 1;
        private String nombre = "Rodri";

        public String getClub() {
            return club;
        }

        public void setClub(String club) {
            if (club == null || club.trim().isEmpty()) {
                throw new ReglaInvalidaException("El club no puede estar vacio.");
            }
            this.club = club.trim().toLowerCase();
        }

        public int getFichas() {
            return fichas;
        }

        public void setFichas(int fichas) {
            if (fichas < 0) {
                throw new ReglaInvalidaException("Las fichas no pueden ser negativas: " + fichas);
            }
            this.fichas = fichas;
        }

        public void sumarFichas(int cantidad) {
            setFichas(fichas + cantidad);
        }

        public int getPuntos() {
            return puntos;
        }

        public void setPuntos(int puntos) {
            if (puntos < 0) {
                throw new ReglaInvalidaException("Los puntos no pueden ser negativos: " + puntos);
            }
            this.puntos = puntos;
        }

        public void sumarPuntos(int cantidad) {
            setPuntos(puntos + cantidad);
        }

        public int getRonda() {
            return ronda;
        }

        public void setRonda(int ronda) {
            if (ronda < 1) {
                throw new ReglaInvalidaException("La ronda empieza en 1: " + ronda);
            }
            this.ronda = ronda;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new ReglaInvalidaException("El nombre no puede estar vacio.");
            }
            this.nombre = nombre.trim();
        }
    }

    public static Progreso cargarProgreso() {
        return cargarProgreso(new File(RUTA_PROGRESO));
    }

    /** Sobrecarga con el archivo a leer (las pruebas usan un archivo temporal). */
    public static Progreso cargarProgreso(File archivo) {
        Progreso p = new Progreso();
        if (!archivo.exists()) {
            Avisos.registrar(archivo.getName() + " no existe: empieza una gira nueva.");
            return p;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), "UTF-8"))) {
            String linea;
            int numero = 0;
            while ((linea = reader.readLine()) != null) {
                numero++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                try {
                    leerLinea(p, linea, archivo.getName(), numero);
                } catch (DatoDanadoException e) {
                    Avisos.registrar(e.getMessage() + " (se usa el valor inicial)");
                }
            }
        } catch (Exception e) {
            Avisos.registrar("No se pudo leer " + archivo.getName() + ": " + e.getMessage() + ". Gira nueva.");
            return new Progreso();
        }
        return p;
    }

    private static void leerLinea(Progreso p, String linea, String archivo, int numero) throws DatoDanadoException {
        int igual = linea.indexOf('=');
        if (igual <= 0) {
            throw new DatoDanadoException(archivo, numero, "se esperaba clave=valor y hay \"" + linea + "\"");
        }
        String clave = linea.substring(0, igual).trim().toLowerCase();
        String valor = linea.substring(igual + 1).trim();
        try {
            switch (clave) {
                case "club":   p.setClub(valor); break;
                case "fichas": p.setFichas(Integer.parseInt(valor)); break;
                case "puntos": p.setPuntos(Integer.parseInt(valor)); break;
                case "ronda":  p.setRonda(Integer.parseInt(valor)); break;
                case "nombre": p.setNombre(valor); break;
                default:
                    throw new DatoDanadoException(archivo, numero, "clave desconocida \"" + clave + "\"");
            }
        } catch (IllegalArgumentException e) { // numero mal escrito o valor imposible
            throw new DatoDanadoException(archivo, numero, "valor invalido para " + clave + " (\"" + valor + "\")");
        }
    }

    public static void guardarProgreso(Progreso p) {
        guardarProgreso(p, new File(RUTA_PROGRESO));
    }

    public static void guardarProgreso(Progreso p, File archivo) {
        if (p == null) return;
        Respaldo.prepararParaEscribir(archivo);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8"))) {
            writer.write("# Estado de la gira del jugador\n");
            writer.write("club=" + p.getClub() + "\n");
            writer.write("fichas=" + p.getFichas() + "\n");
            writer.write("puntos=" + p.getPuntos() + "\n");
            writer.write("ronda=" + p.getRonda() + "\n");
            writer.write("nombre=" + p.getNombre() + "\n");
        } catch (Exception e) {
            Avisos.registrar("No se pudo guardar " + archivo.getName() + ": " + e.getMessage());
        }
    }
}
