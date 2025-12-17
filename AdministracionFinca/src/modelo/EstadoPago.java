package modelo;

/**
 * Estado de pago controlado para una FichaVisita.
 */
public enum EstadoPago implements java.io.Serializable {
    IMPAGADA,
    PAGADA;

    @Override
    public String toString() {
        return this == IMPAGADA ? "Impagada" : "Pagada";
    }
}
