import graph_element.Edge;
import graph_element.Vertex;
import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 26/10/2017.
 */
public final class Graph_Map {

    @SuppressWarnings("rawtypes")
    public static Graph g1, g2; // References to processed graphs g1 and g2

    public static int nb_vertices_g1; // number of row elements or number of element of the first set
    public static int nb_vertices_g2; // number of column elements or number of element of the seconds set

    public static ArrayList<Vertex> list_vertices_g1;
    public static ArrayList<Vertex> list_vertices_g2;

    public static int nb_edges_g1; // number of egdes in g1
    public static int nb_edges_g2; // number of egdes in g2

    public static ArrayList<Edge> list_edges_g1;
    public static ArrayList<Edge> list_edges_g2;


    private Graph_Map() {
    }


    public static void initialize_Graph_Map(Graph g1, Graph g2)
    {
        Graph_Map.g1 = g1;
        Graph_Map.g2 = g2;

        Graph_Map.nb_vertices_g1 = g1.vertexSet().size();// get the nomber of vertices in g1
        Graph_Map.nb_vertices_g2 = g2.vertexSet().size();// get the nomber of vertices in g2

        Graph_Map.list_vertices_g1 = new ArrayList<Vertex>(g1.vertexSet());
        Graph_Map.list_vertices_g2 = new ArrayList<Vertex>(g2.vertexSet());

        Graph_Map.nb_edges_g1 = g1.edgeSet().size();
        Graph_Map.nb_edges_g2 = g2.edgeSet().size();

        Graph_Map.list_edges_g1 = new ArrayList<Edge>(g1.edgeSet());
        Graph_Map.list_edges_g2 = new ArrayList<Edge>(g2.edgeSet());
    }

    public static String to_String()
    {
        String str = " nb_vertices_g1= "+ nb_vertices_g1 + " , m="+nb_vertices_g2 + "\n";

        if(list_vertices_g1 != null) str += " listV1 ="+list_vertices_g1.toString() + "\n";
        else str +=" listV1 =NULL" + "\n";

        if(list_vertices_g2 != null) str += " listV2 ="+list_vertices_g2.toString() + "\n";
        else str +=" listV2 =NULL" + "\n";

        return str;
    }
}
