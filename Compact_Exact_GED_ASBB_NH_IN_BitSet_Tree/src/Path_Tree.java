import graph_element.Edge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class Path_Tree extends Path_Node{

    private int vertex_edit_operation_idx; // The edit operation associated with current node in the search tree.
    Path_Node parent; // (a link to the) Parent path



    /**
     * Constructor to create a new Path_BitSet Object
     *
     */
    @SuppressWarnings("rawtypes")
    public Path_Tree()
    {
        super();
        this.parent = null;
        this.vertex_edit_operation_idx = -1;
    }

    /**
     * Constructor to clone a path to a new one
     *
     * @param path_node
     */
    public Path_Tree(Path_Node path_node)
    {
        super(path_node);

        this.parent = path_node;
        this.vertex_edit_operation_idx = -1;
    }

    /**
     * Compute the h and UB values.
     *
     * h : the cost of the best vertices assignment + the cost of the best assignment edges.
     *
     * UB : the cost of the best vertices assignment + the cost of the implied edges.
     *
     * NUB_Path is the path that represent the intermediate UB.
     *
     */
    protected void compute_h_and_newUpperBound()
    {
        double h_Vertices_Cost;
        double h_Edges_Cost;

        Path_Node father_path=this;
        Path_Node child_path=null;

        int n1 = Graph_Map.nb_vertices_g1 - this.index_processed_vertices_g1; // get the number of remaining vertices in g1
        int n2 = remaining_unprocessed_vertex_g2.cardinality(); // get the number of remaining vertices in g2

        ArrayList<Integer> indexes_remaining_V2 = get_allBites_set_indexes(remaining_unprocessed_vertex_g2);

        int nb_unprocessed_edges_g1 = unprocessed_edges_g1.cardinality();
        int nb_unprocessed_edges_g2 = unprocessed_edges_g2.cardinality();

        ArrayList<Integer> indexes_remaining_E1 = get_allBites_set_indexes(unprocessed_edges_g1);
        ArrayList<Integer> indexes_remaining_E2 = get_allBites_set_indexes(unprocessed_edges_g2);

        double[][] cost_matrix;

        // -----------------------------------------------------------------------------------
        // ------------ this first part for :
        //                                   1) compute the cost of the best vertices assignment.
        //                                   2) compute the NUB_Path that represent the intermediate UB.

        // if the tow sets exist, then we can execute the hangarian algo
        if(n1>0 && n2>0)
        {
            cost_matrix=this.create_cost_matrix_vertices(n1,n2,indexes_remaining_V2);

            int[][] assignment = new int[cost_matrix.length][2];
            assignment = HungarianAlgorithm.hgAlgorithm(cost_matrix, "min");	//Call Hungarian algorithm.


            double sum = 0;
            int i1;
            int j1;
            for (int i=0; i<assignment.length; i++)
            {
                sum = sum + cost_matrix[assignment[i][0]][assignment[i][1]];

                //*******************************************************************************
                i1=assignment[i][0]; // Row
                j1=assignment[i][1]; // column

                child_path = new Path_Tree(father_path);

                if(i1<n1 && j1<n2) // Upper left == SUBSTITUTION : substitution vertex i1 with vertex j1
                {
                    Edit_Operation vertex_substitution_operation = new Edit_Operation(this.index_processed_vertices_g1 + i1, indexes_remaining_V2.get(j1));
                    child_path.add_to_real_path(vertex_substitution_operation,Graph_Map.get_idx_in_map_sub(this.index_processed_vertices_g1 + i1, indexes_remaining_V2.get(j1)));
                }
                else if(i1<n1 && (j1>=n2 && j1<n2+n1)) // Upper right == DELETION : delete the vertex i1 from g1
                {
                    Edit_Operation vertex_deletion_operation = new Edit_Operation(this.index_processed_vertices_g1 + i1, -1);
                    child_path.add_to_real_path(vertex_deletion_operation,Graph_Map.get_idx_in_map_del(this.index_processed_vertices_g1 + i1));
                }
                else if((i1>=n1 && i1<n1+n2) && j1<n2) // Bottom left == insertion : insert the vertex j1 in g2
                {
                    Edit_Operation newOp3 = new Edit_Operation(-1, indexes_remaining_V2.get(j1));
                    child_path.add_to_real_path(newOp3,Graph_Map.get_idx_in_map_insert(indexes_remaining_V2.get(j1)));
                }
                else if( (i1>=n1 && i1<n1+n2) && (j1>=n2 && j1<n2+n1)) // Bottom right == delete-->delete : no operation
                {
                    //System.out.println(" delete delete ");
                }

                father_path = child_path;

                //*******************************************************************************
            }

            h_Vertices_Cost = sum;

        }
        else // at this level, either n <=0  or m <=0
        {
            // The cost of max{0, n1-n2} node deletions and max{0, n2-n1} node insertions
            h_Vertices_Cost =  Math.max(0, n1-n2)* GED_Operations_Cost.getVertex_deletion_cost() + Math.max(0, n2-n1)* GED_Operations_Cost.getVertex_insertion_cost();

            // ----------------------------------------------------------------------------------
            // ----------------  compute the new upper bound

            if(n1>0) // g1: n1 , it's mean we delete all n vertex of g1
            {
                for(int i=this.index_processed_vertices_g1;i<Graph_Map.nb_vertices_g1;i++)
                {
                    child_path = new Path_Tree(father_path);

                    Edit_Operation vertex_deletion_operation = new Edit_Operation(i, -1);
                    child_path.add_to_real_path(vertex_deletion_operation,Graph_Map.get_idx_in_map_del(i));

                    father_path = child_path;
                }
            }

            if(n2>0) // g2: n2 , it's mean, we insert all m vertex of g2
            {
                for(int j:indexes_remaining_V2)
                {
                    child_path = new Path_Tree(father_path);

                    Edit_Operation newOp3 = new Edit_Operation(-1, j);
                    child_path.add_to_real_path(newOp3,Graph_Map.get_idx_in_map_insert(j));

                    father_path = child_path;
                }
            }
        }

        // --------------------------------------------------------------------------------
        //  ----------------  calculate the cost of the best edges assignment

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


        NUB_path = child_path;

        g_cost_PLUS_h_cost = g_cost + (h_Vertices_Cost + h_Edges_Cost);
    }

    /**
     * Add an operation to the path
     * @param edit_operation the Edit_Operation Object to add
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean add(Edit_Operation edit_operation, int idx_edit_operation_in_map)
    {
        boolean source_Edge_Exists;
        boolean target_Edge_Exists;

        double vertex_Cost=0;
        double edges_Implied_Cost = 0;

        List<Edit_Operation> old_Edit_Operation_List = get_Operations_List(); // All previous edit operations (vertices) performed

        this.vertex_edit_operation_idx=idx_edit_operation_in_map;
        {
            if(edit_operation.getOperation_type() == GED_Operations.Vertex_SUBSTITUTION) // Vertex_sub v --> w
            {
                index_processed_vertices_g1++;

                remaining_unprocessed_vertex_g2.clear(edit_operation.get_idx_ToVertex());

                vertex_Cost = GED_Operations_Cost.getVertex_substitution_cost(edit_operation.get_FromVertex(),edit_operation.get_ToVertex());

            }else
            if(edit_operation.getOperation_type() == GED_Operations.Vertex_DELETION) // Vertex_DELETION v --> null
            {
                index_processed_vertices_g1++;

                vertex_Cost = GED_Operations_Cost.getVertex_deletion_cost();

            }else  // Vertex Insertion null --> w
            {
                remaining_unprocessed_vertex_g2.clear(edit_operation.get_idx_ToVertex());

                vertex_Cost = GED_Operations_Cost.getVertex_insertion_cost();
            }

            // -------------------------------------------------------------------------------------
            // --------------------- compute the cost of the implied edges --------------------------
            for(Edit_Operation old_Edit_Operation : old_Edit_Operation_List)
            {

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

                    edges_Implied_Cost += GED_Operations_Cost.getEdge_deletion_cost();

                    this.unprocessed_edges_g1.clear(Graph_Map.get_idx_from_list_edges_g1(e1));
                }
                else
                if(!source_Edge_Exists && target_Edge_Exists) // Add edge (epsilon --> e2)
                {
                    Edge e2 = (Edge) Graph_Map.g2.getEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());

                    edges_Implied_Cost += GED_Operations_Cost.getEdge_insertion_cost();

                    this.unprocessed_edges_g2.clear(Graph_Map.get_idx_from_list_edges_g2(e2));

                }else
                if(source_Edge_Exists && target_Edge_Exists) // substitution e1-->e2
                {
                    Edge e1 = (Edge) Graph_Map.g1.getEdge(old_Edit_Operation.get_FromVertex(), edit_operation.get_FromVertex());

                    Edge e2 = (Edge) Graph_Map.g2.getEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());

                    edges_Implied_Cost += GED_Operations_Cost.getEdge_substitution_cost(e1,e2);

                    this.unprocessed_edges_g1.clear(Graph_Map.get_idx_from_list_edges_g1(e1));
                    this.unprocessed_edges_g2.clear(Graph_Map.get_idx_from_list_edges_g2(e2));
                }
            }

            // Update costs
            g_cost += vertex_Cost;
            g_cost += edges_Implied_Cost;

            //g_cost_PLUS_h_cost = g_cost + h();

            compute_h_and_newUpperBound();

            return true;
        }
        //return false;
    }

    /**
     * Add an operation to the real path (computed for the new Upper Bound)
     * @param edit_operation the Edit_Operation Object to add
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean add_to_real_path(Edit_Operation edit_operation, int idx_edit_operation_in_map)
    {
        boolean source_Edge_Exists;
        boolean target_Edge_Exists;

        double vertex_Cost=0;
        double edges_Implied_Cost = 0;

        List<Edit_Operation> old_Edit_Operation_List = get_Operations_List(); // All previous edit operations (vertices) performed

        this.vertex_edit_operation_idx=idx_edit_operation_in_map;
        {
            if(edit_operation.getOperation_type() == GED_Operations.Vertex_SUBSTITUTION) // Vertex_sub v --> w
            {
                index_processed_vertices_g1++;

                remaining_unprocessed_vertex_g2.clear(edit_operation.get_idx_ToVertex());

                vertex_Cost = GED_Operations_Cost.getVertex_substitution_cost(edit_operation.get_FromVertex(),edit_operation.get_ToVertex());

            }else
            if(edit_operation.getOperation_type() == GED_Operations.Vertex_DELETION) // Vertex_DELETION v --> null
            {
                index_processed_vertices_g1++;

                vertex_Cost = GED_Operations_Cost.getVertex_deletion_cost();

            }else  // Vertex Insertion null --> w
            {
                remaining_unprocessed_vertex_g2.clear(edit_operation.get_idx_ToVertex());

                vertex_Cost = GED_Operations_Cost.getVertex_insertion_cost();
            }

            // -------------------------------------------------------------------------------------
            // --------------------- compute the cost of the implied edges --------------------------
            for(Edit_Operation old_Edit_Operation : old_Edit_Operation_List)
            {

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

                    edges_Implied_Cost += GED_Operations_Cost.getEdge_deletion_cost();

                    this.unprocessed_edges_g1.clear(Graph_Map.get_idx_from_list_edges_g1(e1));
                }
                else
                if(!source_Edge_Exists && target_Edge_Exists) // Add edge (epsilon --> e2)
                {
                    Edge e2 = (Edge) Graph_Map.g2.getEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());

                    edges_Implied_Cost += GED_Operations_Cost.getEdge_insertion_cost();

                    this.unprocessed_edges_g2.clear(Graph_Map.get_idx_from_list_edges_g2(e2));

                }else
                if(source_Edge_Exists && target_Edge_Exists) // substitution e1-->e2
                {
                    Edge e1 = (Edge) Graph_Map.g1.getEdge(old_Edit_Operation.get_FromVertex(), edit_operation.get_FromVertex());

                    Edge e2 = (Edge) Graph_Map.g2.getEdge(old_Edit_Operation.get_ToVertex(), edit_operation.get_ToVertex());

                    edges_Implied_Cost += GED_Operations_Cost.getEdge_substitution_cost(e1,e2);

                    this.unprocessed_edges_g1.clear(Graph_Map.get_idx_from_list_edges_g1(e1));
                    this.unprocessed_edges_g2.clear(Graph_Map.get_idx_from_list_edges_g2(e2));
                }
            }

            // Update costs
            g_cost += vertex_Cost;
            g_cost += edges_Implied_Cost;

            g_cost_PLUS_h_cost = g_cost + 0; // here we don't need to compute the 'h' value

            // because : we have a real complete path, we add all implied egde to it,
            // at the end when we reach the leaf, the 'h' value will be 0
            // also, we don't need the intermdiate (partial) path in this real complete path.
            // so simply we put 0 in the place of 'h' to vaoid the computation

            return true;
        }
        //return false;
    }




    /**
     * This method collect all the edit operation from the root to actual node in the search tree.
     * when we use idx instead of edit operation at each node,
     * we can in this method return a list of integer,
     * then at the moment of treatment we get the edit operation based on its idx.
     *
     *
     * @return a list of object edit operation.
     */
    public List<Edit_Operation> get_Operations_List()
    {
        List<Edit_Operation> operationsList= new ArrayList<Edit_Operation>();


        // this condition to avoid the root node in which we have no parent
        // When we arrive to root node: we will have two things
        // 1) The node is a BitSet node, that's mean it contain the edit operation from the real root in the search tree until the level of this end BitSet node.
        //      So, the method 'get_Operations_List' in the Path_BitSet
        //      will return the list of edit operations from the real root to this node.
        //      then stop.
        // 2) The node is a real root, So, stop.
        if(this.parent!=null)
        {
            operationsList.addAll(this.parent.get_Operations_List());
        }

        // this condition to avoid the case in which we have not added the current edit operation.
        if(this.vertex_edit_operation_idx!=-1) {
            operationsList.add(Graph_Map.get_Edit_Operation(this.vertex_edit_operation_idx));
        }

        return operationsList;
    }

    // to use it i the dynamic version of choice method
    protected List<Integer> get_Operations_ids_List()
    {
        List<Integer> operations_ids_List= new ArrayList<Integer>();

        if(this.parent!=null)
        {
            operations_ids_List.addAll(this.parent.get_Operations_ids_List());
        }

        // this condition to avoid the case in which we have not added the current edit operation.
        if(this.vertex_edit_operation_idx!=-1) {
            operations_ids_List.add(this.vertex_edit_operation_idx);
        }

        return operations_ids_List;
    }

}

