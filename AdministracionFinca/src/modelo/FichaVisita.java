package modelo;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Documento que registra la intervención de un administrador en el hogar de un vecino.
 * Nace impagada.
 */
public class FichaVisita implements java.io.Serializable {
    private final int id;                      // identificador interno
    private final Vecino vecino;
    private final LocalDate fecha;             // constante
    private final String descripcion;          // constante
    private final double importe;              // constante
    private final String nombreAdministrador;  // constante

    private EstadoPago estado;                 // variable controlada

    public FichaVisita(int id,
                       Vecino vecino,
                       LocalDate fecha,
                       String descripcion,
                       double importe,
                       String nombreAdministrador) {
        this.id = id;
        this.vecino = Objects.requireNonNull(vecino, "vecino");
        this.fecha = Objects.requireNonNull(fecha, "fecha");
        this.descripcion = Objects.requireNonNull(descripcion, "descripcion").trim();
        this.importe = importe;
        this.nombreAdministrador = Objects.requireNonNull(nombreAdministrador, "nombreAdministrador").trim();
        this.estado = EstadoPago.IMPAGADA;
    }

    public int getId() { return id; }
    public Vecino getVecino() { return vecino; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public double getImporte() { return importe; }
    public String getNombreAdministrador() { return nombreAdministrador; }

    public EstadoPago getEstado() { return estado; }
    public void marcarPagada() { this.estado = EstadoPago.PAGADA; }

    @Override
    public String toString() {
        return "Visita #" + id + " | " + vecino + " | " + importe + "€ | " + estado;
    }
}
