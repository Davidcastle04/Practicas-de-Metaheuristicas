import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayList;

public class AlgGRE_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> Greedy(Dato dato, String archivoActual) {
        //Configuracion.Logger logger = new Configuracion.Logger("Greedy", archivoActual, null);

        int tamMatriz = dato.DIMENSION;
        int[][] distancias = calcularMatrizDistancias(dato);

        ArrayList<Integer> UnidadesDistancias = new ArrayList<>();

       // logger.appendLine("Calculando sumas de distancias al resto de ciudades...");

        for (int i = 0; i < tamMatriz; i++) {
            int sum = 0;
            for (int j = 0; j < tamMatriz; j++) {
                sum += distancias[i][j];
            }
            UnidadesDistancias.add(sum);
            //logger.appendLine("Suma de distancias para ciudad " + i + ": " + sum);
        }

        ArrayList<Tuple<Integer, Integer>> Resultado = new ArrayList<>();
        ArrayList<Integer> distanciasCopia = new ArrayList<>(UnidadesDistancias);

       // logger.appendLine("\nIniciando construcción greedy de la ruta...");

        for (int i = 0; i < tamMatriz; i++) {
            int menor = Integer.MAX_VALUE;
            int posicion = -1;

            // Buscar la ciudad con menor suma de distancias restante
            for (int j = 0; j < tamMatriz; j++) {
                if (distanciasCopia.get(j) < menor) {
                    menor = distanciasCopia.get(j);
                    posicion = j;
                }
            }

            //logger.appendLine("Iteración " + i + ": Añadiendo ciudad " + posicion
            //        + " (suma distancias=" + menor + ")");

            distanciasCopia.set(posicion, Integer.MAX_VALUE);
            Resultado.add(new Tuple<>(posicion, menor));

            //logger.appendLine("Ruta parcial: " + Resultado);
        }

        double costeFinal = Configuracion.funcionEvaluatoria(dato, Resultado);
        //logger.logFinalSolution(Resultado, costeFinal);

        return Resultado;
    }

    /**
     * Calcula la matriz de distancias euclídeas entre todas las ciudades
     * a partir de las coordenadas del Dato (formato TSPLIB, EUC_2D).
     */
    private static int[][] calcularMatrizDistancias(Dato dato) {
        int n = dato.DIMENSION;
        int[][] distancias = new int[n][n];

        for (int i = 0; i < n; i++) {
            Coordenadas ci = dato.NODE_COORD_SECTION.get(i);
            for (int j = 0; j < n; j++) {
                Coordenadas cj = dato.NODE_COORD_SECTION.get(j);
                double dx = ci.x - cj.x;
                double dy = ci.y - cj.y;
                distancias[i][j] = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
            }
        }
        return distancias;
    }

}