import modelo.*;
import persistencia.GestorPersistencia;
import servicio.GestorComunidad;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * SIGCO (demo Swing) - Implementación de funcionalidades descritas en el PDF.
 */
public class App extends JFrame {

    private final File ficheroDatos;
    private final GestorComunidad gestor;

    // Modelos de tablas
    private final DefaultTableModel vecinosModel = new DefaultTableModel(new Object[]{"DNI", "Nombre", "Dirección", "CP", "Ciudad", "Teléfono"}, 0);
    private final DefaultTableModel visitasModel = new DefaultTableModel(new Object[]{"ID", "Fecha", "Vecino", "Descripción", "Importe", "Admin", "Estado"}, 0);
    private final DefaultTableModel facturasModel = new DefaultTableModel(new Object[]{"ID", "Fecha", "Vecino", "Total", "#Visitas"}, 0);
    private final DefaultTableModel cursosModel = new DefaultTableModel(new Object[]{"Curso", "Duración", "Precio", "Inscritos"}, 0);
    private final DefaultTableModel materiasModel = new DefaultTableModel(new Object[]{"Materia", "Horas", "Profesor"}, 0);
    private final DefaultTableModel inscritosModel = new DefaultTableModel(new Object[]{"DNI", "Vecino"}, 0);
    private final DefaultTableModel auditoriasModel = new DefaultTableModel(new Object[]{"ID", "Auditor", "Creación", "Fin", "Sueldo", "#Visitas", "#Materiales"}, 0);
    private final DefaultTableModel auditoriaVisitasModel = new DefaultTableModel(new Object[]{"ID", "Vecino", "Fecha", "Importe", "Estado"}, 0);
    private final DefaultTableModel auditoriaMaterialesModel = new DefaultTableModel(new Object[]{"Material", "Precio"}, 0);

    // Componentes que dependen de selección
    private final JComboBox<Vecino> comboVecinosVisita = new JComboBox<>();
    private final JComboBox<Vecino> comboVecinosFactura = new JComboBox<>();
    private final JComboBox<Vecino> comboVecinosInscripcion = new JComboBox<>();
    private final JComboBox<Curso> comboCursosInscripcion = new JComboBox<>();
    private final JComboBox<Curso> comboCursoMateria = new JComboBox<>();
    private final JComboBox<Profesor> comboProfesorMateria = new JComboBox<>();
    private final JComboBox<Auditor> comboAuditores = new JComboBox<>();
    private final JComboBox<Auditoria> comboAuditorias = new JComboBox<>();
    private final JComboBox<FichaVisita> comboVisitasParaAuditoria = new JComboBox<>();
    private final JComboBox<Material> comboMaterialesParaAuditoria = new JComboBox<>();

    private final JTable tablaVisitas = new JTable(visitasModel);
    private final JTable tablaAuditorias = new JTable(auditoriasModel);

    public App() {
        setTitle("SIGCO - Gestión de Comunidades (Java)");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Persistencia: cargar datos (si no existe, iniciar vacío)
        this.ficheroDatos = new File("sigco.dat");
        GestorComunidad.Datos datos;
        if (ficheroDatos.exists()) {
            try {
                datos = GestorPersistencia.cargar(ficheroDatos);
            } catch (Exception ex) {
                // Si hay corrupción o incompatibilidad de serialización, arrancar vacío y avisar.
                datos = new GestorComunidad.Datos();
                JOptionPane.showMessageDialog(this,
                        "No se han podido cargar los datos guardados (se iniciará vacío).\n\nMotivo: " + ex.getMessage(),
                        "Persistencia", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            datos = new GestorComunidad.Datos();
        }
        this.gestor = new GestorComunidad(datos);

        // Guardado automático al cerrar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarDatosYSalir();
            }
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vecinos", buildVecinosPanel());
        tabs.addTab("Visitas", buildVisitasPanel());
        tabs.addTab("Facturación", buildFacturacionPanel());
        tabs.addTab("Cursos", buildCursosPanel());
        tabs.addTab("Auditorías", buildAuditoriasPanel());

        setContentPane(tabs);

        // Render para distinguir pagadas/impagadas
        tablaVisitas.setDefaultRenderer(Object.class, new EstadoPagoRenderer());

        refreshAll();
    }

    private void guardarDatosYSalir() {
        try {
            GestorPersistencia.guardar(ficheroDatos, gestor.getDatos());
            dispose();
            System.exit(0);
        } catch (Exception ex) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "No se han podido guardar los datos:\n" + ex.getMessage() + "\n\n¿Salir igualmente? (Se perderán los cambios)",
                    "Persistencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }

    // -------------------- VECINOS --------------------
    private JPanel buildVecinosPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        JTable tabla = new JTable(vecinosModel);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        JTextField dni = new JTextField();
        JTextField nombre = new JTextField();
        JTextField direccion = new JTextField();
        JTextField cp = new JTextField();
        JTextField ciudad = new JTextField();
        JTextField telefono = new JTextField();

        form.add(new JLabel("DNI (12345678A):")); form.add(dni);
        form.add(new JLabel("Nombre y apellidos:")); form.add(nombre);
        form.add(new JLabel("Dirección:")); form.add(direccion);
        form.add(new JLabel("Código postal:")); form.add(cp);
        form.add(new JLabel("Ciudad:")); form.add(ciudad);
        form.add(new JLabel("Teléfono (9 dígitos):")); form.add(telefono);

        JButton add = new JButton("Añadir vecino");
        add.addActionListener(e -> {
            try {
                gestor.registrarVecino(dni.getText(), nombre.getText(), direccion.getText(), cp.getText(), ciudad.getText(), telefono.getText());
                dni.setText(""); nombre.setText(""); direccion.setText(""); cp.setText(""); ciudad.setText(""); telefono.setText("");
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(add, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    // -------------------- VISITAS --------------------
    private JPanel buildVisitasPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        root.add(new JScrollPane(tablaVisitas), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        JTextField fecha = new JTextField(); // Se pide siempre al usuario (no asumir LocalDate.now())
        JTextField descripcion = new JTextField();
        JTextField importe = new JTextField();
        JTextField admin = new JTextField();

        form.add(new JLabel("Vecino:")); form.add(comboVecinosVisita);
        form.add(new JLabel("Fecha (YYYY-MM-DD):")); form.add(fecha);
        form.add(new JLabel("Descripción:")); form.add(descripcion);
        form.add(new JLabel("Importe:")); form.add(importe);
        form.add(new JLabel("Administrador:")); form.add(admin);

        JButton add = new JButton("Crear ficha de visita");
        add.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosVisita.getSelectedItem();
                LocalDate f = LocalDate.parse(fecha.getText().trim());
                double imp = Double.parseDouble(importe.getText().trim());
                gestor.crearFichaVisita(v, f, descripcion.getText(), imp, admin.getText());
                descripcion.setText(""); importe.setText(""); admin.setText("");
                refreshAll();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Fecha inválida. Usa YYYY-MM-DD.", "Validación", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Importe inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(add, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    // -------------------- FACTURACIÓN --------------------
    private JPanel buildFacturacionPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        JTable tabla = new JTable(facturasModel);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField fechaFactura = new JTextField(10);
        top.add(new JLabel("Vecino:"));
        top.add(comboVecinosFactura);
        top.add(new JLabel("Fecha factura (YYYY-MM-DD):"));
        top.add(fechaFactura);
        JButton facturar = new JButton("Crear factura (batch de visitas pendientes)");
        top.add(facturar);

        facturar.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosFactura.getSelectedItem();
                LocalDate fecha = LocalDate.parse(fechaFactura.getText().trim());
                Factura f = gestor.crearFactura(v, fecha);
                refreshAll();
                JOptionPane.showMessageDialog(this, "Factura creada (#" + f.getId() + ") Total: " + f.getTotal() + "€", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Facturación", JOptionPane.WARNING_MESSAGE);
            }
        });

        root.add(top, BorderLayout.NORTH);
        return root;
    }

    // -------------------- CURSOS --------------------
    private JPanel buildCursosPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        JTable tablaCursos = new JTable(cursosModel);
        JTable tablaMaterias = new JTable(materiasModel);
        JTable tablaInscritos = new JTable(inscritosModel);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tablaCursos),
                new JPanel(new GridLayout(2, 1)));
        split.setResizeWeight(0.45);
        ((JPanel) split.getRightComponent()).add(new JScrollPane(tablaMaterias));
        ((JPanel) split.getRightComponent()).add(new JScrollPane(tablaInscritos));

        root.add(split, BorderLayout.CENTER);

        // Selección de curso: cargar materias e inscritos
        tablaCursos.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaCursos.getSelectedRow();
            if (row < 0) return;
            Curso c = (Curso) comboCursosInscripcion.getItemAt(row);
            refreshMateriasEInscritos(c);
        });

        // Panel inferior: crear curso, añadir materia, inscribir
        JPanel south = new JPanel(new GridLayout(1, 3, 10, 10));
        south.add(buildCrearCursoPanel());
        south.add(buildAddMateriaPanel());
        south.add(buildInscripcionPanel());
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildCrearCursoPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Crear curso"));
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField max = new JTextField("10");
        JTextField inicio = new JTextField(); // Se pide siempre al usuario
        JTextField fin = new JTextField();    // Se pide siempre al usuario
        JButton crear = new JButton("Crear");

        p.add(new JLabel("Nombre:")); p.add(nombre);
        p.add(new JLabel("Precio:")); p.add(precio);
        p.add(new JLabel("Máx vecinos:")); p.add(max);
        p.add(new JLabel("Inicio:")); p.add(inicio);
        p.add(new JLabel("Fin:")); p.add(fin);
        p.add(new JLabel("")); p.add(crear);

        crear.addActionListener(e -> {
            try {
                double pr = Double.parseDouble(precio.getText().trim());
                int mx = Integer.parseInt(max.getText().trim());
                LocalDate i = LocalDate.parse(inicio.getText().trim());
                LocalDate f = LocalDate.parse(fin.getText().trim());
                gestor.crearCurso(nombre.getText(), pr, mx, i, f);
                nombre.setText(""); precio.setText("");
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cursos", JOptionPane.WARNING_MESSAGE);
            }
        });
        return p;
    }

    private JPanel buildAddMateriaPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Añadir materia"));

        JTextField nombre = new JTextField();
        JTextField horas = new JTextField();
        JButton add = new JButton("Añadir");

        p.add(new JLabel("Curso:")); p.add(comboCursoMateria);
        p.add(new JLabel("Materia:")); p.add(nombre);
        p.add(new JLabel("Horas:")); p.add(horas);
        p.add(new JLabel("Profesor:")); p.add(comboProfesorMateria);
        p.add(new JLabel("")); p.add(add);

        add.addActionListener(e -> {
            try {
                Curso c = (Curso) comboCursoMateria.getSelectedItem();
                Profesor prof = (Profesor) comboProfesorMateria.getSelectedItem();
                int h = Integer.parseInt(horas.getText().trim());
                gestor.addMateriaACurso(c, nombre.getText(), h, prof);
                nombre.setText(""); horas.setText("");
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cursos", JOptionPane.WARNING_MESSAGE);
            }
        });
        return p;
    }

    private JPanel buildInscripcionPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Inscribir vecino"));

        JButton inscribir = new JButton("Inscribir");

        p.add(new JLabel("Vecino:")); p.add(comboVecinosInscripcion);
        p.add(new JLabel("Curso:")); p.add(comboCursosInscripcion);
        p.add(new JLabel("")); p.add(inscribir);

        inscribir.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosInscripcion.getSelectedItem();
                Curso c = (Curso) comboCursosInscripcion.getSelectedItem();
                gestor.inscribirVecinoEnCurso(v, c);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cursos", JOptionPane.WARNING_MESSAGE);
            }
        });
        return p;
    }

    private void refreshMateriasEInscritos(Curso c) {
        materiasModel.setRowCount(0);
        for (Materia m : c.getMaterias()) {
            materiasModel.addRow(new Object[]{m.getNombre(), m.getHoras(), m.getProfesor()});
        }

        inscritosModel.setRowCount(0);
        for (Vecino v : c.getInscritos()) {
            inscritosModel.addRow(new Object[]{v.getDni(), v.getNombreApellidos()});
        }
    }

    // -------------------- AUDITORÍAS --------------------
    private JPanel buildAuditoriasPanel() {
        JPanel root = new JPanel(new BorderLayout(10, 10));

        tablaAuditorias.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaAuditorias.getSelectedRow();
            if (row < 0) return;
            Auditoria a = (Auditoria) comboAuditorias.getItemAt(row);
            refreshDetalleAuditoria(a);
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tablaAuditorias),
                buildAuditoriaDetallePanel());
        split.setResizeWeight(0.55);
        root.add(split, BorderLayout.CENTER);

        root.add(buildAuditoriaAccionesPanel(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildAuditoriaDetallePanel() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(new JScrollPane(new JTable(auditoriaVisitasModel)));
        p.add(new JScrollPane(new JTable(auditoriaMaterialesModel)));
        return p;
    }

    private JPanel buildAuditoriaAccionesPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 10));
        p.add(buildCrearAuditoriaPanel());
        p.add(buildAsignarVisitasPanel());
        p.add(buildCerrarYMaterialPanel());
        return p;
    }

    private JPanel buildCrearAuditoriaPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Crear auditoría"));
        JTextField fechaCreacion = new JTextField();
        JButton crear = new JButton("Crear");
        p.add(new JLabel("Auditor:")); p.add(comboAuditores);
        p.add(new JLabel("Fecha creación (YYYY-MM-DD):")); p.add(fechaCreacion);
        p.add(new JLabel("")); p.add(crear);

        crear.addActionListener(e -> {
            try {
                Auditor aud = (Auditor) comboAuditores.getSelectedItem();
                LocalDate fc = LocalDate.parse(fechaCreacion.getText().trim());
                gestor.crearAuditoria(aud, fc);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Auditorías", JOptionPane.WARNING_MESSAGE);
            }
        });
        return p;
    }

    private JPanel buildAsignarVisitasPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Asignar visitas"));

        JButton asignar = new JButton("Asignar");
        p.add(new JLabel("Auditoría:")); p.add(comboAuditorias);
        p.add(new JLabel("Visita:")); p.add(comboVisitasParaAuditoria);
        p.add(new JLabel("")); p.add(asignar);

        asignar.addActionListener(e -> {
            try {
                Auditoria a = (Auditoria) comboAuditorias.getSelectedItem();
                FichaVisita v = (FichaVisita) comboVisitasParaAuditoria.getSelectedItem();
                gestor.asignarVisitasAAuditoria(a, List.of(v));
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Auditorías", JOptionPane.WARNING_MESSAGE);
            }
        });
        return p;
    }

    private JPanel buildCerrarYMaterialPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Cerrar / Material"));

        JTextField fechaFin = new JTextField(); // Se pide siempre al usuario
        JButton cerrar = new JButton("Finalizar auditoría");
        JButton addMaterial = new JButton("Asignar material");

        p.add(new JLabel("Auditoría:")); p.add(comboAuditorias);
        p.add(new JLabel("Fecha fin (YYYY-MM-DD):")); p.add(fechaFin);
        p.add(new JLabel("")); p.add(cerrar);
        p.add(new JLabel("Material:")); p.add(comboMaterialesParaAuditoria);
        p.add(new JLabel("")); p.add(addMaterial);

        cerrar.addActionListener(e -> {
            try {
                Auditoria a = (Auditoria) comboAuditorias.getSelectedItem();
                LocalDate fin = LocalDate.parse(fechaFin.getText().trim());
                gestor.finalizarAuditoria(a, fin);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Auditorías", JOptionPane.WARNING_MESSAGE);
            }
        });

        addMaterial.addActionListener(e -> {
            try {
                Auditoria a = (Auditoria) comboAuditorias.getSelectedItem();
                Material m = (Material) comboMaterialesParaAuditoria.getSelectedItem();
                gestor.asignarMaterialAAuditoria(a, m);
                refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Auditorías", JOptionPane.WARNING_MESSAGE);
            }
        });
        return p;
    }

    private void refreshDetalleAuditoria(Auditoria a) {
        auditoriaVisitasModel.setRowCount(0);
        for (FichaVisita v : a.getVisitas()) {
            auditoriaVisitasModel.addRow(new Object[]{v.getId(), v.getVecino(), v.getFecha(), v.getImporte(), v.getEstado()});
        }
        auditoriaMaterialesModel.setRowCount(0);
        for (Material m : a.getMateriales()) {
            auditoriaMaterialesModel.addRow(new Object[]{m.getNombre(), m.getPrecio()});
        }
    }

    // -------------------- REFRESH GLOBAL --------------------
    private void refreshAll() {
        refreshVecinos();
        refreshVisitas();
        refreshFacturas();
        refreshCursos();
        refreshAuditorias();
        refreshCombos();
    }

    private void refreshVecinos() {
        vecinosModel.setRowCount(0);
        for (Vecino v : gestor.getVecinos()) {
            vecinosModel.addRow(new Object[]{v.getDni(), v.getNombreApellidos(), v.getDireccion(), v.getCodigoPostal(), v.getCiudad(), v.getTelefono()});
        }
    }

    private void refreshVisitas() {
        visitasModel.setRowCount(0);
        for (FichaVisita v : gestor.getVisitas()) {
            visitasModel.addRow(new Object[]{v.getId(), v.getFecha(), v.getVecino(), v.getDescripcion(), v.getImporte(), v.getNombreAdministrador(), v.getEstado()});
        }
    }

    private void refreshFacturas() {
        facturasModel.setRowCount(0);
        for (Factura f : gestor.getFacturas()) {
            facturasModel.addRow(new Object[]{f.getId(), f.getFechaCreacion(), f.getVecino(), f.getTotal(), f.getVisitas().size()});
        }
    }

    private void refreshCursos() {
        cursosModel.setRowCount(0);
        for (Curso c : gestor.getCursos()) {
            cursosModel.addRow(new Object[]{c.getNombre(), c.getDuracionTotalHoras() + "h", c.getPrecio(), c.getInscritos().size() + "/" + c.getMaxVecinos()});
        }
        // refrescar detalle con el curso seleccionado en combo
        Curso selected = (Curso) comboCursosInscripcion.getSelectedItem();
        if (selected != null) refreshMateriasEInscritos(selected);
    }

    private void refreshAuditorias() {
        auditoriasModel.setRowCount(0);
        for (Auditoria a : gestor.getAuditorias()) {
            auditoriasModel.addRow(new Object[]{a.getId(), a.getAuditor(), a.getFechaCreacion(), a.getFechaFin(), a.getSueldoAuditor(), a.getVisitas().size(), a.getMateriales().size()});
        }
        Auditoria selected = (Auditoria) comboAuditorias.getSelectedItem();
        if (selected != null) refreshDetalleAuditoria(selected);
    }

    private void refreshCombos() {
        // Vecinos
        refillCombo(comboVecinosVisita, gestor.getVecinos());
        refillCombo(comboVecinosFactura, gestor.getVecinos());
        refillCombo(comboVecinosInscripcion, gestor.getVecinos());

        // Profesores
        refillCombo(comboProfesorMateria, gestor.getProfesores());

        // Cursos
        refillCombo(comboCursosInscripcion, gestor.getCursos());
        refillCombo(comboCursoMateria, gestor.getCursos());

        // Auditores / auditorías
        refillCombo(comboAuditores, gestor.getAuditores());
        refillCombo(comboAuditorias, gestor.getAuditorias());

        // Visitas / material
        refillCombo(comboVisitasParaAuditoria, gestor.getVisitas());
        refillCombo(comboMaterialesParaAuditoria, gestor.getRepositorioMateriales());
    }

    private static <T> void refillCombo(JComboBox<T> combo, List<T> items) {
        T sel = (T) combo.getSelectedItem();
        combo.removeAllItems();
        for (T it : items) combo.addItem(it);
        if (sel != null) combo.setSelectedItem(sel);
    }

    // Renderer: distinguir visitas pagadas/impagadas
    private static class EstadoPagoRenderer extends JLabel implements TableCellRenderer {
        EstadoPagoRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Object estadoObj = table.getModel().getValueAt(row, 6);
            EstadoPago estado = (estadoObj instanceof EstadoPago) ? (EstadoPago) estadoObj : null;

            setText(value == null ? "" : value.toString());

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
                return this;
            }

            if (estado == EstadoPago.PAGADA) {
                setBackground(new Color(230, 255, 230));
            } else {
                setBackground(new Color(255, 235, 235));
            }
            setForeground(Color.BLACK);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
