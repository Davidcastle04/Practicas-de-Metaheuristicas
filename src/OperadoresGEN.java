import java.util.Arrays;
import java.util.Random;

/**
 * Módulos del esquema generacional (inicializar, evaluar, élite, selección, recombinación,
 * mutación y reemplazo). Cada método es un bloque del esquema de la práctica.
 */
public class OperadoresGEN {

    // ------------------------------------------------------------------ Inicializar Pt

    /** M individuos: un porcentaje totalmente aleatorio y el resto con el greedy aleatorizado de la práctica 1. */
    static Individuo[] inicializarPoblacion(Dato dato, ParametrosGEN p, Random rnd) {
        int n = dato.DIMENSION;
        int numAleatorios = (int) Math.round(p.porcAleatorios * p.tamPoblacion);

        Individuo[] poblacion = new Individuo[p.tamPoblacion];
        for (int i = 0; i < p.tamPoblacion; i++) {
            if (i < numAleatorios) {
                poblacion[i] = new Individuo(permutacionAleatoria(n, rnd));
            } else {
                poblacion[i] = new Individuo(Distancias.aArray(
                        AlgGRA_Clase05_Grupo03.generarSolucion(dato, p.kGreedy, rnd)));
            }
        }
        return poblacion;
    }

    private static int[] permutacionAleatoria(int n, Random rnd) {
        int[] r = new int[n];
        for (int i = 0; i < n; i++) {
            r[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {   // Fisher-Yates
            int j = rnd.nextInt(i + 1);
            int aux = r[i];
            r[i] = r[j];
            r[j] = aux;
        }
        return r;
    }

    // ------------------------------------------------------------------ Evaluar

    /**
     * Evalúa solo los individuos que han cambiado (cada evaluación cuenta). Si se agota la
     * condición de parada, los que quedan sin evaluar se quedan con coste infinito.
     */
    static void evaluar(Individuo[] poblacion, Distancias dist, ControlParada parada) {
        for (Individuo ind : poblacion) {
            if (ind.evaluado) continue;

            if (parada.agotada()) {
                ind.coste = Double.POSITIVE_INFINITY;
                continue;
            }
            ind.coste = dist.coste(ind.ruta);
            ind.evaluado = true;
            parada.evaluaciones++;
        }
    }

    // ------------------------------------------------------------------ Calcular élite Pt

    /** Copia de los E mejores individuos de la población. */
    static Individuo[] calcularElite(Individuo[] poblacion, int e) {
        Individuo[] ordenada = poblacion.clone();
        Arrays.sort(ordenada, (a, b) -> Double.compare(a.coste, b.coste));

        Individuo[] elite = new Individuo[e];
        for (int i = 0; i < e; i++) {
            elite[i] = ordenada[i].copia();
        }
        return elite;
    }

    // ------------------------------------------------------------------ Selección Pt en P't

    /** Torneo de tamaño kBest, tantas veces como individuos tiene la población. */
    static Individuo[] seleccionar(Individuo[] poblacion, int kBest, Random rnd) {
        Individuo[] seleccionados = new Individuo[poblacion.length];
        for (int i = 0; i < seleccionados.length; i++) {
            Individuo ganador = poblacion[rnd.nextInt(poblacion.length)];
            for (int t = 1; t < kBest; t++) {
                Individuo rival = poblacion[rnd.nextInt(poblacion.length)];
                if (rival.coste < ganador.coste) ganador = rival;
            }
            seleccionados[i] = ganador.copia();
        }
        return seleccionados;
    }

    // ------------------------------------------------------------------ Recombinar P't en Qt

    /** Los individuos seleccionados se emparejan en orden; cada pareja se cruza con probabilidad probCruce. */
    static Individuo[] recombinar(Individuo[] seleccionados, ParametrosGEN p, Random rnd) {
        Individuo[] qt = seleccionados.clone();

        for (int i = 0; i + 1 < qt.length; i += 2) {
            if (rnd.nextDouble() < p.probCruce) {
                int[][] hijos = Cruces.cruzar(p.cruce, qt[i].ruta, qt[i + 1].ruta, p, rnd);
                qt[i] = new Individuo(hijos[0]);
                qt[i + 1] = new Individuo(hijos[1]);
            }
        }
        return qt;
    }

    // ------------------------------------------------------------------ Mutación

    /** Con probabilidad probMutacion por individuo, intercambia dos posiciones distintas de la ruta. */
    static void mutar(Individuo[] qt, double probMutacion, Random rnd) {
        for (Individuo ind : qt) {
            if (rnd.nextDouble() < probMutacion) {
                int n = ind.ruta.length;
                int i = rnd.nextInt(n);
                int j = rnd.nextInt(n - 1);
                if (j >= i) j++;

                int aux = ind.ruta[i];
                ind.ruta[i] = ind.ruta[j];
                ind.ruta[j] = aux;
                ind.evaluado = false;
            }
        }
    }

    // ------------------------------------------------------------------ Reemplazar los Qt peores con élite Pt

    /**
     * La población nueva sustituye a la anterior. Para conservar el elitismo, cada individuo de la
     * élite que no esté ya en Qt sustituye al peor de un torneo de perdedores de tamaño kWorst
     * (sin tocar los que ya son élite en Qt). Devuelve cuántos individuos de la élite se reinsertaron.
     */
    static int reemplazar(Individuo[] qt, Individuo[] elite, int kWorst, Random rnd) {
        boolean[] protegido = new boolean[qt.length];

        // primero se marca la élite que ya sobrevive en Qt, así no se la puede sustituir después
        boolean[] sobrevive = new boolean[elite.length];
        for (int e = 0; e < elite.length; e++) {
            int pos = buscar(qt, elite[e]);
            if (pos >= 0) {
                sobrevive[e] = true;
                protegido[pos] = true;
            }
        }

        int reinsertados = 0;
        for (int e = 0; e < elite.length; e++) {
            if (sobrevive[e]) continue;

            int peor = torneoPerdedores(qt, protegido, kWorst, rnd);
            qt[peor] = elite[e].copia();
            protegido[peor] = true;
            reinsertados++;
        }
        return reinsertados;
    }

    private static int torneoPerdedores(Individuo[] qt, boolean[] protegido, int kWorst, Random rnd) {
        int peor = -1;
        for (int t = 0; t < kWorst; t++) {
            int candidato;
            do {
                candidato = rnd.nextInt(qt.length);
            } while (protegido[candidato]);

            if (peor == -1 || qt[candidato].coste > qt[peor].coste) peor = candidato;
        }
        return peor;
    }

    /** Posición en la población de un individuo con la misma ruta, o -1 si no está. */
    private static int buscar(Individuo[] poblacion, Individuo ind) {
        for (int i = 0; i < poblacion.length; i++) {
            if (poblacion[i].coste == ind.coste && Arrays.equals(poblacion[i].ruta, ind.ruta)) return i;
        }
        return -1;
    }

    // ------------------------------------------------------------------ Estadísticas de la población

    static Individuo mejor(Individuo[] poblacion) {
        Individuo m = poblacion[0];
        for (Individuo ind : poblacion) {
            if (ind.coste < m.coste) m = ind;
        }
        return m;
    }

    /** Peor coste entre los individuos evaluados. */
    static double peorCoste(Individuo[] poblacion) {
        double peor = Double.NEGATIVE_INFINITY;
        for (Individuo ind : poblacion) {
            if (ind.coste != Double.POSITIVE_INFINITY && ind.coste > peor) peor = ind.coste;
        }
        return peor;
    }

    /** Coste medio entre los individuos evaluados. */
    static double costeMedio(Individuo[] poblacion) {
        double suma = 0;
        int cuenta = 0;
        for (Individuo ind : poblacion) {
            if (ind.coste != Double.POSITIVE_INFINITY) {
                suma += ind.coste;
                cuenta++;
            }
        }
        return cuenta == 0 ? Double.POSITIVE_INFINITY : suma / cuenta;
    }
}
