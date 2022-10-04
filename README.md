# Graph Edit Distance Compacted Search Tree (GED_compaction)
 
## Abstract
We propose two methods to compact the used search tree during the graph edit distance (GED) computation. The first maps the node information and encodes the different edit operations by numbers and the needed remaining vertices and edges by BitSets. The second represents the tree succinctly by bit-vectors. The proposed methods require 24 to 250 times less memory than traditional versions without negatively influencing the running time.

## Keywords
Graph Edit Distance (GED) | Compacted GED search space.
 
**Papaer link:** https://link.springer.com/chapter/10.1007/978-3-031-17849-8_14

## Eight (08) implementations:
### A-star:
* **𝐴 − 𝑠𝑡𝑎𝑟**: in the sub-folder Exact_GED_AStar
* **𝐴 ∗ 𝐸𝑑𝑔𝑒**: in the sub-folder Exact_GED_AStar_store_Edge
* **𝐴 ∗ _𝐶𝑇**: in the sub-folder Exact_GED_AStar_Tree
* **𝐴 ∗ _𝐶𝐵**: in the sub-folder Exact_GED_AStar_BitSet
### ASBB: A-Star with Branch and Bound
* **𝐴𝑆𝐵𝐵**: in the sub-folder Compacted_Exact_GED_AStar_BB_IN
* **𝐴𝑆𝐵𝐵 𝐸𝑑𝑔𝑒**: in the sub-folder Compacted_Exact_GED_AStar_BB_IN_Store_Edge 
* **𝐴𝑆𝐵𝐵_𝐶𝑇**: in the sub-folder Compacted_Exact_GED_ASBB_IN_Tree
* **𝐴𝑆𝐵𝐵_𝐶𝐵**: in the sub-folder Compacted_Exact_GED_ASBB_IN_BitSet
