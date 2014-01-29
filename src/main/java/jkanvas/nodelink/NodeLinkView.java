package jkanvas.nodelink;

import jkanvas.animation.Position2D;

/**
 * Provides a view on a graph with enumerable edges and nodes.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public interface NodeLinkView<T extends Position2D> extends GraphView {

  /**
   * Getter.
   * 
   * @param index The id of the node.
   * @return The corresponding node.
   */
  T getNode(int index);

  /**
   * Getter.
   * 
   * @return The nodes in proper order.
   */
  Iterable<T> nodes();

  /**
   * Returns all edges from the given node. Note that when the graph is
   * undirected this contains only nodes with a higher id.
   * 
   * @param from The starting id.
   * @return All connected edges (with a higher id when undirected).
   */
  Iterable<Integer> edgesFrom(int from);

}
