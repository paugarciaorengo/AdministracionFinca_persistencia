package modelo;

import java.util.Objects;

public class Material implements java.io.Serializable {
    private String nombre; // Ya no es final
    private double precio;

    public Material(String nombre, double precio) {
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim();
        this.precio = precio;
    }

    public String getNombre() { return nombre; }
    
    // Setter añadido para modificación
    public void setNombre(String nombre) { 
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim(); 
    }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override
    public String toString() {
        return nombre + " (" + precio + "€)";
    }
}