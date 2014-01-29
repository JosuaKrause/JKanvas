package jkanvas.selection;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;

/**
 * A rectangle selection.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class RectangleSelection extends AbstractSelector {

  /**
   * Creates a rectangular selection.
   * 
   * @param canvas The canvas the selection operates on.
   * @param color The color.
   */
  public RectangleSelection(final Canvas canvas, final Color color) {
    super(canvas, color);
  }

  /**
   * Creates a rectangular selection.
   * 
   * @param canvas The canvas the selection operates on.
   * @param inner The inner color.
   * @param outer The outer color.
   */
  public RectangleSelection(final Canvas canvas, final Color inner, final Color outer) {
    super(canvas, inner, outer);
  }

  /**
   * Creates a rectangular selection.
   * 
   * @param canvas The canvas the selection operates on.
   * @param inner The inner color.
   * @param alphaInner The alpha value of the inner color.
   * @param outer The outer color.
   * @param alphaOuter The alpha value of the outer color.
   */
  public RectangleSelection(final Canvas canvas, final Color inner,
      final double alphaInner, final Color outer, final double alphaOuter) {
    super(canvas, inner, alphaInner, outer, alphaOuter);
  }

  @Override
  public Shape growShape(final Point2D a, final Point2D b) {
    final double minX = Math.min(a.getX(), b.getX());
    final double minY = Math.min(a.getY(), b.getY());
    final double maxX = Math.max(a.getX(), b.getX());
    final double maxY = Math.max(a.getY(), b.getY());
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

}
