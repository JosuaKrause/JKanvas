package jkanvas.nodelink;

/**
 * A view on a graph.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface GraphView {

  /**
   * Getter.
   * 
   * @return The number of nodes.
   */
  int nodeCount();

  /**
   * Getter.
   * 
   * @param index The index of the node.
   * @return The name of the node.
   */
  String getName(int index);

  /**
   * Whether two nodes are connected. The order of the nodes is only important
   * when the graph is directed.
   * 
   * @param a One node.
   * @param b Another node.
   * @return Whether both nodes are connected via an edge.
   */
  boolean areConnected(int a, int b);

  /**
   * Getter.
   * 
   * @return Whether the graph is directed.
   */
  boolean isDirected();

}
