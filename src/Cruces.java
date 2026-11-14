import java.util.Random;

// Operadores de cruce OX2 y MOC. Los dos funcionan igual: elegimos unas posiciones de un padre
// y reordenamos esas ciudades en el otro padre siguiendo el orden del primero.
public class Cruces {

    // Cruzamos dos padres con el operador elegido y devolvemos los dos hijos.
    static int[][] cruzar(TipoCruce tipo, int[] p1, int[] p2, double probPosOX2, Random rnd) {
        int n = p1.length;
        if (tipo == TipoCruce.MOC) {
            return moc(p1, p2, 1 + rnd.nextInt(n - 1));
        }
        return ox2(p1, p2, mascaraAleatoria(n, probPosOX2, rnd));
    }

    // OX2: las posiciones marcadas en la máscara eligen ciudades de un padre, y en el otro padre
    // esas ciudades se reordenan según el orden del primero.
    static int[][] ox2(int[] p1, int[] p2, boolean[] mascara) {
        return new int[][]{imponerOrden(p2, p1, mascara), imponerOrden(p1, p2, mascara)};
    }

    // MOC: igual que OX2, pero las posiciones elegidas son todas las que quedan a la derecha del punto de corte.
    static int[][] moc(int[] p1, int[] p2, int corte) {
        boolean[] mascara = new boolean[p1.length];
        for (int i = corte; i < mascara.length; i++) {
            mascara[i] = true;
        }
        return new int[][]{imponerOrden(p1, p2, mascara), imponerOrden(p2, p1, mascara)};
    }

    // Copiamos la base y colocamos en los huecos las ciudades que el donante tiene en las
    // posiciones marcadas, en el mismo orden en el que aparecen en el donante.
    static int[] imponerOrden(int[] base, int[] donante, boolean[] mascara) {
        int n = base.length;

        // Marcamos qué ciudades hay que recolocar.
        boolean[] elegida = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (mascara[i]) {
                elegida[donante[i]] = true;
            }
        }

        int[] hijo = base.clone();
        int sig = 0;
        for (int pos = 0; pos < n; pos++) {
            if (elegida[base[pos]]) {
                while (!mascara[sig]) {
                    sig++;
                }
                hijo[pos] = donante[sig];
                sig++;
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
