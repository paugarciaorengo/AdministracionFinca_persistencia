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

    public String getNombreCompleto() { return (nombre + " " + apellidos).trim(); }
    public String getCifEmpresa() { return cifEmpresa; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public String getDireccionEmpresa() { return direccionEmpresa; }
    public String getTelefono() { return telefono; }

    @Override
    public String toString() {
        String empresa = nombreEmpresa.isEmpty() ? "" : (" - " + nombreEmpresa);
        return getNombreCompleto() + empresa;
    }
}
