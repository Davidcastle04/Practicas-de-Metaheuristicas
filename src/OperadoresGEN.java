import java.util.Arrays;
import java.util.Random;

// Bloques del esquema generacional de la práctica 2: inicializar, evaluar, élite,
// selección, recombinación, mutación y reemplazo.
public class OperadoresGEN {

    // Generamos la población inicial: una parte totalmente aleatoria y el resto con el
    // greedy aleatorizado de la práctica 1.
    static Individuo[] inicializarPoblacion(Dato dato, int tamPoblacion, double porcAleatorios, int kGreedy, Random rnd) {
        int n = dato.DIMENSION;
        int numAleatorios = (int) Math.round(porcAleatorios * tamPoblacion);

        Individuo[] poblacion = new Individuo[tamPoblacion];
        for (int i = 0; i < tamPoblacion; i++) {
            if (i < numAleatorios) {
                poblacion[i] = new Individuo(permutacionAleatoria(n, rnd));
            } else {
                poblacion[i] = new Individuo(Distancias.aArray(
                        AlgGRA_Clase05_Grupo03.generarSolucion(dato, kGreedy, rnd)));
            }
        }
        return poblacion;
    }

    // Permutación aleatoria con el algoritmo de Fisher-Yates.
    private static int[] permutacionAleatoria(int n, Random rnd) {
        int[] r = new int[n];
        for (int i = 0; i < n; i++) {
            r[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int aux = r[i];
            r[i] = r[j];
            r[j] = aux;
        }
        return r;
    }

    // Evaluamos solo los individuos que han cambiado, y cada evaluación cuenta para la parada.
    // Si se agota la parada, los que quedan sin evaluar se quedan con coste infinito.
    static void evaluar(Individuo[] poblacion, Distancias dist, ControlParada parada) {
        for (int i = 0; i < poblacion.length; i++) {
            Individuo ind = poblacion[i];
            if (ind.evaluado) {
                continue;
            }
            if (parada.agotada()) {
                ind.coste = Double.POSITIVE_INFINITY;
                continue;
            }
            ind.coste = dist.coste(ind.ruta);
            ind.evaluado = true;
            parada.evaluaciones++;
        }
    }

    // Copiamos los e mejores individuos de la población.
    static Individuo[] calcularElite(Individuo[] poblacion, int e) {
        Individuo[] ordenada = poblacion.clone();
        Arrays.sort(ordenada, (a, b) -> Double.compare(a.coste, b.coste));

        Individuo[] elite = new Individuo[e];
        for (int i = 0; i < e; i++) {
            elite[i] = ordenada[i].copia();
        }
        return elite;
    }

    // Selección por torneo de tamaño kBest, tantas veces como individuos hay en la población.
    static Individuo[] seleccionar(Individuo[] poblacion, int kBest, Random rnd) {
        Individuo[] seleccionados = new Individuo[poblacion.length];
        for (int i = 0; i < seleccionados.length; i++) {
            Individuo ganador = poblacion[rnd.nextInt(poblacion.length)];
            for (int t = 1; t < kBest; t++) {
                Individuo rival = poblacion[rnd.nextInt(poblacion.length)];
                if (rival.coste < ganador.coste) {
                    ganador = rival;
                }
            }
            seleccionados[i] = ganador.copia();
        }
        return seleccionados;
    }

    // Emparejamos los seleccionados de dos en dos y cruzamos cada pareja con probabilidad probCruce.
    static Individuo[] recombinar(Individuo[] seleccionados, TipoCruce cruce, double probCruce,
                                  double probPosOX2, Random rnd) {
        Individuo[] qt = seleccionados.clone();

        for (int i = 0; i + 1 < qt.length; i += 2) {
            if (rnd.nextDouble() < probCruce) {
                int[][] hijos = Cruces.cruzar(cruce, qt[i].ruta, qt[i + 1].ruta, probPosOX2, rnd);
                qt[i] = new Individuo(hijos[0]);
                qt[i + 1] = new Individuo(hijos[1]);
            }
        }
        return qt;
    }

    // Con probabilidad probMutacion por individuo, intercambiamos dos posiciones distintas de la ruta.
    static void mutar(Individuo[] qt, double probMutacion, Random rnd) {
        for (int k = 0; k < qt.length; k++) {
            Individuo ind = qt[k];
            if (rnd.nextDouble() < probMutacion) {
                int n = ind.ruta.length;
                int i = rnd.nextInt(n);
                int j = rnd.nextInt(n - 1);
                if (j >= i) {
                    j++;
                }

                int aux = ind.ruta[i];
                ind.ruta[i] = ind.ruta[j];
                ind.ruta[j] = aux;
                ind.evaluado = false;
            }
        }
    }

    // La población nueva sustituye a la anterior. Para conservar el elitismo, cada individuo de la
    // élite que no sobreviva en Qt sustituye al peor de un torneo de perdedores de tamaño kWorst.
    // Devolvemos cuántos individuos de la élite hemos tenido que reinsertar.
    static int reemplazar(Individuo[] qt, Individuo[] elite, int kWorst, Random rnd) {
        boolean[] protegido = new boolean[qt.length];

        // Primero marcamos la élite que ya está en Qt para no sustituirla después.
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
            if (sobrevive[e]) {
                continue;
            }
            int peor = torneoPerdedores(qt, protegido, kWorst, rnd);
            qt[peor] = elite[e].copia();
            protegido[peor] = true;
            reinsertados++;
        }
        return reinsertados;
    }

    // Elegimos kWorst individuos al azar (sin tocar los protegidos) y nos quedamos con el peor.
    private static int torneoPerdedores(Individuo[] qt, boolean[] protegido, int kWorst, Random rnd) {
        int peor = -1;
        for (int t = 0; t < kWorst; t++) {
            int candidato;
            do {
                candidato = rnd.nextInt(qt.length);
            } while (protegido[candidato]);

            if (peor == -1 || qt[candidato].coste > qt[peor].coste) {
                peor = candidato;
            }
        }
        return peor;
    }

    // Posición de un individuo con la misma ruta en la población, o -1 si no está.
    private static int buscar(Individuo[] poblacion, Individuo ind) {
        for (int i = 0; i < poblacion.length; i++) {
            if (poblacion[i].coste == ind.coste && Arrays.equals(poblacion[i].ruta, ind.ruta)) {
                return i;
            }
        }
        return -1;
    }

    static Individuo mejor(Individuo[] poblacion) {
        Individuo m = poblacion[0];
        for (int i = 1; i < poblacion.length; i++) {
            if (poblacion[i].coste < m.coste) {
                m = poblacion[i];
            }
        }
        return m;
    }

    // Peor coste entre los individuos ya evaluados.
    static double peorCoste(Individuo[] poblacion) {
        double peor = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < poblacion.length; i++) {
            if (poblacion[i].coste != Double.POSITIVE_INFINITY && poblacion[i].coste > peor) {
                peor = poblacion[i].coste;
            }
        }
        return peor;
    }

    // Coste medio entre los individuos ya evaluados.
    static double costeMedio(Individuo[] poblacion) {
        double suma = 0;
        int cuenta = 0;
        for (int i = 0; i < poblacion.length; i++) {
            if (poblacion[i].coste != Double.POSITIVE_INFINITY) {
                suma += poblacion[i].coste;
                cuenta++;
            }
        }
        return cuenta == 0 ? Double.POSITIVE_INFINITY : suma / cuenta;
    }
}
