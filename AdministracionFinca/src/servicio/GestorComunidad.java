package servicio;

import modelo.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Fachada de lógica de negocio.
 *
 * Nota: la persistencia se gestiona fuera de esta clase (ver paquete persistencia),
 * serializando un contenedor de datos.
 */
public class GestorComunidad {

    // Validaciones visibles
    private static final Pattern DNI_PATTERN = Pattern.compile("^[0-9]{8}[A-Za-z]$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[0-9]{9}$");

    /** Contenedor serializable con todos los datos del dominio. */
    private final Datos datos;

    /**
     * Contenedor de datos serializable (repositorio en memoria persistible).
     * Se serializa/deserializa desde el paquete persistencia.
     */
    public static class Datos implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Map<String, Vecino> vecinosPorDni = new LinkedHashMap<>();
        public final List<FichaVisita> visitas = new ArrayList<>();
        public final List<Factura> facturas = new ArrayList<>();
        public final List<Curso> cursos = new ArrayList<>();
        public final List<Profesor> profesores = new ArrayList<>();
        public final List<Auditor> auditores = new ArrayList<>();
        public final List<Auditoria> auditorias = new ArrayList<>();
        public final List<Material> repositorioMateriales = new ArrayList<>();

        public int nextVisitaId = 1;
        public int nextFacturaId = 1;
        public int nextAuditoriaId = 1;
    }

    public GestorComunidad(Datos datos) {
        this.datos = Objects.requireNonNull(datos, "datos");
    }

    public Datos getDatos() {
        return datos;
    }

    // --- Utilidades ---
    public boolean validarDni(String dni) {
        return dni != null && DNI_PATTERN.matcher(dni.trim()).matches();
    }

    public boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) return true; // opcional
        return TELEFONO_PATTERN.matcher(telefono.trim()).matches();
    }

    // --- Vecinos ---
    public Vecino registrarVecino(String dni, String nombreApellidos,
                                  String direccion, String codigoPostal, String ciudad, String telefono) {
        if (!validarDni(dni)) throw new IllegalArgumentException("DNI inválido. Formato esperado: 12345678A");
        if (!validarTelefono(telefono)) throw new IllegalArgumentException("Teléfono inválido. Debe tener 9 dígitos.");

        String key = dni.trim().toUpperCase();
        if (datos.vecinosPorDni.containsKey(key)) {
            throw new IllegalArgumentException("Ya existe un vecino con ese DNI.");
        }
        Vecino v = new Vecino(key, nombreApellidos, direccion, codigoPostal, ciudad, telefono);
        datos.vecinosPorDni.put(key, v);
        return v;
    }

    public List<Vecino> getVecinos() {
        return new ArrayList<>(datos.vecinosPorDni.values());
    }

    public Optional<Vecino> buscarVecinoPorDni(String dni) {
        if (dni == null) return Optional.empty();
        return Optional.ofNullable(datos.vecinosPorDni.get(dni.trim().toUpperCase()));
    }

    // --- Visitas ---
    public FichaVisita crearFichaVisita(Vecino vecino, LocalDate fecha, String descripcion, double importe, String administrador) {
        if (vecino == null) throw new IllegalArgumentException("Debe seleccionar un vecino.");
        if (fecha == null) throw new IllegalArgumentException("Debe indicar la fecha.");
        if (descripcion == null || descripcion.trim().isEmpty()) throw new IllegalArgumentException("Debe indicar una descripción.");
        if (administrador == null || administrador.trim().isEmpty()) throw new IllegalArgumentException("Debe indicar el nombre del administrador.");
        if (importe < 0) throw new IllegalArgumentException("El importe no puede ser negativo.");

        FichaVisita v = new FichaVisita(datos.nextVisitaId++, vecino, fecha, descripcion, importe, administrador);
        datos.visitas.add(v);
        return v;
    }

    public List<FichaVisita> getVisitas() { return new ArrayList<>(datos.visitas); }

    public List<FichaVisita> getVisitasPendientes(Vecino vecino) {
        return datos.visitas.stream()
                .filter(v -> v.getVecino().equals(vecino) && v.getEstado() == EstadoPago.IMPAGADA)
                .collect(Collectors.toList());
    }

    // --- Facturación (batch) ---
    public Factura crearFactura(Vecino vecino, LocalDate fechaFactura) {
        Objects.requireNonNull(vecino, "vecino");
        if (fechaFactura == null) throw new IllegalArgumentException("Debe indicar la fecha de la factura.");
        List<FichaVisita> pendientes = getVisitasPendientes(vecino);
        if (pendientes.isEmpty()) {
            throw new IllegalStateException("El vecino no tiene visitas pendientes.");
        }

        // Marcar pagadas (transacción simulada)
        for (FichaVisita v : pendientes) {
            v.marcarPagada();
        }

        Factura f = new Factura(datos.nextFacturaId++, fechaFactura, vecino, pendientes);
        datos.facturas.add(f);
        return f;
    }

    public List<Factura> getFacturas() { return new ArrayList<>(datos.facturas); }

    // --- Profesores / Cursos / Inscripciones ---
    public Profesor registrarProfesor(String nombre, String apellidos, String direccion, String telefono, double sueldo) {
        Profesor p = new Profesor(nombre, apellidos, direccion, telefono, sueldo);
        datos.profesores.add(p);
        return p;
    }

    public List<Profesor> getProfesores() { return new ArrayList<>(datos.profesores); }

    public Curso crearCurso(String nombre, double precio, int maxVecinos, LocalDate inicio, LocalDate fin) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre de curso obligatorio.");
        if (maxVecinos <= 0) throw new IllegalArgumentException("El máximo de vecinos debe ser > 0.");
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
        Curso c = new Curso(nombre.trim(), precio, maxVecinos, inicio, fin);
        datos.cursos.add(c);
        return c;
    }

    public List<Curso> getCursos() { return new ArrayList<>(datos.cursos); }

    public Materia addMateriaACurso(Curso curso, String nombreMateria, int horas, Profesor profesor) {
        Objects.requireNonNull(curso, "curso");
        if (nombreMateria == null || nombreMateria.trim().isEmpty()) throw new IllegalArgumentException("Nombre de materia obligatorio.");
        if (horas <= 0) throw new IllegalArgumentException("Horas debe ser > 0.");
        Materia m = new Materia(nombreMateria.trim(), horas, Objects.requireNonNull(profesor, "profesor"));
        curso.addMateria(m);
        return m;
    }

    public void inscribirVecinoEnCurso(Vecino vecino, Curso curso) {
        Objects.requireNonNull(vecino, "vecino");
        Objects.requireNonNull(curso, "curso");
        curso.inscribir(vecino); // valida cupo
    }

    // --- Auditores / Auditorías / Materiales ---
    public Auditor registrarAuditor(String nombre, String apellidos, String cif, String empresa, String direccionEmpresa, String telefono) {
        Auditor a = new Auditor(nombre, apellidos, cif, empresa, direccionEmpresa, telefono);
        datos.auditores.add(a);
        return a;
    }

    public List<Auditor> getAuditores() { return new ArrayList<>(datos.auditores); }

    public Auditoria crearAuditoria(Auditor auditor, LocalDate fechaCreacion) {
        Objects.requireNonNull(auditor, "auditor");
        if (fechaCreacion == null) throw new IllegalArgumentException("Debe indicar la fecha de creación de la auditoría.");
        Auditoria au = new Auditoria(datos.nextAuditoriaId++, auditor, fechaCreacion);
        datos.auditorias.add(au);
        return au;
    }

    public List<Auditoria> getAuditorias() { return new ArrayList<>(datos.auditorias); }

    public void asignarVisitasAAuditoria(Auditoria auditoria, List<FichaVisita> visitasAAsignar) {
        Objects.requireNonNull(auditoria, "auditoria");
        Objects.requireNonNull(visitasAAsignar, "visitasAAsignar");
        for (FichaVisita v : visitasAAsignar) {
            auditoria.asignarVisita(v);
        }
    }

    public void finalizarAuditoria(Auditoria auditoria, LocalDate fechaFin) {
        Objects.requireNonNull(auditoria, "auditoria");
        auditoria.cerrar(fechaFin);
    }

    public Material registrarMaterial(String nombre, double precio) {
        Material m = new Material(nombre, precio);
        datos.repositorioMateriales.add(m);
        return m;
    }

    public List<Material> getRepositorioMateriales() { return new ArrayList<>(datos.repositorioMateriales); }

    public void asignarMaterialAAuditoria(Auditoria auditoria, Material material) {
        Objects.requireNonNull(auditoria, "auditoria");
        Objects.requireNonNull(material, "material");
        auditoria.asignarMaterial(material);
    }

    /**
     * Datos de ejemplo desactivados por defecto para no asumir fechas.
     * Si se quisieran cargar, deberían introducirse manualmente desde la UI.
     */
}
