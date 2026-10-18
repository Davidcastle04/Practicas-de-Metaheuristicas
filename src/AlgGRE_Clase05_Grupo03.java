import java.util.ArrayList;
import java.util.Arrays;

public class AlgGRE_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> Greedy(Dato dato, String archivoActual) {
        // TODO -- Corregido Claude
        // el logger estaba comentado, ahora escribe en logs/ un paso por cada ciudad añadida
        Logger logger = new Logger("Greedy", archivoActual, null);

        int n = dato.DIMENSION;

        // TODO -- Corregido Claude
        // antes se hacía una matriz de n x n a mano en cada algoritmo (con d18512 no cabe en memoria),
        // ahora se usa la clase Distancias que la calcula una vez y se salta la matriz si n es grande
        Distancias dist = Distancias.de(dato);
        int[] sumas = dist.sumas();

        // vector <ciudad, suma de distancias al resto> ordenado de menor a mayor suma
        Integer[] orden = new Integer[n];
        for (int i = 0; i < n; i++) {
            orden[i] = i;
        }
        // TODO -- Corregido Claude
        // antes se buscaba el mínimo n veces (n^2), con ordenar una vez basta. El sort es estable,
        // así que si hay empate sale antes la ciudad con índice menor, igual que antes
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
