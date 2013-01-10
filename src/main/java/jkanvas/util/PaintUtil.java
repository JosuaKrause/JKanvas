package jkanvas.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Utility methods for painting.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class PaintUtil {

  /** Hidden default constructor. */
  private PaintUtil() {
    throw new AssertionError();
  }

  /**
   * Creates a rectangle to draw a point.
   * 
   * @param pos The point.
   * @param size The size of the point.
   * @return The rectangle.
   */
  public static Rectangle2D pixel(final Point2D pos, final double size) {
    final double s2 = size * 0.5;
    return new Rectangle2D.Double(pos.getX() - s2, pos.getY() - s2, size, size);
  }

  /**
   * Creates a rectangle to draw a point.
   * 
   * @param pos The point.
   * @return The rectangle.
   */
  public static Rectangle2D pixel(final Point2D pos) {
    return pixel(pos, 1);
  }

  /**
   * Adds padding to a rectangle.
   * 
   * @param rect The rectangle.
   * @param padding The padding.
   * @return The new rectangle.
   */
  public static Rectangle2D addPadding(final Rectangle2D rect, final double padding) {
    final double p2 = padding * 2;
    return new Rectangle2D.Double(rect.getX() - padding, rect.getY() - padding,
        rect.getWidth() + p2, rect.getHeight() + p2);
  }

}
