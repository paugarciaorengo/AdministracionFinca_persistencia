package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Proceso de verificación realizado por un agente externo sobre un conjunto de visitas.
 * - Sueldo auditor es derivado: 20% de los importes de las visitas asociadas.
 * - Mientras la auditoría esté abierta, el sueldo se recalcula dinámicamente.
 * - Al cerrarse, el sueldo queda fijo.
 */
public class Auditoria implements java.io.Serializable {
    private final int id;
    private final LocalDate fechaCreacion;
    private LocalDate fechaFin; // null => abierta
    private final Auditor auditor;

    private final List<FichaVisita> visitas = new ArrayList<>();
    private final List<Material> materiales = new ArrayList<>();

    private Double sueldoFijado; // null mientras abierta

    public Auditoria(int id, Auditor auditor, LocalDate fechaCreacion) {
        this.id = id;
        this.auditor = Objects.requireNonNull(auditor, "auditor");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "fechaCreacion");
    }

    public int getId() { return id; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Auditor getAuditor() { return auditor; }

    public boolean estaCerrada() { return fechaFin != null; }

    public List<FichaVisita> getVisitas() { return Collections.unmodifiableList(visitas); }
    public List<Material> getMateriales() { return Collections.unmodifiableList(materiales); }

    /**
     * Asignar una visita a una auditoría activa.
     * Restricción: si está cerrada, no se pueden asignar más visitas.
     */
    public void asignarVisita(FichaVisita visita) {
        Objects.requireNonNull(visita, "visita");
        if (estaCerrada()) {
            throw new IllegalStateException("La auditoría está cerrada; no se pueden asignar más visitas.");
        }
        visitas.add(visita);
    }

    public void asignarMaterial(Material material) {
        Objects.requireNonNull(material, "material");
        if (estaCerrada()) {
            throw new IllegalStateException("La auditoría está cerrada; no se pueden asignar más materiales.");
        }
        materiales.add(material);
    }

    /** Sueldo derivado (20%). Fijo tras cerrar. */
    public double getSueldoAuditor() {
        if (sueldoFijado != null) return sueldoFijado;
        return calcularSueldo();
    }

    private double calcularSueldo() {
        double totalVisitas = visitas.stream().mapToDouble(FichaVisita::getImporte).sum();
        return totalVisitas * 0.20;
    }

    /**
     * Finalizar auditoría.
     * Restricción: fechaFin > fechaCreacion.
     */
    public void cerrar(LocalDate fechaFin) {
        Objects.requireNonNull(fechaFin, "fechaFin");
        if (!fechaFin.isAfter(fechaCreacion)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de creación.");
        }
        if (estaCerrada()) return; // idempotente
        this.fechaFin = fechaFin;
        this.sueldoFijado = calcularSueldo();
    }

    @Override
    public String toString() {
        String estado = estaCerrada() ? ("Cerrada " + fechaFin) : "Abierta";
        return "Auditoría #" + id + " | " + auditor + " | " + estado + " | Sueldo: " + getSueldoAuditor() + "€";
    }
}
