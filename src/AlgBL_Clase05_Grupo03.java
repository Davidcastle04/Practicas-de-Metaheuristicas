import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AlgBL_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> BusquedaLocal(Dato dato, int k, Long semilla, String Dataset, int maxIteraciones) {
        int n = dato.DIMENSION;
        Distancias dist = Distancias.de(dato);

        Logger logger = new Logger("BusquedaLocal", Dataset, semilla);
        logger.parametro("k (greedy aleatorizado)", k);
        logger.parametro("Iteraciones maximas", maxIteraciones);
        logger.parametro("Entorno", "2-opt con DLB, primer mejor");

        // TODO -- Corregido Claude
        // la solución inicial se saca con el mismo Random(semilla) que el greedy aleatorizado,
        // así con la misma semilla parte de la misma ruta que GRA y que la tabú
        Random random = (semilla == null) ? new Random() : new Random(semilla);
        int[] ruta = Distancias.aArray(AlgGRA_Clase05_Grupo03.generarSolucion(dato, k, random));

        double coste = dist.coste(ruta);
        logger.solucionInicial(ruta, coste);

        // DLB indexado por ciudad: 0 = prometedora, 1 = no mirar
        int[] dlb = new int[n];

        int iteraciones = 0;
        boolean mejoraGlobal = true;

        // se acaba al llegar al máximo de iteraciones o al llegar a un óptimo local de verdad
        while (iteraciones < maxIteraciones) {
            boolean hayMejora = false;
            boolean saltadas = false;

            for (int i = 0; i < n && iteraciones < maxIteraciones; i++) {
                int ciudadI = ruta[i];

                if (dlb[ciudadI] == 1) {
                    saltadas = true;
                    continue;
                }

                boolean improveFlag = false;

                for (int j = i + 1; j < n; j++) {
                    // invertir toda la ruta deja el mismo ciclo (problema simétrico)
                    if (i == 0 && j == n - 1) continue;

                    int ciudadJ = ruta[j];
                    int anterior = ruta[(i - 1 + n) % n];
                    int siguiente = ruta[(j + 1) % n];

                    // factorización: solo cambian dos arcos
                    int arcosDesaparecen = dist.d(anterior, ciudadI) + dist.d(ciudadJ, siguiente);
                    int arcosNuevos = dist.d(anterior, ciudadJ) + dist.d(ciudadI, siguiente);
                    int delta = arcosNuevos - arcosDesaparecen;

                    // primer mejor: el primer vecino que mejora se aplica ya
                    if (delta < 0) {
                        Distancias.invertir(ruta, i, j);
                        coste += delta;
                        iteraciones++;

                        dlb[ciudadI] = 0;
                        dlb[ciudadJ] = 0;
                        improveFlag = true;
                        hayMejora = true;

                        // TODO -- Corregido Claude
                        // ahora cada movimiento aplicado se guarda en el log (una iteración = un movimiento)
                        logger.iteracion(iteraciones, i, j, ciudadI, ciudadJ, delta, coste, ruta, null);
                        break;
                    }
                }

                if (!improveFlag) {
                    dlb[ciudadI] = 1;
                }
            }

            // TODO -- Corregido Claude
            // Antes se paraba en cuanto una pasada no mejoraba, pero con el DLB esa pasada se saltaba
            // ciudades y se dejaba la búsqueda a medias (en a280 se quedaba en ~63% del óptimo con 184
            // movimientos que aún mejoraban). Ahora, si hubo ciudades saltadas, se reactivan todas y
            // se comprueba otra vez; solo se termina si una pasada completa no mejora nada.
            if (!hayMejora) {
                if (!saltadas) {
                    break;
                }
                Arrays.fill(dlb, 0);
            }
        }

        logger.finalizar(ruta, coste);

        return dist.aTuplas(ruta);
    }
}
