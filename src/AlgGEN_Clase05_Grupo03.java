import java.util.ArrayList;
import java.util.Random;

// Algoritmo evolutivo generacional (esquema de la práctica 2):
// inicializar y evaluar Pt, y mientras no se agote la parada: calcular élite, seleccionar,
// recombinar, mutar, evaluar Qt, reemplazar con la élite y quedarnos con Qt como nueva población.
// Todos los parámetros vienen del fichero.conf a través de Configuracion.
public class AlgGEN_Clase05_Grupo03 {

    public static String nombreVersion(TipoCruce cruce, int elite, int kBest, int kWorst) {
        return "GEN_" + cruce + "_E" + elite + "_kB" + kBest + "_kW" + kWorst;
    }

    public static ArrayList<Tuple<Integer, Integer>> Generacional(Dato dato, Configuracion conf, TipoCruce cruce,
                                                                  int elite, int kBest, int kWorst,
                                                                  Long semilla, String dataset) {
        int n = dato.DIMENSION;
        if (n < 4) {
            throw new IllegalArgumentException("El problema necesita al menos 4 ciudades.");
        }
        if (elite >= conf.tamPoblacionGEN) {
            throw new IllegalArgumentException("La élite no puede ser mayor que la población: " + elite);
        }

        Random rnd = (semilla == null) ? new Random() : new Random(semilla);
        Distancias dist = Distancias.de(dato);
        ControlParada parada = new ControlParada(conf.evaluacionesGEN, conf.tiempoMaxGEN);

        // Guardamos los parámetros de esta versión en el log.
        Logger log = new Logger(nombreVersion(cruce, elite, kBest, kWorst), dataset, semilla);
        log.parametro("Cruce", cruce);
        log.parametro("Tamano poblacion (M)", conf.tamPoblacionGEN);
        log.parametro("% inicial aleatorio", conf.porcAleatoriosGEN + " (el resto greedy aleatorizado, k=" + conf.kGreedyGEN + ")");
        log.parametro("Elite (E)", elite);
        log.parametro("kBest (torneo de seleccion)", kBest);
        log.parametro("kWorst (torneo de perdedores)", kWorst);
        log.parametro("Probabilidad de cruce", conf.probCruceGEN);
        log.parametro("Probabilidad de mutacion (por individuo)", conf.probMutacionGEN);
        if (cruce == TipoCruce.OX2) {
            log.parametro("Probabilidad de elegir cada posicion en OX2", conf.probPosOX2GEN);
        }
        log.parametro("Maximo de evaluaciones", conf.evaluacionesGEN);
        log.parametro("Maximo de segundos", conf.tiempoMaxGEN);

        // Población inicial.
        Individuo[] pt = OperadoresGEN.inicializarPoblacion(dato, conf.tamPoblacionGEN,
                conf.porcAleatoriosGEN, conf.kGreedyGEN, rnd);
        OperadoresGEN.evaluar(pt, dist, parada);
        Individuo mejorGlobal = OperadoresGEN.mejor(pt).copia();
        log.solucionInicial(mejorGlobal.ruta, mejorGlobal.coste);

        int generacion = 0;
        while (!parada.agotada()) {
            // Guardamos la élite de la generación actual.
            Individuo[] eliteActual = OperadoresGEN.calcularElite(pt, elite);
            // Selección por torneo, cruce y mutación.
            Individuo[] seleccionados = OperadoresGEN.seleccionar(pt, kBest, rnd);
            // Hacemos la recombinacion con los datos del torneo cruce o mutacion
            Individuo[] qt = OperadoresGEN.recombinar(seleccionados, cruce, conf.probCruceGEN, conf.probPosOX2GEN, rnd);
            // Se realiza la Mutacion
            OperadoresGEN.mutar(qt, conf.probMutacionGEN, rnd);
            // Evaluamos
            OperadoresGEN.evaluar(qt, dist, parada);

            // Si la élite no ha sobrevivido, la reinsertamos en lugar de los peores.
            int reinsertados = OperadoresGEN.reemplazar(qt, eliteActual, kWorst, rnd);

            pt = qt;
            generacion++;

            // Comprobamos si hemos mejorado la mejor solución encontrada hasta ahora.
            Individuo mejor = OperadoresGEN.mejor(pt);
            String nota = null;
            if (mejor.coste < mejorGlobal.coste) {
                mejorGlobal = mejor.copia();
                nota = "NUEVO MEJOR GLOBAL";
            }
            log.generacion(generacion, parada.evaluaciones, mejor.coste, OperadoresGEN.costeMedio(pt),
                    OperadoresGEN.peorCoste(pt), reinsertados, nota, mejor.ruta);
        }

        log.linea("Generaciones: " + generacion + " | Evaluaciones: " + parada.evaluaciones);
        log.finalizar(mejorGlobal.ruta, mejorGlobal.coste);

        return dist.aTuplas(mejorGlobal.ruta);
    }
}
