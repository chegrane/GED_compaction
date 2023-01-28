import graph_element.Edge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public abstract class Path_Node {

    protected int index_processed_vertices_g1; // The last index of the processed vertices in the first graph g1
                                            // this variable is related to the node at the level "i"
                                            // when we chose a node to process, we can get "index_processed_vertices_g1"
                                            // chose node at level 5, and after we can chose another node at the level 2 because it have a better g+h value.


    protected BitSet remaining_unprocessed_vertex_g2; // The remaining set of unprocessed vertex in g2

    // in the process of resolving the Graph Edit Distance, in the search tree, at each level,
    // we take one vertex from g1, and we do all combination of vertices of g1 (and also one deletion)
    // so we will hav different path children outgoing from one node (in the search tree),
    // each path has an operation v from g1 and w_i from g2 (i: each time different vertex of g2).
    // so: when we deal with the set of g1, at each level of the search tree we choose one vertex, level i, we choose vertex i.
    //     It is therefore sufficient to use a single list and counter for all the process of search tree.
    // but in the second graph g2, at each time we choose a different vertex (each outgoing path has a different vertex from g2)
    // so we need to at each node to know which vertices that are not yet processed (The remaining set of unprocessed of vertices of g2 )



    protected BitSet unprocessed_edges_g1; // The set of unprocessed edges in g1
    protected BitSet unprocessed_edges_g2; // The set of unprocessed edges in g2

    // we need these two variables, in orders to compute the h value at each intermediate node,
    // because each node in the tree have a different set of an unprocessed edges.
    // which is related to the processed or unprocessed vertices.
    // we can not store these variables, and instead, each time we compute the implied edges and then the unprocessed edges
    // but i think that will take a lot of amount of computing time because we have huge number of node to explore...
    // by the same thinking we can say that will take a lot of space memory...
    // here a chose to use these variable instead to do calculate theme.



    protected double g_cost; // Effective path cost g(p)

    protected double g_cost_PLUS_h_cost; // Estimated path cost: g(p) + h(p);

    protected static final int Value_Min_Max=Integer.MAX_VALUE; // in this class (pass) we compute only cost min.


    /**
     * Constructor to create a new Path_BitSet Object
     *
     */
    @SuppressWarnings("rawtypes")
    public Path_Node()
    {

        this.index_processed_vertices_g1 = 0;

        this.remaining_unprocessed_vertex_g2 = new BitSet(Graph_Map.nb_vertices_g2);
        this.remaining_unprocessed_vertex_g2.set(0,Graph_Map.nb_vertices_g2); // set all bites to 1

        this.unprocessed_edges_g1 = new BitSet(Graph_Map.nb_edges_g1);
        this.unprocessed_edges_g1.set(0,Graph_Map.nb_edges_g1);

        this.unprocessed_edges_g2 = new BitSet(Graph_Map.nb_edges_g2);
        this.unprocessed_edges_g2.set(0,Graph_Map.nb_edges_g2);

        this.g_cost = 0;

        this.g_cost_PLUS_h_cost = g_cost + h();
    }

    /**
     * Constructor to clone a path to a new one
     *
     * @param path_node
     */
    public Path_Node(Path_Node path_node)
    {

        this.index_processed_vertices_g1 = path_node.getIndex_processed_vertices_g1();

        this.remaining_unprocessed_vertex_g2 = (BitSet) path_node.remaining_unprocessed_vertex_g2.clone();

        this.unprocessed_edges_g1 = (BitSet) path_node.unprocessed_edges_g1.clone();
        this.unprocessed_edges_g2 = (BitSet) path_node.unprocessed_edges_g2.clone();

        this.g_cost = path_node.getG_cost();

        this.g_cost_PLUS_h_cost = path_node.getG_cost_PLUS_h_cost();
    }



    /**
     * Heuristic h(p): aims at the estimation of the lower bound of the future costs
     * @return return an estimation of the remaining optimal edit path cost
     */


    //  cost_matrix :
    //
    //  g1 : n
    //  g2 : m
    //            m          n
    //      +-----------+-----------+
    //      |           |           |
    //  n   |    Sub    |   Del     |
    //      |           |           |
    //      +-----------+-----------+
    //      |           |           |
    //  m   |   Insr    | Del-Del   |
    //      |           |           |
    //      +-----------+-----------+
    //
    // all operation are made from g1 to g2, (substitute,delete, insert): element of g1 (with,from,in) g2
    //
    // Upper left == SUBSTITUTION : (elements of g1:n) X (elements of g2:m)
    //
    // Upper right == DELETION : (all elements of g1:n), delete each elements of g1,  (its like a substitution of n element g1 with n epsilon)
    // we put the cost of each element on the diagonal (for the sake of mankers algorithm),
    // because if we put them on the same column, when mankers algo choose one element, all the elements on the same column will be exclude.
    // so, tu insure that each element can be chosen, we put in separate column
    // only diagonal are filled by cost, others case of the matrix (upper right) ar filed by Value_Min_Max
    //
    // Bottom left == insertion: (insert element of g2:m), its mean from g1 we insert all element of g2.
    // also, each element is inserted in the diagonal.
    // only diagonal are filled by cost, others case of the matrix (Bottom left) ar filed by Value_Min_Max
    //
    // Bottom right == Delete --> Delete  (substitution of empty (deleted) elements of g1 with empty (deleted) elements of g2)
    // The matrix (Bottom right) ar filed by 0, since the substitution of the form (empty-->empty) should not cause any cost.
    //
    // Value_Min_Max = +Infinity   if we compute the MIN assignment, (the assignment with the minimum cost)
    //                              like this, those region will not influence for the chose of assignment
    //                              because the value is +Infinity, so Mankers algo will not choose the element.
    //
    // Value_Min_Max = -Infinity    if we compute the Max assignment
    //                               same explanation (but in reverse :)
    //
    // ps : the matrix is always square (n+m)x2 , so when we apply mankers algo we don't need to check if (rows>column) to transpose the matrix.

    protected double[][] create_cost_matrix_edges(int Nb_element_first_set, int Nb_elements_second_set, ArrayList<Integer> indexes_remaining_E1, ArrayList<Integer> indexes_remaining_E2)
    {

        int n = Nb_element_first_set;
        int m = Nb_elements_second_set;

        // create the cost matrix:
        double[][] cost_matrix = new double[n + m][n + m];

        // Upper left == SUBSTITUTION :  == n x m == i x j
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                cost_matrix[i][j] = GED_Operations_Cost.getEdge_substitution_cost(indexes_remaining_E1.get(i),indexes_remaining_E2.get(j));
            }
        }


        // Upper right == DELETION :  == (n X n) : n x (m + n) = i x m+j
        for (int i = 0; i < n; i++)
        {
            for (int j = 0 + m; j < m + n; j++)
            {
                if(j==i+m) // check the diagonal element
                {
                    cost_matrix[i][j] = cost_matrix[i][j] = GED_Operations_Cost.getEdge_deletion_cost();
                }
                else
                {
                    cost_matrix[i][j] = Value_Min_Max;
                }
            }
        }

        // Bottom left == insertion :  == (m X m): (n + m) x + m = i+n x j
        for (int i = 0 + n; i < n + m; i++) {
            for (int j = 0; j < m; j++)
            {
                if(i==j+n) // check the diagonal element
                {
                    cost_matrix[i][j] = GED_Operations_Cost.getEdge_insertion_cost();
                }
                else
                {
                    cost_matrix[i][j] = Value_Min_Max;
                }
            }
        }

        // Bottom right == delete-->delete :  ==   (m X n): n+m x m+n = n+i x m+j
        for (int i = 0 + n; i < n + m; i++) {
            for (int j = 0 + m; j < m + n; j++) {
                cost_matrix[i][j] = 0;
            }
        }

        return cost_matrix;
    }


    protected double[][] create_cost_matrix_vertices(int Nb_element_first_set, int Nb_elements_second_set, ArrayList<Integer> indexes_remaining_V2)
    {
        int n = Nb_element_first_set;
        int m = Nb_elements_second_set;

        // create the cost matrix:
        double[][] cost_matrix = new double[n + m][n + m];


        // Upper left == SUBSTITUTION :  == n x m == i x j
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                cost_matrix[i][j] = GED_Operations_Cost.getVertex_substitution_cost((this.index_processed_vertices_g1 +i),indexes_remaining_V2.get(j));
            }
        }

        // Upper right == DELETION :  == (n X n) : n x (m + n) = i x m+j
        for (int i = 0; i < n; i++)
        {
            for (int j = 0 + m; j < m + n; j++)
            {
                if(j==i+m) // check the diagonal element
                {
                    cost_matrix[i][j] = GED_Operations_Cost.getVertex_deletion_cost();
                }
                else
                {
                    cost_matrix[i][j] = Value_Min_Max;
                }
            }
        }

        // Bottom left == insertion :  == (m X m): (n + m) x + m = i+n x j
        for (int i = 0 + n; i < n + m; i++) {
            for (int j = 0; j < m; j++)
            {
                if(i==j+n) // check the diagonal element
                {
                    cost_matrix[i][j] = GED_Operations_Cost.getVertex_insertion_cost();
                }
                else
                {
                    cost_matrix[i][j] = Value_Min_Max;
                }
            }
        }

        // Bottom right == delete-->delete :  ==   (m X n): n+m x m+n = n+i x m+j
        for (int i = 0 + n; i < n + m; i++) {
            for (int j = 0 + m; j < m + n; j++) {
                cost_matrix[i][j] = 0;
            }
        }

        return cost_matrix;
    }

    public void print_cost_matrix_at_once(double[][] cost_matrix)
    {

        if(cost_matrix==null) return;

        System.out.println("------------------------ Displaying the cost matrix at once: nx2 x mx2 ");

        for (int i = 0; i < cost_matrix.length; i++) {
            for (int j = 0; j < cost_matrix[0].length; j++) {
                System.out.print(cost_matrix[i][j] + "\t");
            }

            System.out.println();
        }
    }


    protected double h()
    {
        double h_Vertices_Cost;
        double h_Edges_Cost;


        int n1 = Graph_Map.nb_vertices_g1 - this.index_processed_vertices_g1; // get the number of remaining vertices in g1

        int n2 = remaining_unprocessed_vertex_g2.cardinality(); // get the number of remaining vertices in g2

        ArrayList<Integer> indexes_remaining_V2 = get_allBites_set_indexes(remaining_unprocessed_vertex_g2);


        int nb_unprocessed_edges_g1 = unprocessed_edges_g1.cardinality();
        int nb_unprocessed_edges_g2 = unprocessed_edges_g2.cardinality();

        ArrayList<Integer> indexes_remaining_E1 = get_allBites_set_indexes(unprocessed_edges_g1);
        ArrayList<Integer> indexes_remaining_E2 = get_allBites_set_indexes(unprocessed_edges_g2);

        double[][] cost_matrix;


        // create the cost matrix for vertices:

        // if the tow sets exist, then we can execute the hangarian algo
        if(n1>0 && n2>0)
        {
            cost_matrix=this.create_cost_matrix_vertices(n1,n2,indexes_remaining_V2);


            int[][] assignment = new int[cost_matrix.length][2];
            assignment = HungarianAlgorithm.hgAlgorithm(cost_matrix, "min");	//Call Hungarian algorithm.


            double sum = 0;
            for (int i=0; i<assignment.length; i++)
            {
                sum = sum + cost_matrix[assignment[i][0]][assignment[i][1]];
            }

            h_Vertices_Cost = sum;

        }
        else
        {
            // The cost of max{0, n1-n2} node deletions and max{0, n2-n1} node insertions
            h_Vertices_Cost =  Math.max(0, n1-n2)* GED_Operations_Cost.getVertex_deletion_cost() + Math.max(0, n2-n1)* GED_Operations_Cost.getVertex_insertion_cost();

        }


        // --------------------------------------------------------------------------------
        //  ----------------  do the same thing with edges :)


        // create the cost matrix for edges:

        if(nb_unprocessed_edges_g1 >0 && nb_unprocessed_edges_g2 >0)
        {
            cost_matrix=this.create_cost_matrix_edges(nb_unprocessed_edges_g1, nb_unprocessed_edges_g2, indexes_remaining_E1, indexes_remaining_E2);

            int[][] assignment_edges = new int[cost_matrix.length][2];
            assignment_edges = HungarianAlgorithm.hgAlgorithm(cost_matrix, "min");	//Call Hungarian algorithm.


            double sum_edges = 0;
            for (int i=0; i<assignment_edges.length; i++)
            {
                sum_edges = sum_edges + cost_matrix[assignment_edges[i][0]][assignment_edges[i][1]];
            }

            h_Edges_Cost =  sum_edges;

        }
        else
        {
            // The cost of max{0, nb_unprocessed_edges_g1-nb_unprocessed_edges_g2} edge deletions and max{0, nb_unprocessed_edges_g2-nb_unprocessed_edges_g1} edge insertions
            h_Edges_Cost =  Math.max(0, nb_unprocessed_edges_g1 - nb_unprocessed_edges_g2)* GED_Operations_Cost.getEdge_deletion_cost() + Math.max(0, nb_unprocessed_edges_g2 - nb_unprocessed_edges_g1)* GED_Operations_Cost.getEdge_insertion_cost();
        }


        // ------------------------------------------------------------------------------------


        return (h_Vertices_Cost + h_Edges_Cost);
    }


    /**
     * Add an operation to the path
     * @param edit_operation the Edit_Operation Object to add
     * @return
     */
    @SuppressWarnings("unchecked")
    public abstract boolean add(Edit_Operation edit_operation, int idx_edit_operation_in_map);



    /**
     * This method collect all the edit operation from the root to actual node in the search tree.
     * when we use idx instead of edit operation at each node,
     * we can in this method return a list of integer,
     * then at the moment of treatment we get the edit operation based on its idx.
     *
     *
     * @return a list of object edit operation.
     */
    public abstract List<Edit_Operation> get_Operations_List();

    /**
     *
     * @param bs  a BitSet contain 1 and 0
     * @return a list of integer that represent the position of 1.
     */
    public static ArrayList<Integer> get_allBites_set_indexes(BitSet bs)
    {
        ArrayList<Integer> indexes = new ArrayList<Integer>(bs.cardinality());

        for (int i = bs.nextSetBit(0); i >= 0 ; i = bs.nextSetBit(i+1))
        {
            indexes.add(i);
        }

        return indexes;
    }


	/*
	 **********************************************
	 * Getters									  *
	 **********************************************
	 */

    /**
     * @return the index_processed_vertices_g1
     */
    public int getIndex_processed_vertices_g1() {
        return index_processed_vertices_g1;
    }


    /**
     * @return the g_cost
     */
    public double getG_cost() {
        return g_cost;
    }

    /**
     * @return the g_cost_PLUS_h_cost
     */
    public double getG_cost_PLUS_h_cost() {
        return g_cost_PLUS_h_cost;
    }

    /**
     * Check if the path is complete
     * @return
     */
    public boolean isCompleteEditPath()
    {
        return (index_processed_vertices_g1 >= Graph_Map.nb_vertices_g1 && remaining_unprocessed_vertex_g2.cardinality()<=0);
    }


    public BitSet getRemaining_unprocessed_vertex_g2() {
        return remaining_unprocessed_vertex_g2;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String str_path="";

        boolean source_Edge_Exists;
        boolean target_Edge_Exists;
        EdgeOperation edgeOperation;

        Edit_Operation edit_operation;
        Edit_Operation old_Edit_Operation;

        int nb_edit_operations=0;

        List<Edit_Operation> old_Edit_Operation_List = get_Operations_List();
        nb_edit_operations = old_Edit_Operation_List.size();

        str_path+="NB ops : "+ nb_edit_operations +" | Path [p=";

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------

        for(int i=0; i<nb_edit_operations;i++)
        {
            edit_operation = old_Edit_Operation_List.get(i);

            str_path=str_path+ (edit_operation).toString(); // vertex edit operation :)

            // for implied edges :)
            // when i=0, the condition of j=0 is false because 0<0 false. So, we don't face the case of the same edit operation.
            // even so, (we have the same edit operation, this don't cause a pbm, because when checking the edgeConatin of the same vertex we don't find an edge because we are in simple graph.
            for (int j=0;j<i;j++)
            {
                old_Edit_Operation = old_Edit_Operation_List.get(j);


                // Check whether the source edge exists
                if(!old_Edit_Operation.is_fromVertex_exist() || !edit_operation.is_fromVertex_exist())
                {
                    source_Edge_Exists = false;
                }
                else
                {
                    source_Edge_Exists = Graph_Map.g1.containsEdge(old_Edit_Operation.get_FromVertex(), edit_operation.get_FromVertex());
                }

                // Check whether the target edge exists
                if(!old_Edit_Operation.is_toVertex_exist() || !edit_operation.is_toVertex_exist())
                {
                    target_Edge_Exists = false;
                }
                else
                {
                    target_Edge_Exists = Graph_Map.g2.containsEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());
                }


                if(source_Edge_Exists && !target_Edge_Exists) // Delete edge (e1 --> epsilon)
                {
                    Edge e1 = (Edge) Graph_Map.g1.getEdge(old_Edit_Operation.get_FromVertex(), edit_operation.get_FromVertex());

                    edgeOperation = new EdgeOperation(e1, null);
                    str_path = str_path + edgeOperation.toString();
                }
                else
                if(!source_Edge_Exists && target_Edge_Exists) // Add edge (epsilon --> e2)
                {
                    Edge e2 = (Edge) Graph_Map.g2.getEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());

                    edgeOperation = new EdgeOperation(null,e2);
                    str_path = str_path + edgeOperation.toString();

                }else
                if(source_Edge_Exists && target_Edge_Exists) // substitution e1-->e2
                {
                    Edge e1 = (Edge) Graph_Map.g1.getEdge(old_Edit_Operation.get_FromVertex(), edit_operation.get_FromVertex());

                    Edge e2 = (Edge) Graph_Map.g2.getEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());

                    edgeOperation = new EdgeOperation(e1, e2);
                    str_path = str_path + edgeOperation.toString();
                }
            }
        }
        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------


        str_path += ", g(p)+h(p)=" + this.g_cost_PLUS_h_cost + "]";

        return str_path;
    }

}

