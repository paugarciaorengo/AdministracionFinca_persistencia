import modelo.*;
import persistencia.GestorPersistencia;
import servicio.GestorComunidad;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SIGCO - Versión Profesional con Iconos Vectoriales
 */
public class App extends JFrame {

    private final File ficheroDatos;
    private final GestorComunidad gestor;

    // Caches
    private List<Profesor> listaProfesores = new ArrayList<>();
    private List<Auditor> listaAuditoresGestion = new ArrayList<>();
    private List<Material> listaMaterialesGestion = new ArrayList<>();

    // Modelos
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

    // Combos
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

    // Tablas
    private final JTable tablaVisitas = createStyledTable(visitasModel);
    private final JTable tablaAuditorias = createStyledTable(auditoriasModel);
    private final JTable tablaProfesores = createStyledTable(profesoresModel);
    private final JTable tablaAuditoresGestion = createStyledTable(auditoresGestionModel);
    private final JTable tablaMateriales = createStyledTable(materialesModel);
    private final JTable tablaVecinos = createStyledTable(vecinosModel);

    // Dashboard labels
    private JLabel lblTotalVecinos = new JLabel("0");
    private JLabel lblTotalRecaudado = new JLabel("0.0 €");
    private JLabel lblVisitasPendientes = new JLabel("0");

    public App() {
        setupLookAndFeel();
        setTitle("SIGCO - Gestión Integral v2.0");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        UIManager.put("Table.alternateRowColor", new Color(240, 248, 255));

        // Carga
        this.ficheroDatos = new File("sigco.dat");
        GestorComunidad.Datos datos;
        if (ficheroDatos.exists()) {
            try {
                datos = GestorPersistencia.cargar(ficheroDatos);
            } catch (Exception ex) {
                datos = new GestorComunidad.Datos();
                JOptionPane.showMessageDialog(this, "Error cargando datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            datos = new GestorComunidad.Datos();
        }
        this.gestor = new GestorComunidad(datos);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { guardarDatosYSalir(); }
        });

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // MODIFICACIÓN: Uso de ModernIcon en lugar de emojis
        tabs.addTab("Dashboard", new ModernIcon(ModernIcon.HOME), buildDashboardPanel());
        tabs.addTab("Vecinos", new ModernIcon(ModernIcon.USER), buildVecinosPanel());
        tabs.addTab("Profesores", new ModernIcon(ModernIcon.HAT), buildProfesoresPanel());
        tabs.addTab("Auditores", new ModernIcon(ModernIcon.CASE), buildAuditoresGestionPanel());
        tabs.addTab("Materiales", new ModernIcon(ModernIcon.BOX), buildMaterialesPanel());
        tabs.addTab("Visitas", new ModernIcon(ModernIcon.CALENDAR), buildVisitasPanel());
        tabs.addTab("Facturación", new ModernIcon(ModernIcon.MONEY), buildFacturacionPanel());
        tabs.addTab("Cursos", new ModernIcon(ModernIcon.BOOK), buildCursosPanel());
        tabs.addTab("Auditorías", new ModernIcon(ModernIcon.SEARCH), buildAuditoriasPanel());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) updateDashboard();
        });

        setContentPane(tabs);
        tablaVisitas.setDefaultRenderer(Object.class, new EstadoPagoRenderer());
        refreshAll();
    }

    private void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void guardarDatosYSalir() {
        try {
            GestorPersistencia.guardar(ficheroDatos, gestor.getDatos());
            dispose();
            System.exit(0);
        } catch (Exception ex) {
            if (JOptionPane.showConfirmDialog(this, "Error al guardar. ¿Salir sin guardar?", "Error CRÍTICO", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }

    // --- DASHBOARD ---
    private JPanel buildDashboardPanel() {
        JPanel root = new JPanel(new GridLayout(2, 2, 20, 20));
        root.setBorder(new EmptyBorder(30, 30, 30, 30));
        root.setBackground(Color.WHITE);

        root.add(createCard("Vecinos Activos", lblTotalVecinos, new Color(65, 105, 225)));
        root.add(createCard("Visitas Impagadas", lblVisitasPendientes, new Color(220, 20, 60)));
        root.add(createCard("Facturación Total", lblTotalRecaudado, new Color(46, 139, 87)));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Información"));
        JTextArea info = new JTextArea("Sistema de Gestión de Comunidades.\nVersión 2.0\n\n- Use el buscador para filtrar tablas.\n- Exporte datos a CSV con un clic.\n- Guardado automático al cerrar.");
        info.setEditable(false);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        info.setMargin(new Insets(15,15,15,15));
        infoPanel.add(info);
        root.add(infoPanel);

        return root;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setBackground(color);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // --- HELPER TABLAS & BUSCADOR MEJORADO ---
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(new TableRowSorter<>(model));
        return table;
    }

    private JPanel createStandardPanel(JTable table, JPanel formPanel) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- BARRA SUPERIOR: BUSCADOR + EXPORTAR ---
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        
        // 1. Panel de Búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.add(new JLabel("🔍 Buscar en:"));
        
        // Combo para elegir columna
        JComboBox<String> colCombo = new JComboBox<>();
        colCombo.addItem("Todas"); // Opción por defecto
        for (int i = 0; i < table.getColumnCount(); i++) {
            colCombo.addItem(table.getColumnName(i));
        }
        searchPanel.add(colCombo);
        
        // Campo de texto
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
        
        // 2. Botón Exportar
        JButton exportBtn = new JButton("📂 Exportar CSV");
        exportBtn.addActionListener(e -> exportarCSV(table));
        
        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(exportBtn, BorderLayout.EAST);

        // --- LÓGICA DE BÚSQUEDA ---
        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        
        DocumentListener dl = new DocumentListener() {
            private void filter() {
                String text = searchField.getText();
                int colIndex = colCombo.getSelectedIndex() - 1; // -1 porque 0 es "Todas"

                if (text == null || text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    try {
                        String regex = "(?i)" + Pattern.quote(text);
                        if (colIndex < 0) {
                            sorter.setRowFilter(RowFilter.regexFilter(regex)); // Busca en todas
                        } else {
                            sorter.setRowFilter(RowFilter.regexFilter(regex, colIndex)); // Busca en columna específica
                        }
                    } catch (Exception ignored) {}
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        };
        searchField.getDocument().addDocumentListener(dl);
        colCombo.addActionListener(e -> dl.insertUpdate(null)); // Refiltrar al cambiar combo

        root.add(topBar, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        if (formPanel != null) root.add(formPanel, BorderLayout.SOUTH);
        
        return root;
    }

    // --- LÓGICA EXPORTACIÓN CSV ---
    private void exportarCSV(JTable table) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar como CSV");
        fc.setSelectedFile(new File("exportacion.csv"));
        
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()))) {
                // Cabeceras
                for (int i = 0; i < table.getColumnCount(); i++) {
                    bw.write(table.getColumnName(i) + (i == table.getColumnCount()-1 ? "" : ","));
                }
                bw.newLine();
                // Datos
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object val = table.getValueAt(i, j);
                        String str = (val == null) ? "" : val.toString().replace(",", " "); // Evitar romper CSV
                        bw.write(str + (j == table.getColumnCount()-1 ? "" : ","));
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "Datos exportados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- DATE SPINNER ---
    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setValue(new Date());
        return spinner;
    }

    private LocalDate getDateFromSpinner(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // --- VECINOS ---
    private JPanel buildVecinosPanel() {
        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10));
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
        add.addActionListener(e -> {
            try {
                gestor.registrarVecino(dni.getText(), nombre.getText(), direccion.getText(), cp.getText(), ciudad.getText(), telefono.getText());
                clearFields(dni, nombre, direccion, cp, ciudad, telefono);
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        JPanel south = new JPanel(new BorderLayout(10, 10));
        south.add(form, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(add);
        south.add(btnPanel, BorderLayout.SOUTH);

        return createStandardPanel(tablaVecinos, south);
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

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Añadir");
        JButton update = new JButton("Modificar");
        JButton delete = new JButton("Eliminar");
        buttons.add(add); buttons.add(update); buttons.add(delete);

        tablaProfesores.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaProfesores.getSelectedRow();
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
            if (row < 0) return;
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
            if (row < 0) return;
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
        addLabeledField(form, "CIF:", cif);
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
            if (row < 0) return;
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
            if (row < 0) return;
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
            if (row < 0) return;
            try {
                Material m = listaMaterialesGestion.get(tablaMateriales.convertRowIndexToModel(row));
                m.setNombre(nombre.getText());
                m.setPrecio(Double.parseDouble(precio.getText().trim()));
                refreshAll();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        delete.addActionListener(e -> {
            int row = tablaMateriales.getSelectedRow();
            if (row < 0) return;
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

        JSpinner fechaSpinner = createDateSpinner();
        JTextField descripcion = new JTextField();
        JTextField importe = new JTextField();
        JTextField admin = new JTextField();

        addLabeledField(form, "Vecino:", comboVecinosVisita);
        addLabeledField(form, "Fecha:", fechaSpinner);
        addLabeledField(form, "Descripción:", descripcion);
        addLabeledField(form, "Importe (€):", importe);
        addLabeledField(form, "Administrador:", admin);

        JButton add = new JButton("Crear Visita");
        add.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosVisita.getSelectedItem();
                LocalDate f = getDateFromSpinner(fechaSpinner);
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
        
        JSpinner fechaFactura = createDateSpinner();
        
        top.add(new JLabel("Vecino:"));
        top.add(comboVecinosFactura);
        top.add(new JLabel("Fecha Factura:"));
        top.add(fechaFactura);
        
        JButton facturar = new JButton("Facturar Pendientes");
        top.add(facturar);

        facturar.addActionListener(e -> {
            try {
                Vecino v = (Vecino) comboVecinosFactura.getSelectedItem();
                LocalDate fecha = getDateFromSpinner(fechaFactura);
                Factura f = gestor.crearFactura(v, fecha);
                refreshAll();
                JOptionPane.showMessageDialog(this, "Factura creada con éxito\nID: " + f.getId() + "\nTotal: " + f.getTotal() + "€", "OK", JOptionPane.INFORMATION_MESSAGE);
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

        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaMaterias), new JScrollPane(tablaInscritos));
        splitRight.setResizeWeight(0.5);
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaCursos), splitRight);
        splitMain.setResizeWeight(0.4);

        tablaCursos.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = tablaCursos.getSelectedRow();
            if (row < 0) return;
            Curso c = (Curso) comboCursosInscripcion.getItemAt(tablaCursos.convertRowIndexToModel(row));
            refreshMateriasEInscritos(c);
        });

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
        JSpinner inicio = createDateSpinner();
        JSpinner fin = createDateSpinner();
        JButton crear = new JButton("Crear");

        p.add(new JLabel("Nombre:")); p.add(nombre);
        p.add(new JLabel("Precio:")); p.add(precio);
        p.add(new JLabel("Máx:")); p.add(max);
        p.add(new JLabel("Inicio:")); p.add(inicio);
        p.add(new JLabel("Fin:")); p.add(fin);
        p.add(new JLabel("")); p.add(crear);

        crear.addActionListener(e -> {
            try {
                gestor.crearCurso(nombre.getText(), Double.parseDouble(precio.getText()), Integer.parseInt(max.getText()), 
                        getDateFromSpinner(inicio), getDateFromSpinner(fin));
                clearFields(nombre, precio);
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
                clearFields(nombre, horas); refreshAll();
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
            try { gestor.inscribirVecinoEnCurso((Vecino)comboVecinosInscripcion.getSelectedItem(), (Curso)comboCursosInscripcion.getSelectedItem()); refreshAll(); } catch (Exception ex) { showError(ex.getMessage()); }
        });
        return p;
    }

    private void refreshMateriasEInscritos(Curso c) {
        materiasModel.setRowCount(0);
        inscritosModel.setRowCount(0);
        if (c == null) return;
        for (Materia m : c.getMaterias()) materiasModel.addRow(new Object[]{m.getNombre(), m.getHoras(), m.getProfesor()});
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
        JSpinner fechaC = createDateSpinner(); JButton bCrear = new JButton("Crear");
        p1.add(new JLabel("Auditor:")); p1.add(comboAuditores); p1.add(new JLabel("Fecha:")); p1.add(fechaC); p1.add(new JLabel("")); p1.add(bCrear);
        bCrear.addActionListener(ev -> {
            try { gestor.crearAuditoria((Auditor)comboAuditores.getSelectedItem(), getDateFromSpinner(fechaC)); refreshAll(); } catch(Exception ex){showError(ex.getMessage());}
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
        JSpinner fechaF = createDateSpinner(); JButton bCerrar = new JButton("Cerrar"); JButton bMat = new JButton("Add Mat");
        p3.add(new JLabel("Fecha Fin:")); p3.add(fechaF); p3.add(bCerrar); p3.add(new JLabel(""));
        p3.add(new JLabel("Mat:")); p3.add(comboMaterialesParaAuditoria); p3.add(bMat);
        
        bCerrar.addActionListener(ev -> { try { gestor.finalizarAuditoria((Auditoria)comboAuditorias.getSelectedItem(), getDateFromSpinner(fechaF)); refreshAll(); } catch(Exception ex){showError(ex.getMessage());}});
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

    // --- UTILS ---
    private void addLabeledField(JPanel p, String label, JComponent c) { p.add(new JLabel(label, SwingConstants.RIGHT)); p.add(c); }
    private void clearFields(JTextField... fields) { for (JTextField f : fields) f.setText(""); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private boolean confirm(String msg) { return JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }

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
        updateDashboard();
    }

    private void updateDashboard() {
        lblTotalVecinos.setText(String.valueOf(gestor.getVecinos().size()));
        long pendientes = gestor.getVisitas().stream().filter(v -> v.getEstado() == EstadoPago.IMPAGADA).count();
        lblVisitasPendientes.setText(String.valueOf(pendientes));
        double total = gestor.getFacturas().stream().mapToDouble(f -> f.getTotal()).sum();
        lblTotalRecaudado.setText(String.format("%.2f €", total));
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

    private static class NonEditableModel extends DefaultTableModel {
        public NonEditableModel(Object[] columnNames, int rowCount) { super(columnNames, rowCount); }
        @Override public boolean isCellEditable(int row, int column) { return false; }
    }

    private static class EstadoPagoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object estadoObj = table.getValueAt(row, 6); 
                if ("PAGADA".equals(estadoObj.toString())) c.setBackground(new Color(220, 255, 220));
                else c.setBackground(new Color(255, 220, 220));
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    /** * CLASE PARA ICONOS VECTORIALES (AÑADIDO NUEVO)
     * Dibuja los iconos directamente con código Java 2D.
     */
    static class ModernIcon implements Icon {
        static final int HOME=0, USER=1, HAT=2, CASE=3, BOX=4, CALENDAR=5, MONEY=6, BOOK=7, SEARCH=8;
        private final int type;
        public ModernIcon(int type) { this.type = type; }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(new Color(80, 80, 80)); // Gris oscuro
            switch(type) {
                case HOME: g2.fillPolygon(new int[]{9,2,16}, new int[]{2,9,9}, 3); g2.fillRect(5, 9, 8, 7); break;
                case USER: g2.fillOval(5, 2, 8, 8); g2.fillArc(2, 10, 14, 10, 0, 180); break;
                case HAT: g2.fillRect(2, 8, 14, 2); g2.fillRect(5, 4, 8, 4); break;
                case CASE: g2.fillRect(3, 5, 12, 10); g2.drawRect(6, 2, 6, 3); break;
                case BOX: g2.drawRect(3, 3, 12, 12); g2.drawLine(3, 3, 15, 15); g2.drawLine(15, 3, 3, 15); break;
                case CALENDAR: g2.drawRect(3, 4, 12, 11); g2.fillRect(3, 4, 12, 3); break;
                case MONEY: g2.drawOval(2, 2, 14, 14); g2.drawString("$", 6, 14); break;
                case BOOK: g2.fillRect(3, 3, 5, 12); g2.fillRect(10, 3, 5, 12); break;
                case SEARCH: g2.drawOval(4, 4, 8, 8); g2.drawLine(11, 11, 15, 15); break;
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}