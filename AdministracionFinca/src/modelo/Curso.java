package modelo;

import java.time.LocalDate;
import java.util.*;

/** Actividad formativa organizada por la empresa. */
public class Curso implements java.io.Serializable {
    private final String nombre;
    private final double precio;
    private int maxVecinos; // variable
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    private final List<Materia> materias = new ArrayList<>();
    private final Set<Vecino> inscritos = new LinkedHashSet<>();

    public Curso(String nombre, double precio, int maxVecinos, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim();
        this.precio = precio;
        this.maxVecinos = maxVecinos;
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "fechaInicio");
        this.fechaFin = Objects.requireNonNull(fechaFin, "fechaFin");
    }

    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getMaxVecinos() { return maxVecinos; }
    public void setMaxVecinos(int maxVecinos) { this.maxVecinos = maxVecinos; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }

    public List<Materia> getMaterias() { return Collections.unmodifiableList(materias); }
    public Set<Vecino> getInscritos() { return Collections.unmodifiableSet(inscritos); }

    /** Duración total derivada: suma de horas de las materias. */
    public int getDuracionTotalHoras() {
        return materias.stream().mapToInt(Materia::getHoras).sum();
    }

    public void addMateria(Materia materia) {
        materias.add(Objects.requireNonNull(materia, "materia"));
    }

    /** Inscribir vecino cumpliendo la restricción de cupo. */
    public void inscribir(Vecino vecino) {
        Objects.requireNonNull(vecino, "vecino");
        if (inscritos.size() >= maxVecinos) {
            throw new IllegalStateException("Cupo completo: no se pueden inscribir más vecinos en el curso.");
        }
        inscritos.add(vecino);
    }

    @Override
    public String toString() {
        return nombre + " (" + getDuracionTotalHoras() + "h, " + inscritos.size() + "/" + maxVecinos + ")";
    }
}
