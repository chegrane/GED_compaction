/**
 * Created by user on 13/06/2017.
 *
 * Modified : 03/04/2018.
 *
 * Compute an effective valid path : by the best assignment of vertices plus the implied edges.
 *
 * This total of this path represent a valid Upper Bound.
 */
public class NewH_function {

    private static String min_OR_max="min";
    private static  int Value_Min_Max=Integer.MAX_VALUE; // Integer.MAX_VALUE if wa calculate min cost, or Integer.MIN_VALUE if max cost;

    private NewH_function() {
    }


    public final static Path_Node compute()
    {
        Path_Node father_path=new Path_Tree();
        Path_Node child_path=father_path; // at the beginning the father and the child are in the same place
        // if child_path to null, and the graph is empty, so we will return null, and bug :)

        // we call this method only once at the beginning (1)
        // and we use only one path that contains one list of numbers (2)
        // from 1 and 2 : we can either choose a path : BitSet or Tree
        // the two data structure can resize one a new element is added.
        // I : choose the Tree,
        //    for the bitSet, when the path size is big, it will take more space than ArrayList in the Tree class.
        //  but with same argument we can say that with small path BitSet is better :p
        // but is still choose Tree.
        // because each time with BitSet we decide the size needed for it. But with ArrayList we let the change dynamically.


        int n = Graph_Map.nb_vertices_g1;// get the number of vertices in g1
        int m = Graph_Map.nb_vertices_g2;// get the number of vertices in g2

        double[][] cost_matrix;



        // if the tow sets exist, then we can execute the hangarian algo
        if(n>0 && m>0)
        {
            cost_matrix= NewH_function.create_cost_matrix_vertices(n,m);

            int[][] assignment = new int[cost_matrix.length][2];
            assignment = HungarianAlgorithm.hgAlgorithm(cost_matrix, min_OR_max);	//Call Hungarian algorithm.


            int i1;
            int j1;
            for (int i=0; i<assignment.length; i++)
            {
                i1=assignment[i][0]; // Row
                j1=assignment[i][1]; // column

                child_path = new Path_Tree(father_path);

                if(i1<n && j1<m) // Upper left == SUBSTITUTION : substitution vertex i1 with vertex j1
                {
                    Edit_Operation vertex_substitution_operation = new Edit_Operation(i1, j1);
                    child_path.add_to_real_path(vertex_substitution_operation, Graph_Map.get_idx_in_map_sub(i1, j1));
                }
                else if(i1<n && (j1>=m && j1<m+n)) // Upper right == DELETION : delete the vertex i1 from g1
                {
                    Edit_Operation vertex_deletion_operation = new Edit_Operation(i1, -1);
                    child_path.add_to_real_path(vertex_deletion_operation, Graph_Map.get_idx_in_map_del(i1));
                }
                else if((i1>=n && i1<n+m) && j1<m) // Bottom left == insertion : insert the vertex j1 in g2
                {
                    Edit_Operation newOp3 = new Edit_Operation(-1, j1);
                    child_path.add_to_real_path(newOp3, Graph_Map.get_idx_in_map_insert(j1));
                }
                else if( (i1>=n && i1<n+m) && (j1>=m && j1<m+n)) // Bottom right == delete-->delete : no operation
                {
                    //System.out.println(" delete delete ");
                }

                father_path = child_path;
            }
        }
        else // at this level, either n <=0  or m <=0
        {
            // The cost of max{0, n-m} node deletions and max{0, m-n} node insertions
            /// h_Vertices_Cost =  Math.max(0, n-m)* GED_Operations_Cost.getVertex_deletion_cost() + Math.max(0, m-n)* GED_Operations_Cost.getVertex_insertion_cost();

            if(n>0) // g1: n , it's mean we delete all n vertex of g1
            {
                for(int i = 0; i< Graph_Map.nb_vertices_g1; i++)
                {
                    child_path = new Path_Tree(father_path);

                    Edit_Operation vertex_deletion_operation = new Edit_Operation(i, -1);
                    child_path.add_to_real_path(vertex_deletion_operation, Graph_Map.get_idx_in_map_del(i));

                    father_path = child_path;
                }
            }

            if(m>0) // g2: m , it's mean, we insert all m vertex of g2
            {
                for(int j = 0; j< Graph_Map.nb_vertices_g2; j++)
                {
                    child_path = new Path_Tree(father_path);

                    Edit_Operation newOp3 = new Edit_Operation(-1, j);
                    child_path.add_to_real_path(newOp3, Graph_Map.get_idx_in_map_insert(j));

                    father_path = child_path;
                }
            }
        }

        return child_path;
    }


    //  cost_matrix :
    //
    //  g1 : n
    //  g2 : m
    //            m          n
    //      +-----------+-----------+
    //      |           |           |
    //  n   |    Sub    |   Del     |
    //      |           |           |
    //      +-----------+-----------+
    //      |           |           |
    //  m   |   Insr    | Del-Del   |
    //      |           |           |
    //      +-----------+-----------+
    //
    // all operation are made from g1 to g2, (substitute,delete, insert): element of g1 (with,from,in) g2
    //
    // Upper left == SUBSTITUTION : (elements of g1:n) X (elements of g2:m)
    //
    // Upper right == DELETION : (all elements of g1:n), delete each elements of g1,  (its like a substitution of n element g1 with n epsilon)
    // we put the cost of each element on the diagonal (for the sake of mankers algorithm),
    // because if we put them on the same column, when mankers algo choose one element, all the elements on the same column will be exclude.
    // so, tu insure that each element can be chosen, we put in separate column
    // only diagonal are filled by cost, others case of the matrix (upper right) ar filed by Value_Min_Max
    //
    // Bottom left == insertion: (insert element of g2:m), its mean from g1 we insert all element of g2.
    // also, each element is inserted in the diagonal.
    // only diagonal are filled by cost, others case of the matrix (Bottom left) ar filed by Value_Min_Max
    //
    // Bottom right == Delete --> Delete  (substitution of empty (deleted) elements of g1 with empty (deleted) elements of g2)
    // The matrix (Bottom right) ar filed by 0, since the substitution of the form (empty-->empty) should not cause any cost.
    //
    // Value_Min_Max = +Infinity   if we compute the MIN assignment, (the assignment with the minimum cost)
    //                              like this, those region will not influence for the chose of assignment
    //                              because the value is +Infinity, so Mankers algo will not choose the element.
    //
    // Value_Min_Max = -Infinity    if we compute the Max assignment
    //                               same explanation (but in reverse :)
    //
    // ps : the matrix is always square (n+m)x2 , so when we apply mankers algo we don't need to check if (rows>column) to transpose the matrix.


    private static double[][] create_cost_matrix_vertices(int Nb_element_first_set, int Nb_elements_second_set)
    {

        int n = Nb_element_first_set;
        int m = Nb_elements_second_set;

        // create the cost matrix:
        double[][] cost_matrix = new double[n + m][n + m];

        // Upper left == SUBSTITUTION :  == n x m == i x j
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                cost_matrix[i][j] = GED_Operations_Cost.getVertex_substitution_cost(i, j);
            }
        }


        // Upper right == DELETION :  == (n X n) : n x (m + n) = i x m+j
        for (int i = 0; i < n; i++)
        {
            for (int j = 0 + m; j < m + n; j++)
            {
                if(j==i+m) // check the diagonal element
                {
                    cost_matrix[i][j] = GED_Operations_Cost.getVertex_deletion_cost();
                }
                else
                {
                    cost_matrix[i][j] = Value_Min_Max;
                }
            }
        }

        // Bottom left == insertion :  == (m X m): (n + m) x + m = i+n x j
        for (int i = 0 + n; i < n + m; i++) {
            for (int j = 0; j < m; j++)
            {
                if(i==j+n) // check the diagonal element
                {
                    cost_matrix[i][j] = GED_Operations_Cost.getVertex_insertion_cost();
                }
                else
                {
                    cost_matrix[i][j] = Value_Min_Max;
                }
            }
        }

        // Bottom right == delete-->delete :  ==   (m X n): n+m x m+n = n+i x m+j
        for (int i = 0 + n; i < n + m; i++) {
            for (int j = 0 + m; j < m + n; j++) {
                cost_matrix[i][j] = 0;
            }
        }

        return cost_matrix;

    }


    private static void print_cost_matrix_at_once(double[][] cost_matrix)
    {

        if(cost_matrix==null) return;

        System.out.println("------------------------ Displaying the cost matrix at once: nx2 x mx2 ");

        for (int i = 0; i < cost_matrix.length; i++) {
            for (int j = 0; j < cost_matrix[0].length; j++) {
                System.out.print(cost_matrix[i][j] + "\t");
            }

            System.out.println();
        }
    }

}
