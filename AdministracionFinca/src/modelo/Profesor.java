package modelo;

import java.util.Objects;

public class Profesor implements java.io.Serializable {
    private String nombre;
    private String apellidos;
    private String direccion;
    private String telefono;
    private double sueldo;

    public Profesor(String nombre, String apellidos, String direccion, String telefono, double sueldo) {
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim();
        this.apellidos = Objects.requireNonNull(apellidos, "apellidos").trim();
        this.direccion = safe(direccion);
        this.telefono = safe(telefono);
        this.sueldo = sueldo;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = Objects.requireNonNull(nombre).trim(); }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = Objects.requireNonNull(apellidos).trim(); }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = safe(direccion); }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = safe(telefono); }

    public double getSueldo() { return sueldo; }
    public void setSueldo(double sueldo) { this.sueldo = sueldo; }

    public String getNombreCompleto() {
        return (nombre + " " + apellidos).trim();
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
