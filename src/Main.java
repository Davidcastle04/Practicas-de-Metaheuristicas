import java.io.File;

public class Main {
    public static void main(String[] args) {

        try {
            // La ruta asume que el directorio raíz de ejecución es el del proyecto (Metaheuristicas_P1)
            String rutaArchivo = "fichero.conf";
            File ruta = new File(rutaArchivo);
            Configuracion conf = new Configuracion(ruta);
        } catch (Exception e) {
            System.err.println(" Ocurrió un error inesperado al intentar procesar el archivo:");
            e.printStackTrace();
        }
    }
}
