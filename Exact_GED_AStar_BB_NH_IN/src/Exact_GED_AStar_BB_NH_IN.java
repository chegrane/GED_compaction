
/**
 *
 */


import java.util.*;

import graph_element.*;
import org.github.jamm.MemoryMeter;
import org.jgrapht.Graph;


public class Exact_GED_AStar_BB_NH_IN {

    /**
     * The set OPEN of partial edit paths contains the search tree nodes
     * to be processed in the next steps.
     */
    private PriorityQueue<Path> OPEN;
    private Comparator<Path> comparator;

    private long amount_RunTime=0;

    private double upper_bound = Double.MAX_VALUE;

    // Just for debugging purpose
    Path optimal_Path;

    int nb_all_path_added_to_open=0; // just for debugging

    long size_all_data_used =0; // for debugging.
    private static final boolean compute_Used_Space=false;
    private MemoryMeter meter;

    /**
     * Default Constructor
     */
    public Exact_GED_AStar_BB_NH_IN(Graph g1, Graph g2)
    {
        comparator = new Path_Cost_Comparator();
        OPEN = new PriorityQueue<Path>(10, comparator);

        Graph_Map.initialize_Graph_Map(g1,g2);
    }

    public Exact_GED_AStar_BB_NH_IN(Graph g1, Graph g2, long amount_RunTime) {
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

        meter = new MemoryMeter();
        if(compute_Used_Space) {
            size_all_data_used += meter.measure(this);
        }

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

        // Compute the Upper Bound
        upper_bound = compute_upper_bound();

        {// ------------------- generate the first level of the search Tree :

            // The substitution : Insert substitution (u1 --> w) into OPEN
            for (Vertex w : Graph_Map.list_vertices_g2) {
                Edit_Operation vertex_substitution_operation = new Edit_Operation(Graph_Map.list_vertices_g1.get(0), w);
                Path path = new Path();
                path.add(vertex_substitution_operation);

                check_and_update(path);
            }

            // The deletion : Insert deletion (u1 --> null) into OPEN
            Edit_Operation vertex_deletion_operation = new Edit_Operation(Graph_Map.list_vertices_g1.get(0), null);
            Path path0 = new Path();
            path0.add(vertex_deletion_operation);

            check_and_update(path0);
        }


        Path best_path;
        while (true)
        {
            best_path = get_Best_Node(); // get the best node (path); and delete it from OPEN.

            if (best_path.isCompleteEditPath()) // check if we arrived to a solution
            {
                //optimal_Path = new Path(best_path); // Make a copy for debugging purpose
                optimal_Path = best_path; // Make a copy for debugging purpose

                return best_path.getG_cost(); // Return the optimal edit distance path cost, because in A-star the firs solution is the optimal one.
            }
            else
            {
                /// The remaining vertices from g2.
                List<Vertex> remainsV2 = best_path.getRemaining_unprocessed_vertex_g2();
                int index_processed_vertices_g1 = best_path.getIndex_processed_vertices_g1();

                if (index_processed_vertices_g1 < Graph_Map.nb_vertices_g1)
                {
                    {// ------------------- generate the intermediate levels of the search Tree :
                        // The substitution
                        for (Vertex w1 : remainsV2) {
                            Path newP1 = new Path(best_path);
                            Edit_Operation newOp1 = new Edit_Operation(Graph_Map.list_vertices_g1.get(index_processed_vertices_g1), w1);
                            newP1.add(newOp1);

                            check_and_update(newP1);
                        }

                        // The deletion
                        Path newP2 = new Path(best_path);
                        Edit_Operation newOp2 = new Edit_Operation(Graph_Map.list_vertices_g1.get(index_processed_vertices_g1), null);
                        newP2.add(newOp2);

                        check_and_update(newP2);
                    }
                }
                else
                {
                    // Insert all the remaining vertices of g2 in one step (one loop) in one linked path.
                    // at the end add this path to OPEN.
                    Path newP3=null;
                    for (Vertex w2 : remainsV2)
                    {
                        newP3 = new Path(best_path);
                        Edit_Operation newOp3 = new Edit_Operation(null, w2);
                        newP3.add(newOp3);

                        best_path = newP3; // we add this to ensure that each insertion node are linked in one single step
                    }

                    check_and_update(newP3);
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

    /**
     * Compute the Upper Bound
     *
     * We use the method that compute an effective valid path :
     * by the best assignment of vertices plus the implied edges.
     *
     * We update also the OPEN:
     *
     * we add this solution to open, beacaus this new h filtre toutes les solutions non optimal
     * et si cette solution (path_newH) est la solution optimal, donc on trouve pas de solution
     * et OPEN returne null à la fin, et donc bug.
     * lorsque on ajoute cette solutio (path_newH) on est sure qu'on a une solution valide.
     *
     * @return the value UB
     */
    private double compute_upper_bound()
    {
        Double upper_bound;

        Path path_newH = NewH_function.compute();
        upper_bound =path_newH.getG_cost();

        this.OPEN.add(path_newH);
        this.nb_all_path_added_to_open++;

        return upper_bound;
    }


    /**
     *
     * check if the g+h cost path is lower than the upper bound in order to add it OPEN.
     *
     * Also, check if the intermediate computed UB is best than the previous one.
     *       if yes, we update the global UB, and we keep the path that represent the new UB.
     *
     * @param path : Path that we must check if is valid or not
     *             valid path when its cost is between the UB and LB.
     *
     *             if the path is valid,
     *             we check if its computed intermediate UB is best than the old one.
     *             in order to update the global UB.
     *
     * @return true if we add the path to OPEN, false otherwise.
     */
    private Boolean check_and_update(Path path)
    {
        if(path.getG_cost_PLUS_h_cost()<this.upper_bound)
        {
            this.OPEN.add(path);
            this.nb_all_path_added_to_open++;

            if(compute_Used_Space){
                size_all_data_used+=meter.measureDeep(path);
            }

            if(path.getNUB_path().getG_cost()<upper_bound)
            {
                this.upper_bound = path.getNUB_path().getG_cost(); // new upper bound found

                OPEN.add(path.getNUB_path()); // add the path that represent the new UB.
                nb_all_path_added_to_open++;

                if(compute_Used_Space){
                    size_all_data_used+=meter.measureDeep(path.getNUB_path());
                }
            }
            else // because we don't need NUB_Path we delete it
            {
                path.delete_NUB_path();
            }

            return true;
        }

        return false;
    }
}
