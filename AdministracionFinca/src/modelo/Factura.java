package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Documento contable que consolida la deuda de un vecino.
 * Total a pagar es un atributo derivado: suma de los importes de las visitas.
 */
public class Factura implements java.io.Serializable {
    private final int id;
    private final LocalDate fechaCreacion;
    private final Vecino vecino;
    private final List<FichaVisita> visitas;

    public Factura(int id, LocalDate fechaCreacion, Vecino vecino, List<FichaVisita> visitas) {
        this.id = id;
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "fechaCreacion");
        this.vecino = Objects.requireNonNull(vecino, "vecino");
        this.visitas = new ArrayList<>(Objects.requireNonNull(visitas, "visitas"));
    }

    public int getId() { return id; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public Vecino getVecino() { return vecino; }

    public List<FichaVisita> getVisitas() {
        return Collections.unmodifiableList(visitas);
    }

    /** Total derivado: suma de importes de las visitas. */
    public double getTotal() {
        return visitas.stream().mapToDouble(FichaVisita::getImporte).sum();
    }

    @Override
    public String toString() {
        return "Factura #" + id + " | " + vecino + " | " + getTotal() + "€";
    }
}
