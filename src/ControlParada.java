// Controla la condición de parada del evolutivo: máximo de evaluaciones o máximo de segundos.
class ControlParada {
    final long maxEvaluaciones;
    final double maxSegundos;
    long evaluaciones = 0;
    private final long inicio = System.nanoTime();

    ControlParada(long maxEvaluaciones, double maxSegundos) {
        this.maxEvaluaciones = maxEvaluaciones;
        this.maxSegundos = maxSegundos;
    }

    // Paramos cuando se cumple cualquiera de las dos condiciones.
    boolean agotada() {
        return evaluaciones >= maxEvaluaciones || segundos() >= maxSegundos;
    }

    double segundos() {
        return (System.nanoTime() - inicio) / 1e9;
    }
}
