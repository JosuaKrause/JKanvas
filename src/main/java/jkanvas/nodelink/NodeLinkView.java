package jkanvas.nodelink;

import jkanvas.animation.Position2D;

/**
 * Provides a view on a graph.
 * 
 * @author Joschi <josua.krause@googlemail.com>
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
   * Returns all edges from the given node. Note that this contains only nodes
   * with a higher id.
   * 
   * @param from The starting id.
   * @return All connected edges with a higher id.
   */
  Iterable<Integer> edgesTo(int from);

}
