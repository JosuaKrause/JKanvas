package jkanvas.nodelink;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import jkanvas.animation.AnimatedPosition;
import jkanvas.util.PaintUtil;

/**
 * A default implementation of an edge realizer.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node type.
 */
public class DefaultEdgeRealizer<T extends AnimatedPosition> implements EdgeRealizer<T> {

  @Override
  public Shape createLineShape(final T from, final T to) {
    return PaintUtil.createLine(from.getX(), from.getY(), to.getX(), to.getY(), 1.0);
  }

  /**
   * Getter.
   * 
   * @param from The starting node.
   * @param to The ending node.
   * @return The color of the edge. The default implementation returns
   *         {@link Color#BLACK}.
   */
  public Color getColor(@SuppressWarnings("unused") final T from,
      @SuppressWarnings("unused") final T to) {
    return Color.BLACK;
  }

  @Override
  public void drawLines(final Graphics2D g, final Shape edgeShape,
      final T from, final T to) {
    g.setColor(getColor(from, to));
    g.fill(edgeShape);
  }

}
