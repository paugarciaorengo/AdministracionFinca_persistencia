import modelo.*;
import persistencia.GestorPersistencia;
import servicio.GestorComunidad;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * SIGCO (demo Swing) - Interfaz mejorada con Nimbus L&F y diseño limpio.
 */
public class App extends JFrame {

    private final File ficheroDatos;
    private final GestorComunidad gestor;

    // Caches locales para mapeo (necesarios para edición/borrado)
    private List<Profesor> listaProfesores = new ArrayList<>();
    private List<Auditor> listaAuditoresGestion = new ArrayList<>();
    private List<Material> listaMaterialesGestion = new ArrayList<>();

    // --- MODELOS DE TABLAS ---
    // Hacemos que las celdas no sean editables directamente (isCellEditable return false) para evitar confusiones
    private final DefaultTableModel vecinosModel = new NonEditableModel(new Object[]{"DNI", "Nombre", "Dirección", "CP", "Ciudad", "Teléfono"}, 0);
    private final DefaultTableModel profesoresModel = new NonEditableModel(new Object[]{"Nombre", "Apellidos", "Dirección", "Teléfono", "Sueldo"}, 0);
    private final DefaultTableModel auditoresGestionModel = new NonEditableModel(new Object[]{"Nombre", "Apellidos", "CIF", "Empresa", "Dirección", "Teléfono"}, 0);
    private final DefaultTableModel materialesModel = new NonEditableModel(new Object[]{"Nombre", "Precio"}, 0);
    
    private final DefaultTableModel visitasModel = new NonEditableModel(new Object[]{"ID", "Fecha", "Vecino", "Descripción", "Importe", "Admin", "Estado"}, 0);
    private final DefaultTableModel facturasModel = new NonEditableModel(new Object[]{"ID", "Fecha", "Vecino", "Total", "#Visitas"}, 0);
    private final DefaultTableModel cursosModel = new NonEditableModel(new Object[]{"Curso", "Duración", "Precio", "Inscritos"}, 0);
    private final DefaultTableModel materiasModel = new NonEditableModel(new Object[]{"Materia", "Horas", "Profesor"}, 0);
    private final DefaultTableModel inscritosModel = new NonEditableModel(new Object[]{"DNI", "Vecino"}, 0);
    private final DefaultTableModel auditoriasModel = new NonEditableModel(new Object[]{"ID", "Auditor", "Creación", "Fin", "Sueldo", "#Visitas", "#Materiales"}, 0);
    private final DefaultTableModel auditoriaVisitasModel = new NonEditableModel(new Object[]{"ID", "Vecino", "Fecha", "Importe", "Estado"}, 0);
    private final DefaultTableModel auditoriaMaterialesModel = new NonEditableModel(new Object[]{"Material", "Precio"}, 0);

    // --- COMPONENTES DE SELECCIÓN ---
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

    // --- TABLAS ---
    private final JTable tablaVisitas = createStyledTable(visitasModel);
    private final JTable tablaAuditorias = createStyledTable(auditoriasModel);
    private final JTable tablaProfesores = createStyledTable(profesoresModel);
    private final JTable tablaAuditoresGestion = createStyledTable(auditoresGestionModel);
    private final JTable tablaMateriales = createStyledTable(materialesModel);

    public App() {
        // Configurar tema Nimbus antes de iniciar componentes
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }

        setTitle("SIGCO - Gestión de Comunidades");
        setSize(1200, 750); // Un poco más grande para respirar
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ajustes globales de UI
        UIManager.put("Table.alternateRowColor", new Color(242, 242, 242));

        // Persistencia
        this.ficheroDatos = new File("sigco.dat");
        GestorComunidad.Datos datos;
        if (ficheroDatos.exists()) {
            try {
                datos = GestorPersistencia.cargar(ficheroDatos);
            } catch (Exception ex) {
                datos = new GestorComunidad.Datos();
                JOptionPane.showMessageDialog(this,
                        "Error al cargar datos. Se iniciará vacío.\n" + ex.getMessage(),
                        "Error de Carga", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            datos = new GestorComunidad.Datos();
        }
        this.gestor = new GestorComunidad(datos);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarDatosYSalir();
            }
        });

        // Construcción de pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        tabs.addTab("👥 Vecinos", buildVecinosPanel());
        tabs.addTab("🎓 Profesores", buildProfesoresPanel());
        tabs.addTab("📋 Auditores", buildAuditoresGestionPanel());
        tabs.addTab("📦 Materiales", buildMaterialesPanel());
        tabs.addTab("📅 Visitas", buildVisitasPanel());
        tabs.addTab("💶 Facturación", buildFacturacionPanel());
        tabs.addTab("📚 Cursos", buildCursosPanel());
        tabs.addTab("🔍 Auditorías", buildAuditoriasPanel());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.add(tabs, BorderLayout.CENTER);
        
        setContentPane(mainPanel);

        // Render específico para visitas
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
                    "No se han podido guardar los datos:\n" + ex.getMessage() + "\n\n¿Salir igualmente?",
                    "Error de Guardado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }

    // --- HELPER PARA ESTILAR TABLAS ---
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28); // Filas más altas
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // Permite ordenar por columnas
        return table;
    }

    // --- HELPER PARA PANELES COMUNES ---
    // Crea un panel estándar con tabla arriba y formulario abajo
    private JPanel createStandardPanel(JTable table, JPanel formPanel) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        if (formPanel != null) {
            root.add(formPanel, BorderLayout.SOUTH);
        }
        return root;
    }

    // --- VECINOS ---
    private JPanel buildVecinosPanel() {
        JTable tabla = createStyledTable(vecinosModel);

        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10)); // Más columnas para ahorrar altura
        form.setBorder(BorderFactory.createTitledBorder("Nuevo Vecino"));
        
        JTextField dni = new JTextField();
        JTextField nombre = new JTextField();
        JTextField direccion = new JTextField();
        JTextField cp = new JTextField();
        JTextField ciudad = new JTextField();
        JTextField telefono = new JTextField();

        addLabeledField(form, "DNI:", dni);
        addLabeledField(form, "Nombre:", nombre);
        addLabeledField(form, "Dirección:", direccion);
        addLabeledField(form, "CP:", cp);
        addLabeledField(form, "Ciudad:", ciudad);
        addLabeledField(form, "Teléfono:", telefono);

        JButton add = new JButton("Añadir Vecino");
        add.setFont(new Font("SansSerif", Font.BOLD, 12));
        add.addActionListener(e -> {
            try {
                gestor.registrarVecino(dni.getText(), nombre.getText(), direccion.getText(), cp.getText(), ciudad.getText(), telefono.getText());
                clearFields(dni, nombre, direccion, cp, ciudad, telefono);
                refreshAll();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        JPanel south = new JPanel(new BorderLayout(10, 10));
        south.add(form, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(add);
        south.add(btnPanel, BorderLayout.SOUTH);

        return createStandardPanel(tabla, south);
    }

    // --- PROFESORES ---
    private JPanel buildProfesoresPanel() {
        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Gestión Profesor"));

        JTextField nombre = new JTextField();
        JTextField apellidos = new JTextField();
        JTextField direccion = new JTextField();
        JTextField telefono = new JTextField();
        JTextField sueldo = new JTextField();

        addLabeledField(form, "Nombre:", nombre);
        addLabeledField(form, "Apellidos:", apellidos);
        addLabeledField(form, "Dirección:", direccion);
        addLabeledField(form, "Teléfono:", telefono);
        addLabeledField(form, "Sueldo:", sueldo);

        // Botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Añadir");
        JButton update = new JButton("Modificar");
        JButton delete = new JButton("Eliminar");
        buttons.add(add); buttons.add(update); buttons.add(delete);

        tablaProfesores.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaProfesores.getSelectedRow();
            // Convertir índice de vista a modelo (por si está ordenado)
            if (row >= 0) {
                int modelRow = tablaProfesores.convertRowIndexToModel(row);
                if (modelRow < listaProfesores.size()) {
                    Profesor p = listaProfesores.get(modelRow);
                    nombre.setText(p.getNombre());
                    apellidos.setText(p.getApellidos());
                    direccion.setText(p.getDireccion());
                    telefono.setText(p.getTelefono());
                    sueldo.setText(String.valueOf(p.getSueldo()));
                }
            }
        });

        add.addActionListener(e -> {
            try {
                gestor.registrarProfesor(nombre.getText(), apellidos.getText(), direccion.getText(), telefono.getText(), Double.parseDouble(sueldo.getText().trim()));
                refreshAll();
                clearFields(nombre, apellidos, direccion, telefono, sueldo);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        update.addActionListener(e -> {
            int row = tablaProfesores.getSelectedRow();
            if (row < 0) { showWarning("Selecciona un profesor."); return; }
            try {
                Profesor p = listaProfesores.get(tablaProfesores.convertRowIndexToModel(row));
                p.setNombre(nombre.getText());
                p.setApellidos(apellidos.getText());
                p.setDireccion(direccion.getText());
                p.setTelefono(telefono.getText());
                p.setSueldo(Double.parseDouble(sueldo.getText().trim()));
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        delete.addActionListener(e -> {
            int row = tablaProfesores.getSelectedRow();
            if (row < 0) { showWarning("Selecciona un profesor."); return; }
            if (confirm("¿Eliminar profesor?")) {
                gestor.eliminarProfesor(listaProfesores.get(tablaProfesores.convertRowIndexToModel(row)));
                refreshAll();
                clearFields(nombre, apellidos, direccion, telefono, sueldo);
            }
        });

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        return createStandardPanel(tablaProfesores, south);
    }

    // --- AUDITORES ---
    private JPanel buildAuditoresGestionPanel() {
        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Gestión Auditor"));

        JTextField nombre = new JTextField();
        JTextField apellidos = new JTextField();
        JTextField cif = new JTextField();
        JTextField empresa = new JTextField();
        JTextField direccion = new JTextField();
        JTextField telefono = new JTextField();

        addLabeledField(form, "Nombre:", nombre);
        addLabeledField(form, "Apellidos:", apellidos);
        addLabeledField(form, "CIF Empresa:", cif);
        addLabeledField(form, "Empresa:", empresa);
        addLabeledField(form, "Dirección:", direccion);
        addLabeledField(form, "Teléfono:", telefono);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Añadir");
        JButton update = new JButton("Modificar");
        JButton delete = new JButton("Eliminar");
        buttons.add(add); buttons.add(update); buttons.add(delete);

        tablaAuditoresGestion.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaAuditoresGestion.getSelectedRow();
            if (row >= 0) {
                Auditor a = listaAuditoresGestion.get(tablaAuditoresGestion.convertRowIndexToModel(row));
                nombre.setText(a.getNombre());
                apellidos.setText(a.getApellidos());
                cif.setText(a.getCifEmpresa());
                empresa.setText(a.getNombreEmpresa());
                direccion.setText(a.getDireccionEmpresa());
                telefono.setText(a.getTelefono());
            }
        });

        add.addActionListener(e -> {
            try {
                gestor.registrarAuditor(nombre.getText(), apellidos.getText(), cif.getText(), empresa.getText(), direccion.getText(), telefono.getText());
                refreshAll();
                clearFields(nombre, apellidos, cif, empresa, direccion, telefono);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        update.addActionListener(e -> {
            int row = tablaAuditoresGestion.getSelectedRow();
            if (row < 0) { showWarning("Selecciona un auditor."); return; }
            try {
                Auditor a = listaAuditoresGestion.get(tablaAuditoresGestion.convertRowIndexToModel(row));
                a.setNombre(nombre.getText());
                a.setApellidos(apellidos.getText());
                a.setCifEmpresa(cif.getText());
                a.setNombreEmpresa(empresa.getText());
                a.setDireccionEmpresa(direccion.getText());
                a.setTelefono(telefono.getText());
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        delete.addActionListener(e -> {
            int row = tablaAuditoresGestion.getSelectedRow();
            if (row < 0) { showWarning("Selecciona un auditor."); return; }
            if (confirm("¿Eliminar auditor?")) {
                gestor.eliminarAuditor(listaAuditoresGestion.get(tablaAuditoresGestion.convertRowIndexToModel(row)));
                refreshAll();
                clearFields(nombre, apellidos, cif, empresa, direccion, telefono);
            }
        });

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        return createStandardPanel(tablaAuditoresGestion, south);
    }

    // --- MATERIALES ---
    private JPanel buildMaterialesPanel() {
        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Gestión Material"));

        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();

        addLabeledField(form, "Nombre:", nombre);
        addLabeledField(form, "Precio (€):", precio);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Añadir");
        JButton update = new JButton("Modificar");
        JButton delete = new JButton("Eliminar");
        buttons.add(add); buttons.add(update); buttons.add(delete);

        tablaMateriales.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaMateriales.getSelectedRow();
            if (row >= 0) {
                Material m = listaMaterialesGestion.get(tablaMateriales.convertRowIndexToModel(row));
                nombre.setText(m.getNombre());
                precio.setText(String.valueOf(m.getPrecio()));
            }
        });

        add.addActionListener(e -> {
            try {
                gestor.registrarMaterial(nombre.getText(), Double.parseDouble(precio.getText().trim()));
                refreshAll();
                clearFields(nombre, precio);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        update.addActionListener(e -> {
            int row = tablaMateriales.getSelectedRow();
            if (row < 0) { showWarning("Selecciona un material."); return; }
            try {
                Material m = listaMaterialesGestion.get(tablaMateriales.convertRowIndexToModel(row));
                m.setNombre(nombre.getText());
                m.setPrecio(Double.parseDouble(precio.getText().trim()));
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        delete.addActionListener(e -> {
            int row = tablaMateriales.getSelectedRow();
            if (row < 0) { showWarning("Selecciona un material."); return; }
            if (confirm("¿Eliminar material?")) {
                gestor.eliminarMaterial(listaMaterialesGestion.get(tablaMateriales.convertRowIndexToModel(row)));
                refreshAll();
                clearFields(nombre, precio);
            }
        });

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        return createStandardPanel(tablaMateriales, south);
    }

    // --- VISITAS ---
    private JPanel buildVisitasPanel() {
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Nueva Visita"));

        JTextField fecha = new JTextField();
        JTextField descripcion = new JTextField();
        JTextField importe = new JTextField();
        JTextField admin = new JTextField();

        addLabeledField(form, "Vecino:", comboVecinosVisita);
        addLabeledField(form, "Fecha (YYYY-MM-DD):", fecha);
        addLabeledField(form, "Descripción:", descripcion);
        addLabeledField(form, "Importe (€):", importe);
        addLabeledField(form, "Administrador:", admin);

        JButton add = new JButton("Crear Visita");
        add.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosVisita.getSelectedItem();
                LocalDate f = LocalDate.parse(fecha.getText().trim());
                double imp = Double.parseDouble(importe.getText().trim());
                gestor.crearFichaVisita(v, f, descripcion.getText(), imp, admin.getText());
                clearFields(descripcion, importe, admin);
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        JPanel south = new JPanel(new BorderLayout(10,10));
        south.add(form, BorderLayout.CENTER);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(add);
        south.add(btnP, BorderLayout.SOUTH);
        
        return createStandardPanel(tablaVisitas, south);
    }

    // --- FACTURACIÓN ---
    private JPanel buildFacturacionPanel() {
        JTable tabla = createStyledTable(facturasModel);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        top.setBorder(BorderFactory.createTitledBorder("Generar Factura"));
        
        JTextField fechaFactura = new JTextField(10);
        
        top.add(new JLabel("Vecino:"));
        top.add(comboVecinosFactura);
        top.add(new JLabel("Fecha (YYYY-MM-DD):"));
        top.add(fechaFactura);
        
        JButton facturar = new JButton("Facturar Pendientes");
        top.add(facturar);

        facturar.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosFactura.getSelectedItem();
                LocalDate fecha = LocalDate.parse(fechaFactura.getText().trim());
                Factura f = gestor.crearFactura(v, fecha);
                refreshAll();
                JOptionPane.showMessageDialog(this, 
                    "Factura creada con éxito\nID: " + f.getId() + "\nTotal: " + f.getTotal() + "€", 
                    "Facturación", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return root;
    }

    // --- CURSOS ---
    private JPanel buildCursosPanel() {
        JTable tablaCursos = createStyledTable(cursosModel);
        JTable tablaMaterias = createStyledTable(materiasModel);
        JTable tablaInscritos = createStyledTable(inscritosModel);

        // Panel dividido: Izq(Cursos) - Der(Materias/Inscritos)
        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaMaterias), new JScrollPane(tablaInscritos));
        splitRight.setResizeWeight(0.5);

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tablaCursos), splitRight);
        splitMain.setResizeWeight(0.4);

        // Listener selección curso
        tablaCursos.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaCursos.getSelectedRow();
            if (row < 0) return;
            // Ojo: usar modelo si hay ordenación
            Curso c = (Curso) comboCursosInscripcion.getItemAt(tablaCursos.convertRowIndexToModel(row));
            refreshMateriasEInscritos(c);
        });

        // Panel inferior con 3 formularios
        JPanel south = new JPanel(new GridLayout(1, 3, 10, 0));
        south.setBorder(new EmptyBorder(5, 0, 0, 0));
        south.add(buildCrearCursoPanel());
        south.add(buildAddMateriaPanel());
        south.add(buildInscripcionPanel());

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        root.add(splitMain, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildCrearCursoPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Nuevo Curso"));
        
        JTextField nombre = new JTextField();
        JTextField precio = new JTextField();
        JTextField max = new JTextField("10");
        JTextField inicio = new JTextField();
        JTextField fin = new JTextField();
        JButton crear = new JButton("Crear");

        p.add(new JLabel("Nombre:")); p.add(nombre);
        p.add(new JLabel("Precio:")); p.add(precio);
        p.add(new JLabel("Máx:")); p.add(max);
        p.add(new JLabel("Inicio:")); p.add(inicio);
        p.add(new JLabel("Fin:")); p.add(fin);
        p.add(new JLabel("")); p.add(crear);

        crear.addActionListener(e -> {
            try {
                gestor.crearCurso(nombre.getText(), Double.parseDouble(precio.getText()), 
                        Integer.parseInt(max.getText()), LocalDate.parse(inicio.getText()), LocalDate.parse(fin.getText()));
                clearFields(nombre, precio, inicio, fin);
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        return p;
    }

    private JPanel buildAddMateriaPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Añadir Materia"));
        
        JTextField nombre = new JTextField();
        JTextField horas = new JTextField();
        JButton add = new JButton("Añadir");

        p.add(new JLabel("Curso:")); p.add(comboCursoMateria);
        p.add(new JLabel("Materia:")); p.add(nombre);
        p.add(new JLabel("Horas:")); p.add(horas);
        p.add(new JLabel("Prof:")); p.add(comboProfesorMateria);
        p.add(new JLabel("")); p.add(add);

        add.addActionListener(e -> {
            try {
                gestor.addMateriaACurso((Curso)comboCursoMateria.getSelectedItem(), nombre.getText(), Integer.parseInt(horas.getText()), (Profesor)comboProfesorMateria.getSelectedItem());
                clearFields(nombre, horas);
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        return p;
    }

    private JPanel buildInscripcionPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Inscripción"));
        
        JButton inscribir = new JButton("Inscribir");
        p.add(new JLabel("Vecino:")); p.add(comboVecinosInscripcion);
        p.add(new JLabel("Curso:")); p.add(comboCursosInscripcion);
        p.add(new JLabel("")); p.add(inscribir);

        inscribir.addActionListener(e -> {
            try {
                gestor.inscribirVecinoEnCurso((Vecino)comboVecinosInscripcion.getSelectedItem(), (Curso)comboCursosInscripcion.getSelectedItem());
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        return p;
    }

    private void refreshMateriasEInscritos(Curso c) {
        materiasModel.setRowCount(0);
        if (c == null) return;
        for (Materia m : c.getMaterias()) materiasModel.addRow(new Object[]{m.getNombre(), m.getHoras(), m.getProfesor()});
        inscritosModel.setRowCount(0);
        for (Vecino v : c.getInscritos()) inscritosModel.addRow(new Object[]{v.getDni(), v.getNombreApellidos()});
    }

    // --- AUDITORÍAS ---
    private JPanel buildAuditoriasPanel() {
        tablaAuditorias.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaAuditorias.getSelectedRow();
            if (row < 0) return;
            Auditoria a = (Auditoria) comboAuditorias.getItemAt(tablaAuditorias.convertRowIndexToModel(row));
            refreshDetalleAuditoria(a);
        });

        JTable tVisitas = createStyledTable(auditoriaVisitasModel);
        JTable tMateriales = createStyledTable(auditoriaMaterialesModel);
        
        JPanel detailPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        detailPanel.add(new JScrollPane(tVisitas));
        detailPanel.add(new JScrollPane(tMateriales));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaAuditorias), detailPanel);
        split.setResizeWeight(0.5);

        // Acciones
        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 0));
        actions.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        // Crear
        JPanel p1 = new JPanel(new GridLayout(0,2)); p1.setBorder(BorderFactory.createTitledBorder("Nueva"));
        JTextField fechaC = new JTextField(); JButton bCrear = new JButton("Crear");
        p1.add(new JLabel("Auditor:")); p1.add(comboAuditores); p1.add(new JLabel("Fecha:")); p1.add(fechaC); p1.add(new JLabel("")); p1.add(bCrear);
        bCrear.addActionListener(ev -> {
            try { gestor.crearAuditoria((Auditor)comboAuditores.getSelectedItem(), LocalDate.parse(fechaC.getText())); refreshAll(); } catch(Exception ex){showError(ex.getMessage());}
        });

        // Asignar Visita
        JPanel p2 = new JPanel(new GridLayout(0,2)); p2.setBorder(BorderFactory.createTitledBorder("Asignar Visita"));
        JButton bAsig = new JButton("Asignar");
        p2.add(new JLabel("Auditoría:")); p2.add(comboAuditorias); p2.add(new JLabel("Visita:")); p2.add(comboVisitasParaAuditoria); p2.add(new JLabel("")); p2.add(bAsig);
        bAsig.addActionListener(ev -> {
            try { gestor.asignarVisitasAAuditoria((Auditoria)comboAuditorias.getSelectedItem(), List.of((FichaVisita)comboVisitasParaAuditoria.getSelectedItem())); refreshAll(); } catch(Exception ex){showError(ex.getMessage());}
        });

        // Cerrar / Material
        JPanel p3 = new JPanel(new GridLayout(0,2)); p3.setBorder(BorderFactory.createTitledBorder("Gestión"));
        JTextField fechaF = new JTextField(); JButton bCerrar = new JButton("Cerrar"); JButton bMat = new JButton("Add Mat");
        p3.add(new JLabel("Fecha Fin:")); p3.add(fechaF); p3.add(bCerrar); p3.add(new JLabel(""));
        p3.add(new JLabel("Mat:")); p3.add(comboMaterialesParaAuditoria); p3.add(bMat);
        
        bCerrar.addActionListener(ev -> { try { gestor.finalizarAuditoria((Auditoria)comboAuditorias.getSelectedItem(), LocalDate.parse(fechaF.getText())); refreshAll(); } catch(Exception ex){showError(ex.getMessage());}});
        bMat.addActionListener(ev -> { try { gestor.asignarMaterialAAuditoria((Auditoria)comboAuditorias.getSelectedItem(), (Material)comboMaterialesParaAuditoria.getSelectedItem()); refreshAll(); } catch(Exception ex){showError(ex.getMessage());}});
        
        actions.add(p1); actions.add(p2); actions.add(p3);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        root.add(split, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void refreshDetalleAuditoria(Auditoria a) {
        auditoriaVisitasModel.setRowCount(0);
        auditoriaMaterialesModel.setRowCount(0);
        if (a == null) return;
        for (FichaVisita v : a.getVisitas()) auditoriaVisitasModel.addRow(new Object[]{v.getId(), v.getVecino(), v.getFecha(), v.getImporte(), v.getEstado()});
        for (Material m : a.getMateriales()) auditoriaMaterialesModel.addRow(new Object[]{m.getNombre(), m.getPrecio()});
    }

    // --- UTILS UI ---
    private void addLabeledField(JPanel p, String label, JComponent c) {
        p.add(new JLabel(label, SwingConstants.RIGHT));
        p.add(c);
    }
    
    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
    
    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // --- REFRESH ---
    private void refreshAll() {
        refreshVecinos();
        refreshProfesores(); 
        refreshAuditoresGestion();
        refreshMateriales();
        refreshVisitas();
        refreshFacturas();
        refreshCursos();
        refreshAuditorias();
        refreshCombos();
    }

    private void refreshVecinos() {
        vecinosModel.setRowCount(0);
        for (Vecino v : gestor.getVecinos()) vecinosModel.addRow(new Object[]{v.getDni(), v.getNombreApellidos(), v.getDireccion(), v.getCodigoPostal(), v.getCiudad(), v.getTelefono()});
    }
    
    private void refreshProfesores() {
        profesoresModel.setRowCount(0);
        listaProfesores = gestor.getProfesores();
        for (Profesor p : listaProfesores) profesoresModel.addRow(new Object[]{p.getNombre(), p.getApellidos(), p.getDireccion(), p.getTelefono(), p.getSueldo()});
    }

    private void refreshAuditoresGestion() {
        auditoresGestionModel.setRowCount(0);
        listaAuditoresGestion = gestor.getAuditores();
        for (Auditor a : listaAuditoresGestion) auditoresGestionModel.addRow(new Object[]{a.getNombre(), a.getApellidos(), a.getCifEmpresa(), a.getNombreEmpresa(), a.getDireccionEmpresa(), a.getTelefono()});
    }

    private void refreshMateriales() {
        materialesModel.setRowCount(0);
        listaMaterialesGestion = gestor.getRepositorioMateriales();
        for (Material m : listaMaterialesGestion) materialesModel.addRow(new Object[]{m.getNombre(), m.getPrecio()});
    }

    private void refreshVisitas() {
        visitasModel.setRowCount(0);
        for (FichaVisita v : gestor.getVisitas()) visitasModel.addRow(new Object[]{v.getId(), v.getFecha(), v.getVecino(), v.getDescripcion(), v.getImporte(), v.getNombreAdministrador(), v.getEstado()});
    }

    private void refreshFacturas() {
        facturasModel.setRowCount(0);
        for (Factura f : gestor.getFacturas()) facturasModel.addRow(new Object[]{f.getId(), f.getFechaCreacion(), f.getVecino(), f.getTotal(), f.getVisitas().size()});
    }

    private void refreshCursos() {
        cursosModel.setRowCount(0);
        for (Curso c : gestor.getCursos()) cursosModel.addRow(new Object[]{c.getNombre(), c.getDuracionTotalHoras() + "h", c.getPrecio(), c.getInscritos().size() + "/" + c.getMaxVecinos()});
        Curso selected = (Curso) comboCursosInscripcion.getSelectedItem();
        refreshMateriasEInscritos(selected);
    }

    private void refreshAuditorias() {
        auditoriasModel.setRowCount(0);
        for (Auditoria a : gestor.getAuditorias()) auditoriasModel.addRow(new Object[]{a.getId(), a.getAuditor(), a.getFechaCreacion(), a.getFechaFin(), a.getSueldoAuditor(), a.getVisitas().size(), a.getMateriales().size()});
        Auditoria selected = (Auditoria) comboAuditorias.getSelectedItem();
        refreshDetalleAuditoria(selected);
    }

    private void refreshCombos() {
        refillCombo(comboVecinosVisita, gestor.getVecinos());
        refillCombo(comboVecinosFactura, gestor.getVecinos());
        refillCombo(comboVecinosInscripcion, gestor.getVecinos());
        refillCombo(comboProfesorMateria, gestor.getProfesores());
        refillCombo(comboCursosInscripcion, gestor.getCursos());
        refillCombo(comboCursoMateria, gestor.getCursos());
        refillCombo(comboAuditores, gestor.getAuditores());
        refillCombo(comboAuditorias, gestor.getAuditorias());
        refillCombo(comboVisitasParaAuditoria, gestor.getVisitas());
        refillCombo(comboMaterialesParaAuditoria, gestor.getRepositorioMateriales());
    }

    private static <T> void refillCombo(JComboBox<T> combo, List<T> items) {
        Object sel = combo.getSelectedItem();
        combo.removeAllItems();
        for (T it : items) combo.addItem(it);
        if (sel != null && items.contains(sel)) combo.setSelectedItem(sel);
    }

    // Modelo de tabla no editable
    private static class NonEditableModel extends DefaultTableModel {
        public NonEditableModel(Object[] columnNames, int rowCount) { super(columnNames, rowCount); }
        @Override public boolean isCellEditable(int row, int column) { return false; }
    }

    // Renderer para visitas
    private static class EstadoPagoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                // Obtenemos el valor de la columna Estado (índice 6)
                Object estadoObj = table.getValueAt(row, 6); 
                // Color suave
                if ("PAGADA".equals(estadoObj.toString())) c.setBackground(new Color(220, 255, 220));
                else c.setBackground(new Color(255, 220, 220));
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}