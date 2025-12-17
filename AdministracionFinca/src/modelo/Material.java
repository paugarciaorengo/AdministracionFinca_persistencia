package modelo;

import java.util.Objects;

public class Material implements java.io.Serializable {
    private final String nombre;
    private double precio;

    public Material(String nombre, double precio) {
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim();
        this.precio = precio;
    }

    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override
    public String toString() {
        return nombre + " (" + precio + "€)";
    }
}
