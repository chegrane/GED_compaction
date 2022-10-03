
import graph_element.*;


public class Edit_Operation {
	
	private int idx_fromVertex; // the index of the Vertex (in the list of vertices g1)
	
	private int idx_toVertex; // the index of the Vertex (in the list of vertices g2)
	
	private GED_Operations operation_type; // Type of the operation
	
	/**
	 * Default Constructor
	 */
	
	@SuppressWarnings("unused")
	private Edit_Operation() {
		// Ensure non-instantiability (without arguments)
	}


	public Edit_Operation(int idx_fromVertex, int idx_toVertex)
	{
		
		// assert the tow args not null 
		if(idx_fromVertex == -1 && idx_toVertex == -1)
		{
			throw new IllegalArgumentException("illegal operation from"+ idx_fromVertex +"to"+ idx_toVertex);
		}
		else if(idx_fromVertex == -1)
		{
			this.operation_type = GED_Operations.Vertex_INSERTION;
		}
		else if(idx_toVertex == -1)
		{
			this.operation_type = GED_Operations.Vertex_DELETION;
		}
		else
		{
			this.operation_type = GED_Operations.Vertex_SUBSTITUTION;
		}
					
		// Set the vertices of the operation		
		this.idx_fromVertex = idx_fromVertex;
		this.idx_toVertex = idx_toVertex;
	}
	

	/**
	 * @return the operation_type
	 */
	public GED_Operations getOperation_type() {
		return operation_type;
	}

	/**
	 * @return the fromVertex
	 */
	public int get_idx_FromVertex() {
		return this.idx_fromVertex;
	}



	/**
	 * idx_fromVertex must be != -1
	 * we have to
	 * either : 1) add a test in this method,
	 * or     : 2) when we call this method we ensure that (idx_fromVertex!=-1)
	 *
	 * in our code, we chose the second method because
	 * i) to avoid the test which result in additional execution time, because we call this method frequently.
	 * ii) we can ensure from out side the existence of the vertex.
	 *
	 * @return the object Vertex of the actual idx of the fromVertex.
	 */
	public Vertex get_FromVertex()
	{
	    /* 1) check if the from vertex exit (!=-1)
	    if(is_fromVertex_exist())
		    return Graph_Map.list_vertices_g1.get(this.idx_fromVertex);

	    return null;
	    */

	    // 2) without checking, because the check step is done outside before calling this method
        return Graph_Map.list_vertices_g1.get(this.idx_fromVertex);
	}

	/**
	 * @return the toVertex
	 */
	public int get_idx_ToVertex()
	{
		return this.idx_toVertex;
	}

	public Vertex get_ToVertex() // idx_toVertex must be != -1
	{
		return Graph_Map.list_vertices_g2.get(this.idx_toVertex);
	}

	public boolean is_fromVertex_exist() // for Insertion : fromVertex = null or -1
	{
		return (idx_fromVertex!=-1);
	}

	public boolean is_toVertex_exist() // for Deletion : toVertex = null or -1
	{
		return (idx_toVertex!=-1);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String str="(";

		if(this.idx_fromVertex ==-1)
			str +="null";
		else str += Graph_Map.list_vertices_g1.get(idx_fromVertex);

		str+="-->";

		if(this.idx_toVertex ==-1)
			str +="null";
		else str += Graph_Map.list_vertices_g2.get(idx_toVertex);

		str+=")";

		return str;
	}
}