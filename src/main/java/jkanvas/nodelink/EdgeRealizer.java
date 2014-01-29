package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;

import jkanvas.animation.AnimatedPosition;

/**
 * Realizes the actual painting of edges.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node type.
 */
public interface EdgeRealizer<T extends AnimatedPosition> {

  /**
   * The shape of the edge between nodes. The order of the nodes is not defined
   * when the underlying graph is not directed. The shape should not be smaller
   * than the visible edge.
   * 
   * @param from The source node.
   * @param to The sink node.
   * @return The shape of the edge.
   */
  Shape createLineShape(T from, T to);

  /**
   * Draws edges. The order of the nodes is not defined when the underlying
   * graph is not directed.
   * 
   * @param g The graphics context.
   * @param edgeShape The computed edge shape. As returned by
   *          {@link #createLineShape(AnimatedPosition, AnimatedPosition)}.
   * @param from The source node.
   * @param to The sink node.
   */
  void drawLines(Graphics2D g, Shape edgeShape, T from, T to);

}
