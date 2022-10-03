
/**
 *
 */


import org.github.jamm.MemoryMeter;
import org.jgrapht.Graph;

import java.util.BitSet;
import java.util.Comparator;
import java.util.PriorityQueue;


public class Compact_Exact_GED_AStar_BitSet {

    /**
     * The set OPEN of partial edit paths contains the search tree nodes
     * to be processed in the next steps.
     */
    private PriorityQueue<Path> OPEN;
    private Comparator<Path> comparator;

    private long amount_RunTime=0;

    // Just for debugging purpose
    Path optimal_Path;

    int nb_all_path_added_to_open=0; // just for debugging

    long size_all_data_used =0; // for debugging.

    /**
     * Default Constructor
     */
    public Compact_Exact_GED_AStar_BitSet(Graph g1, Graph g2)
    {
        comparator = new Path_Cost_Comparator();
        OPEN = new PriorityQueue<Path>(10, comparator);

        Graph_Map.initialize_Graph_Map(g1,g2);
    }

    public Compact_Exact_GED_AStar_BitSet(Graph g1, Graph g2, long amount_RunTime) {
        comparator = new Path_Cost_Comparator();
        OPEN = new PriorityQueue<Path>(10, comparator);

        this.amount_RunTime = amount_RunTime;

        Graph_Map.initialize_Graph_Map(g1,g2);
    }


    /**
     * Compute the exact edit distance between two graphs.
     * The method implements A* algorithm
     *
     * @return returns the exact edit distance between the to graphs
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public double computeGED() {

        // add this line to run computeGED  just for a special amout of time (amount_RunTime)
        // long startTime = System.nanoTime();

        OPEN.clear(); // Clear OPEN

        //MemoryMeter meter = new MemoryMeter();
        //size_all_data_used+=meter.measure(this);

        // if both graph are empty, then the edit distance is "0"
        if (Graph_Map.nb_vertices_g1 == 0 && Graph_Map.nb_vertices_g2 == 0) {
            return 0;
        }

        // if the graph g1 is empty (g2 is not), then
        // edit distance is = the cost of (insertion of all vertices of g2) + (insertion of all edges of g2)
        if (Graph_Map.nb_vertices_g1 == 0) {
            return (GED_Operations_Cost.getVertex_insertion_cost() * Graph_Map.nb_vertices_g2 + GED_Operations_Cost.getEdge_insertion_cost() * Graph_Map.nb_edges_g2);
        }


        // if the graph g1 have vertices and edges; and g2 is empty, then
        // edit distance is = the cost of (deletion of all vertices of g1) + (deletion of all edges of g1)
        if (Graph_Map.nb_vertices_g2 == 0) {
            return (GED_Operations_Cost.getVertex_deletion_cost() * Graph_Map.nb_vertices_g1 + GED_Operations_Cost.getEdge_deletion_cost() * Graph_Map.nb_edges_g1);
        }


        {// ------------------- generate the first level of the search Tree :

            // The substitution : Insert substitution (u1 --> w) into OPEN
            for (int j = 0; j< Graph_Map.nb_vertices_g2; j++)
            {
                Edit_Operation vertex_substitution_operation = new Edit_Operation(0, j);
                Path path = new Path();
                path.add(vertex_substitution_operation, Graph_Map.get_idx_in_map_sub(0,j));

                OPEN.add(path);
                nb_all_path_added_to_open++;

                //size_all_data_used+=meter.measureDeep(path);
            }

            // The deletion : Insert deletion (u1 --> null) into OPEN
            Edit_Operation vertex_deletion_operation = new Edit_Operation(0, -1);
            Path path0 = new Path();
            path0.add(vertex_deletion_operation, Graph_Map.get_idx_in_map_del(0));

            OPEN.add(path0);
            nb_all_path_added_to_open++;

            //size_all_data_used+=meter.measureDeep(path0);
        }


        Path best_path;
        while (true)
        {
            best_path = get_Best_Node(); // get the best node (path); and delete it from OPEN.

            if (best_path.isCompleteEditPath()) // check if we arrived to a solution
            {
                //optimal_Path = new Path(best_path); // Make a copy for debugging purpose
                optimal_Path = best_path; // Make a copy for debugging purpose

                //System.out.println("size all data used : "+size_all_data_used);

                return best_path.getG_cost(); // Return the optimal edit distance path cost, because in A-star the firs solution is the optimal one.
            }
            else
            {
                /// The remaining vertices from g2.
                BitSet remainsV2 = best_path.getRemaining_unprocessed_vertex_g2();
                int index_processed_vertices_g1 = best_path.getIndex_processed_vertices_g1();

                if (index_processed_vertices_g1 < Graph_Map.nb_vertices_g1)
                {
                    {// ------------------- generate the intermediate levels of the search Tree :
                        // The substitution
                        for (int j = remainsV2.nextSetBit(0); j >= 0 ; j = remainsV2.nextSetBit(j+1))
                        {
                            Path newP1 = new Path(best_path);
                            Edit_Operation newOp1 = new Edit_Operation(index_processed_vertices_g1, j);
                            newP1.add(newOp1, Graph_Map.get_idx_in_map_sub(index_processed_vertices_g1, j));

                            OPEN.add(newP1);
                            nb_all_path_added_to_open++;

                            //size_all_data_used+=meter.measureDeep(newP1);
                        }

                        // The deletion
                        Path newP2 = new Path(best_path);
                        Edit_Operation newOp2 = new Edit_Operation(index_processed_vertices_g1, -1);
                        newP2.add(newOp2, Graph_Map.get_idx_in_map_del(index_processed_vertices_g1));

                        OPEN.add(newP2);
                        nb_all_path_added_to_open++;

                        //size_all_data_used+=meter.measureDeep(newP2);
                    }
                }
                else
                {
                    // Insert all the remaining vertices of g2 in one step (one loop) in one linked path.
                    // at the end add this path to OPEN.
                    Path newP3=null;
                    for (int j = remainsV2.nextSetBit(0); j >= 0 ; j = remainsV2.nextSetBit(j+1))
                    {
                        newP3 = new Path(best_path);
                        Edit_Operation newOp3 = new Edit_Operation(-1, j);
                        newP3.add(newOp3, Graph_Map.get_idx_in_map_insert(j));

                        //size_all_data_used+=meter.measureDeep(newP3);

                        best_path = newP3; // we add this to ensure that each insertion node are linked in one single step
                    }

                    OPEN.add(newP3);
                    nb_all_path_added_to_open++;

                }
            }

            //long endTime = System.nanoTime();

            //if(TimeUnit.NANOSECONDS.toSeconds(endTime - startTime)>= this.amount_RunTime) break;
        }

        //optimal_Path = new Path(pMin);
        //return pMin.getG_cost_PLUS_h_cost();
    }


    /**
     * Get the best node (path) and delete it from OPEN.
     * in A-Star :
     * best node (path) is represented by the min value g+h;
     *
     * @return returns the best path p.
     */

    private Path get_Best_Node()
    {
        Path p = OPEN.poll();
        return p;
    }
}
