
import java.io.*;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
   Esta clase la utilizaremos para configurar y decirle a nuestro programa
   como debe de tratar los datos y cuáles son esos archivos
*/

public class Configuracion {

    // Aquí pondremos los atributos que necesitaremos configurar

    Set<File> archivos;
    Set<String> algoritmos;
    Set<Long> semilla;
    int k_GRA;
    int iteracionesBL=1000;

    // Aquí el constructor que necesitaremos para rellenar los atributos

    public Configuracion(File archivo) {

        if (archivo == null) {
            throw new InvalidParameterException("Archivo no puede ser nulo.");
        }

        if (!archivo.exists() || !archivo.getName().toLowerCase().endsWith(".conf")) {
            throw new InvalidParameterException(archivo.getName() + " ----> El archivo introducido NO ES VALIDO");
        }


        archivos = new HashSet<>();
        algoritmos = new HashSet<>();
        semilla = new HashSet<>();
        k_GRA = 0;

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
                            System.err.println("Valor de semilla inválido ignorado debido a que no es un LONG o un int: " + sem);
                        }
                    }
                }  else if (linea.startsWith("IteracionesBL=")) {
                String valor = linea.substring("IteracionesBL=".length()).trim();
                try {
                        iteracionesBL = Integer.parseInt(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear IteracionesBL: " + valor);
                    }
                }

                else if (linea.startsWith("k-GRA=")) {
                    String[] partes = linea.substring("k-GRA=".length()).trim().split("\\s+");
                    for (String param : partes) {
                        try {
                            // Intenta convertir a Integer
                            int intVal = Integer.parseInt(param);
                            k_GRA = intVal;
                        } catch (NumberFormatException e1) {
                            System.err.println("Error al parsear k-GRA: " + param);
                        }
                    }

                }

            }

            // Mostramos los datos del fichero de configuración
            System.out.println("\n\u001B[38;5;117m*********************** ARCHIVO CONFIGURACION ***********************");
            System.out.println("\tARCHIVOS --> " + getArchivos());
            System.out.println("\tALGORITMOS --> " + getAlgoritmos());
            System.out.println("\tSEMILLAS --> " + getSemilla());
            System.out.println("\tk_GRA --> " + k_GRA);
            System.out.println("\tIteracionesBL --> " + iteracionesBL);
            System.out.println("*********************************************************************");
            System.out.println("\u001B[0m");

        } catch (Exception e) {
            throw new RuntimeException("Error al leer el archivo de configuración: " + e.getMessage(), e);
        }

        // Ejecutamos las funciones de evaluación

        ArrayList<Long> semillas = new ArrayList<>(semilla);
        ArrayList<String> Parametros = new ArrayList<>();

        for (String alg : getAlgoritmos()) {
            evaluacion(alg, Parametros, semillas);
        }


    }

    // Métodos para consultar los diferentes datos

    public Set<File> getArchivos() {
        return archivos;
    }

    public Set<Long> getSemilla() {
        return semilla;
    }

    public Set<String> getAlgoritmos() {
        return algoritmos;
    }

    // Método de evaluación

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

            // Leer el archivo línea por línea
            while ((linea = br.readLine()) != null) {
                linea = linea.trim(); // Limpiar espacios al inicio y final

                if (linea.isEmpty()) continue; // Ignorar líneas en blanco
                if (linea.equals("EOF")) break; // Fin del archivo

                // Si no llegado a la sección de coordenadas, lee la cabecera
                if (!leyendoCoordenadas) {

                    if (linea.startsWith("NODE_COORD_SECTION")) {
                        leyendoCoordenadas = true;
                        continue;
                    }

                    // Separar por :
                    if (linea.contains(":")) {
                        String[] partes = linea.split(":");
                        String clave = partes[0].trim();
                        String valor = partes.length > 1 ? partes[1].trim() : "";

                        // Guardar el valor dependiendo de la clave
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
                    // leyendo nodos, separa por uno o más espacios (\\s+)
                    String[] partes = linea.split("\\s+");

                    if (partes.length >= 3) {
                        int id = Integer.parseInt(partes[0]);
                        double x = Double.parseDouble(partes[1]);
                        double y = Double.parseDouble(partes[2]);

                        // Añadir la coordenada a la lista
                        listaCoordenadas.add(new Coordenadas(id, x, y));
                    }
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al leer el archivo .tsp: " + e.getMessage(), e);
        }

        // Devolvemos el objeto dato
        return new Dato(name, typeValue, dimension, comment, edgeWeightTypeValue, listaCoordenadas);
    }

    public static double funcionEvaluatoria(Dato dato_original, ArrayList<Tuple<Integer, Integer>> solucion) {
        double costeTotal = 0;
        int n = solucion.size();

        for (int i = 0; i < n; i++) {
            int ciudadActual = solucion.get(i).first;
            int ciudadSiguiente = solucion.get((i + 1) % n).first; // cierra el ciclo: última -> primera

            Coordenadas c1 = dato_original.NODE_COORD_SECTION.get(ciudadActual);
            Coordenadas c2 = dato_original.NODE_COORD_SECTION.get(ciudadSiguiente);

            double dx = c1.x - c2.x;
            double dy = c1.y - c2.y;

            costeTotal += Math.round(Math.sqrt(dx * dx + dy * dy));
        }

        return costeTotal;
    }
    void evaluacion(String tipo, ArrayList<String> otrosParametros, ArrayList<Long> semillas) {


        if (tipo.equals("greedy")) {
            System.out.println("\n\u001B[38;5;82m-------------------------- EVALUACION GREEDY --------------------------");
            for (File archivoActual : getArchivos()) {
                Dato dato = cargar(archivoActual);
                System.out.println("La funcion Evaluadora para el archivo " + archivoActual.getName());
                ArrayList<Tuple<Integer, Integer>> resultado = new ArrayList<>();
                resultado = AlgGRE_Clase05_Grupo03.Greedy(dato, archivoActual.getName());
                System.out.printf("\t->Resultado: %.0f\n", funcionEvaluatoria(dato, resultado));
            }
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("\u001B[0m");
        }


        if (tipo.equals("greedyALE")) {
            System.out.println("\n\u001B[38;5;214m ------------------- EVALUACION GREEDY ALEATORIZADO -------------------");


            if (k_GRA != -1) {
                for (File archivoActual : getArchivos()) {
                    for (long semilla : semillas) {
                        System.out.printf("PARA LA SEMILLA %d\n", semilla);
                        Dato dato = cargar(archivoActual);
                        System.out.println("La funcion Evaluadora para el archivo " + archivoActual.getName());
                        ArrayList<Tuple<Integer, Integer>> resultado = AlgGRA_Clase05_Grupo03.GreedyAleatorizado(dato, k_GRA, semilla, archivoActual.getName());
                        System.out.printf("\t->Resultado: %.0f\n", funcionEvaluatoria(dato, resultado));
                    }
                }
            } else {
                System.err.printf("Error al leer parámetros del archivo %s, falta el parámetro 'k='\n", otrosParametros);
            }
        }

        if (tipo.equals("busquedaLocal")) {
            System.out.println("\n\u001B[38;5;34m---------------------- EVALUACION BUSQUEDA LOCAL ----------------------");

            for (File archivoActual : getArchivos()) {
                System.out.println("La función Evaluadora para el archivo " + archivoActual.getName());
                Dato dato = cargar(archivoActual);

                if (!semillas.isEmpty()) {
                    for (Long aLong : semillas) {
                        System.out.println("\t-> La semilla utilizada para la solucion Inicial es: " + aLong);

                        // USAMOS DIRECTAMENTE LAS VARIABLES GLOBALES k_GRA e iteracionesBL
                        ArrayList<Tuple<Integer, Integer>> resultado = AlgBL_Clase05_Grupo03.BusquedaLocal(dato, k_GRA, aLong, archivoActual.getName(), iteracionesBL);

                        // Opcional: imprimir el coste de la ruta
                        System.out.printf("\t\t->Resultado Final: %.0f\n", funcionEvaluatoria(dato, resultado));
                    }
                }
            }
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("\u001B[0m");
        }

        System.out.print("\u001B[0m");


/*
        if (tipo.equals("busquedaLocal")) {

            System.out.print("\u001B[34m");
            System.out.println("---------------------- EVALUACION BUSQUEDA LOCAL ----------------------");
            for (File archivoActual : getArchivos()) {
                int k = 0;

                for (Object param : otrosParametros) {
                    String str = param.toString();
                    if (str.startsWith("k=")) {
                        try {
                            k = Integer.parseInt(str.substring(2));
                            break;
                        } catch (NumberFormatException e) {
                            System.err.println("Error: valor de k no es un número válido en -> " + str);
                        }
                    }

                }

                if (!semillas.isEmpty()) {

                    System.out.println("La función Evaluadora para el archivo " + archivoActual.getName());
                    for (Long aLong : semillas) {

                        int[][] flujo = Archivo.leerDAT(archivoActual, 1);
                        int[][] distancia = Archivo.leerDAT(archivoActual, 2);
                        System.out.println("\t-> La semilla utilizada para la solucion Inicial es: " + aLong);
                        ArrayList<Integer> asignacion;
                        asignacion = BusquedaLocal(flujo, distancia, aLong.intValue(), k, archivoActual.getName());
                        System.out.println("\t\t->Vector de permutacion: " + asignacion);
                        System.out.println("\t\t->Resultado: " + funcionEvaluatoria(flujo, distancia, asignacion));
                    }
                } else {
                    int[][] flujo = Archivo.leerDAT(archivoActual, 1);
                    int[][] distancia = Archivo.leerDAT(archivoActual, 2);
                    System.out.println("\t-> La funcion Evaluadora para el archivo " + archivoActual.getName());
                    ArrayList<Integer> asignacion;
                    asignacion = BusquedaLocal(flujo, distancia, -1, -1, archivoActual.getName());
                    System.out.println("\t\t->Vector de permutacion: " + asignacion);
                    System.out.println("\t\t->Resultado: " + funcionEvaluatoria(flujo, distancia, asignacion));
                }

            }
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("\u001B[0m");
        }
*/
        if (tipo.equals("busquedaTABU")) {

            System.out.print("\u001B[35m");
            System.out.println("---------------------- EVALUACION BUSQUEDA TABÚ -----------------------");

            int k = 0;
            int tenencia = -2;
            double Oscilacion = -2;
            double Estancamiento = -2;
            int iteracionesBT= 10000;

            for (Object param : otrosParametros) {
                String str = param.toString();

                // La k la utilizaremos para el greedy aleatorizado
                if (str.startsWith("k-GRA=")) {
                    try {
                        k = Integer.parseInt(str.substring(2));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + str);
                    }
                }

                if (str.startsWith("TenTAB=")) {
                    try {
                        String valor = str.substring("TenTAB=".length()).trim();
                        tenencia = Integer.parseInt(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de TenTAB no es un número válido en -> " + str);
                    }
                }


                if (str.startsWith("OscilaEst=")) {
                    try {
                        String valor = str.substring("OscilaEst=".length()).trim(); // extrae después de "OscilaEst="
                        Oscilacion = Double.parseDouble(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de OscilaEst no es un número válido en -> " + str);
                    }
                }

                if (str.startsWith("Est=")) {
                    try {
                        String valor = str.substring("Est=".length()).trim(); // extrae después de "Est="
                        Estancamiento = Double.parseDouble(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de Est no es un número válido en -> " + str);
                    }
                }

                if (str.startsWith("IteracionesBT=")) {
                    try {
                        String valor = str.substring("IteracionesBT=".length()).trim(); // extrae después de "IteracionesBT="
                        iteracionesBT = Integer.parseInt(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de Est no es un número válido en -> " + str);
                    }
                }


            }

            for (File archivoActual : getArchivos()) {
                Dato dato = cargar(archivoActual);

                if (tenencia == -2 || Oscilacion == -2 || Estancamiento == -2) {
                    System.err.print(" <------ !!!!! ERROR FALTAN PARÁMETROS PARA LA BÚSQUEDA TABÚ !!!!! ------>");
                    break;
                }

                if (!semillas.isEmpty()) {

                    System.out.println("La función Evaluadora para el archivo " + archivoActual.getName());
                    for (Long aLong : semillas) {

                        System.out.println("\t-> La semilla utilizada para la solucion Inicial es: " + aLong);
                        ArrayList<Tuple<Integer, Integer>> asignacion;
                        asignacion = AlgBT_Clase05_Grupo03.BusquedaTabu(dato, aLong, k,iteracionesBT, tenencia, Oscilacion, Estancamiento, archivoActual.getName());
                        System.out.println("\t\t->Vector de permutacion: " + asignacion);
                        System.out.println("\t\t->Resultado: " + funcionEvaluatoria(dato, asignacion));
                    }
                } else {
                    System.out.println("\t-> La funcion Evaluadora para el archivo " + archivoActual.getName());
                    ArrayList<Tuple<Integer, Integer>> asignacion;
                    asignacion = AlgBT_Clase05_Grupo03.BusquedaTabu(dato, Long.parseLong("-1"), -1,iteracionesBT, tenencia, Oscilacion, Estancamiento, archivoActual.getName());
                    System.out.println("\t\t->Vector de permutacion: " + asignacion);
                    System.out.println("\t\t->Resultado: " + funcionEvaluatoria(dato, asignacion));
                }

            }
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("\u001B[0m");

        }
    }

    public static class Logger {
        private final String fileName;
        private final StringBuilder log;
        private final long startTime;

        public Logger(String algorithmName, String dataset, Long semilla) {
            this.startTime = System.currentTimeMillis();
            this.log = new StringBuilder();

            // Crear nombre del archivo con timestamp
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            this.fileName = String.format("logs/%s_%s_%d_%s.log",
                    algorithmName,
                    dataset,
                    semilla != null ? semilla : 0,
                    timestamp);

            // Crear directorio de logs si no existe
            new File("logs").mkdirs();

            // Escribir cabecera
            appendLine("=== PARÁMETROS DEL ALGORITMO ===");
            appendLine("Algoritmo: " + algorithmName);
            appendLine("Dataset: " + dataset);
            appendLine("Semilla: " + semilla);
            appendLine("Fecha y hora: " + now);
            appendLine("==============================\n");
        }

        public void logInitialSolution(ArrayList<Integer> solution, double cost) {
            appendLine("=== SOLUCIÓN INICIAL ===");
            appendLine("Solución: " + solution);
            appendLine("Coste: " + cost);
            appendLine("=====================\n");
        }

        public void logChange(int i, int j, ArrayList<Integer> newSolution, double newCost, boolean improved) {
            appendLine("=== CAMBIO EN LA SOLUCIÓN ===");
            appendLine("Posiciones intercambiadas: " + i + " <-> " + j);
            appendLine(improved ? "MEJORA" : "EMPEORA");
            appendLine("Nueva solución: " + newSolution);
            appendLine("Nuevo coste: " + newCost);
            appendLine("=========================\n");
        }

        public void logFinalSolution(ArrayList<Tuple<Integer, Integer>> solution, double cost) {
            long totalTime = System.currentTimeMillis() - startTime;
            appendLine("=== SOLUCIÓN FINAL ===");
            appendLine("Solución: " + solution);
            appendLine("Coste final: " + cost);
            appendLine("Tiempo total: " + totalTime + "ms");
            appendLine("===================\n");

            // Guardar el log en archivo
            saveToFile();
        }

        // Forzar escritura del log actual en disco (útil para flushing por iteración)
        public void flush() {
            saveToFile();
        }

        public void appendLine(String line) {
            log.append(line).append("\n");
        }

        private void saveToFile() {
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.print(log.toString());
            } catch (IOException e) {
                System.err.println("Error al guardar el log: " + e.getMessage());
            }
        }
    }

}
