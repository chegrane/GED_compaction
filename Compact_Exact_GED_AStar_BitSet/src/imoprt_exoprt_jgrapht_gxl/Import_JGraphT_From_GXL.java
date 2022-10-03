package imoprt_exoprt_jgrapht_gxl;

import graph_element.Edge;
import graph_element.Vertex;
import net.sourceforge.gxl.*;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by user on 14/05/2017.
 */
public class Import_JGraphT_From_GXL {


    // acyclic, alkane, mao and pah Benchmark :  Vertex Weight (int),  Edge Weight (int).
    //                                           Id begin from _1 , with the char "_" before the ID.
    public static UndirectedGraph<Vertex, Edge> import_simple_graph(String gxl_path_file)
    {
        UndirectedGraph<Vertex, Edge> graph_JGJ = new SimpleGraph(Edge.class);

        try {
            File gxlFile = new File(gxl_path_file);
            GXLDocument gxlDocument = new GXLDocument(gxlFile);


            GXLGraph graph_GXL = gxlDocument.getDocumentElement().getGraphAt(0);

            GXLNode gxlNode;
            GXLEdge gxlEdge;
            GXLAttr gxlAttr;
            GXLInt gxlInt;

            int nodeId;
            int nodeValue;

            int edgeValue;

            int nb_element = graph_GXL.getGraphElementCount();

            //int nbNode=(int) Math.ceil((0.75)*(nb_element)); // on supose que le nombre de nodes est de 3/4 de toutes les elemeents
            //ArrayList<Vertex> arrayList = new ArrayList<Vertex>(nbNode);

            int nbNode = nb_element; // we supose that all element are node, because here we can't risk, unless we find a fast methode to know the number of nodes

            Vertex[] arry_ref_vertex = new Vertex[nbNode+1]; // +1 , beacause Node ID begin from 1. so to not each time do (nodeID-1)


            for (int i = 0; i < nb_element; i++)
            {
                GXLElement graphElement = graph_GXL.getGraphElementAt(i);

                if (graphElement instanceof GXLNode)
                {
                    gxlNode = ((GXLNode) graphElement);
                    nodeId = Integer.parseInt((gxlNode.getID()).substring(1));

                    gxlAttr = gxlNode.getAttrAt(0);

                    gxlInt = (GXLInt) gxlAttr.getChildAt(0);

                    nodeValue = gxlInt.getIntValue();

                    Vertex v = new Vertex(nodeId, nodeValue);

                    graph_JGJ.addVertex(v);

                    arry_ref_vertex[v.getVertex_id()] = v;
                } else if (graphElement instanceof GXLEdge) {
                    gxlEdge = (GXLEdge) graphElement;

                /*System.out.println(" Edge : "+((GXLEdge) graphElement).getID()+
                        "| form: "+((GXLEdge) graphElement).getSourceID()+
                        "--> to : "+((GXLEdge) graphElement).getTargetID());*/

                    int nodeID_from = Integer.parseInt(((GXLEdge) graphElement).getSourceID().substring(1));
                    int nodeID_to = Integer.parseInt(((GXLEdge) graphElement).getTargetID().substring(1));

                    edgeValue = ((GXLInt) (gxlEdge.getAttrAt(0).getChildAt(0))).getIntValue();

                    if (arry_ref_vertex[nodeID_from] != null && arry_ref_vertex[nodeID_to] != null) {
                        Edge myEdge = graph_JGJ.addEdge(arry_ref_vertex[nodeID_from], arry_ref_vertex[nodeID_to]);

                        myEdge.setWeight(edgeValue);
                    }
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return graph_JGJ;
    }

    // MUTA-GED Benchmark :  Vertex Label (string),  Edge Weight (int).
    //                       Id begin from 1 without any char before the ID.
    public static UndirectedGraph<Vertex, Edge> import_simple_graph_from_MUTA_GED(String gxl_path_file)
    {
        UndirectedGraph<Vertex, Edge> graph_JGJ = new SimpleGraph(Edge.class);

        try {
            File gxlFile = new File(gxl_path_file);
            GXLDocument gxlDocument = new GXLDocument(gxlFile);


            GXLGraph graph_GXL = gxlDocument.getDocumentElement().getGraphAt(0);

            GXLNode gxlNode;
            GXLEdge gxlEdge;
            GXLAttr gxlAttr;
            GXLInt gxlInt;
            GXLString gxlString;

            int nodeId;
            String nodeValue;
            int nodeValueHash;

            int edgeValue;

            int nb_element = graph_GXL.getGraphElementCount();

            //int nbNode=(int) Math.ceil((0.75)*(nb_element)); // on supose que le nombre de nodes est de 3/4 de toutes les elemeents
            //ArrayList<Vertex> arrayList = new ArrayList<Vertex>(nbNode);

            int nbNode = nb_element; // we supose that all element are node, because here we can't risk, unless we find a fast methode to know the number of nodes

            Vertex[] arry_ref_vertex = new Vertex[nbNode+1]; // +1 , beacause Node ID begin from 1. so to not each time do (nodeID-1)


            for (int i = 0; i < nb_element; i++)
            {
                GXLElement graphElement = graph_GXL.getGraphElementAt(i);

                if (graphElement instanceof GXLNode)
                {
                    gxlNode = ((GXLNode) graphElement);
                    nodeId = Integer.parseInt(gxlNode.getID());

                    gxlAttr = gxlNode.getAttrAt(0);

                    gxlString = (GXLString) gxlAttr.getChildAt(0);

                    nodeValue = gxlString.getValue();

                    nodeValueHash = HashFunction.getHash(nodeValue);

                    Vertex v = new Vertex(nodeId, nodeValueHash);

                    graph_JGJ.addVertex(v);

                    arry_ref_vertex[v.getVertex_id()] = v;
                } else if (graphElement instanceof GXLEdge) {
                    gxlEdge = (GXLEdge) graphElement;

                /*System.out.println(" Edge : "+((GXLEdge) graphElement).getID()+
                        "| form: "+((GXLEdge) graphElement).getSourceID()+
                        "--> to : "+((GXLEdge) graphElement).getTargetID());*/

                    int nodeID_from = Integer.parseInt(((GXLEdge) graphElement).getSourceID());
                    int nodeID_to = Integer.parseInt(((GXLEdge) graphElement).getTargetID());

                    edgeValue = ((GXLInt) (gxlEdge.getAttrAt(0).getChildAt(0))).getIntValue();

                    if (arry_ref_vertex[nodeID_from] != null && arry_ref_vertex[nodeID_to] != null) {
                        Edge myEdge = graph_JGJ.addEdge(arry_ref_vertex[nodeID_from], arry_ref_vertex[nodeID_to]);

                        myEdge.setWeight(edgeValue);
                    }
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return graph_JGJ;
    }

    // CMU-GED Benchmark :  Vertex : 2 Label (double, double),  Edge Weight (double).
    //                       Id begin from 1 without any char before the ID.
    public static UndirectedGraph<Vertex, Edge> import_simple_graph_from_CMU_GED(String gxl_path_file)
    {
        UndirectedGraph<Vertex, Edge> graph_JGJ = new SimpleGraph(Edge.class);

        try {
            File gxlFile = new File(gxl_path_file);
            GXLDocument gxlDocument = new GXLDocument(gxlFile);


            GXLGraph graph_GXL = gxlDocument.getDocumentElement().getGraphAt(0);

            GXLNode gxlNode;
            GXLEdge gxlEdge;
            GXLAttr gxlAttr;
            GXLInt gxlInt;
            GXLString gxlString;
            GXLFloat gxlFloat;

            int nodeId;
            Float nodeValue_1;
            Float nodeValue_2;
            String nodeValue_1_2;
            int nodeValueHash;

            Float edgeValue;
            int edgeValueHash;

            int nb_element = graph_GXL.getGraphElementCount();

            //int nbNode=(int) Math.ceil((0.75)*(nb_element)); // on supose que le nombre de nodes est de 3/4 de toutes les elemeents
            //ArrayList<Vertex> arrayList = new ArrayList<Vertex>(nbNode);

            int nbNode = nb_element; // we supose that all element are node, because here we can't risk, unless we find a fast methode to know the number of nodes

            Vertex[] arry_ref_vertex = new Vertex[nbNode+1]; // +1 , beacause Node ID begin from 1. so to not each time do (nodeID-1)


            for (int i = 0; i < nb_element; i++)
            {
                GXLElement graphElement = graph_GXL.getGraphElementAt(i);

                if (graphElement instanceof GXLNode)
                {
                    gxlNode = ((GXLNode) graphElement);
                    nodeId = Integer.parseInt(gxlNode.getID());

                    gxlAttr = gxlNode.getAttrAt(0);
                    gxlFloat = (GXLFloat) gxlAttr.getChildAt(0);
                    nodeValue_1 = gxlFloat.getFloatValue();

                    gxlAttr = gxlNode.getAttrAt(1);
                    gxlFloat = (GXLFloat) gxlAttr.getChildAt(0);
                    nodeValue_2 = gxlFloat.getFloatValue();

                    nodeValue_1_2 = nodeValue_1+"_"+nodeValue_2;

                    nodeValueHash = HashFunction.getHash(nodeValue_1_2);

                    Vertex v = new Vertex(nodeId, nodeValueHash);

                    graph_JGJ.addVertex(v);

                    arry_ref_vertex[v.getVertex_id()] = v;
                }
                else if (graphElement instanceof GXLEdge)
                {
                    gxlEdge = (GXLEdge) graphElement;

                /*System.out.println(" Edge : "+((GXLEdge) graphElement).getID()+
                        "| form: "+((GXLEdge) graphElement).getSourceID()+
                        "--> to : "+((GXLEdge) graphElement).getTargetID());*/

                    int nodeID_from = Integer.parseInt(((GXLEdge) graphElement).getSourceID());
                    int nodeID_to = Integer.parseInt(((GXLEdge) graphElement).getTargetID());

                    //edgeValue = ((GXLInt) (gxlEdge.getAttrAt(0).getChildAt(0))).getIntValue();
                    edgeValue = ((GXLFloat) (gxlEdge.getAttrAt(0).getChildAt(0))).getFloatValue();

                    edgeValueHash = Double.hashCode(edgeValue);

                    if (arry_ref_vertex[nodeID_from] != null && arry_ref_vertex[nodeID_to] != null) {
                        Edge myEdge = graph_JGJ.addEdge(arry_ref_vertex[nodeID_from], arry_ref_vertex[nodeID_to]);

                        myEdge.setWeight(edgeValueHash);
                    }
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return graph_JGJ;
    }


    // GREC-GED Benchmark :  Vertex : 3 Label (Integer, Integer, String),  Edge 3 labels (Integer, String, String).
    //                       Id begin from 1 without any char before the ID.
    public static UndirectedGraph<Vertex, Edge> import_simple_graph_from_GREC_GED(String gxl_path_file)
    {
        UndirectedGraph<Vertex, Edge> graph_JGJ = new SimpleGraph(Edge.class);

        try {
            File gxlFile = new File(gxl_path_file);
            GXLDocument gxlDocument = new GXLDocument(gxlFile);


            GXLGraph graph_GXL = gxlDocument.getDocumentElement().getGraphAt(0);

            GXLNode gxlNode;
            GXLEdge gxlEdge;
            GXLAttr gxlAttr;
            GXLInt gxlInt;
            GXLString gxlString;
            GXLFloat gxlFloat;

            int nodeId;
            int nodeValue_1;
            int nodeValue_2;
            String nodeValue_3;
            String nodeValue_1_2_3;
            int nodeValueHash;

            int edgeValue_1;
            String edgeValue_2;
            String edgeValue_3;
            String edgeValue_1_2_3;
            int edgeValueHash;

            int nb_element = graph_GXL.getGraphElementCount();

            //int nbNode=(int) Math.ceil((0.75)*(nb_element)); // on supose que le nombre de nodes est de 3/4 de toutes les elemeents
            //ArrayList<Vertex> arrayList = new ArrayList<Vertex>(nbNode);

            int nbNode = nb_element; // we supose that all element are node, because here we can't risk, unless we find a fast methode to know the number of nodes

            Vertex[] arry_ref_vertex = new Vertex[nbNode+1]; // +1 , beacause Node ID begin from 1. so to not each time do (nodeID-1)


            for (int i = 0; i < nb_element; i++)
            {
                GXLElement graphElement = graph_GXL.getGraphElementAt(i);

                if (graphElement instanceof GXLNode)
                {
                    gxlNode = ((GXLNode) graphElement);
                    nodeId = Integer.parseInt(gxlNode.getID());

                    gxlAttr = gxlNode.getAttrAt(0);
                    gxlInt = (GXLInt) gxlAttr.getChildAt(0);
                    nodeValue_1 = gxlInt.getIntValue();

                    gxlAttr = gxlNode.getAttrAt(1);
                    gxlInt = (GXLInt) gxlAttr.getChildAt(0);
                    nodeValue_2 = gxlInt.getIntValue();

                    gxlAttr = gxlNode.getAttrAt(2);
                    gxlString = (GXLString) gxlAttr.getChildAt(0);
                    nodeValue_3 = gxlString.getValue();

                    nodeValue_1_2_3 = nodeValue_1+"_"+nodeValue_2+"_"+nodeValue_3;

                    nodeValueHash = HashFunction.getHash(nodeValue_1_2_3);

                    Vertex v = new Vertex(nodeId, nodeValueHash);

                    graph_JGJ.addVertex(v);

                    arry_ref_vertex[v.getVertex_id()] = v;
                }
                else if (graphElement instanceof GXLEdge)
                {
                    gxlEdge = (GXLEdge) graphElement;

                /*System.out.println(" Edge : "+((GXLEdge) graphElement).getID()+
                        "| form: "+((GXLEdge) graphElement).getSourceID()+
                        "--> to : "+((GXLEdge) graphElement).getTargetID());*/

                    int nodeID_from = Integer.parseInt(((GXLEdge) graphElement).getSourceID());
                    int nodeID_to = Integer.parseInt(((GXLEdge) graphElement).getTargetID());

                    edgeValue_1 = ((GXLInt) (gxlEdge.getAttrAt(0).getChildAt(0))).getIntValue();
                    edgeValue_2 = ((GXLString) (gxlEdge.getAttrAt(1).getChildAt(0))).getValue();
                    edgeValue_3 = ((GXLString) (gxlEdge.getAttrAt(2).getChildAt(0))).getValue();

                    edgeValue_1_2_3=edgeValue_1+"_"+edgeValue_2+"_"+edgeValue_3;

                    edgeValueHash = HashFunction.getHash(edgeValue_1_2_3);

                    if (arry_ref_vertex[nodeID_from] != null && arry_ref_vertex[nodeID_to] != null) {
                        Edge myEdge = graph_JGJ.addEdge(arry_ref_vertex[nodeID_from], arry_ref_vertex[nodeID_to]);

                        myEdge.setWeight(edgeValueHash);
                    }
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return graph_JGJ;
    }

}
