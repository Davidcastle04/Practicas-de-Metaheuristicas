import java.util.ArrayList;
import java.util.Random;

public class AlgGRA_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> GreedyAleatorizado(Dato dato, int k, Long semilla, String archivoActual) {
        Logger logger = new Logger("GreedyAleatorizado", archivoActual, semilla);
        logger.parametro("k", k);

        Random random = (semilla == null) ? new Random() : new Random(semilla);

        ArrayList<Tuple<Integer, Integer>> resultado = construir(dato, k, random, logger);

        double costeFinal = Configuracion.funcionEvaluatoria(dato, resultado);
        logger.finalizar(resultado, costeFinal);

        return resultado;
    }


    public static ArrayList<Tuple<Integer, Integer>> generarSolucion(Dato dato, int k, Random random) {
        return construir(dato, k, random, null);
    }

    private static ArrayList<Tuple<Integer, Integer>> construir(Dato dato, int k, Random random, Logger logger) {

        if (k < 1) {
            throw new IllegalArgumentException("k del greedy aleatorizado tiene que ser al menos 1 (valor: " + k + ")");
        }

        int n = dato.DIMENSION;
        int[] sumas = Distancias.de(dato).sumas();

        // ciudades ordenadas de menor a mayor suma de distancias
        ArrayList<Integer> candidatas = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            candidatas.add(i);
        }
        candidatas.sort((a, b) -> Integer.compare(sumas[a], sumas[b]));

        ArrayList<Tuple<Integer, Integer>> resultado = new ArrayList<>(n);

        for (int paso = 0; paso < n; paso++) {
            // se elige al azar entre las k primeras que quedan y se quita de la lista
            int kActual = Math.min(k, candidatas.size());
            int posicion = random.nextInt(kActual);
            int ciudad = candidatas.remove(posicion);

            resultado.add(new Tuple<>(ciudad, sumas[ciudad]));

            if (logger != null) {
                logger.paso(paso, ciudad, sumas[ciudad], "(elegida entre las " + kActual + " mejores)");
            }
        }

        return resultado;
    }
}
