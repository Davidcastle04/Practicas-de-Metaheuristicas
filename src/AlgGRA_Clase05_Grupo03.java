import java.util.ArrayList;
import java.util.Random;

public class AlgGRA_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> GreedyAleatorizado(Dato dato, int k, Long semilla, String archivoActual) {
        //Configuracion.Logger logger = new Configuracion.Logger("GreedyAleatorizado", archivoActual, semilla);
       // logger.appendLine("Parámetro k: " + k);

        Random random = new Random(semilla);
        int tamMatriz = dato.DIMENSION;
        int[][] distancias = calcularMatrizDistancias(dato);

        ArrayList<Integer> ciudadesDisponibles = new ArrayList<>();
        ArrayList<Integer> sumaDistancias = new ArrayList<>();

        //logger.appendLine("Calculando sumas de distancias al resto de ciudades...");

        for (int i = 0; i < tamMatriz; i++) {
            int sum = 0;
            for (int j = 0; j < tamMatriz; j++) {
                sum += distancias[i][j];
            }
            ciudadesDisponibles.add(i);
            sumaDistancias.add(sum);
        }

        ArrayList<Tuple<Integer, Integer>> Resultado = new ArrayList<>();

        //logger.appendLine("\nIniciando construcción greedy aleatorizada de la ruta...");

        for (int i = 0; i < tamMatriz; i++) {
            // Ordenar las ciudades disponibles por suma de distancias ascendente
            ciudadesDisponibles.sort((a, b) -> Integer.compare(sumaDistancias.get(a), sumaDistancias.get(b)));

            int kActual = Math.min(k, ciudadesDisponibles.size());
            int idxElegido = random.nextInt(kActual);
            int ciudadElegida = ciudadesDisponibles.remove(idxElegido);
            int distanciaElegida = sumaDistancias.get(ciudadElegida);

            //logger.appendLine("Iteración " + i + ": elegida entre las " + kActual
            //        + " mejores -> ciudad " + ciudadElegida + " (suma=" + distanciaElegida + ")");

            Resultado.add(new Tuple<>(ciudadElegida, distanciaElegida));
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
                euclideo e = new euclideo();
                e.x = ci.x - cj.x;
                e.y = ci.y - cj.y;
                distancias[i][j] = e.aplicarformula();
            }
        }
        return distancias;
    }
}