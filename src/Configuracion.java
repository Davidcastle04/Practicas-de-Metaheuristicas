import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

// Leemos el fichero.conf y lanzamos los algoritmos sobre cada archivo con cada semilla.
// Los algoritmos son greedy, greedyALE, busquedaLocal, busquedaTABU y GEN (evolutivo generacional).
// Los parámetros de los algoritmos se leen del fichero.conf (si falta alguno del evolutivo, avisamos y usamos el del guion).

public class Configuracion {

    // Mejores costes conocidos (los de las tablas del guion) para calcular la desviación
    private static final Map<String, Integer> MEJOR_CONOCIDO = new HashMap<>();
    static {
        MEJOR_CONOCIDO.put("a280", 2579);
        MEJOR_CONOCIDO.put("ch130", 6110);
        MEJOR_CONOCIDO.put("d18512", 645238);
        MEJOR_CONOCIDO.put("pr144", 58537);
        MEJOR_CONOCIDO.put("u1060", 224094);
    }

    Set<File> archivos;
    Set<String> algoritmos;
    Set<Long> semilla;

    int k_GRA = 5;
    int iteracionesBL = 10000;

    int iteracionesBT = 10000;
    int tenenciaTabu = 10;
    double oscilacionEst = 0.5;
    double estancamiento = 0.05;

    // Evolutivo generacional: los valores se leen del fichero.conf. Solo si falta alguno
    // se usa el valor por defecto de aquí (el del guion) y avisamos por pantalla.
    // Cruces, élites, kBest y kWorst son listas: se ejecutan todas las combinaciones (las 24 versiones del guion).
    List<TipoCruce> crucesGEN = new ArrayList<>(Arrays.asList(TipoCruce.OX2, TipoCruce.MOC));
    List<Integer> elitesGEN = new ArrayList<>(Arrays.asList(1, 2, 3));
    List<Integer> kBestGEN = new ArrayList<>(Arrays.asList(2, 3));
    List<Integer> kWorstGEN = new ArrayList<>(Arrays.asList(2, 3));

    int tamPoblacionGEN = 100;
    double porcAleatoriosGEN = 0.8;   // parte de la población inicial totalmente aleatoria, el resto es greedy aleatorizado
    int kGreedyGEN = 10;              // tamaño de la lista de candidatos del greedy aleatorizado inicial
    double probCruceGEN = 0.7;
    double probMutacionGEN = 0.1;     // por individuo
    double probPosOX2GEN = 0.5;       // probabilidad de elegir cada posición en OX2
    int evaluacionesGEN = 50000;
    double tiempoMaxGEN = 120;        // en segundos

    // Claves que hemos encontrado en el fichero.conf
    private Set<String> clavesLeidas = new HashSet<>();

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

                // Apuntamos la clave de la línea para saber después qué parámetros faltan.
                if (linea.contains("=")) {
                    clavesLeidas.add(linea.substring(0, linea.indexOf("=")).trim());
                }

                if (linea.startsWith("Archivos=")) {
                    String contenido = linea.substring("Archivos=".length()).trim();
                    String[] partes = contenido.split("\"\\s+\"");

                    for (int i = 0; i < partes.length; i++) {
                        String nombreArchivo = partes[i].replace("\"", "").trim();
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
                    for (int i = 0; i < partes.length; i++) {
                        try {
                            semilla.add(Long.parseLong(partes[i]));
                        } catch (NumberFormatException e) {
                            System.err.println("Semilla inválida (no es un número), se ignora: " + partes[i]);
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

                } else if (linea.startsWith("Cruces=")) {
                    crucesGEN = leerCruces(linea, "Cruces=", crucesGEN);

                } else if (linea.startsWith("Elites=")) {
                    elitesGEN = leerListaEnteros(linea, "Elites=", elitesGEN);

                } else if (linea.startsWith("KBest=")) {
                    kBestGEN = leerListaEnteros(linea, "KBest=", kBestGEN);

                } else if (linea.startsWith("KWorst=")) {
                    kWorstGEN = leerListaEnteros(linea, "KWorst=", kWorstGEN);

                } else if (linea.startsWith("TamPoblacion=")) {
                    tamPoblacionGEN = leerEntero(linea, "TamPoblacion=", tamPoblacionGEN);

                } else if (linea.startsWith("PorcAleatorios=")) {
                    porcAleatoriosGEN = leerDecimal(linea, "PorcAleatorios=", porcAleatoriosGEN);

                } else if (linea.startsWith("k-GEN=")) {
                    kGreedyGEN = leerEntero(linea, "k-GEN=", kGreedyGEN);

                } else if (linea.startsWith("ProbCruce=")) {
                    probCruceGEN = leerDecimal(linea, "ProbCruce=", probCruceGEN);

                } else if (linea.startsWith("ProbMutacion=")) {
                    probMutacionGEN = leerDecimal(linea, "ProbMutacion=", probMutacionGEN);

                } else if (linea.startsWith("ProbPosOX2=")) {
                    probPosOX2GEN = leerDecimal(linea, "ProbPosOX2=", probPosOX2GEN);

                } else if (linea.startsWith("EvaluacionesGEN=")) {
                    evaluacionesGEN = leerEntero(linea, "EvaluacionesGEN=", evaluacionesGEN);

                } else if (linea.startsWith("TiempoMaxGEN=")) {
                    tiempoMaxGEN = leerDecimal(linea, "TiempoMaxGEN=", tiempoMaxGEN);
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
        System.out.println("\tGEN cruces --> " + crucesGEN + " | elites --> " + elitesGEN
                + " | kBest --> " + kBestGEN + " | kWorst --> " + kWorstGEN);
        System.out.println("\tGEN poblacion --> " + tamPoblacionGEN + " (" + porcAleatoriosGEN
                + " aleatorios, resto greedy k=" + kGreedyGEN + ") | cruce --> " + probCruceGEN
                + " | mutacion --> " + probMutacionGEN + " | posiciones OX2 --> " + probPosOX2GEN);
        System.out.println("\tGEN parada --> " + evaluacionesGEN + " evaluaciones o "
                + tiempoMaxGEN + " s");
        System.out.println("*********************************************************************");
        System.out.println("\u001B[0m");

        // Si vamos a lanzar el evolutivo, avisamos de los parámetros que no están en el fichero.conf
        // y comprobamos que los valores tienen sentido.
        if (algoritmos.contains("GEN")) {
            avisarParametrosPorDefecto();
            comprobarParametrosGEN();
        }

        ArrayList<Long> semillas = new ArrayList<>(semilla);
        ArrayList<String> listaAlgoritmos = new ArrayList<>(algoritmos);

        for (int i = 0; i < listaAlgoritmos.size(); i++) {
            evaluacion(listaAlgoritmos.get(i), semillas);
        }
    }

    // Si falta algún parámetro del evolutivo en el fichero.conf, lo indicamos y usamos el valor por defecto.
    private void avisarParametrosPorDefecto() {
        String[] claves = {"Cruces", "Elites", "KBest", "KWorst", "TamPoblacion", "PorcAleatorios", "k-GEN",
                "ProbCruce", "ProbMutacion", "ProbPosOX2", "EvaluacionesGEN", "TiempoMaxGEN"};
        String[] valores = {crucesGEN.toString(), elitesGEN.toString(), kBestGEN.toString(), kWorstGEN.toString(),
                "" + tamPoblacionGEN, "" + porcAleatoriosGEN, "" + kGreedyGEN, "" + probCruceGEN,
                "" + probMutacionGEN, "" + probPosOX2GEN, "" + evaluacionesGEN, "" + tiempoMaxGEN};

        for (int i = 0; i < claves.length; i++) {
            if (!clavesLeidas.contains(claves[i])) {
                System.out.println("Parametros por defecto: " + claves[i] + " = " + valores[i]);
            }
        }
    }

    // Comprobamos que los parámetros del evolutivo tienen sentido.
    private void comprobarParametrosGEN() {
        if (tamPoblacionGEN < 2) {
            throw new IllegalArgumentException("TamPoblacion tiene que ser al menos 2");
        }
        for (int i = 0; i < elitesGEN.size(); i++) {
            if (elitesGEN.get(i) < 0 || elitesGEN.get(i) >= tamPoblacionGEN) {
                throw new IllegalArgumentException("Elite fuera de rango: " + elitesGEN.get(i));
            }
        }
        for (int i = 0; i < kBestGEN.size(); i++) {
            if (kBestGEN.get(i) < 1) {
                throw new IllegalArgumentException("KBest tiene que ser al menos 1");
            }
        }
        for (int i = 0; i < kWorstGEN.size(); i++) {
            if (kWorstGEN.get(i) < 1) {
                throw new IllegalArgumentException("KWorst tiene que ser al menos 1");
            }
        }
        if (kGreedyGEN < 1) {
            throw new IllegalArgumentException("k-GEN tiene que ser al menos 1");
        }
        if (!esProbabilidad(porcAleatoriosGEN) || !esProbabilidad(probCruceGEN)
                || !esProbabilidad(probMutacionGEN) || !esProbabilidad(probPosOX2GEN)) {
            throw new IllegalArgumentException("PorcAleatorios, ProbCruce, ProbMutacion y ProbPosOX2 tienen que estar entre 0 y 1");
        }
        if (evaluacionesGEN < 1 || tiempoMaxGEN <= 0) {
            throw new IllegalArgumentException("EvaluacionesGEN y TiempoMaxGEN tienen que ser mayores que 0");
        }
    }

    private static boolean esProbabilidad(double v) {
        return v >= 0.0 && v <= 1.0;
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

    private static List<Integer> leerListaEnteros(String linea, String clave, List<Integer> porDefecto) {
        List<Integer> lista = new ArrayList<>();
        String[] partes = linea.substring(clave.length()).trim().split("\\s+");
        for (int i = 0; i < partes.length; i++) {
            try {
                lista.add(Integer.parseInt(partes[i]));
            } catch (NumberFormatException e) {
                System.err.println("Valor invalido en " + clave + " (" + partes[i] + "), se ignora");
            }
        }
        return lista.isEmpty() ? porDefecto : lista;
    }

    private static List<TipoCruce> leerCruces(String linea, String clave, List<TipoCruce> porDefecto) {
        List<TipoCruce> lista = new ArrayList<>();
        String[] partes = linea.substring(clave.length()).trim().split("\\s+");
        for (int i = 0; i < partes.length; i++) {
            try {
                lista.add(TipoCruce.valueOf(partes[i].toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("Cruce desconocido en " + clave + " (" + partes[i] + "), los validos son OX2 y MOC");
            }
        }
        return lista.isEmpty() ? porDefecto : lista;
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

    // Calculamos el coste de una ruta cerrada con distancias euclídeas redondeadas.
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

            case "GEN":
                evaluacionGEN(semillas);
                break;

            default:
                System.err.println("Algoritmo desconocido en el .conf, se ignora: " + tipo
                        + " (los válidos son greedy, greedyALE, busquedaLocal, busquedaTABU y GEN)");
        }
    }


    // Lanzamos todas las combinaciones de cruce, élite, kWorst y kBest que hay en el fichero.conf.
    private void evaluacionGEN(ArrayList<Long> semillas) {
        for (int c = 0; c < crucesGEN.size(); c++) {
            for (int e = 0; e < elitesGEN.size(); e++) {
                for (int w = 0; w < kWorstGEN.size(); w++) {
                    for (int b = 0; b < kBestGEN.size(); b++) {
                        final TipoCruce cruce = crucesGEN.get(c);
                        final int elite = elitesGEN.get(e);
                        final int kWorst = kWorstGEN.get(w);
                        final int kBest = kBestGEN.get(b);

                        String nombre = AlgGEN_Clase05_Grupo03.nombreVersion(cruce, elite, kBest, kWorst);

                        ejecutarAlgoritmo("EVALUACION " + nombre, "\u001B[38;5;203m", true, semillas,
                                (dato, f, s) -> AlgGEN_Clase05_Grupo03.Generacional(dato, this, cruce, elite,
                                        kBest, kWorst, s, f.getName()));
                    }
                }
            }
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

        // El greedy no es aleatorio, así que lo ejecutamos una sola vez por archivo.
        ArrayList<Long> lista = new ArrayList<>();
        if (usaSemilla) {
            lista.addAll(semillas);
        } else {
            lista.add(null);
        }

        ArrayList<File> listaArchivos = new ArrayList<>(archivos);

        for (int a = 0; a < listaArchivos.size(); a++) {
            File archivoActual = listaArchivos.get(a);
            Dato dato = cargar(archivoActual);
            System.out.println("Archivo " + archivoActual.getName());

            ArrayList<Double> costes = new ArrayList<>();
            ArrayList<Double> tiempos = new ArrayList<>();

            for (int i = 0; i < lista.size(); i++) {
                Long s = lista.get(i);
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
        for (int i = 0; i < valores.size(); i++) {
            suma += valores.get(i);
        }
        return suma / valores.size();
    }

    // Desviación típica muestral (n-1), como la de Excel.
    private static double desvTipica(List<Double> valores) {
        if (valores.size() < 2) return 0;
        double m = media(valores);
        double suma = 0;
        for (int i = 0; i < valores.size(); i++) {
            suma += (valores.get(i) - m) * (valores.get(i) - m);
        }
        return Math.sqrt(suma / (valores.size() - 1));
    }
}
