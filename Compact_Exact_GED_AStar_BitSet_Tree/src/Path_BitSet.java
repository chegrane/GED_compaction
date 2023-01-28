import graph_element.Edge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class Path_BitSet extends Path_Node{

    private BitSet list_vertex_edit_operations; // List of edit operations


    /**
     * Constructor to create a new Path_BitSet Object
     *
     */
    @SuppressWarnings("rawtypes")
    public Path_BitSet()
    {
        super();

        this.list_vertex_edit_operations = new BitSet(Graph_Map.nb_vertices_g2+1); // the size of the bitset is the number of vertices of g2 (for the substitution case) + one 1 for the column of the deletion case.
    }

    /**
     * Constructor to clone a path to a new one
     *
     * @param path_node
     */
    public Path_BitSet(Path_Node path_node)
    {
        super(path_node);

        // the size of the bit set is : (i x (m+1)) where i is the level, and m the number of vertices for substitution and 1 for the deletion
        // i: is represented by "index_processed_vertices_g1"
        // we put (index_processed_vertices_g1+1) because "index_processed_vertices_g1" has the value of the previous node (level).
        this.list_vertex_edit_operations = new BitSet((path_node.index_processed_vertices_g1+1)*(Graph_Map.nb_vertices_g2+1));
        this.list_vertex_edit_operations.or(((Path_BitSet) path_node).list_vertex_edit_operations);
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

        this.list_vertex_edit_operations.set(idx_edit_operation_in_map);
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

            g_cost_PLUS_h_cost = g_cost + h();

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

        for (int i = this.list_vertex_edit_operations.nextSetBit(0); i >= 0 ; i = this.list_vertex_edit_operations.nextSetBit(i+1))
        {
            operationsList.add(Graph_Map.get_Edit_Operation(i));
        }

        return operationsList;
    }
}

