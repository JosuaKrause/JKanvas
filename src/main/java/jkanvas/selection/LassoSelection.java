package jkanvas.selection;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import jkanvas.Canvas;

/**
 * A lasso selection.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class LassoSelection extends AbstractSelector {

  /**
   * Creates a lasso selection using default colors.
   * 
   * @param canvas The canvas the selection operates on.
   */
  public LassoSelection(final Canvas canvas) {
    super(canvas);
  }

  /**
   * Creates a lasso selection.
   *
   * @param canvas The canvas the selection operates on.
   * @param color The color.
   */
  public LassoSelection(final Canvas canvas, final Color color) {
    super(canvas, color);
  }

  /**
   * Creates a lasso selection.
   *
   * @param canvas The canvas the selection operates on.
   * @param inner The inner color.
   * @param outer The outer color.
   */
  public LassoSelection(final Canvas canvas, final Color inner, final Color outer) {
    super(canvas, inner, outer);
  }

  /**
   * Creates a lasso selection.
   *
   * @param canvas The canvas the selection operates on.
   * @param inner The inner color.
   * @param alphaInner The alpha value of the inner color.
   * @param outer The outer color.
   * @param alphaOuter The alpha value of the outer color.
   */
  public LassoSelection(final Canvas canvas, final Color inner, final double alphaInner,
      final Color outer, final double alphaOuter) {
    super(canvas, inner, alphaInner, outer, alphaOuter);
  }

  /** The selection path. */
  private Path2D path;

  @Override
  public Shape beginShape(final Point2D start, final Point2D cur) {
    path = new Path2D.Double();
    path.moveTo(start.getX(), start.getY());
    return growShape(start, cur);
  }

  @Override
  public Shape growShape(final Point2D start, final Point2D cur) {
    path.lineTo(cur.getX(), cur.getY());
    return path;
  }

}
