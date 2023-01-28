
/**
 *
 */


import java.util.*;

import org.github.jamm.MemoryMeter;
import org.jgrapht.Graph;


public class Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree {

    /**
     * The set OPEN of partial edit paths contains the search tree nodes
     * to be processed in the next steps.
     */
    private PriorityQueue<Path_Node> OPEN;
    private Comparator<Path_Node> comparator;

    private long amount_RunTime=0;

    private double upper_bound = Double.MAX_VALUE;

    // Just for debugging purpose
    Path_Node optimal_Path;

    int nb_all_path_added_to_open=0; // just for debugging

    long size_all_data_used =0; // for debugging.
    private static final boolean compute_Used_Space=false;
    private MemoryMeter meter;

    private Boolean is_BitSet_Less_than_Tree = true;

    int nb_switch_between_method =0; // for debugging
    // static version: we will have only (01) one change, if we have
    // the first is BitSetMethod, than we change to PointerMethod. and stop.
    //
    // Dynamic version: we will have from 01 to probably more than (02) two change. tow change for sure.
    //                  first, we have initialize BitSet, than change to PointerMethod, than we change to BitSet, so we have 02
    //                  this whene we begin by pointer, than we switch to pointer.
    //            I dont know if we will switch more than that...????

    Boolean is_switched_before_the_first_level=false; // for debugging purpose
    // if we switch before the first level, that means we really begin by the PointerMethod
    // if not, that mean we really began the BitSetMethod.
    //so using nb_switch_between_method and is_switched_before_the_first_level
    // we can know the combination methods used.
    // is_switched_before_the_first_level == true
    //     nb_switch = 0 : only bitset method used
    //                 1 : only pointer method used
    //                 2 : pointer method, then Bitset
    //                 3.. : pointer method, then Bitset,  then pointer...
    // is_switched_before_the_first_level == false
    //     nb_switch = 0 : only bitset method used
    //                 1 : bitset then pointer
    //                 2.. : bitset then pointer then Bitset..

    private int size_static_data_needed = 0; // the size common data needed in both methods
    private int size_one_node_Tree = 0; // The size needed for one node in the Tree Method.
    private int size_Tree_Method = 0;   // The size needed in a given node for Tree Method
    private int size_BitSet_Method = 0; // The size needed in a given node for Tree Method


    /**
     * Default Constructor
     */
    public Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree(Graph g1, Graph g2)
    {
        comparator = new Path_Cost_Comparator();
        OPEN = new PriorityQueue<Path_Node>(10, comparator);

        Graph_Map.initialize_Graph_Map(g1,g2);

        initialize_size_data();
    }

    public Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree(Graph g1, Graph g2, long amount_RunTime) {
        comparator = new Path_Cost_Comparator();
        OPEN = new PriorityQueue<Path_Node>(10, comparator);

        this.amount_RunTime = amount_RunTime;

        Graph_Map.initialize_Graph_Map(g1,g2);

        initialize_size_data();
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

            check_if_is_BitSet_Less_than_Tree(1);
            check_if_is_switched_before_the_first_level();// we don't include the code with "check_if_is_BitSet..." we call it several time, for that we need just one test here.

            // The substitution : Insert substitution (u1 --> w) into OPEN
            for (int j = 0; j < Graph_Map.nb_vertices_g2; j++)
            {
                Edit_Operation vertex_substitution_operation = new Edit_Operation(0, j);
                Path_Node path_node = instantiate_right_path_node();
                path_node.add(vertex_substitution_operation, Graph_Map.get_idx_in_map_sub(0, j));

                check_and_update(path_node);
            }

            // The deletion : Insert deletion (u1 --> null) into OPEN
            Edit_Operation vertex_deletion_operation = new Edit_Operation(0, -1);
            Path_Node path_node_delete = instantiate_right_path_node();
            path_node_delete.add(vertex_deletion_operation, Graph_Map.get_idx_in_map_del(0));

            check_and_update(path_node_delete);
        }


        Path_Node best_path;
        while (true)
        {
            best_path = get_Best_Node(); // get the best node (path); and delete it from OPEN.

            if (best_path.isCompleteEditPath()) // check if we arrived to a solution
            {
                //optimal_Path = instantiate_right_path_node(best_path); // Make a copy for debugging purpose
                optimal_Path = best_path; // Make a copy for debugging purpose

                return best_path.getG_cost(); // Return the optimal edit distance path cost, because in A-star the firs solution is the optimal one.
            }
            else
            {
                /// The remaining vertices from g2.
                BitSet remainsV2 = best_path.getRemaining_unprocessed_vertex_g2();
                int index_processed_vertices_g1 = best_path.getIndex_processed_vertices_g1();

                check_if_is_BitSet_Less_than_Tree(index_processed_vertices_g1+1);

                if (index_processed_vertices_g1 < Graph_Map.nb_vertices_g1)
                {
                    {// ------------------- generate the intermediate levels of the search Tree :

                        // The substitution
                        for (int j = remainsV2.nextSetBit(0); j >= 0; j = remainsV2.nextSetBit(j + 1))
                        {
                            Path_Node newP1 = instantiate_right_path_node(best_path);

                            Edit_Operation newOp1 = new Edit_Operation(index_processed_vertices_g1, j);
                            newP1.add(newOp1, Graph_Map.get_idx_in_map_sub(index_processed_vertices_g1, j));

                            check_and_update(newP1);
                        }

                        // The deletion
                        Path_Node newP2 = instantiate_right_path_node(best_path);
                        Edit_Operation newOp2 = new Edit_Operation(index_processed_vertices_g1, -1);
                        newP2.add(newOp2, Graph_Map.get_idx_in_map_del(index_processed_vertices_g1));

                        check_and_update(newP2);
                    }
                }
                else
                {
                    // Insert all the remaining vertices of g2 in one step (one loop) in one linked path.
                    // at the end add this path to OPEN.
                    Path_Node newP3=null;
                    for (int j = remainsV2.nextSetBit(0); j >= 0 ; j = remainsV2.nextSetBit(j+1))
                    {
                        newP3 = instantiate_right_path_node(best_path);
                        Edit_Operation newOp3 = new Edit_Operation(-1, j);
                        newP3.add(newOp3,Graph_Map.get_idx_in_map_insert(j));

                        best_path = newP3; // we add this to ensure that each insertion node are linked in one single step
                    }

                    check_and_update(newP3);

                }
            }

            //long endTime = System.nanoTime();

            //if(TimeUnit.NANOSECONDS.toSeconds(endTime - startTime)>= this.amount_RunTime) break;
        }

        //optimal_Path = new Path_BitSet(pMin);
        //return pMin.getG_cost_PLUS_h_cost();
    }


    /**
     * Get the best node (path) and delete it from OPEN.
     * in A-Star :
     * best node (path) is represented by the min value g+h;
     *
     * @return returns the best path p.
     */
    private Path_Node get_Best_Node()
    {
        Path_Node p = OPEN.poll();

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

        Path_Node path_newH = NewH_function.compute();
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
    private Boolean check_and_update(Path_Node path)
    {
        if(path.getG_cost_PLUS_h_cost()<this.upper_bound)
        {
            this.OPEN.add(path);
            this.nb_all_path_added_to_open++;

            if(compute_Used_Space) {
                size_all_data_used += meter.measure(path);
            }

            if(path.getNUB_path().getG_cost()<upper_bound)
            {
                this.upper_bound = path.getNUB_path().getG_cost(); // new upper bound found

                OPEN.add(path.getNUB_path()); // add the path that represent the new UB.
                nb_all_path_added_to_open++;

                if(compute_Used_Space) {
                    size_all_data_used += meter.measure(path.getNUB_path());
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

    /**
     *  we can use the package "Instrumentation" to get the object size
     *  which give an approximate size not the exact one
     *  https://docs.oracle.com/javase/1.5.0/docs/api/java/lang/instrument/Instrumentation.html#getObjectSize(java.lang.Object)
     *
     *  long getObjectSize(Object objectToSize)
     *  Returns an implementation-specific approximation of the amount of storage consumed by the specified object.
     *
     *  But in our case we keep it simple:
     *  we based our calculation only on the size of the data used in the object
     *  without adding the extra size by the object meta data.
     */
    private void initialize_size_data()
    {
        size_static_data_needed =
                Integer.SIZE/*index_processed_vertices_g1*/+
                        Graph_Map.nb_vertices_g2/*remaining_unprocessed_vertex_g2*/+
                        Graph_Map.nb_edges_g1/*unprocessed_edges_g1*/+
                        Graph_Map.nb_edges_g2/*unprocessed_edges_g2*/+
                        Double.SIZE/*g*/+
                        Double.SIZE/*h*/
        ;


        size_one_node_Tree =
                get_SizeOf_Reference()/*link to parent*/+
                        Integer.SIZE/*vertex_edit_operation_idx*/+
                        size_static_data_needed
        ;

        // (m+2) * (a+b)
        // (m+2) : number of nodes (all  previous fathers + father + outgoing children)
        // (a+b) : the size of one node, here not just a+b, but we add all the static data needed in one node.
        this.size_Tree_Method = (Graph_Map.nb_vertices_g2+2) * this.size_one_node_Tree;
    }



    /**
     *
     * @return the size of reference in Java which is 32 or 64 bits
     *
     * SizeOf Reference (link) :
     * in practice it will be an address: 32 bit on 32 bit CPU, 64 at 64.
     * Oracle Hotspot JVM has a feature called "Compressed Oops" which are 32 bit references in a 64 bit JVM
     * http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html
     */
    private int get_SizeOf_Reference() {
        // its a bit complicated to get the actual size of reference used by the JVM
        // for that i will use 32 bits just for now.

        // I can use "long heapMaxSize = Runtime.getRuntime().maxMemory();"
        // and check if its less than 32 GO, that means 32 bits
        // else : if the JVM use "Compressed Oops" so 32 bits also, and if not so 64 bits.
        //
        // I found a class in github to do that
        // https://gist.github.com/salyh/2cea2b99a0547d266d56

        //System.out.println("Is 64bit Hotspot JVM: " + CompressedOopsChecker.JRE_IS_64BIT_HOTSPOT);
        //System.out.println("Compressed Oops enabled: " + CompressedOopsChecker.COMPRESSED_REFS_ENABLED);
        //System.out.println("isCompressedOopsOffOn64Bit: " + CompressedOopsChecker.isCompressedOopsOffOn64Bit());


        // return true only if : system = 64 && CompressedOops are not enabled.
        if(CompressedOopsChecker.isCompressedOopsOffOn64Bit())
            return 64;

        return 32;
    }


    private Path_Node instantiate_right_path_node()
    {
        Path_Node path_node=null;

        // If the size of the BitSet method is less, then we use the BitSet Method.
        if(is_BitSet_Less_than_Tree) {
            path_node = new Path_BitSet();
        }
        else // else, if the size of the linked tree is best, we switch to the Tree method.
        {
            path_node = new Path_Tree();
        }

        return path_node;
    }

    private Path_Node instantiate_right_path_node(Path_Node path_node_in)
    {
        Path_Node path_node=null;

        // If the size of the BitSet method is less, then we use the BitSet Method.
        if(is_BitSet_Less_than_Tree)
        {
            path_node = new Path_BitSet(path_node_in);
        }
        else // else, if the size of the linked tree is best, we switch to the Tree method.
        {
            path_node = new Path_Tree(path_node_in);
        }

        return path_node;
    }


    // method static:
    // in this version we use at the beginning the BitSet method,
    // than we switch to the pointer method
    // for that we have to ensure that we switch just one time.
    // by testing the "is_BitSet_Less_than_Tree", if its already false we don't turn it again to true.
    // at the beginning is_BitSet_Less_than_Tree == true.
    //
    // Dynamic version:
    // we allow switching each time between BitSetMethod and PointerMethod

    private Boolean check_if_is_BitSet_Less_than_Tree(int node_level)
    {
        // for static version;
        // if the var is already turn into false, we return immediately.
        if(!this.is_BitSet_Less_than_Tree) return this.is_BitSet_Less_than_Tree;

        // ((m+2)-i)* (i* m + static_data)
        // ((m+2)-i) : only outgoing children.
        //           (m+2) : number of nodes (all  previous fathers + father + outgoing children)
        //              -i : all  previous fathers + father
        // (i* m + static_data) : the size needed for one node in BitSet
        //                      i* m : the size needed for the BitSet that contain all the path information.
        this.size_BitSet_Method =
                ((Graph_Map.nb_vertices_g2+1)-node_level)*
                (node_level*Graph_Map.nb_vertices_g2 + this.size_static_data_needed)
                ;

        ///System.out.println("node_level ="+node_level+"-->BitSet:"+this.size_BitSet_Method+" | " +this.size_Tree_Method+":Tree");

        // static version
        if(this.size_BitSet_Method>this.size_Tree_Method)
        {
            this.is_BitSet_Less_than_Tree = false;
            this.nb_switch_between_method +=1;
        }

        /** dynamic version
        if(this.size_BitSet_Method<this.size_Tree_Method)
         {
            // we check before increment the value "nb_switch_between_method"
            // because if "is_BitSet_Less_than_Tree" is already true, so we don't change anything.
            if (!this.is_BitSet_Less_than_Tree)
            {
                this.is_BitSet_Less_than_Tree =true;
                this.nb_switch_between_method +=1;
            }
         }
        else
         {
             if (this.is_BitSet_Less_than_Tree)
             {
                this.is_BitSet_Less_than_Tree =false;
                this.nb_switch_between_method +=1;
             }
         }
         */

        return this.is_BitSet_Less_than_Tree;
    }

    private Boolean check_if_is_switched_before_the_first_level()
    {
        if(!this.is_BitSet_Less_than_Tree) this.is_switched_before_the_first_level=true;

        return this.is_switched_before_the_first_level;
    }
}
