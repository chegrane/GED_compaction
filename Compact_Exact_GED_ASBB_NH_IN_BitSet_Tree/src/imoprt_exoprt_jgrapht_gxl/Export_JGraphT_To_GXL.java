package imoprt_exoprt_jgrapht_gxl;

import graph_element.Edge;
import graph_element.Vertex;
import net.sourceforge.gxl.*;
import org.jgrapht.UndirectedGraph;

import java.io.File;
import java.io.IOException;

/**
 * Created by user on 14/05/2017.
 */
public class Export_JGraphT_To_GXL {

    public static boolean export_simple_graph(UndirectedGraph<Vertex, Edge> graph_JGT, String output_file_name)
    {
        // Create document and elements
        GXLDocument gxlDocument = new GXLDocument();
        GXLGraph graph_GXL = new GXLGraph("graph1");

        for(Vertex v: graph_JGT.vertexSet())
        {
            //GXLNode node = new GXLNode(v.getVertexLabel());
            GXLNode node = new GXLNode("_"+v.getVertex_id());

            GXLInt gxlInt=new GXLInt(v.getVertex_weight());

           // node.add(gxlInt);

            graph_GXL.add(node);
        }

        for(Edge e: graph_JGT.edgeSet())
        {
            GXLNode node_from = new GXLNode("_"+((Vertex) e.getSource()).getVertex_id());
            GXLNode node_to = new GXLNode("_"+((Vertex) e.getTarget()).getVertex_id());

            GXLEdge edge = new GXLEdge(node_from, node_to);

            graph_GXL.add(edge);
        }

        gxlDocument.getDocumentElement().add(graph_GXL);

        // Write the document to file
        try {
            gxlDocument.write(new File(output_file_name));
            return true;
        }
        catch (IOException ioe) {
            System.out.println("Error while writing to file: " + ioe);
            return false;
        }
    }
}
