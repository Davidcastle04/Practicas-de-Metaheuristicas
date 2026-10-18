
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Collections;

public class AlgBT_Clase05_Grupo03 {

    /*
     * ============================================================
     *                 BÚSQUEDA TABÚ PARA TSP
     * ============================================================
     *
     * OBJETIVO:
     * Encontrar una ruta de coste mínimo que visite todas las
     * ciudades una sola vez y vuelva a la ciudad de origen.
     *
     * ESTRATEGIA:
     *  1. Generamos una solución inicial mediante Greedy o GRA.
     *  2. Exploramos el vecindario mediante movimientos 2-opt.
     *  3. Utilizamos una lista tabú para evitar ciclos.
     *  4. Aplicamos el criterio de aspiración.
     *  5. Utilizamos DLB para reducir exploraciones innecesarias.
     *  6. Diversificamos la búsqueda cuando hay estancamiento.
     * ============================================================
     */

    public static ArrayList<Tuple<Integer, Integer>> BusquedaTabu(
            Dato dato,
            Long semilla,
            int k,
            int maxIteraciones,
            int Ten,
            Double Osc,
            Double Esta,
            String Dataset) {

        // --------------------------------------------------------
        // 1. VALIDACIÓN DE PARÁMETROS
        // --------------------------------------------------------

        if (dato == null || dato.NODE_COORD_SECTION == null) {
            throw new IllegalArgumentException(
                    "El dato o sus coordenadas no pueden ser nulos.");
        }

        int n = dato.DIMENSION;

        if (n < 1 || dato.NODE_COORD_SECTION.size() != n) {
            throw new IllegalArgumentException(
                    "La dimensión no coincide con el número de coordenadas.");
        }

        if (maxIteraciones < 0 || Ten <= 0
                || Osc == null || Esta == null
                || !Double.isFinite(Osc)
                || !Double.isFinite(Esta)
                || Osc < 0.0 || Osc > 1.0
                || Esta < 0.0 || Esta > 1.0) {

            throw new IllegalArgumentException(
                    "Parámetros de búsqueda tabú no válidos.");
        }

        // --------------------------------------------------------
        // 2. GENERADOR ALEATORIO
        // --------------------------------------------------------

        Random rnd = (semilla == null || semilla == -1L)
                ? new Random()
                : new Random(semilla);

        // --------------------------------------------------------
        // 3. PRECALCULAR MATRIZ DE DISTANCIAS
        // --------------------------------------------------------

        int[][] distancias = calcularMatrizDistancias(dato);

        // --------------------------------------------------------
        // 4. GENERAR SOLUCIÓN INICIAL
        // --------------------------------------------------------

        ArrayList<Tuple<Integer, Integer>> solucionActual;

        if (semilla == null || (semilla == -1L && k == -1)) {

            solucionActual = AlgGRE_Clase05_Grupo03.Greedy(
                    dato, Dataset + "--BUSQUEDATABU-INICIAL");

        } else {

            solucionActual =
                    AlgGRA_Clase05_Grupo03.GreedyAleatorizado(
                            dato, k, semilla,
                            Dataset + "--BUSQUEDATABU-INICIAL");
        }

        if (solucionActual == null || solucionActual.size() != n) {
            throw new IllegalStateException(
                    "Greedy no ha generado una ruta válida.");
        }

        solucionActual = new ArrayList<>(solucionActual);

        double costeActual = costeRuta(solucionActual, distancias);

        ArrayList<Tuple<Integer, Integer>> mejorSolucion =
                new ArrayList<>(solucionActual);

        double mejorCoste = costeActual;

        // --------------------------------------------------------
        // 5. ESTRUCTURAS DE LA BÚSQUEDA TABÚ
        // --------------------------------------------------------

        /*
         * Cada movimiento tabú almacena las posiciones i y j
         * y el número de iteraciones durante las que permanece activo.
         */
        class MovimientoTabu {
            int i;
            int j;
            int vida;

            MovimientoTabu(int i, int j, int vida) {
                this.i = i;
                this.j = j;
                this.vida = vida;
            }
        }

        List<MovimientoTabu> listaTabu = new ArrayList<>();

        /*
         * DLB (Don't Look Bits):
         * true  -> no exploramos movimientos desde esta posición.
         * false -> la posición puede explorarse.
         */
        boolean[] dlb = new boolean[n];
        Arrays.fill(dlb, false);

        int iteraciones = 0;
        int sinMejora = 0;

        int maxSinMejora = Math.max(
                1, (int) Math.ceil(Esta * maxIteraciones));

        // --------------------------------------------------------
        // 6. BUCLE PRINCIPAL
        // --------------------------------------------------------

        while (iteraciones < maxIteraciones
                && sinMejora < maxSinMejora) {

            double mejorVecinoCoste = Double.POSITIVE_INFINITY;

            ArrayList<Tuple<Integer, Integer>> mejorVecino = null;

            int mejorI = -1;
            int mejorJ = -1;

            // ----------------------------------------------------
            // 7. EXPLORACIÓN DEL VECINDARIO 2-OPT
            // ----------------------------------------------------

            for (int i = 0; i < n - 1; i++) {

                if (dlb[i]) {
                    continue;
                }

                boolean mejoraPosicion = false;

                for (int j = i + 1; j < n; j++) {

                    // Evitamos invertir toda la ruta.
                    if (i == 0 && j == n - 1) {
                        continue;
                    }

                    /*
                     * FACTORIZACIÓN 2-OPT:
                     * Quitamos los arcos (i-1, i) y (j, j+1).
                     * Añadimos los arcos (i-1, j) y (i, j+1).
                     */

                    int anteriorI = (i - 1 + n) % n;
                    int siguienteJ = (j + 1) % n;

                    int ciudadAnterior =
                            solucionActual.get(anteriorI).first;

                    int ciudadI =
                            solucionActual.get(i).first;

                    int ciudadJ =
                            solucionActual.get(j).first;

                    int ciudadSiguiente =
                            solucionActual.get(siguienteJ).first;

                    int arcosAntiguos =
                            distancias[ciudadAnterior][ciudadI]
                                    + distancias[ciudadJ][ciudadSiguiente];

                    int arcosNuevos =
                            distancias[ciudadAnterior][ciudadJ]
                                    + distancias[ciudadI][ciudadSiguiente];

                    double costeVecino = costeActual
                            + arcosNuevos - arcosAntiguos;

                    // Comprobar si el movimiento es tabú.
                    boolean esTabu = false;

                    for (MovimientoTabu movimiento : listaTabu) {
                        if (movimiento.i == i
                                && movimiento.j == j) {
                            esTabu = true;
                            break;
                        }
                    }

                    /*
                     * CRITERIO DE ASPIRACIÓN:
                     * Permitimos un movimiento tabú si mejora
                     * la mejor solución global.
                     */
                    if (esTabu && costeVecino >= mejorCoste) {
                        continue;
                    }

                    // Seleccionamos el mejor vecino admisible.
                    if (costeVecino < mejorVecinoCoste) {

                        mejorVecinoCoste = costeVecino;
                        mejorI = i;
                        mejorJ = j;

                        mejorVecino = new ArrayList<>(solucionActual);

                        Collections.reverse(
                                mejorVecino.subList(i, j + 1));

                        mejoraPosicion = costeVecino < costeActual;
                    }
                }

                // Si no hay mejora desde i, activamos su DLB.
                if (!mejoraPosicion) {
                    dlb[i] = true;
                }
            }

            // ----------------------------------------------------
            // 8. APLICAR EL MOVIMIENTO ELEGIDO
            // ----------------------------------------------------

            iteraciones++;

            if (mejorVecino == null) {
                Arrays.fill(dlb, false);
                sinMejora++;
                continue;
            }

            solucionActual = mejorVecino;
            costeActual = mejorVecinoCoste;

            // ----------------------------------------------------
            // 9. ACTUALIZAR LISTA TABÚ
            // ----------------------------------------------------

            listaTabu.add(
                    new MovimientoTabu(mejorI, mejorJ, Ten));

            Iterator<MovimientoTabu> iterador =
                    listaTabu.iterator();

            while (iterador.hasNext()) {
                MovimientoTabu movimiento = iterador.next();

                movimiento.vida--;

                if (movimiento.vida <= 0) {
                    iterador.remove();
                }
            }

            // ----------------------------------------------------
            // 10. ACTUALIZAR MEJOR SOLUCIÓN GLOBAL
            // ----------------------------------------------------

            if (costeActual < mejorCoste) {

                mejorSolucion = new ArrayList<>(solucionActual);
                mejorCoste = costeActual;
                sinMejora = 0;

                // Reactivamos la exploración del vecindario.
                Arrays.fill(dlb, false);

            } else {
                sinMejora++;
            }

            // Las posiciones modificadas vuelven a explorarse.
            dlb[mejorI] = false;
            dlb[mejorJ] = false;

            // ----------------------------------------------------
            // 11. DIVERSIFICACIÓN
            // ----------------------------------------------------

            if (sinMejora > 0
                    && sinMejora % maxSinMejora == 0
                    && rnd.nextDouble() < Osc) {

                Arrays.fill(dlb, false);
            }
        }

        // --------------------------------------------------------
        // 12. DEVOLVER LA MEJOR SOLUCIÓN ENCONTRADA
        // --------------------------------------------------------

        return mejorSolucion;
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    /*
     * Calcula la matriz simétrica de distancias euclídeas
     * redondeadas, siguiendo el criterio EUC_2D.
     */
    private static int[][] calcularMatrizDistancias(Dato dato) {

        int n = dato.DIMENSION;
        int[][] distancias = new int[n][n];

        for (int i = 0; i < n; i++) {

            Coordenadas ci = dato.NODE_COORD_SECTION.get(i);

            for (int j = i + 1; j < n; j++) {

                Coordenadas cj = dato.NODE_COORD_SECTION.get(j);

                double dx = ci.x - cj.x;
                double dy = ci.y - cj.y;

                int distancia = (int) Math.round(
                        Math.sqrt(dx * dx + dy * dy));

                distancias[i][j] = distancia;
                distancias[j][i] = distancia;
            }
        }

        return distancias;
    }

    /*
     * Calcula el coste total de una ruta cerrada:
     * última ciudad -> primera ciudad.
     */
    private static double costeRuta(
            ArrayList<Tuple<Integer, Integer>> ruta,
            int[][] distancias) {

        double coste = 0.0;
        int n = ruta.size();

        for (int i = 0; i < n; i++) {

            int actual = ruta.get(i).first;
            int siguiente = ruta.get((i + 1) % n).first;

            coste += distancias[actual][siguiente];
        }

        return coste;
    }
}
