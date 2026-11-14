import java.io.File;

public class Main {
    public static void main(String[] args) {

        try {
            // Leemos el fichero.conf, que está en la carpeta raíz del proyecto.
            String rutaArchivo = "fichero.conf";
            File ruta = new File(rutaArchivo);
            Configuracion conf = new Configuracion(ruta);
        } catch (Exception e) {
            System.err.println(" Ocurrió un error inesperado al intentar procesar el archivo:");
            e.printStackTrace();
        }
    }
}
