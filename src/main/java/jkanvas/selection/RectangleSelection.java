package jkanvas.selection;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;
import jkanvas.KanvasContext;

/**
 * A rectangle selection.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class RectangleSelection extends AbstractSelector {

  /** The inner alpha value. */
  private final float alphaInner;

  /** The outer alpha value. */
  private final float alphaOuter;

  /** The inner color. */
  private final Color inner;

  /** The outer color. */
  private final Color outer;

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
    super(canvas);
    this.inner = inner;
    this.alphaInner = (float) alphaInner;
    this.outer = outer;
    this.alphaOuter = (float) alphaOuter;
  }

  /** The displayed selection. */
  private Rectangle2D selection;

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    if(selection == null) return;
    final Graphics2D g = (Graphics2D) gfx.create();
    final Graphics2D gInner = (Graphics2D) g.create();
    gInner.setColor(inner);
    gInner.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaInner));
    gInner.fill(selection);
    gInner.dispose();
    final Graphics2D gOuter = (Graphics2D) g.create();
    gOuter.setColor(outer);
    gOuter.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaOuter));
    gOuter.draw(selection);
    gOuter.dispose();
    g.dispose();
  }

  @Override
  public abstract boolean acceptDragHUD(Point2D p, MouseEvent e);

  /**
   * Computes the rectangle from the given points.
   * 
   * @param a One corner.
   * @param b The other corner.
   * @return The rectangle.
   */
  private static Rectangle2D computeRect(final Point2D a, final Point2D b) {
    final double minX = Math.min(a.getX(), b.getX());
    final double minY = Math.min(a.getY(), b.getY());
    final double maxX = Math.max(a.getX(), b.getX());
    final double maxY = Math.max(a.getY(), b.getY());
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  @Override
  public void dragHUD(final Point2D start, final Point2D cur, final double dx,
      final double dy) {
    selection = computeRect(start, cur);
    doSelection(selection, true);
  }

  @Override
  public void endDragHUD(final Point2D start, final Point2D end, final double dx,
      final double dy) {
    doSelection(computeRect(start, end), false);
    selection = null;
  }

}
