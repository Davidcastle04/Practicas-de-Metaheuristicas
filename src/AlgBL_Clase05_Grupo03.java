import java.util.ArrayList;
import java.util.Collections;

public class AlgBL_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> BusquedaLocal(Dato dato, int k, Long semilla, String Dataset, int maxIteraciones) {
        int n = dato.DIMENSION;
        int[][] distancias = calcularMatrizDistancias(dato);

        // Llamamos al Greedy Aleatorizado para generar la primera solución inicial
        ArrayList<Tuple<Integer, Integer>> rutaActual = AlgGRA_Clase05_Grupo03.GreedyAleatorizado(dato,k,semilla,Dataset);

        // Creamos el DLB.
        int[] DLB = new int[n];

        // Lo inicializamos a 0.
        for (int i = 0; i < n; i++) {
            DLB[i] = 0;
        }

        // Controlamos la parada y si en una pasada completa ha habido alguna mejora.
        int iteraciones = 0;
        boolean mejoraGlobal = true;

        // Repetimos hasta que no haya mejoras o se agoten las iteraciones.
        while (mejoraGlobal && iteraciones < maxIteraciones) {
            mejoraGlobal = false;

            for (int i = 0; i < n; i++) {
                // Si el bit es 1, ignoramos la ciudad porque no es prometedora.
                if (DLB[i] == 0) {
                    boolean improve_flag = false;

                    // Exploramos el entorno invirtiendo el tramo de 'i' a 'j'.
                    for (int j = i + 1; j < n; j++) {

                        // Invertir toda la ruta no cambia el ciclo, así que lo saltamos.
                        if (i == 0 && j == n - 1) continue;

                        // Calculamos solo los arcos que cambian (factorización).
                        int nodoAnteriorI = rutaActual.get((i - 1 + n) % n).first;
                        int nodoI = rutaActual.get(i).first;
                        int nodoJ = rutaActual.get(j).first;
                        int nodoSiguienteJ = rutaActual.get((j + 1) % n).first;

                        int arcosDesaparecen = distancias[nodoAnteriorI][nodoI] + distancias[nodoJ][nodoSiguienteJ];
                        int arcosNuevos = distancias[nodoAnteriorI][nodoJ] + distancias[nodoI][nodoSiguienteJ];

                        int delta = arcosNuevos - arcosDesaparecen;

                        // Primer mejor: si el delta es negativo, aplicamos el movimiento en cuanto lo encontramos.
                        if (delta < 0) {
                            // Aplicamos el 2-opt invirtiendo el tramo de i a j.
                            Collections.reverse(rutaActual.subList(i, j + 1));

                            // Reactivamos las dos ciudades porque su entorno ha cambiado.
                            DLB[i] = 0;
                            DLB[j] = 0;
                            iteraciones++; // Contamos una iteración por cada movimiento aplicado, no por evaluarlo.
                            improve_flag = true;
                            mejoraGlobal = true;
                            break;
                        }

                        // Comprobamos la parada dentro del bucle interno.
                        if (iteraciones >= maxIteraciones) break;
                    }

                    // Si ningún 'j' ha mejorado con esta ciudad, apagamos su bit.
                    if (!improve_flag) {
                        DLB[i] = 1;
                    }

                    if (iteraciones >= maxIteraciones) break;
                }
            }
        }
        return rutaActual;
    }

    // Calculamos la matriz de distancias euclídeas.
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