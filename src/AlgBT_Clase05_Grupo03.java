import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class AlgBT_Clase05_Grupo03 {

    // hasta este tamaño la matriz de memorias es un array normal, si no usamos un mapa
    private static final int LIMITE_MATRIZ = 3000;

    // TODO -- Corregido Claude
    // El algoritmo estaba mal respecto al guión: tenía criterio de aspiración, usaba DLB para elegir
    // el movimiento, no había reinicio por estancamiento ni MLP, y la tabú era una lista de posiciones.
    // Se ha rehecho siguiendo el guión (MCP/MLP en una matriz cuadrada, oscilación estratégica, etc.)
    public static ArrayList<Tuple<Integer, Integer>> BusquedaTabu(
            Dato dato,
            Long semilla,
            int k,
            int maxIteraciones,
            int Ten,
            Double Osc,
            Double Esta,
            String Dataset) {

        if (dato == null || dato.NODE_COORD_SECTION == null) {
            throw new IllegalArgumentException("El dato o sus coordenadas no pueden ser nulos.");
        }

        int n = dato.DIMENSION;

        if (n < 4 || dato.NODE_COORD_SECTION.size() != n) {
            throw new IllegalArgumentException("La dimensión no coincide con el número de coordenadas.");
        }

        if (k < 1 || maxIteraciones < 0 || Ten <= 0
                || Osc == null || Esta == null
                || Osc < 0.0 || Osc > 1.0
                || Esta < 0.0 || Esta > 1.0) {
            throw new IllegalArgumentException("Parámetros de búsqueda tabú no válidos.");
        }

        Random rnd = (semilla == null) ? new Random() : new Random(semilla);
        Distancias dist = Distancias.de(dato);

        // nº de movimientos seguidos que no mejoran el mejor del momento para decir que se estancó
        int maxEmpeoramientos = Math.max(1, (int) Math.round(Esta * maxIteraciones));

        Logger logger = new Logger("BusquedaTabu", Dataset, semilla);
        logger.parametro("k (greedy aleatorizado)", k);
        logger.parametro("Iteraciones maximas", maxIteraciones);
        logger.parametro("Tenencia tabu", Ten);
        logger.parametro("Oscilacion estrategica", Osc);
        logger.parametro("Estancamiento", Esta + " (" + maxEmpeoramientos + " empeoramientos seguidos)");

        // solución inicial: greedy aleatorizado con la misma semilla que usa la BL
        int[] actual = Distancias.aArray(AlgGRA_Clase05_Grupo03.generarSolucion(dato, k, rnd));
        double costeActual = dist.coste(actual);

        int[] mejorMomento = actual.clone();
        double costeMejorMomento = costeActual;

        int[] mejorGlobal = actual.clone();
        double costeMejorGlobal = costeActual;

        logger.solucionInicial(actual, costeActual);

        // TODO -- Corregido Claude
        // Una sola matriz cuadrada: por encima de la diagonal la MCP (tenencia de cada movimiento)
        // y por debajo la MLP (veces que se ha usado cada arco)
        MatrizMemoria memoria = new MatrizMemoria(n);

        // movimientos tabú vivos {ciudadMenor, ciudadMayor, vida}, así no hay que recorrer toda la matriz
        ArrayDeque<int[]> tabuVivos = new ArrayDeque<>();

        boolean[] dlb = new boolean[n];

        int empeoramientos = 0;
        int iteraciones = 0;

        while (iteraciones < maxIteraciones) {

            int mejorI = -1;
            int mejorJ = -1;
            int mejorDelta = 0;
            boolean hayMovimiento = false;

            // TODO -- Corregido Claude
            // Fase 1: igual que la BL, primer vecino que mejora a la actual (con DLB) saltándose los tabú
            for (int i = 0; i < n - 1 && !hayMovimiento; i++) {
                int ciudadI = actual[i];
                if (dlb[ciudadI]) continue;

                boolean mejora = false;
                boolean saltadoPorTabu = false;

                for (int j = i + 1; j < n; j++) {
                    if (i == 0 && j == n - 1) continue;

                    if (esTabu(memoria, ciudadI, actual[j])) {
                        saltadoPorTabu = true;
                        continue;
                    }

                    int delta = delta2opt(actual, i, j, n, dist);
                    if (delta < 0) {
                        mejorI = i;
                        mejorJ = j;
                        mejorDelta = delta;
                        hayMovimiento = true;
                        mejora = true;
                        break;
                    }
                }

                // si había algún movimiento tabú no apagamos la ciudad, puede que luego sí valga
                if (!mejora && !saltadoPorTabu) {
                    dlb[ciudadI] = true;
                }
            }

            // TODO -- Corregido Claude
            // Fase 2: ningún vecino mejora, así que nos movemos al mejor del entorno completo
            // aunque empeore ("la mejor de las peores"). Sin criterio de aspiración.
            if (!hayMovimiento) {
                int mejorDeltaEntorno = Integer.MAX_VALUE;

                for (int i = 0; i < n - 1; i++) {
                    int ciudadI = actual[i];
                    for (int j = i + 1; j < n; j++) {
                        if (i == 0 && j == n - 1) continue;
                        if (esTabu(memoria, ciudadI, actual[j])) continue;

                        int delta = delta2opt(actual, i, j, n, dist);
                        if (delta < mejorDeltaEntorno) {
                            mejorDeltaEntorno = delta;
                            mejorI = i;
                            mejorJ = j;
                        }
                    }
                }

                if (mejorI != -1) {
                    mejorDelta = mejorDeltaEntorno;
                    hayMovimiento = true;
                }
            }

            iteraciones++;

            // las memorias se actualizan en cada iteración
            decrementarTabu(tabuVivos, memoria);

            if (!hayMovimiento) {
                // pasa solo si todos los vecinos son tabú
                empeoramientos++;
                logger.sinMovimiento(iteraciones);
            } else {
                int ciudadA = actual[mejorI];
                int ciudadB = actual[mejorJ];

                Distancias.invertir(actual, mejorI, mejorJ);
                costeActual += mejorDelta;

                // MCP: el movimiento queda tabú durante Ten iteraciones
                int menor = Math.min(ciudadA, ciudadB);
                int mayor = Math.max(ciudadA, ciudadB);
                memoria.set(menor, mayor, Ten);
                tabuVivos.add(new int[]{menor, mayor, Ten});

                // MLP: sumamos 1 a cada arco de la solución actual
                for (int p = 0; p < n; p++) {
                    int c1 = actual[p];
                    int c2 = actual[(p + 1) % n];
                    memoria.sumar(Math.max(c1, c2), Math.min(c1, c2));
                }

                dlb[ciudadA] = false;
                dlb[ciudadB] = false;

                // mejor del momento: si no lo mejora cuenta como empeoramiento
                if (costeActual < costeMejorMomento) {
                    mejorMomento = actual.clone();
                    costeMejorMomento = costeActual;
                    empeoramientos = 0;
                } else {
                    empeoramientos++;
                }

                String nota = null;
                if (costeActual < costeMejorGlobal) {
                    mejorGlobal = actual.clone();
                    costeMejorGlobal = costeActual;
                    nota = "NUEVO MEJOR GLOBAL";
                }

                logger.iteracion(iteraciones, mejorI, mejorJ, ciudadA, ciudadB,
                        mejorDelta, costeActual, actual, nota);
            }

            // TODO -- Corregido Claude
            // Estancamiento: nueva solución por oscilación estratégica (Osc = prob. de intensificar,
            // el resto diversifica) y se reinician todas las estructuras menos la MLP
            if (empeoramientos >= maxEmpeoramientos && iteraciones < maxIteraciones) {
                boolean intensificar = rnd.nextDouble() < Osc;

                actual = generarNueva(n, intensificar, memoria, dist, rnd);
                costeActual = dist.coste(actual);

                for (int[] t : tabuVivos) {
                    memoria.set(t[0], t[1], 0);
                }
                tabuVivos.clear();
                Arrays.fill(dlb, false);
                empeoramientos = 0;

                mejorMomento = actual.clone();
                costeMejorMomento = costeActual;

                if (costeActual < costeMejorGlobal) {
                    mejorGlobal = actual.clone();
                    costeMejorGlobal = costeActual;
                }

                logger.estancamiento(iteraciones, intensificar ? "intensificacion" : "diversificacion",
                        costeActual, actual);
            }
        }

        logger.finalizar(mejorGlobal, costeMejorGlobal);

        return dist.aTuplas(mejorGlobal);
    }

    // Cambio de coste de invertir el tramo i..j (solo cambian dos arcos)
    private static int delta2opt(int[] ruta, int i, int j, int n, Distancias dist) {
        int anterior = ruta[(i - 1 + n) % n];
        int siguiente = ruta[(j + 1) % n];
        int ci = ruta[i];
        int cj = ruta[j];

        return dist.d(anterior, cj) + dist.d(ci, siguiente)
                - dist.d(anterior, ci) - dist.d(cj, siguiente);
    }

    private static boolean esTabu(MatrizMemoria memoria, int a, int b) {
        return memoria.get(Math.min(a, b), Math.max(a, b)) > 0;
    }

    // resta 1 a la tenencia de cada movimiento tabú y quita los que llegan a 0
    private static void decrementarTabu(ArrayDeque<int[]> tabuVivos, MatrizMemoria memoria) {
        Iterator<int[]> it = tabuVivos.iterator();
        while (it.hasNext()) {
            int[] t = it.next();
            t[2]--;
            memoria.set(t[0], t[1], Math.max(t[2], 0));
            if (t[2] <= 0) {
                it.remove();
            }
        }
    }

    // TODO -- Corregido Claude
    // Nueva solución a partir de la MLP: desde una ciudad al azar se va a la ciudad sin visitar con el arco
    // más usado (intensificar) o menos usado (diversificar); si empatan, la más cercana
    private static int[] generarNueva(int n, boolean intensificar, MatrizMemoria memoria,
                                      Distancias dist, Random rnd) {
        int[] nueva = new int[n];
        boolean[] visitada = new boolean[n];

        int actual = rnd.nextInt(n);
        nueva[0] = actual;
        visitada[actual] = true;

        for (int pos = 1; pos < n; pos++) {
            int elegida = -1;
            int mejorFrecuencia = 0;
            int mejorDistancia = 0;

            for (int c = 0; c < n; c++) {
                if (visitada[c]) continue;

                int frecuencia = memoria.get(Math.max(actual, c), Math.min(actual, c));
                int distancia = dist.d(actual, c);

                boolean mejor;
                if (elegida == -1) {
                    mejor = true;
                } else if (frecuencia != mejorFrecuencia) {
                    mejor = intensificar ? frecuencia > mejorFrecuencia : frecuencia < mejorFrecuencia;
                } else {
                    mejor = distancia < mejorDistancia;
                }

                if (mejor) {
                    elegida = c;
                    mejorFrecuencia = frecuencia;
                    mejorDistancia = distancia;
                }
            }

            nueva[pos] = elegida;
            visitada[elegida] = true;
            actual = elegida;
        }

        return nueva;
    }

    // Matriz cuadrada n x n: encima de la diagonal la MCP, debajo la MLP
    private static class MatrizMemoria {
        private final long n;
        private final int[][] densa;
        private final HashMap<Long, Integer> dispersa;

        MatrizMemoria(int n) {
            this.n = n;
            if (n <= LIMITE_MATRIZ) {
                densa = new int[n][n];
                dispersa = null;
            } else {
                // con d18512 la matriz entera ocuparía 1,3 GB, solo guardamos las celdas distintas de 0
                densa = null;
                dispersa = new HashMap<>();
            }
        }

        int get(int fila, int col) {
            if (densa != null) return densa[fila][col];
            Integer v = dispersa.get(fila * n + col);
            return v == null ? 0 : v;
        }

        void set(int fila, int col, int valor) {
            if (densa != null) {
                densa[fila][col] = valor;
            } else if (valor == 0) {
                dispersa.remove(fila * n + col);
            } else {
                dispersa.put(fila * n + col, valor);
            }
        }

        void sumar(int fila, int col) {
            if (densa != null) {
                densa[fila][col]++;
            } else {
                dispersa.merge(fila * n + col, 1, Integer::sum);
            }
        }
    }
}
