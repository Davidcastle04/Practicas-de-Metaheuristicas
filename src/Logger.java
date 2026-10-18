import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Logger {

    // hasta este tamaño se guarda la ruta entera en cada iteración
    private static final int LIMITE_RUTA_COMPLETA = 300;
    // en las instancias grandes se guarda la ruta solo cada tantas iteraciones
    private static final int CADA_CUANTAS_RUTA = 1000;

    private final BufferedWriter out;
    private final long inicio;
    private final String nombreFichero;

    public Logger(String algoritmo, String dataset, Long semilla) {
        inicio = System.currentTimeMillis();

        new File("logs").mkdirs();

        String nombreDataset = dataset.replace(".tsp", "");
        String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        nombreFichero = String.format("logs/%s_%s_%d_%s.log",
                algoritmo, nombreDataset, semilla != null ? semilla : 0, marca);

        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(nombreFichero));
        } catch (IOException e) {
            System.err.println("No se pudo crear el log " + nombreFichero + ": " + e.getMessage());
        }
        out = w;

        linea("=== PARAMETROS ===");
        linea("Algoritmo: " + algoritmo);
        linea("Dataset: " + dataset);
        linea("Semilla: " + semilla);
        linea("Fecha y hora: " + LocalDateTime.now());
        linea("(las ciudades se numeran desde 0)");
    }

    public void parametro(String nombre, Object valor) {
        linea(nombre + ": " + valor);
    }

    public void linea(String texto) {
        if (out == null) return;
        try {
            out.write(texto);
            out.newLine();
        } catch (IOException e) {
            System.err.println("Error escribiendo el log: " + e.getMessage());
        }
    }

    public void solucionInicial(int[] ruta, double coste) {
        linea("");
        linea("=== SOLUCION INICIAL ===");
        linea("Coste: " + coste);
        linea("Ruta: " + texto(ruta));
        linea("");
        linea("=== ITERACIONES ===");
    }

    public void solucionInicial(ArrayList<Tuple<Integer, Integer>> ruta, double coste) {
        solucionInicial(Distancias.aArray(ruta), coste);
    }

    /** Un paso de construcción en los greedy (una ciudad añadida). */
    public void paso(int paso, int ciudad, int suma, String nota) {
        linea("Paso " + paso + ": ciudad " + ciudad + " (suma distancias = " + suma + ")"
                + (nota == null ? "" : " " + nota));
    }

    /** Un movimiento aplicado = una iteración. */
    public void iteracion(int iter, int i, int j, int ciudadA, int ciudadB,
                          int delta, double coste, int[] ruta, String nota) {
        String tipo = delta < 0 ? "MEJORA" : (delta > 0 ? "EMPEORA" : "IGUAL");
        linea("Iteracion " + iter + " | 2-opt posiciones (" + i + "," + j + ") ciudades ("
                + ciudadA + "," + ciudadB + ") | " + tipo + " delta=" + delta
                + " | coste=" + coste + (nota == null ? "" : " | " + nota));

        if (ruta != null && (ruta.length <= LIMITE_RUTA_COMPLETA || iter % CADA_CUANTAS_RUTA == 0)) {
            linea("    Ruta: " + texto(ruta));
        }
    }

    public void sinMovimiento(int iter) {
        linea("Iteracion " + iter + " | sin movimiento admisible (todo el entorno es tabu)");
    }

    public void estancamiento(int iter, String tipo, double costeNueva, int[] nueva) {
        linea("Iteracion " + iter + " | ESTANCAMIENTO -> nueva solucion por " + tipo
                + " | coste=" + costeNueva);
        if (nueva != null && nueva.length <= LIMITE_RUTA_COMPLETA) {
            linea("    Ruta: " + texto(nueva));
        }
    }

    public void finalizar(int[] ruta, double coste) {
        long total = System.currentTimeMillis() - inicio;
        linea("");
        linea("=== SOLUCION FINAL ===");
        linea("Coste final: " + coste);
        linea("Tiempo total: " + total + " ms");
        linea("Ruta: " + texto(ruta));
        cerrar();
    }

    public void finalizar(ArrayList<Tuple<Integer, Integer>> ruta, double coste) {
        finalizar(Distancias.aArray(ruta), coste);
    }

    public void cerrar() {
        if (out == null) return;
        try {
            out.close();
        } catch (IOException e) {
            System.err.println("Error cerrando el log " + nombreFichero + ": " + e.getMessage());
        }
    }

    private static String texto(int[] ruta) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ruta.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(ruta[i]);
        }
        return sb.append("]").toString();
    }
}
