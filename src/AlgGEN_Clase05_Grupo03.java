import java.util.ArrayList;
import java.util.Random;

/**
 * Algoritmo evolutivo generacional (esquema de la práctica 2):
 * Inicializar Pt -> Evaluar Pt -> [ Calcular élite -> Selección -> Recombinar -> Mutar ->
 * Evaluar Qt -> Reemplazar élite -> Pt+1 = Qt ] hasta agotar evaluaciones o tiempo.
 */
public class AlgGEN_Clase05_Grupo03 {

    public static ArrayList<Tuple<Integer, Integer>> Generacional(Dato dato, ParametrosGEN p, Long semilla, String dataset) {
        p.validar(dato.DIMENSION);

        Random rnd = (semilla == null) ? new Random() : new Random(semilla);
        Distancias dist = Distancias.de(dato);
        ControlParada parada = new ControlParada(p.maxEvaluaciones, p.maxSegundos);

        Logger log = new Logger(p.nombre(), dataset, semilla);
        p.registrar(log);

        //poblacion inicial

        Individuo[] pt = OperadoresGEN.inicializarPoblacion(dato, p, rnd);
        OperadoresGEN.evaluar(pt, dist, parada);
        Individuo mejorGlobal = OperadoresGEN.mejor(pt).copia();
        log.solucionInicial(mejorGlobal.ruta, mejorGlobal.coste);

        int generacion = 0;
        while (!parada.agotada()) {
            // elites
            Individuo[] elite = OperadoresGEN.calcularElite(pt, p.elite);
            // torneo
            Individuo[] seleccionados = OperadoresGEN.seleccionar(pt, p.kBest, rnd);
            Individuo[] qt = OperadoresGEN.recombinar(seleccionados, p, rnd);
            OperadoresGEN.mutar(qt, p.probMutacion, rnd);
            OperadoresGEN.evaluar(qt, dist, parada);
            int reinsertados = OperadoresGEN.reemplazar(qt, elite, p.kWorst, rnd);

            pt = qt;
            generacion++;

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
