import java.util.ArrayList;

/**
 * Aqui tenemos los Enums Globales de Tipos
 */

enum type{
    TSP
}

enum edge_weight_type{
    EUC_2D
}


/**
 * Clase para calcular la funcion euclidia
 */

class euclideo{
    double x;
    double y;
    int aplicarformula(){
        return (int) Math.round(Math.sqrt((x * x) + (y * y)));
    }
}

/**
 * Clase para almacenar las coordenadas
 */

class Coordenadas {
    int id;
    double x;
    double y;

    Coordenadas(int id, double x, double y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

}

class Tuple<A, B> {
    A first;
    B second;

    Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}

/**
 * Aquí tenemos la clase del Dato
 */

public class Dato {
    String NAME; // Es del problema
    type TYPE; // Tipo de problema (TSP)
    String COMMENT; // Descripcion
    int DIMENSION; // Entero con la dimension
    edge_weight_type EDGE_WEIGHT_TYPE; // Tipo de weight (euc_2D por ejemplo)
    ArrayList<Coordenadas> NODE_COORD_SECTION; // Aquí irian las coordenadas

    Dato(String NAME , type TYPE, int DIMENSION, String COMMENT, edge_weight_type EDGE_WEIGHT_TYPE, ArrayList<Coordenadas> NODE_COORD_SECTION){
        this.NAME = NAME;
        this.TYPE = TYPE;
        this.DIMENSION = DIMENSION;
        this.COMMENT = COMMENT;
        this.EDGE_WEIGHT_TYPE = EDGE_WEIGHT_TYPE;
        this.NODE_COORD_SECTION = NODE_COORD_SECTION;
    }
}