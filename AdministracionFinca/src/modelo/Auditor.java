package modelo;

import java.util.Objects;

public class Auditor implements java.io.Serializable {
    private String nombre;
    private String apellidos;
    private String cifEmpresa;
    private String nombreEmpresa;
    private String direccionEmpresa;
    private String telefono;

    public Auditor(String nombre, String apellidos, String cifEmpresa, String nombreEmpresa,
                   String direccionEmpresa, String telefono) {
        this.nombre = Objects.requireNonNull(nombre, "nombre").trim();
        this.apellidos = Objects.requireNonNull(apellidos, "apellidos").trim();
        this.cifEmpresa = safe(cifEmpresa);
        this.nombreEmpresa = safe(nombreEmpresa);
        this.direccionEmpresa = safe(direccionEmpresa);
        this.telefono = safe(telefono);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = Objects.requireNonNull(nombre).trim(); }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = Objects.requireNonNull(apellidos).trim(); }

    public String getCifEmpresa() { return cifEmpresa; }
    public void setCifEmpresa(String cifEmpresa) { this.cifEmpresa = safe(cifEmpresa); }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = safe(nombreEmpresa); }

    public String getDireccionEmpresa() { return direccionEmpresa; }
    public void setDireccionEmpresa(String direccionEmpresa) { this.direccionEmpresa = safe(direccionEmpresa); }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = safe(telefono); }

    public String getNombreCompleto() { return (nombre + " " + apellidos).trim(); }

    @Override
    public String toString() {
        String empresa = nombreEmpresa.isEmpty() ? "" : (" - " + nombreEmpresa);
        return getNombreCompleto() + empresa;
    }
}