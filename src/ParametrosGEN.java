/**
 * Tipos de cruce disponibles en el evolutivo.
 */
enum TipoCruce {
    OX2, MOC
}

/**
 * Todos los parámetros del algoritmo evolutivo generacional (los valores por defecto son los del guion).
 * Los lee Configuracion desde el .conf.
 */
public class ParametrosGEN {

    TipoCruce cruce = TipoCruce.OX2;

    int tamPoblacion = 100;          // M
    double porcAleatorios = 0.8;     // % de la población inicial totalmente aleatoria (el resto, greedy aleatorizado)
    int kGreedy = 10;                // lista de candidatos del greedy aleatorizado inicial

    int elite = 1;                   // E
    int kBest = 2;                   // tamaño del torneo de selección
    int kWorst = 2;                  // tamaño del torneo de perdedores (reemplazo de la élite)

    double probCruce = 0.7;
    double probMutacion = 0.1;       // por individuo
    double probPosicionesOX2 = 0.5;  // probabilidad de que cada posición se elija en OX2

    long maxEvaluaciones = 50000;
    double maxSegundos = 120;

    /** Copia de estos parámetros cambiando solo lo que distingue cada versión del guion. */
    ParametrosGEN variante(TipoCruce cruce, int elite, int kBest, int kWorst) {
        ParametrosGEN p = new ParametrosGEN();
        p.cruce = cruce;
        p.elite = elite;
        p.kBest = kBest;
        p.kWorst = kWorst;
        p.tamPoblacion = tamPoblacion;
        p.porcAleatorios = porcAleatorios;
        p.kGreedy = kGreedy;
        p.probCruce = probCruce;
        p.probMutacion = probMutacion;
        p.probPosicionesOX2 = probPosicionesOX2;
        p.maxEvaluaciones = maxEvaluaciones;
        p.maxSegundos = maxSegundos;
        return p;
    }

    String nombre() {
        return "GEN_" + cruce + "_E" + elite + "_kB" + kBest + "_kW" + kWorst;
    }

    void validar(int n) {
        if (n < 4) throw new IllegalArgumentException("El problema necesita al menos 4 ciudades.");
        if (tamPoblacion < 2) throw new IllegalArgumentException("La poblacion necesita al menos 2 individuos.");
        if (elite < 0 || elite >= tamPoblacion) throw new IllegalArgumentException("Elite fuera de rango: " + elite);
        if (kBest < 1 || kWorst < 1) throw new IllegalArgumentException("kBest y kWorst tienen que ser al menos 1.");
        if (kGreedy < 1) throw new IllegalArgumentException("k del greedy aleatorizado tiene que ser al menos 1.");
        if (!prob(porcAleatorios) || !prob(probCruce) || !prob(probMutacion) || !prob(probPosicionesOX2))
            throw new IllegalArgumentException("Las probabilidades/porcentajes tienen que estar entre 0 y 1.");
        if (maxEvaluaciones < 1 || maxSegundos <= 0) throw new IllegalArgumentException("Condicion de parada no valida.");
    }

    private static boolean prob(double v) {
        return v >= 0.0 && v <= 1.0;
    }

    void registrar(Logger log) {
        log.parametro("Cruce", cruce);
        log.parametro("Tamano poblacion (M)", tamPoblacion);
        log.parametro("% inicial aleatorio", porcAleatorios + " (el resto greedy aleatorizado, k=" + kGreedy + ")");
        log.parametro("Elite (E)", elite);
        log.parametro("kBest (torneo seleccion)", kBest);
        log.parametro("kWorst (torneo de perdedores)", kWorst);
        log.parametro("Probabilidad de cruce", probCruce);
        log.parametro("Probabilidad de mutacion (por individuo)", probMutacion);
        if (cruce == TipoCruce.OX2) log.parametro("Probabilidad de elegir cada posicion en OX2", probPosicionesOX2);
        log.parametro("Maximo de evaluaciones", maxEvaluaciones);
        log.parametro("Maximo de segundos", maxSegundos);
    }
}
