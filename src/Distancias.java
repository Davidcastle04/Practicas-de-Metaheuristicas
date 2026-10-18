import java.util.ArrayList;

/**
 * Distancias euclídeas (EUC_2D redondeadas) del problema y utilidades comunes
 * a los cuatro algoritmos.
 */
public class Distancias {

    // con más ciudades que esto la matriz no cabe en memoria (d18512 serían 1,3 GB)
    private static final int LIMITE_MATRIZ = 5000;

    private final int n;
    private final double[] x;
    private final double[] y;
    private final int[][] matriz;   // null si el problema es grande
    private int[] sumas;            // suma de distancias de cada ciudad al resto

    // guardamos la última instancia para no recalcular en cada semilla
    private static Dato ultimoDato;
    private static Distancias ultimaDistancia;

    private Distancias(Dato dato) {
        n = dato.DIMENSION;
        x = new double[n];
        y = new double[n];
        for (int i = 0; i < n; i++) {
            Coordenadas c = dato.NODE_COORD_SECTION.get(i);
            x[i] = c.x;
            y[i] = c.y;
        }

        if (n <= LIMITE_MATRIZ) {
            matriz = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    int d = calcular(i, j);
                    matriz[i][j] = d;
                    matriz[j][i] = d;
                }
            }
        } else {
            matriz = null;
        }
    }

    public static Distancias de(Dato dato) {
        if (dato != ultimoDato) {
            ultimaDistancia = new Distancias(dato);
            ultimoDato = dato;
        }
        return ultimaDistancia;
    }

    private int calcular(int a, int b) {
        double dx = x[a] - x[b];
        double dy = y[a] - y[b];
        return (int) Math.round(Math.sqrt(dx * dx + dy * dy));
    }

    public int d(int a, int b) {
        if (matriz != null) return matriz[a][b];
        return calcular(a, b);
    }

    public int tamano() {
        return n;
    }

    public int[] sumas() {
        if (sumas == null) {
            sumas = new int[n];
            for (int i = 0; i < n; i++) {
                int s = 0;
                for (int j = 0; j < n; j++) {
                    s += d(i, j);
                }
                sumas[i] = s;
            }
        }
        return sumas;
    }

    /** Coste de la ruta cerrada (la última ciudad vuelve a la primera). */
    public double coste(int[] ruta) {
        double total = 0;
        for (int i = 0; i < ruta.length; i++) {
            total += d(ruta[i], ruta[(i + 1) % ruta.length]);
        }
        return total;
    }

    /** Invierte el tramo [i, j] de la ruta (movimiento 2-opt). */
    public static void invertir(int[] ruta, int i, int j) {
        while (i < j) {
            int aux = ruta[i];
            ruta[i] = ruta[j];
            ruta[j] = aux;
            i++;
            j--;
        }
    }

    public static int[] aArray(ArrayList<Tuple<Integer, Integer>> ruta) {
        int[] r = new int[ruta.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = ruta.get(i).first;
        }
        return r;
    }

    public ArrayList<Tuple<Integer, Integer>> aTuplas(int[] ruta) {
        int[] s = sumas();
        ArrayList<Tuple<Integer, Integer>> res = new ArrayList<>(ruta.length);
        for (int c : ruta) {
            res.add(new Tuple<>(c, s[c]));
        }
        return res;
    }
}
