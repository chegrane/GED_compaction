import imoprt_exoprt_jgrapht_gxl.Import_JGraphT_From_GXL;
import org.github.jamm.MemoryMeter;
import org.jgrapht.UndirectedGraph;

import graph_element.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;


/**
 * Created by user on 27/03/2017.
 */
public class Main {

    private static int cost_node_sub=0;
    private static int cost_node_del_ins=0;

    private static int cost_edge_sub=0;
    private static int cost_edge_del_ins=0;

    private static String file_g1=null;
    private static String file_g2=null;

    private static PrintWriter out=null;

    private static String separator=";";

    public static void main(String[] args)
    {
        String pathname_result_output_file=null;
        String test_benchmark=null; // CMU, MUTA, GREC, PATH (acyclic, alkane, pah, mao)
        int amount_RunTime_S=0;

        if(args.length>=9)
        {
            try {
                cost_node_sub = Integer.parseInt(args[0]);
                cost_node_del_ins = Integer.parseInt(args[1]);
                cost_edge_sub = Integer.parseInt(args[2]);
                cost_edge_del_ins = Integer.parseInt(args[3]);

                file_g1 = args[4];
                file_g2 = args[5];

                pathname_result_output_file=args[6];

                test_benchmark=args[7];

                amount_RunTime_S = Integer.parseInt(args[8]);

            }catch (NumberFormatException e){
                System.err.println("Argument : " + e.getMessage() + " must be an integer.");
                System.exit(1);
            }
        }


        UndirectedGraph<Vertex, Edge> g1=null;
        UndirectedGraph<Vertex, Edge> g2=null;

        if(test_benchmark.equals("PATH")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph(file_g2);
        }
        else if(test_benchmark.equals("GREC")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph_from_GREC_GED(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph_from_GREC_GED(file_g2);
        }
        else if(test_benchmark.equals("MUTA")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph_from_MUTA_GED(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph_from_MUTA_GED(file_g2);
        }
        else if(test_benchmark.equals("CMU")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph_from_CMU_GED(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph_from_CMU_GED(file_g2);
        }


        if(g1==null)
        {
            System.out.println("Can't Load the First Graph : "+file_g1);
            return;
        }
        if(g2==null)
        {
            System.out.println("Can't Load the Second Graph : "+file_g2);
            return;
        }


        //Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree exact_GED = new Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree(g1,g2);
        Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree exact_GED = new Compact_Exact_GED_ASBB_NH_IN_BitSet_Tree(g1,g2,amount_RunTime_S);


        GED_Operations_Cost.setAll_Operations_cost(cost_node_sub,cost_node_del_ins,cost_node_del_ins,cost_edge_sub,cost_edge_del_ins,cost_edge_del_ins,1);

        GED_Operations_Cost.setIs_Weight(true);
        //GED_Operations_Cost.setIs_Label(true);

        MemoryMeter meter = new MemoryMeter();

        // ---------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------
        long startTime = System.nanoTime();

        double editDistance = exact_GED.computeGED();

         long endTime = System.nanoTime();

        long elapsedTime = endTime - startTime;

        long memory_size_remaining_for_solution = meter.measureDeep(exact_GED);

        // ---------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------

        /// String output= "test__"+new File(file_g1).getName()+"__"+new File(file_g2).getName();

        try {
            File file = new File(pathname_result_output_file);

            if(file.getParentFile()!=null){
                file.getParentFile().mkdirs();
            }

            out = new PrintWriter(new FileWriter(file, true), true);

            out.println(file_g1+separator+file_g2+separator + editDistance + separator + TimeUnit.NANOSECONDS.toSeconds(elapsedTime)+separator +TimeUnit.NANOSECONDS.toMillis(elapsedTime) + separator + exact_GED.nb_all_path_added_to_open + separator + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + separator + exact_GED.optimal_Path+ separator + exact_GED.nb_switch_between_method+ separator + exact_GED.is_switched_before_the_first_level   + separator + exact_GED.size_all_data_used + separator +memory_size_remaining_for_solution );

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(out!=null)
                out.close();
        }

        /*
        System.out.println(exact_GED.optimal_Path.toString());

        System.out.println(editDistance);

        System.out.println("\n nb_all_path_added_to_open = "+exact_GED.nb_all_path_added_to_open);

        System.out.println("---------------------------------------- time : ");
        System.out.println("Total execution time: " +elapsedTime +" Nanos");
        System.out.println("Total execution time: " +TimeUnit.NANOSECONDS.toMicros(elapsedTime)+" Micros");
        System.out.println("Total execution time: " +TimeUnit.NANOSECONDS.toMillis(elapsedTime)+" Millis");
        */

        /*
        System.out.println("Total execution time: " +
                String.format("%d min, %d sec",
                        TimeUnit.NANOSECONDS.toHours(elapsedTime),
                        TimeUnit.NANOSECONDS.toSeconds(elapsedTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(elapsedTime))));
                                */


    }
}
