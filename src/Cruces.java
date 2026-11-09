import java.util.Random;

/**
 * Operadores de cruce para permutaciones (siempre generan permutaciones válidas).
 * Ambos son "imponer el orden": se toma un conjunto de ciudades de un padre y se reordenan
 * en el otro padre siguiendo el orden que tienen en el primero.
 */
public class Cruces {

    /** Cruza dos padres con el operador elegido y devuelve los dos hijos. */
    static int[][] cruzar(TipoCruce tipo, int[] p1, int[] p2, ParametrosGEN p, Random rnd) {
        int n = p1.length;
        switch (tipo) {
            case MOC:
                return moc(p1, p2, 1 + rnd.nextInt(n - 1));
            case OX2:
            default:
                return ox2(p1, p2, mascaraAleatoria(n, p.probPosicionesOX2, rnd));
        }
    }

    /**
     * OX2: las posiciones marcadas en 'mascara' eligen ciudades de un padre; en el otro padre
     * esas ciudades se reordenan según el orden del primero. Hijo 1: base P2 con el orden de P1;
     * hijo 2: base P1 con el orden de P2 (mismas posiciones).
     */
    static int[][] ox2(int[] p1, int[] p2, boolean[] mascara) {
        return new int[][]{imponerOrden(p2, p1, mascara), imponerOrden(p1, p2, mascara)};
    }

    /**
     * MOC: igual que OX2, pero las posiciones elegidas son todas las de la derecha del punto de corte.
     * Hijo 1: base P1 con el orden del tramo derecho de P2; hijo 2: base P2 con el orden del tramo derecho de P1.
     */
    static int[][] moc(int[] p1, int[] p2, int corte) {
        boolean[] mascara = new boolean[p1.length];
        for (int i = corte; i < mascara.length; i++) {
            mascara[i] = true;
        }
        return new int[][]{imponerOrden(p1, p2, mascara), imponerOrden(p2, p1, mascara)};
    }

    /**
     * Copia 'base' y reordena en ella las ciudades que 'donante' tiene en las posiciones marcadas,
     * poniéndolas en el orden en que aparecen en 'donante'. Coste O(n).
     */
    static int[] imponerOrden(int[] base, int[] donante, boolean[] mascara) {
        int n = base.length;

        boolean[] elegida = new boolean[n];   // por ciudad
        for (int i = 0; i < n; i++) {
            if (mascara[i]) elegida[donante[i]] = true;
        }

        int[] hijo = base.clone();
        int sig = 0;
        for (int pos = 0; pos < n; pos++) {
            if (elegida[base[pos]]) {
                while (!mascara[sig]) sig++;
                hijo[pos] = donante[sig++];
            }
        }
        return hijo;
    }

    private static boolean[] mascaraAleatoria(int n, double probPosicion, Random rnd) {
        boolean[] m = new boolean[n];
        for (int i = 0; i < n; i++) {
            m[i] = rnd.nextDouble() < probPosicion;
        }
        return m;
    }
}
