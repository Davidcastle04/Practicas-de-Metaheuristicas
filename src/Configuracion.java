import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

/*
   Lee el fichero .conf y lanza los algoritmos sobre los archivos con cada semilla.
   Los algoritmos de esta práctica son greedy, greedyALE, busquedaLocal y busquedaTABU.
*/

public class Configuracion {

    // Mejores costes conocidos (los de las tablas del guión) para calcular la desviación
    private static final Map<String, Integer> MEJOR_CONOCIDO = new HashMap<>();
    static {
        MEJOR_CONOCIDO.put("a280", 2579);
        MEJOR_CONOCIDO.put("ch130", 6110);
        MEJOR_CONOCIDO.put("d18512", 645238);
        MEJOR_CONOCIDO.put("pr144", 58537);
        MEJOR_CONOCIDO.put("u1060", 224094);
    }

    // TODO -- Corregido Claude
    // Antes eran HashSet y el orden salía mezclado. Con LinkedHashSet las semillas se usan en el
    // orden del .conf (la ejecución 1 siempre con la misma semilla en todos los algoritmos)
    Set<File> archivos;
    Set<String> algoritmos;
    Set<Long> semilla;

    int k_GRA = 5;
    int iteracionesBL = 10000;

    // TODO -- Corregido Claude
    // Los parámetros de la tabú nunca se leían del .conf (siempre salía el error de "faltan parámetros").
    // Valores por defecto = los del guión
    int iteracionesBT = 10000;
    int tenenciaTabu = 10;
    double oscilacionEst = 0.5;
    double estancamiento = 0.05;

    private interface Ejecutor {
        ArrayList<Tuple<Integer, Integer>> ejecutar(Dato dato, File archivo, Long semilla);
    }

    public Configuracion(File archivo) {

        if (archivo == null) {
            throw new InvalidParameterException("Archivo no puede ser nulo.");
        }

        if (!archivo.exists() || !archivo.getName().toLowerCase().endsWith(".conf")) {
            throw new InvalidParameterException(archivo.getName() + " ----> El archivo introducido NO ES VALIDO");
        }

        archivos = new LinkedHashSet<>();
        algoritmos = new LinkedHashSet<>();
        semilla = new LinkedHashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;

                if (linea.startsWith("Archivos=")) {
                    String contenido = linea.substring("Archivos=".length()).trim();
                    String[] partes = contenido.split("\"\\s+\"");

                    for (String nombreArchivo : partes) {
                        nombreArchivo = nombreArchivo.replace("\"", "").trim();
                        File f = new File(nombreArchivo);

                        if (f.exists()) {
                            archivos.add(f);
                        } else {
                            System.err.println("El archivo no existe, compruébalo: " + f.getPath());
                        }
                    }
                } else if (linea.startsWith("Algoritmos=")) {
                    String[] partes = linea.substring("Algoritmos=".length()).trim().split("\\s+");
                    Collections.addAll(algoritmos, partes);

                } else if (linea.startsWith("Semilla=")) {
                    String[] partes = linea.substring("Semilla=".length()).trim().split("\\s+");
                    for (String sem : partes) {
                        try {
                            semilla.add(Long.parseLong(sem));
                        } catch (NumberFormatException e) {
                            System.err.println("Semilla inválida (no es un número), se ignora: " + sem);
                        }
                    }
                } else if (linea.startsWith("k-GRA=")) {
                    k_GRA = leerEntero(linea, "k-GRA=", k_GRA);

                } else if (linea.startsWith("IteracionesBL=")) {
                    iteracionesBL = leerEntero(linea, "IteracionesBL=", iteracionesBL);

                } else if (linea.startsWith("IteracionesBT=")) {
                    iteracionesBT = leerEntero(linea, "IteracionesBT=", iteracionesBT);

                } else if (linea.startsWith("TenTAB=")) {
                    tenenciaTabu = leerEntero(linea, "TenTAB=", tenenciaTabu);

                } else if (linea.startsWith("OscilaEst=")) {
                    oscilacionEst = leerDecimal(linea, "OscilaEst=", oscilacionEst);

                } else if (linea.startsWith("Est=")) {
                    estancamiento = leerDecimal(linea, "Est=", estancamiento);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al leer el archivo de configuración: " + e.getMessage(), e);
        }

        System.out.println("\n\u001B[38;5;117m*********************** ARCHIVO CONFIGURACION ***********************");
        System.out.println("\tARCHIVOS --> " + archivos);
        System.out.println("\tALGORITMOS --> " + algoritmos);
        System.out.println("\tSEMILLAS --> " + semilla);
        System.out.println("\tk_GRA --> " + k_GRA);
        System.out.println("\tIteracionesBL --> " + iteracionesBL);
        System.out.println("\tIteracionesBT --> " + iteracionesBT);
        System.out.println("\tTenencia tabu --> " + tenenciaTabu);
        System.out.println("\tOscilacion estrategica --> " + oscilacionEst);
        System.out.println("\tEstancamiento --> " + estancamiento);
        System.out.println("*********************************************************************");
        System.out.println("\u001B[0m");

        ArrayList<Long> semillas = new ArrayList<>(semilla);

        for (String alg : algoritmos) {
            evaluacion(alg, semillas);
        }
    }

    private static int leerEntero(String linea, String clave, int porDefecto) {
        String valor = linea.substring(clave.length()).trim();
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            System.err.println("Error al leer " + clave + " (" + valor + "), se usa " + porDefecto);
            return porDefecto;
        }
    }

    private static double leerDecimal(String linea, String clave, double porDefecto) {
        String valor = linea.substring(clave.length()).trim();
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            System.err.println("Error al leer " + clave + " (" + valor + "), se usa " + porDefecto);
            return porDefecto;
        }
    }

    public Set<File> getArchivos() {
        return archivos;
    }

    public Set<Long> getSemilla() {
        return semilla;
    }

    public Set<String> getAlgoritmos() {
        return algoritmos;
    }

    public static Dato cargar(File archivo) {

        String name = null;
        type typeValue = null;
        String comment = null;
        int dimension = 0;
        edge_weight_type edgeWeightTypeValue = null;
        ArrayList<Coordenadas> listaCoordenadas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean leyendoCoordenadas = false;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                if (linea.isEmpty()) continue;
                if (linea.equals("EOF")) break;

                if (!leyendoCoordenadas) {

                    if (linea.startsWith("NODE_COORD_SECTION")) {
                        leyendoCoordenadas = true;
                        continue;
                    }

                    if (linea.contains(":")) {
                        String[] partes = linea.split(":");
                        String clave = partes[0].trim();
                        String valor = partes.length > 1 ? partes[1].trim() : "";

                        switch (clave) {
                            case "NAME":
                                name = valor;
                                break;
                            case "COMMENT":
                                comment = valor;
                                break;
                            case "TYPE":
                                typeValue = type.valueOf(valor);
                                break;
                            case "DIMENSION":
                                dimension = Integer.parseInt(valor);
                                break;
                            case "EDGE_WEIGHT_TYPE":
                                edgeWeightTypeValue = edge_weight_type.valueOf(valor);
                                break;
                        }
                    }
                } else {
                    String[] partes = linea.split("\\s+");

                    if (partes.length >= 3) {
                        int id = Integer.parseInt(partes[0]);
                        double x = Double.parseDouble(partes[1]);
                        double y = Double.parseDouble(partes[2]);

                        listaCoordenadas.add(new Coordenadas(id, x, y));
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al leer el archivo .tsp: " + e.getMessage(), e);
        }

        return new Dato(name, typeValue, dimension, comment, edgeWeightTypeValue, listaCoordenadas);
    }

    // Coste de una ruta cerrada con distancias euclídeas redondeadas
    public static double funcionEvaluatoria(Dato dato_original, ArrayList<Tuple<Integer, Integer>> solucion) {
        double costeTotal = 0;
        int n = solucion.size();

        for (int i = 0; i < n; i++) {
            int ciudadActual = solucion.get(i).first;
            int ciudadSiguiente = solucion.get((i + 1) % n).first; // la última vuelve a la primera

            Coordenadas c1 = dato_original.NODE_COORD_SECTION.get(ciudadActual);
            Coordenadas c2 = dato_original.NODE_COORD_SECTION.get(ciudadSiguiente);

            double dx = c1.x - c2.x;
            double dy = c1.y - c2.y;

            costeTotal += Math.round(Math.sqrt(dx * dx + dy * dy));
        }

        return costeTotal;
    }

    // TODO -- Corregido Claude
    // Se han quitado los trozos antiguos (el bloque comentado de la búsqueda local con flujo/distancia
    // y la lectura de parámetros de la tabú desde una lista vacía). Ahora los cuatro algoritmos se
    // lanzan con el mismo método, que además mide el tiempo y calcula la desviación y la media
    void evaluacion(String tipo, ArrayList<Long> semillas) {

        switch (tipo) {
            case "greedy":
                ejecutarAlgoritmo("EVALUACION GREEDY", "\u001B[38;5;82m", false, semillas,
                        (dato, f, s) -> AlgGRE_Clase05_Grupo03.Greedy(dato, f.getName()));
                break;

            case "greedyALE":
                ejecutarAlgoritmo("EVALUACION GREEDY ALEATORIZADO", "\u001B[38;5;214m", true, semillas,
                        (dato, f, s) -> AlgGRA_Clase05_Grupo03.GreedyAleatorizado(dato, k_GRA, s, f.getName()));
                break;

            case "busquedaLocal":
                ejecutarAlgoritmo("EVALUACION BUSQUEDA LOCAL", "\u001B[38;5;34m", true, semillas,
                        (dato, f, s) -> AlgBL_Clase05_Grupo03.BusquedaLocal(dato, k_GRA, s, f.getName(), iteracionesBL));
                break;

            case "busquedaTABU":
                ejecutarAlgoritmo("EVALUACION BUSQUEDA TABU", "\u001B[35m", true, semillas,
                        (dato, f, s) -> AlgBT_Clase05_Grupo03.BusquedaTabu(dato, s, k_GRA, iteracionesBT,
                                tenenciaTabu, oscilacionEst, estancamiento, f.getName()));
                break;

            default:
                System.err.println("Algoritmo desconocido en el .conf, se ignora: " + tipo
                        + " (los válidos son greedy, greedyALE, busquedaLocal y busquedaTABU)");
        }
    }

    private void ejecutarAlgoritmo(String titulo, String color, boolean usaSemilla,
                                   ArrayList<Long> semillas, Ejecutor algoritmo) {

        System.out.println("\n" + color + "---------------------- " + titulo + " ----------------------");

        if (usaSemilla && semillas.isEmpty()) {
            System.err.println("Este algoritmo necesita al menos una semilla en el .conf");
            System.out.print("\u001B[0m");
            return;
        }

        // el greedy no es aleatorio, se ejecuta una sola vez por archivo
        ArrayList<Long> lista = new ArrayList<>();
        if (usaSemilla) {
            lista.addAll(semillas);
        } else {
            lista.add(null);
        }

        for (File archivoActual : archivos) {
            Dato dato = cargar(archivoActual);
            System.out.println("Archivo " + archivoActual.getName());

            ArrayList<Double> costes = new ArrayList<>();
            ArrayList<Double> tiempos = new ArrayList<>();

            for (Long s : lista) {
                long inicio = System.nanoTime();
                ArrayList<Tuple<Integer, Integer>> resultado = algoritmo.ejecutar(dato, archivoActual, s);
                double ms = (System.nanoTime() - inicio) / 1e6;

                double coste = funcionEvaluatoria(dato, resultado);
                costes.add(coste);
                tiempos.add(ms);

                System.out.printf("\t-> %sCoste: %.0f | Tiempo: %.0f ms | Desviacion: %s%n",
                        s == null ? "" : "Semilla " + s + " | ",
                        coste, ms, desviacion(archivoActual, coste));
            }

            if (costes.size() > 1) {
                double media = media(costes);
                System.out.printf("\t   Media: %.2f (desviacion %s) | Desv. tipica: %.2f | Tiempo medio: %.0f ms%n",
                        media, desviacion(archivoActual, media), desvTipica(costes), media(tiempos));
            }
        }

        System.out.println("-----------------------------------------------------------------------");
        System.out.print("\u001B[0m");
    }

    private static String desviacion(File archivo, double coste) {
        String nombre = archivo.getName().toLowerCase().replace(".tsp", "");
        Integer mejor = MEJOR_CONOCIDO.get(nombre);
        if (mejor == null) return "-";
        return String.format("%.2f%%", (coste - mejor) * 100.0 / mejor);
    }

    private static double media(List<Double> valores) {
        double suma = 0;
        for (double v : valores) suma += v;
        return suma / valores.size();
    }

    // desviación típica muestral (n-1), como la de Excel
    private static double desvTipica(List<Double> valores) {
        if (valores.size() < 2) return 0;
        double m = media(valores);
        double suma = 0;
        for (double v : valores) suma += (v - m) * (v - m);
        return Math.sqrt(suma / (valores.size() - 1));
    }
}
