package jkanvas.animation;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

/**
 * A point list with circles to represent points.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class CircleList extends PointList<Ellipse2D> {

  /**
   * Creates a circle list with initial size.
   * 
   * @param initialSize The initial size.
   * @param defaultColor The default filling color.
   * @param defaultBorder The default border color.
   */
  public CircleList(final int initialSize,
      final Color defaultColor, final Color defaultBorder) {
    super(initialSize, defaultColor, defaultBorder);
  }

  @Override
  protected Ellipse2D createDrawObject() {
    return new Ellipse2D.Double();
  }

  @Override
  protected void setShape(final Ellipse2D circle, final int index,
      final double x, final double y, final double s) {
    circle.setFrame(x - s, y - s, s * 2.0, s * 2.0);
  }

}
