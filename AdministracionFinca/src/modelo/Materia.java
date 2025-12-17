package modelo;

import java.util.Objects;

public class Materia implements java.io.Serializable {
    private final String nombre;
    private final int horas;
    private Profesor profesor; // una materia solo la puede impartir un único profesor

    public Materia(String nombre, int horas, Profesor profesor) {
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim();
        this.horas = horas;
        this.profesor = Objects.requireNonNull(profesor, "profesor");
    }

    public String getNombre() { return nombre; }
    public int getHoras() { return horas; }

    public Profesor getProfesor() { return profesor; }
    /**
     * Restricción: una materia solo puede ser impartida por un único profesor.
     * En esta implementación significa que la materia siempre tiene exactamente un profesor.
     */
    public void setProfesor(Profesor profesor) {
        this.profesor = Objects.requireNonNull(profesor, "profesor");
    }

    @Override
    public String toString() {
        return nombre + " (" + horas + "h) - " + profesor;
    }
}
