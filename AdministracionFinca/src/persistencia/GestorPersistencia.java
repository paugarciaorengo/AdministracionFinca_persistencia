package persistencia;

import servicio.GestorComunidad;

import java.io.*;

/**
 * Persistencia por serialización (Java estándar).
 *
 * Guarda y carga el contenedor {@link servicio.GestorComunidad.Datos}.
 */
public final class GestorPersistencia {

    private GestorPersistencia() {}

    public static GestorComunidad.Datos cargar(File fichero) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fichero)))) {
            Object obj = ois.readObject();
            return (GestorComunidad.Datos) obj;
        }
    }

    public static void guardar(File fichero, GestorComunidad.Datos datos) throws IOException {
        File parent = fichero.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fichero)))) {
            oos.writeObject(datos);
        }
    }
}
