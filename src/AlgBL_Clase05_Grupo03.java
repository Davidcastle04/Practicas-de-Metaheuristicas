import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AlgBL_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> BusquedaLocal(Dato dato, int k, Long semilla, String Dataset, int maxIteraciones) {
        int n = dato.DIMENSION;
        int[][] distancias = calcularMatrizDistancias(dato);

        // Llamamos al Greedy Aleatorizado para generar la primera solución inicial
        ArrayList<Tuple<Integer, Integer>> rutaActual = AlgGRA_Clase05_Grupo03.GreedyAleatorizado(dato,k,semilla,Dataset);

        // Definimos el DLB
        int[] DLB = new int[n];

        // Incializamos el DLB a 0
        for (int i = 0; i < n; i++) {
            DLB[i] = 0;
        }

        // Controlamos las condiciones de parada, y además sabemos si en una pasada completa hubo alguna mejora
        int iteraciones = 0;
        boolean mejoraGlobal = true;

        // Usamos un While para detenermos cuando la condicion llegue
        while (mejoraGlobal && iteraciones < maxIteraciones) {
            mejoraGlobal = false;

            for (int i = 0; i < n; i++) {
                // Si nodo = 1, lo ignoramos porque no es prometedor
                if (DLB[i] == 0) {
                    boolean improve_flag = false;

                    // Exploramos el vecindario invirtiendo desde 'i' hasta 'j' el otp
                    for (int j = i + 1; j < n; j++) {

                        // como es simetrico, evitamos invertir por completo!!!!!!!!!!!!!!
                        if (i == 0 && j == n - 1) continue;

                        // FACTORIZACIÓN, solo los arcos diferentes
                        int nodoAnteriorI = rutaActual.get((i - 1 + n) % n).first;
                        int nodoI = rutaActual.get(i).first;
                        int nodoJ = rutaActual.get(j).first;
                        int nodoSiguienteJ = rutaActual.get((j + 1) % n).first;

                        int arcosDesaparecen = distancias[nodoAnteriorI][nodoI] + distancias[nodoJ][nodoSiguienteJ];
                        int arcosNuevos = distancias[nodoAnteriorI][nodoJ] + distancias[nodoI][nodoSiguienteJ];

                        int delta = arcosNuevos - arcosDesaparecen;

                        // PRIMER MEJOR: Si el delta es negativo, el movimiento reduce el coste y lo aplicamos INMEDIATAMENTE
                        if (delta < 0) {
                            // Aplicamos el movimiento 2-opt (Invierte el subsegmento de i a j)
                            Collections.reverse(rutaActual.subList(i, j + 1));

                            // Activamos ambos nodos en el DLB porque ahora su entorno ha cambiado y vuelve a ser prometedor
                            DLB[i] = 0;
                            DLB[j] = 0;
                            iteraciones++; // Cada vez que evaluamos un movimiento, sumamos una iteración todo cada vez que se aplique, no al evaluar
                            improve_flag = true;
                            mejoraGlobal = true;
                            break;
                        }

                        // Verificación parada bucle interno
                        if (iteraciones >= maxIteraciones) break;
                    }

                    // Si tras probar todas las combinaciones con 'j', este nodo 'i' no mejoró nada, lo apagamos
                    if (!improve_flag) {
                        DLB[i] = 1;
                    }

                    if (iteraciones >= maxIteraciones) break;
                }
            }
        }
        return rutaActual;
    }

    /**
     * Calcula la matriz de distancias euclídeas (Evitando crear objetos innecesarios)
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