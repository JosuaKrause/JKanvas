package jkanvas.nodelink;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;

import jkanvas.animation.AnimatedPosition;
import jkanvas.util.PaintUtil;

/**
 * A default implementation for a node realizer.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public class DefaultNodeRealizer<T extends AnimatedPosition> implements NodeRealizer<T> {

  /** The node color. */
  public static final Color COLOR = new Color(247, 247, 247);

  /** The node border color. */
  public static final Color BORDER = Color.BLACK;

  /** The default node radius. */
  public static final double RADIUS = 20.0;

  /** A stroke with width one. */
  public static final BasicStroke STROKE = new BasicStroke(1f);

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The color for the given node. The default implementation uses
   *         {@link #COLOR}.
   */
  public Color getColor(@SuppressWarnings("unused") final T node) {
    return COLOR;
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The color for the border of the given node. The default
   *         implementation uses {@link #BORDER}.
   */
  public Color getBorder(@SuppressWarnings("unused") final T node) {
    return BORDER;
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The radius of the given node. The default implementation uses
   *         {@link #RADIUS}. Note that this method should return same values
   *         for same nodes.
   */
  public double getRadius(@SuppressWarnings("unused") final T node) {
    return RADIUS;
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The stroke of the given node. The default implementation uses
   *         {@link #STROKE}. Note that this method should return same values
   *         for same nodes.
   */
  public BasicStroke getStroke(@SuppressWarnings("unused") final T node) {
    return STROKE;
  }

  @Override
  public Shape createNodeShape(final T node, final double x, final double y) {
    final double r = getRadius(node) + getStroke(node).getLineWidth() * 0.5;
    return PaintUtil.createCircle(x, y, r);
  }

  @Override
  public void drawNode(final Graphics2D g, final T node) {
    final Point2D pos = node.getPos();
    g.setColor(getColor(node));
    final Shape s = PaintUtil.createCircle(pos.getX(), pos.getY(), getRadius(node));
    g.fill(s);
    g.setColor(getBorder(node));
    g.fill(getStroke(node).createStrokedShape(s));
  }

}
