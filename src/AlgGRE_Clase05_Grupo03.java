import java.util.ArrayList;
import java.util.Arrays;

public class AlgGRE_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> Greedy(Dato dato, String archivoActual) {
        Logger logger = new Logger("Greedy", archivoActual, null);

        int n = dato.DIMENSION;

        Distancias dist = Distancias.de(dato);
        int[] sumas = dist.sumas();

        // vector <ciudad, suma de distancias al resto> ordenado de menor a mayor suma
        Integer[] orden = new Integer[n];
        for (int i = 0; i < n; i++) {
            orden[i] = i;
        }
        Arrays.sort(orden, (a, b) -> Integer.compare(sumas[a], sumas[b]));

        ArrayList<Tuple<Integer, Integer>> resultado = new ArrayList<>(n);
        for (int paso = 0; paso < n; paso++) {
            int ciudad = orden[paso];
            resultado.add(new Tuple<>(ciudad, sumas[ciudad]));
            logger.paso(paso, ciudad, sumas[ciudad], null);
        }

        double costeFinal = Configuracion.funcionEvaluatoria(dato, resultado);
        logger.finalizar(resultado, costeFinal);

        return resultado;
    }
}
