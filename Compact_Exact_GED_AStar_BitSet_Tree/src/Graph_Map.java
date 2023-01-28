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

    public static int nb_edges_g1;
    public static int nb_edges_g2;

    public static ArrayList<Edge> list_edges_g1;
    public static ArrayList<Edge> list_edges_g2;

    private static Map<Edge,Integer> map_list_edges_g1; // a hash map in order to find the index (its position in the list_edges) associated with each edge.
    private static Map<Edge,Integer> map_list_edges_g2;

    // all tha variables : g1, g2, nb_vertices_g1_from, nb_vertices_g2_to, list_vertices_g1
    // are not related to change at each intermediate node, so we make theme static,
    // like this they will not be in the object node.



    private Graph_Map() {
    }


    public static void initialize_Graph_Map(Graph g1, Graph g2)
    {
        Graph_Map.g1 = g1;
        Graph_Map.g2 = g2;

        Graph_Map.nb_vertices_g1 = g1.vertexSet().size();// get the number of vertices in g1
        Graph_Map.nb_vertices_g2 = g2.vertexSet().size();// get the number of vertices in g2

        Graph_Map.list_vertices_g1 = new ArrayList<Vertex>(g1.vertexSet());
        Graph_Map.list_vertices_g2 = new ArrayList<Vertex>(g2.vertexSet());

        Graph_Map.nb_edges_g1 = g1.edgeSet().size();
        Graph_Map.nb_edges_g2 = g2.edgeSet().size();

        Graph_Map.list_edges_g1 = new ArrayList<Edge>(g1.edgeSet());
        Graph_Map.list_edges_g2 = new ArrayList<Edge>(g2.edgeSet());

        Graph_Map.map_list_edges_g1 = new HashMap<>(Graph_Map.nb_edges_g1);
        for (int i = 0; i< Graph_Map.nb_edges_g1; i++)
        {
            Graph_Map.map_list_edges_g1.put(list_edges_g1.get(i),i);
        }

        Graph_Map.map_list_edges_g2 = new HashMap<>(Graph_Map.nb_edges_g2);
        for (int i = 0; i< Graph_Map.nb_edges_g2; i++)
        {
            Graph_Map.map_list_edges_g2.put(list_edges_g2.get(i),i);
        }
    }

    //  cost_matrix :
    //
    //  n : the number of all vertices of g1
    //  m : the number of all vertices of g2
    //            m      1
    //      +-----------+--+
    //      |           |  |
    //  n   |    Sub    |  |
    //      |           |  |
    //      +-----------+--+
    //  1   |              |
    //      +-----------+--+
    //
    // the upper left part is for substitution
    // the upper right (one column) is for the deletion of vertices from g1
    // the lower left part ( one row) is for the insertion of vertices from g2
    //
    // its clear, that the deletion column is in the (m+1) position, and the insertion row is at (n+1) position.
    // But, because, we begin from 0, so we get m and n for the deletion and insertion position.

    private void generate_Map()
    {
    }


    /**
     *
     * @param i : idx_from, the idx of vertex in g1
     * @param j : idx_to, the idx of vertex in g2
     * @return the idx in the matrix of edit operations.
     */

    public static int get_idx_in_map(int i, int j)
    {
        // Insertion: (-1,j)
        if(i==-1)
            return (Graph_Map.nb_vertices_g1)*(nb_vertices_g2+1)+j;

        // deletion (i,-1)
        if(j==-1)
            return (i)*(nb_vertices_g2+1)+ Graph_Map.nb_vertices_g2;

        // Substitution
        return (i)*(nb_vertices_g2+1)+j;
    }

    // The problem with get_idx_in_map, is we call it a lot in the search tree,
    // so checking the i and j values each time is not a good idea.
    // to avoid to check the i and j values, we split the previous method into three, each one without testing
    // and during the search tree, we know when we call each method without testing :)

    // Substitution
    public static int get_idx_in_map_sub(int i, int j)
    {
        return (i)*(nb_vertices_g2+1)+j;
    }

    // deletion (i,-1)
    public static int get_idx_in_map_del(int i)
    {
        return (i)*(nb_vertices_g2+1)+ Graph_Map.nb_vertices_g2;
    }

    // Insertion: (-1,j)
    public static int get_idx_in_map_insert(int j)
    {
        return (Graph_Map.nb_vertices_g1)*(nb_vertices_g2+1)+j;
    }

    public static int get_idx_in_g1(int idx)
    {
        return idx/(nb_vertices_g2+1); // integer division in java give the low integer value :it's like this: (int) Math.floor(idx/(m+1));
    }

    public static int get_idx_in_g2(int idx)
    {
        return idx % (nb_vertices_g2+1);
    }

    public static void test_idx_i_j_map(int n, int m)
    {
        for(int i=0;i<(n+1);i++)
        // or : for(int i=0;i<=(nb_vertices_g1);i++)
        {
            for(int j=0;j<(m+1);j++)
            // or : for(int j=0;j<=(m);j++)
            {
                int idx=get_idx_in_map(i,j);

                System.out.println(" i="+i+" , j ="+j+" ==> idx = "+idx);
            }
        }

        System.out.println(" --------------- ");

        for(int k=0;k<=get_idx_in_map(n,m);k++)
        {
            int i=get_idx_in_g1(k);
            int j=get_idx_in_g2(k);

            System.out.println(" i="+i+" , j ="+j+" ==> idx = "+k);
        }

    }

    public static void test_idx_i_j_map()
    {
        for(int i = 0; i<(nb_vertices_g1 +1); i++)
        // or : for(int i=0;i<=(nb_vertices_g1);i++)
        {
            for(int j=0;j<(nb_vertices_g2+1);j++)
            // or : for(int j=0;j<=(m);j++)
            {
                int idx=get_idx_in_map(i,j);

                System.out.println(" i="+i+" , j ="+j+" ==> idx = "+idx);
            }
        }

        System.out.println(" --------------- ");

        for(int k = 0; k<=get_idx_in_map(nb_vertices_g1,nb_vertices_g2); k++)
        {
            int i=get_idx_in_g1(k);
            int j=get_idx_in_g2(k);

            System.out.println(" i="+i+" , j ="+j+" ==> idx = "+k);
        }

    }


    /**
     * Construct an edit operation based on an index from the matrix of edit operation.
     * for that:
     *  1) we have to find the indices i and j in the Matrix.
     *  2) retrieve in which region in the Matrix these indices belong to determine the type of the edit operation.
     *
     * @param idx the index (value) of the edit operation in matrix of edit operation.
     * @return  an object edit operation that involve two vertices.
     */
    public static Edit_Operation get_Edit_Operation(int idx)
    {
        int i = get_idx_in_g1(idx); // index of row
        int j = get_idx_in_g2(idx); // index of column

        Edit_Operation edit_operation = null;

        if(i< nb_vertices_g1 && j<nb_vertices_g2) // substitution
        {
            edit_operation = new Edit_Operation(i, j);
        }

        if(j==nb_vertices_g2 && i< nb_vertices_g1)// deletion
        {
            edit_operation = new Edit_Operation(i, -1);
        }

        if(i== nb_vertices_g1 && j<nb_vertices_g2)// insertion
        {
            edit_operation = new Edit_Operation(-1, j);
        }

        return edit_operation;
    }



    public static Vertex get_vertex_from_list_g1(int idx)
    {
        return list_vertices_g1.get(idx);
    }

    public static Vertex get_vertex_from_list_g2(int idx)
    {
        return list_vertices_g2.get(idx);
    }

    public static Edge get_edge_from_list_g1(int idx)
    {
        return list_edges_g1.get(idx);
    }

    public static Edge get_edge_from_list_g2(int idx)
    {
        return list_edges_g2.get(idx);
    }

    public static int get_idx_from_list_edges_g1(Edge e)
    {
        //return list_edges_g1.indexOf(e);
        return map_list_edges_g1.get(e);
    }

    public static int get_idx_from_list_edges_g2(Edge e)
    {
        //return list_edges_g2.indexOf(e);
        return map_list_edges_g2.get(e);
    }


    // just temporary for debugging and testing
    public static int get_idx_from_list_vertices_g1(Vertex vertex)
    {
        return Graph_Map.list_vertices_g1.indexOf(vertex);
    }

    public static int get_idx_from_list_vertices_g2(Vertex vertex)
    {
        return Graph_Map.list_vertices_g2.indexOf(vertex);
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
