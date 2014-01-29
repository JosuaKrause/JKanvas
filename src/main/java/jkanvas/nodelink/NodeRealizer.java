package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;

import jkanvas.animation.AnimatedPosition;

/**
 * Realizes the actual painting of nodes.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node type.
 */
public interface NodeRealizer<T extends AnimatedPosition> {

  /**
   * The shape of the depiction of the node. This is also the area that can be
   * clicked on. However the shape should not be smaller than the visual node.
   * 
   * @param node The node to draw.
   * @param x The x position to draw the node to.
   * @param y The y position to draw the node to.
   * @return The shape of the node.
   */
  Shape createNodeShape(T node, double x, double y);

  /**
   * Draws the node.
   * 
   * @param g The graphics context.
   * @param node The node that is drawn.
   */
  void drawNode(Graphics2D g, T node);

}
