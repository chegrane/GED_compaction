
import graph_element.*;


public class Edit_Operation {
	
	private Vertex fromVertex; // From Vertex (in the operation)
	
	private Vertex toVertex; // To Vertex (in the operation)
	
	private GED_Operations operation_type; // Type of the operation
	
	/**
	 * Default Constructor
	 */
	
	@SuppressWarnings("unused")
	private Edit_Operation() {
		// Ensure non-instantiability (without arguments)
	}

	/**
	 * @param fromVertex
	 * @param toVertex
	 */
	public Edit_Operation(Vertex fromVertex, Vertex toVertex)
	{
		
		// assert the tow args not null 
		if(fromVertex == null && toVertex == null)
		{
			throw new IllegalArgumentException("illegal operation from"+ fromVertex +"to"+ toVertex);
		}
		else if(fromVertex == null)
		{
			this.operation_type = GED_Operations.Vertex_INSERTION;
		}
		else if(toVertex == null)
		{
			this.operation_type = GED_Operations.Vertex_DELETION;
		}
		else
		{
			this.operation_type = GED_Operations.Vertex_SUBSTITUTION;
		}
					
		// Set the vertices of the operation		
		this.fromVertex = fromVertex;
		this.toVertex = toVertex;
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
	public Vertex getFromVertex() {
		return fromVertex;
	}

	/**
	 * @return the toVertex
	 */
	public Vertex getToVertex()
	{
		return toVertex;
	}


	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
			return "(" + fromVertex + "-->" + toVertex + ")";
	}
}