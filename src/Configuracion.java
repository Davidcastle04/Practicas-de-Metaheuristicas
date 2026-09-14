
import java.io.*;
import java.security.InvalidParameterException;
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
                } else if (linea.startsWith("k_GRA=")) {
                    String[] partes = linea.substring("k_GRA=".length()).trim().split("\\s+");
                    for (String param : partes) {
                        try {
                            // Intenta convertir a Integer
                            int intVal = Integer.parseInt(param);
                            k_GRA = intVal;
                        } catch (NumberFormatException e1) {
                            System.err.println("Error al parsear k_GRA: " + param);
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

    private int funcionEvaluatoria(ArrayList<Tuple<Integer, Integer>> solucion) {
        return 0;
    }

    void evaluacion(String tipo, ArrayList<String> otrosParametros, ArrayList<Long> semillas) {


        if (tipo.equals("greedy")) {
            System.out.println("\n\u001B[38;5;82m-------------------------- EVALUACION GREEDY --------------------------");
            for (File archivoActual : getArchivos()) {
                Dato dato = cargar(archivoActual);
                System.out.println("La funcion Evaluadora para el archivo " + archivoActual.getName());
                ArrayList<Tuple<Integer, Integer>> resultado = new ArrayList<>();
                resultado = AlgGRE_Clase05_Grupo03.Greedy(dato, archivoActual.getName());
                System.out.println("\t->Resultado: " + funcionEvaluatoria(resultado));
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
                        System.out.println("\t->Resultado: " + funcionEvaluatoria(resultado));
                    }
                }
            } else {
                System.err.printf("Error al leer parámetros del archivo %s, falta el parámetro 'k='\n", otrosParametros);
            }
        }

        System.out.print("\u001B[0m");
    }

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

        if (tipo.equals("busquedaTABU")) {

            System.out.print("\u001B[35m");
            System.out.println("---------------------- EVALUACION BUSQUEDA TABÚ -----------------------");

            int k = 0;
            int tenencia = -2;
            double Oscilacion = -2;
            double Estancamiento = -2;

            for (Object param : otrosParametros) {
                String str = param.toString();

                // La k la utilizaremos para el greedy aleatorizado
                if (str.startsWith("k=")) {
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


            }

            for (File archivoActual : getArchivos()) {

                if (tenencia == -2 || Oscilacion == -2 || Estancamiento == -2) {
                    System.err.print(" <------ !!!!! ERROR FALTAN PARÁMETROS PARA LA BÚSQUEDA TABÚ !!!!! ------>");
                    break;
                }

                if (!semillas.isEmpty()) {

                    System.out.println("La función Evaluadora para el archivo " + archivoActual.getName());
                    for (Long aLong : semillas) {

                        int[][] flujo = Archivo.leerDAT(archivoActual, 1);
                        int[][] distancia = Archivo.leerDAT(archivoActual, 2);
                        System.out.println("\t-> La semilla utilizada para la solucion Inicial es: " + aLong);
                        ArrayList<Integer> asignacion;
                        asignacion = BusquedaTabu(flujo, distancia, aLong.intValue(), k, tenencia, Oscilacion, Estancamiento, archivoActual.getName());
                        System.out.println("\t\t->Vector de permutacion: " + asignacion);
                        System.out.println("\t\t->Resultado: " + funcionEvaluatoria(flujo, distancia, asignacion));
                    }
                } else {
                    int[][] flujo = Archivo.leerDAT(archivoActual, 1);
                    int[][] distancia = Archivo.leerDAT(archivoActual, 2);
                    System.out.println("\t-> La funcion Evaluadora para el archivo " + archivoActual.getName());
                    ArrayList<Integer> asignacion;
                    asignacion = BusquedaTabu(flujo, distancia, -1, -1, tenencia, Oscilacion, Estancamiento, archivoActual.getName());
                    System.out.println("\t\t->Vector de permutacion: " + asignacion);
                    System.out.println("\t\t->Resultado: " + funcionEvaluatoria(flujo, distancia, asignacion));
                }

            }
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("\u001B[0m");

        }

        if (tipo.equals("AlgEstacionario") || tipo.equals("AlgGeneracional")) {

            if (!tipo.equals("AlgGeneracional")) {
                System.out.println("\u001B[95m");
                System.out.println("----------------- EVALUACIÓN ALGORITMO ESTACIONARIO -------------------");
            } else {
                System.out.println("\u001B[32m");
                System.out.println("----------------- EVALUACIÓN ALGORITMO GENERACIONAL -------------------");
            }

            // Valores para el algoritmo como k, valor de aceptación de mutacion y cruce
            int k = 0;
            double VAM = 0;
            double VAC = 0;
            int INDEVAL = 0;

            // Buscamos el parametro de configuración
            for (Object par : otrosParametros) {

                String p = par.toString();

                // La k la utilizaremos para el greedy aleatorizado
                if (p.startsWith("k=")) {
                    try {
                        k = Integer.parseInt(p.substring(2));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + p);
                    }
                }

                // La VAC la utilizaremos para determinar el valor de aceptación de cruce
                if (p.startsWith("VAC=")) {
                    try {
                        VAC = Double.parseDouble(p.substring(4));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + p);
                    }
                }

                // La VAC la utilizaremos para determinar el valor de aceptación de mutación
                if (p.startsWith("VAM=")) {
                    try {
                        VAM = Double.parseDouble(p.substring(4));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + p);
                    }
                }

                // La VAC la utilizaremos para determinar el valor de aceptación de mutación
                if (p.startsWith("INDEVAL=")) {
                    try {
                        INDEVAL = Integer.parseInt(p.substring(8));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de INDEVAL no es un número válido en -> " + p);
                    }
                }

                if (p.equals("AlgEstacionario.conf") || p.equals("AlgGeneracional.conf")) {


                    File archivo = new File("./AlgEstacionario.conf");
                    if (!archivo.exists()) {
                        throw new InvalidParameterException("Archivo " + archivo.getName() + " no existe");
                    }
                    // Leemos el archivo linea a linea
                    BufferedReader br;
                    try {
                        br = new BufferedReader(new FileReader(archivo));
                        String texto = br.readLine();

                        while (texto != null) { // Repetir mientras no se llegue al final del fichero

                            int Individuos = 0;
                            int aleatorioInit = 0;
                            int Kbest = 0;
                            int Kworks = 0;
                            boolean Cruce = false; // Sí es 0 es OX2 y si es 1 usamos MOC
                            int Elite = 0;
                            int ParadaIteraciones = 0;
                            int ParadaSegundos = 0;

                            String[] argumentos = texto.split(",");

                            for (String argumento : argumentos) {

                                if (argumento.startsWith("M=")) {
                                    Individuos = Integer.parseInt(argumento.substring(2));
                                }

                                if (argumento.startsWith("Init.Aleatorio=")) {
                                    aleatorioInit = Integer.parseInt(argumento.substring("Init.Aleatorio=".length()));
                                }

                                if (argumento.startsWith("Kbest=")) {
                                    Kbest = Integer.parseInt(argumento.substring("Kbest=".length()));
                                }

                                if (argumento.startsWith("Kworks=")) {
                                    Kworks = Integer.parseInt(argumento.substring("Kworks=".length()));
                                }

                                if (argumento.startsWith("Cruce=")) {
                                    if (!argumento.equals("Cruce=OX2")) {
                                        Cruce = true;
                                    }
                                }

                                if (argumento.startsWith("Elite=")) {
                                    Elite = Integer.parseInt(argumento.substring("Elite=".length()));
                                }

                                if (argumento.startsWith("Parada=")) {
                                    String valor = argumento.substring("Parada=".length());
                                    String[] partes = valor.split("/");

                                    if (partes.length == 2) {
                                        ParadaIteraciones = Integer.parseInt(partes[0]);
                                        ParadaSegundos = Integer.parseInt(partes[1]);

                                    } else {
                                        System.out.println("Formato inválido para Parada. Usa Parada=[iteraciones/segundos]");
                                    }
                                }

                            }

                            for (File archivoActual : getArchivos()) {

                                if (!semillas.isEmpty()) {
                                    System.out.println("\bParámetros Utilizados: " +
                                            "NºIndividuos(" + Individuos + "), " +
                                            "Aleatoriedad(" + aleatorioInit + "), " +
                                            "Kworks(" + Kworks + "), " +
                                            "Kbest(" + Kbest + "), " +
                                            "Cruce(" + (Cruce ? "MOC" : "OX2") + "), " +
                                            "Elite(" + Elite + "), " +
                                            "ParadaEvaluaciones(" + ParadaIteraciones + "), " +
                                            "ParadaSegundos(" + ParadaSegundos + ")," +
                                            "Individuos a evaluar(" + INDEVAL + ")");

                                    System.out.println("\tLa función Evaluadora para el archivo " + archivoActual.getName());
                                    for (Long seed : semillas) {
                                        int[][] flujo = Archivo.leerDAT(archivoActual, 1);
                                        int[][] distancia = Archivo.leerDAT(archivoActual, 2);
                                        System.out.println("\t\t-> La semilla utilizada para la solucion Inicial es: " + seed);
                                        ArrayList<Integer> asignacion;
                                        if (tipo.equals("AlgEstacionario")) {
                                            asignacion = AlgoritmoEstacional(flujo, distancia, seed, k, Individuos, aleatorioInit, Kworks, Kbest, Cruce, Elite, ParadaIteraciones, ParadaSegundos, INDEVAL, VAM);
                                        } else {
                                            asignacion = AlgoritmoGeneracional(flujo, distancia, seed, k, Individuos, aleatorioInit, Kworks, Kbest, Cruce, Elite, ParadaIteraciones, ParadaSegundos, VAC, VAM);
                                        }
                                        System.out.println("\t\t\t->Vector de permutacion: " + asignacion);
                                        System.out.println("\t\t\t->Resultado: " + funcionEvaluatoria(flujo, distancia, asignacion));
                                    }
                                } else {
                                    if (tipo.equals("AlgEstacionario")) {
                                        System.out.println("FALTAN PARAMETROS PARA EL ALGORITMO ESTACIONARIO");
                                    } else {
                                        System.out.println("FALTAN PARAMETROS PARA EL ALGORITMO GENERACIONARIO");
                                    }

                                }

                            }
                            texto = br.readLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("-----------------------------------------------------------------------");
                    System.out.println("\u001B[0m");

                }
            }

        }

        if (tipo.equals("AlgMemGeneracional")) {
            System.out.println("\u001B[97m");
            System.out.println("---------- EVALUACIÓN ALGORITMO MEMÉTICO GENERACIONAL ------------");


            // Valores para el algoritmo como k, valor de aceptación de mutacion y cruce utilizados para el memetico generacional
            int k = 0;
            double VAM = 0;
            double VAC = 0;
            int INDEVAL = 0;
            int paradaEval = 0;

            // Buscamos el parámetro de configuración
            for (Object par : otrosParametros) {

                String p = par.toString();

                // La k la utilizaremos para el greedy aleatorizado
                if (p.startsWith("k=")) {
                    try {
                        k = Integer.parseInt(p.substring(2));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + p);
                    }
                }

                // Parada de iteraciones
                if (p.startsWith("parada=")) {
                    try {
                        paradaEval = Integer.parseInt(p.substring(7));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de paradaEval no es un número válido en -> " + paradaEval);
                    }
                }

                // La VAC la utilizaremos para determinar el valor de aceptación de cruce
                if (p.startsWith("VAC=")) {
                    try {
                        VAC = Double.parseDouble(p.substring(4));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + p);
                    }
                }

                // La VAC la utilizaremos para determinar el valor de aceptación de mutación
                if (p.startsWith("VAM=")) {
                    try {
                        VAM = Double.parseDouble(p.substring(4));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de k no es un número válido en -> " + p);
                    }
                }

                // La INDEVAL la utilizaremos para determinar el intervalo de evaluación
                if (p.startsWith("INDEVAL=")) {
                    try {
                        INDEVAL = Integer.parseInt(p.substring(8));
                    } catch (NumberFormatException e) {
                        System.err.println("Error: valor de INDEVAL no es un número válido en -> " + p);
                    }
                }

                if (p.equals("AlgMemGeneracional.conf")) {

                    File archivo = new File("./AlgMemGeneracional.conf");
                    if (!archivo.exists())
                        throw new InvalidParameterException("Archivo " + archivo.getName() + " no existe");


                    // Leémos el archivo linea a linea
                    BufferedReader br;
                    try {
                        br = new BufferedReader(new FileReader(archivo));
                        String texto = br.readLine();

                        while (texto != null) { // Repetir hasta que llegue al final

                            int Individuos = 100;
                            int evaluaciones = 1000;
                            int Kbest = 2;
                            int Kworks = 3;
                            boolean Cruce = true; // OX2
                            int Elite = 1;
                            int iteraciones = 10;

                            String[] argumentos = texto.split(",");

                            for (String argumento : argumentos) {
                                String arg = argumento.trim();

                                if (arg.startsWith("M=")) {
                                    Individuos = Integer.parseInt(arg.substring(2));
                                }

                                // Soporta "evaluaciones:" y "evaluaciones=" y "frecuencia="
                                if (arg.startsWith("evaluaciones:") || arg.startsWith("evaluaciones=") || arg.startsWith("frecuencia=")) {
                                    // Buscamos donde está el separador para cortar el string correctamente
                                    int indexSeparador = arg.indexOf(':');
                                    if(indexSeparador == -1) indexSeparador = arg.indexOf('=');

                                    if(indexSeparador != -1) {
                                        evaluaciones = Integer.parseInt(arg.substring(indexSeparador + 1).trim());
                                    }
                                }

                                if (arg.startsWith("Kbest=") || arg.startsWith("kBest=")) {
                                    Kbest = Integer.parseInt(arg.substring(6));
                                }

                                if (arg.startsWith("Kworks=") || arg.startsWith("kWorst=")) {
                                    int indexEq = arg.indexOf('=');
                                    Kworks = Integer.parseInt(arg.substring(indexEq + 1));
                                }

                                if (arg.startsWith("Cruce=")) {
                                    if (!arg.substring(6).trim().equals("OX2")) {
                                        Cruce = false; // MOC
                                    }
                                }

                                // Soporta "E=" y "Elite="
                                if (arg.startsWith("E=") || arg.startsWith("Elite=")) {
                                    int indexEq = arg.indexOf('=');
                                    Elite = Integer.parseInt(arg.substring(indexEq + 1));
                                }

                                // Soporta "iteraciones:" y "iteraciones="
                                if (arg.startsWith("iteraciones:") || arg.startsWith("iteraciones=")) {
                                    int indexSeparador = arg.indexOf(':');
                                    if(indexSeparador == -1) indexSeparador = arg.indexOf('=');

                                    if(indexSeparador != -1) {
                                        iteraciones = Integer.parseInt(arg.substring(indexSeparador + 1).trim());
                                    }
                                }
                            }

                            for (File archivoActual : getArchivos()) {

                                if (!semillas.isEmpty()) {
                                    System.out.println("\bParámetros Utilizados: " +
                                            "NºIndividuos(" + Individuos + "), " +
                                            "Evaluaciones(" + evaluaciones + "), " +
                                            "Kworks(" + Kworks + "), " +
                                            "Kbest(" + Kbest + "), " +
                                            "Cruce(" + (Cruce ? "MOC" : "OX2") + "), " +
                                            "Elite(" + Elite + "), " +
                                            "Iteraciones(" + iteraciones + "), " +
                                            "Individuos a evaluar(" + INDEVAL + ")");

                                    String color = "\u001B[0m"; // Reset (Blanco/Default)
                                    String nombre = archivoActual.getName().toLowerCase();

                                    if (nombre.contains("ford01")) color = "\u001B[31m";      // Rojo
                                    else if (nombre.contains("ford02")) color = "\u001B[32m"; // Verde
                                    else if (nombre.contains("ford03")) color = "\u001B[33m"; // Amarillo
                                    else if (nombre.contains("ford04")) color = "\u001B[34m"; // Azul

                                    System.out.println("\tLa función Evaluadora para el archivo " + color + archivoActual.getName() + "\u001B[0m");

                                    for (Long seed : semillas) {
                                        int[][] flujo = Archivo.leerDAT(archivoActual, 1);
                                        int[][] distancia = Archivo.leerDAT(archivoActual, 2);
                                        System.out.println("\t\t-> La semilla utilizada para la solucion Inicial es: " + seed);
                                        ArrayList<Integer> asignacion;
                                        long tiempoInicio = System.currentTimeMillis();
                                        asignacion = AlgoritmoMemeticoGeneracional(flujo, distancia, seed, k, Individuos, Kworks, Kbest, Cruce, Elite, paradaEval, VAC, VAM, evaluaciones, iteraciones,archivoActual.getName());
                                        long tiempoFin = System.currentTimeMillis();
                                        double segundos = (tiempoFin - tiempoInicio) / 1000.0;
                                        System.out.println("\t\t\t->Vector de permutacion: " + asignacion);
                                        System.out.println("\t\t\t->Resultado: " + color + funcionEvaluatoria(flujo, distancia, asignacion) + "\u001B[0m");
                                        System.out.println("\t\t\t->Tiempo: " + segundos + " s");
                                    }
                                } else {
                                    System.out.println("FALTAN PARÁMETROS PARA EL ALGORITMO MEMÉTICO GENERACIONAL");
                                }

                            }
                            texto = br.readLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("-----------------------------------------------------------------------");
                    System.out.println("\u001B[0m");


                }
            }

        }*/

}
