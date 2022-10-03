package import_export_graph; /**
 * Define some format conventions to Exported an Imported Tree Graph files
 */

/**
 * @author Said
 *
 */
public interface ImportExportFormat {

	/**
	 * Delimiter
	 */
	public static final String separator = " "; 
	
	/**
	 * Nodes Edges separator
	 */
	public static final String nodesEdgesSeparator = "\n"; // The default separator (always trimed!)
	
	/**
	 * Start of comment
	 */
	public static final String startComment = "#";
}
