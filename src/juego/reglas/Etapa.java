package juego.reglas;

/**
 * Un nodo de la gira: el pais (raiz), una region o una provincia (las provincias son los
 * "puestos" que se juegan). La clave es el nombre en minusculas y sin tildes; es la que se
 * usa en los archivos y en el grafo de rutas.
 */
public class Etapa {

    /** Que lugar ocupa la etapa en el arbol de la gira. */
    public enum Tipo { PAIS, REGION, PROVINCIA }

    private final Tipo tipo;
    private final String clave;
    private final String nombre;
    private boolean completada;

    public Etapa(Tipo tipo, String clave, String nombre) {
        if (tipo == null) {
            throw new ReglaInvalidaException("La etapa necesita un tipo.");
        }
        if (clave == null || clave.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaInvalidaException("La etapa necesita clave y nombre.");
        }
        this.tipo = tipo;
        this.clave = clave.trim().toLowerCase();
        this.nombre = nombre.trim();
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isCompletada() {
        return completada;
    }

    /** Solo las provincias se ganan; regiones y pais se completan cuando se completan sus hijos. */
    public void setCompletada(boolean completada) {
        if (completada && tipo != Tipo.PROVINCIA) {
            throw new ReglaInvalidaException("Solo una provincia se marca como ganada: " + nombre);
        }
        this.completada = completada;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return clave.equals(((Etapa) obj).clave);
    }

    @Override
    public int hashCode() {
        return clave.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
