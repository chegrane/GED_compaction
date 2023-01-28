package import_export_graph; /**
 * 
 */


import graph_element.Edge;
import graph_element.Vertex;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Said
 *
 */
public final class GraphLoader {

	/**
	 * Separator
	 */
	private static String separator = ImportExportFormat.separator;
	
	/**
	 * Nodes Edges separator
	 */
	private static String nodesEdgesSeparator = ImportExportFormat.nodesEdgesSeparator;
	
	/**
	 * The start of comment
	 */
	private static String startComment = ImportExportFormat.startComment;
	
	/**
	 * Map that contains tuples of form (idVertex, treeVertex)
	 */
	private static final Map<Integer, Vertex> idMap = new HashMap<Integer, Vertex>();
	
	
	/**
     *
     */
    private GraphLoader()
    {
    }
	
    /**
     * 
     * @param filePath the File path to load
     * @return the Tree Graph loaded
     */
    public static UndirectedGraph<Vertex, Edge> load(String filePath)
    {
        File file = new File(filePath);

    	UndirectedGraph<Vertex, Edge> graph = new SimpleGraph(Edge.class);
    	
		try {
			// Open the file
			//File file = new File("graph_1.txt");
						
			FileInputStream fInStream = new FileInputStream(file);
		
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fInStream);
    	
			graph  = load(in);
    	
			//Close the input stream
			in.close();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
    	
    	return graph;
    }    	
 
    /**
     * 
     * @param in the InputStream Object
     * @return the Tree Graph loaded
     */
    public static UndirectedGraph<Vertex, Edge> load(InputStream in)
    {
        idMap.clear();

    	UndirectedGraph<Vertex, Edge> graph = new SimpleGraph(Edge.class);

    	try {    		
    		BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
    		// String Line
    		String strLine;
    		boolean isStartedVertexParse = false;
    		boolean isStartedEdgeParse = false;
    		int lineNumber = 0;
    		boolean exitError = false;
    		
    		//Read File Line By Line			
			while ((strLine = br.readLine()) != null && !exitError)
			{
				// Print the content the InputStream
				//System.out.println (strLine);	
				// Begin construction of the TreeGraph object
				//...
				lineNumber++;
				strLine = strLine.trim();				
				
				/// Skip comment
				if(strLine.indexOf(startComment) == 0) // Start of comment 
				 continue;
				
				/// Skip blank line (Before vertex parse)				
				if(strLine.isEmpty() && !isStartedVertexParse)
					continue;
				
				if(!strLine.equals((nodesEdgesSeparator).trim())  && !isStartedEdgeParse)
				{	
					/// Parse vertex
					isStartedVertexParse = true;
					boolean is_vertex_added = parseAddVertex(strLine, graph);

					if(!is_vertex_added)
					{
						error("ERROR(addVertex: exist): Parsing error at ligne "+lineNumber+" --> "+strLine);
						exitError = true;
					}
				}else
				{/// Nodes/Edges Separator
					 // Then, skip it and begin edge parse
					if(!isStartedEdgeParse)
					{
						isStartedEdgeParse = true;
						continue;
					}
				}
				
				///// Block of edges
				if(isStartedEdgeParse)
				{
					/// Skip blank lines				
					if(strLine.isEmpty()) continue;
					
					/// Parse edge 
					Edge e = parseAddEdge(strLine, graph);
					// graph_element.Edge e is already added to the tree if it is not null!
					if(e == null){	
						error("ERROR(parseAddEdge: exist): Parsing error at ligne "+lineNumber+" --> "+strLine);
						exitError = true;
					}					
				}
						
				// Test
				//System.out.println (strLine);				
			  
			}
			
			///// Exit because parsing error!!!				
			if(exitError)
			{	/// Discard all!!!
				graph = null;
			}
    	
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return graph;
    }
    
	/**
	 * Auxiliary method to report errors
	 */
    private static void error(String msg) {
		// TODO Auto-generated method stub
		System.out.println(msg);
	}


	/**
     * 
     * @param strLine line to parse
     * @param graph
     * @return return a vertex
     */    
	private static boolean parseAddVertex(String strLine, UndirectedGraph<Vertex, Edge> graph) {
		// TODO Auto-generated method stub
		Vertex v = null;
		boolean is_Vertex_Added=false;
		String auxStr;
		Integer id;
		String label;
		
		// Delete comment if any		
		if(strLine.indexOf(startComment)>0)		// Rem: when equels 0, it's a comment!
			auxStr = strLine.substring(0, strLine.indexOf(startComment)).trim();
		else
			auxStr = strLine.trim();
		
		/// Split into (id, label)
		if(auxStr.indexOf(separator)>0)
		{
			id = Integer.valueOf(auxStr.substring(0, auxStr.indexOf(separator)).trim());
			label = auxStr.substring(auxStr.indexOf(separator)+1, auxStr.length()).trim();
			
			// Create vertex v
			v = new Vertex(label);
			// Add v and it's id to idMap
			idMap.put(id, v);

			is_Vertex_Added = graph.addVertex(v);
		}
		
		return is_Vertex_Added;
	}
	
	/**
	 * 
	 * @param strLine line to parse
	 * @returnreturn a vertex
	 */
	private static Edge parseAddEdge(String strLine, UndirectedGraph<Vertex, Edge> graph) {
		// TODO Auto-generated method stub
		Edge e = null;
		String auxStr;
		Integer idSource;
		Integer idTarget;
		
		// Delete comment if any		
		if(strLine.indexOf(startComment)>0)		// Rem: when equals 0, it's a comment!
			auxStr = strLine.substring(0, strLine.indexOf(startComment)).trim();
		else
			auxStr = strLine.trim();
		
		/// Split into (idSource, idTarget)
		if(auxStr.indexOf(separator)>0)
		{
			idSource = Integer.valueOf(auxStr.substring(0, auxStr.indexOf(separator)).trim());
			idTarget = Integer.valueOf(auxStr.substring(auxStr.indexOf(separator)+1, auxStr.length()).trim());

			// Add edge e to tree
			e = graph.addEdge(idMap.get(idSource), idMap.get(idTarget));
		}
	
		return e;
	}

	
	/**
	 * @param args
	 */
/*	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		File file = new File("graph_1.txt");
		
		GraphLoader treeGL = new GraphLoader();
		
		UndirectedGraph<TreeVertex, DefaultEdge> tree = treeGL.load(file);				
		if(tree!=null) System.out.println(tree.toString());
		
	}*/

}
