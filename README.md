# Graph Edit Distance Compacted Search Tree (GED_compaction)
 
## Abstract
We propose two methods to compact the used search tree during the graph edit distance (GED) computation. The first maps the node information and encodes the different edit operations by numbers and the needed remaining vertices and edges by BitSets. The second represents the tree succinctly by bit-vectors. The proposed methods require 24 to 250 times less memory than traditional versions without negatively influencing the running time.

## Keywords
Graph Edit Distance (GED) | Compacted GED search space.
 
**Papaer link:** https://link.springer.com/chapter/10.1007/978-3-031-17849-8_14

## Eight (08) implementations:
### A-star:
* **π΄ β π π‘ππ**: in the sub-folder Exact_GED_AStar
* **π΄ β πΈπππ**: in the sub-folder Exact_GED_AStar_store_Edge
* **π΄ β _πΆπ**: in the sub-folder Exact_GED_AStar_Tree
* **π΄ β _πΆπ΅**: in the sub-folder Exact_GED_AStar_BitSet
### ASBB: A-Star with Branch and Bound
* **π΄ππ΅π΅**: in the sub-folder Compacted_Exact_GED_AStar_BB_IN
* **π΄ππ΅π΅ πΈπππ**: in the sub-folder Compacted_Exact_GED_AStar_BB_IN_Store_Edge 
* **π΄ππ΅π΅_πΆπ**: in the sub-folder Compacted_Exact_GED_ASBB_IN_Tree
* **π΄ππ΅π΅_πΆπ΅**: in the sub-folder Compacted_Exact_GED_ASBB_IN_BitSet
