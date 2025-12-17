package modelo;

import java.util.Objects;

/**
 * Vecino de la comunidad.
 * - DNI, nombre y apellidos son constantes.
 * - Dirección, código postal, ciudad y teléfono son variables.
 */
public class Vecino implements java.io.Serializable {
    private final String dni;              // identificador único
    private final String nombreApellidos;  // constante

    private String direccion;
    private String codigoPostal;
    private String ciudad;
    private String telefono;

    public Vecino(String dni, String nombreApellidos,
                  String direccion, String codigoPostal, String ciudad, String telefono) {
        this.dni = Objects.requireNonNull(dni, "dni").trim();
        this.nombreApellidos = Objects.requireNonNull(nombreApellidos, "nombreApellidos").trim();
        this.direccion = nullSafeTrim(direccion);
        this.codigoPostal = nullSafeTrim(codigoPostal);
        this.ciudad = nullSafeTrim(ciudad);
        this.telefono = nullSafeTrim(telefono);
    }

    private static String nullSafeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    public String getDni() { return dni; }
    public String getNombreApellidos() { return nombreApellidos; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = nullSafeTrim(direccion); }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = nullSafeTrim(codigoPostal); }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = nullSafeTrim(ciudad); }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = nullSafeTrim(telefono); }

    @Override
    public String toString() {
        return nombreApellidos + " (" + dni + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vecino)) return false;
        Vecino vecino = (Vecino) o;
        return dni.equalsIgnoreCase(vecino.dni);
    }

    @Override
    public int hashCode() {
        return dni.toUpperCase().hashCode();
    }
}
