// Un individuo de la población: una ruta cerrada (permutación de ciudades) y su coste.
public class Individuo {

    int[] ruta;
    double coste = Double.POSITIVE_INFINITY;
    boolean evaluado = false;   // false si el coste guardado no corresponde a la ruta actual

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
