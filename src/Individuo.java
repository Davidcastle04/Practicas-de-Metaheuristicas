/**
 * Un individuo de la población: una permutación de ciudades (ruta cerrada) y su coste.
 */
public class Individuo {

    int[] ruta;
    double coste = Double.POSITIVE_INFINITY;
    boolean evaluado = false;   // false = el coste guardado no corresponde a la ruta actual

    Individuo(int[] ruta) {
        this.ruta = ruta;
    }

    Individuo copia() {
        Individuo c = new Individuo(ruta.clone());
        c.coste = coste;
        c.evaluado = evaluado;
        return c;
    }
}

/**
 * Condición de parada del evolutivo: máximo de evaluaciones o máximo de segundos (lo que ocurra antes).
 */
class ControlParada {
    final long maxEvaluaciones;
    final double maxSegundos;
    long evaluaciones = 0;
    private final long inicio = System.nanoTime();

    ControlParada(long maxEvaluaciones, double maxSegundos) {
        this.maxEvaluaciones = maxEvaluaciones;
        this.maxSegundos = maxSegundos;
    }

    boolean agotada() {
        return evaluaciones >= maxEvaluaciones || segundos() >= maxSegundos;
    }

    double segundos() {
        return (System.nanoTime() - inicio) / 1e9;
    }
}
