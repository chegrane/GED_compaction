import java.util.Comparator;

/**
 * Class Comparator: to compare two paths according to their g+h cost
 *
 */
public class Path_Cost_Comparator implements Comparator<Path_Node>{

	@Override
	public int compare(Path_Node p1, Path_Node p2)
	{
		if( p1.getG_cost_PLUS_h_cost() < p2.getG_cost_PLUS_h_cost() )
		{
			return -1;
		}
		
		if( p1.getG_cost_PLUS_h_cost() > p2.getG_cost_PLUS_h_cost() )
		{
			return 1;
		}
			
		return 0;
	}
}
