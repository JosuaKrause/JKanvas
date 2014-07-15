package jkanvas.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.painter.HUDRenderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.util.PaintUtil;

/**
 * Allows for arbitrary shaped selections.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class AbstractSelector extends HUDRenderpass {

  /** The inner alpha value. */
  private final float alphaInner;

  /** The outer alpha value. */
  private final float alphaOuter;

  /** The inner color. */
  private final Color inner;

  /** The outer color. */
  private final Color outer;

  /** The canvas to select on. */
  private final Canvas canvas;

  /** The list of selectable render passes. */
  private final List<Selectable> selects = new ArrayList<>();

  /**
   * Creates an abstract selector.
   *
   * @param canvas The canvas the selector operates on.
   * @param color The color.
   */
  public AbstractSelector(final Canvas canvas, final Color color) {
    this(canvas, color, 1, color, 0.6);
  }

  /**
   * Creates an abstract selector with default colors.
   * 
   * @param canvas The canvas the selector operates on.
   */
  public AbstractSelector(final Canvas canvas) {
    this(canvas, new Color(0xa6bddb), 0.6, new Color(0x2b8cbe), 1);
  }

  /**
   * Creates an abstract selector.
   *
   * @param canvas The canvas the selector operates on.
   * @param inner The inner color.
   * @param outer The outer color.
   */
  public AbstractSelector(final Canvas canvas, final Color inner, final Color outer) {
    this(canvas, inner, 1, outer, 1);
  }

  /**
   * Creates an abstract selector.
   *
   * @param canvas The canvas the selector operates on.
   * @param inner The inner color.
   * @param alphaInner The alpha value of the inner color.
   * @param outer The outer color.
   * @param alphaOuter The alpha value of the outer color.
   */
  public AbstractSelector(final Canvas canvas, final Color inner,
      final double alphaInner, final Color outer, final double alphaOuter) {
    this.canvas = Objects.requireNonNull(canvas);
    Objects.requireNonNull(inner);
    Objects.requireNonNull(outer);
    final float[] alpha = new float[1];
    this.inner = PaintUtil.noAlpha(inner, alpha);
    this.alphaInner = (float) (alphaInner * alpha[0]);
    this.outer = PaintUtil.noAlpha(outer, alpha);
    this.alphaOuter = (float) (alphaOuter * alpha[0]);
  }

  /** The displayed selection. */
  private Shape selection;

  @Override
  public void drawHUD(final Graphics2D g, final KanvasContext ctx) {
    if(selection == null) return;
    draw(g, inner, alphaInner, true);
    draw(g, outer, alphaOuter, false);
  }

  /**
   * Draws the selection.
   *
   * @param gfx The graphics context.
   * @param color The color.
   * @param alpha The alpha value.
   * @param fill Whether to fill the shape or draw the outlines.
   */
  private void draw(final Graphics2D gfx, final Color color,
      final float alpha, final boolean fill) {
    if(alpha <= 0.0) return;
    final Graphics2D g = (Graphics2D) gfx.create();
    g.setColor(color);
    PaintUtil.setAlpha(g, alpha);
    if(fill) {
      g.fill(selection);
    } else {
      g.draw(selection);
    }
    g.dispose();
  }

  /**
   * Setter.
   *
   * @param isActive Whether this selector is active.
   */
  public void setActive(final boolean isActive) {
    setVisible(isActive);
  }

  /**
   * Getter.
   *
   * @return Whether this selector is active.
   */
  public boolean isActive() {
    return isVisible();
  }

  /**
   * Adds a selectable render pass. If the render pass is already added nothing
   * happens.
   *
   * @param selectable The render pass.
   */
  public void addSelectable(final Selectable selectable) {
    if(selects.contains(selectable)) return;
    selects.add(selectable);
  }

  /**
   * Removes a selectable render pass. If the render pass is not contained
   * nothing happens.
   *
   * @param selectable The render pass.
   */
  public void removeSelectable(final Selectable selectable) {
    selects.remove(selectable);
  }

  /**
   * Does the actual selection of the given shape on all select-able render
   * passes.
   *
   * @param s The shape in component coordinates.
   * @param preview Whether the selection should only be a preview.
   */
  protected void doSelection(final Shape s, final boolean preview) {
    selection = s;
    final KanvasContext ctx = canvas.getHUDContext();
    for(final Selectable r : selects) {
      final KanvasContext c = RenderpassPainter.getRecursiveContextFor(
          r.getRenderpass(), ctx);
      final AffineTransform at = c.toCanvasTransformation();
      final Shape selection = at.createTransformedShape(s);
      r.select(selection, preview);
    }
  }

  @Override
  public abstract boolean acceptDragHUD(Point2D p, MouseEvent e);

  /**
   * Begins the creation of the selection shape.
   *
   * @param start The start point.
   * @param cur The first other point.
   * @return The initial shape.
   */
  public Shape beginShape(final Point2D start, final Point2D cur) {
    return growShape(start, cur);
  }

  /**
   * Grows the selection shape.
   *
   * @param start The original start point.
   * @param cur The next point.
   * @return The current shape.
   */
  public abstract Shape growShape(Point2D start, Point2D cur);

  @Override
  public void dragHUD(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    doSelection(selection == null ? beginShape(start, cur) : growShape(start, cur), true);
  }

  @Override
  public void endDragHUD(final Point2D start, final Point2D end, final double dx,
      final double dy) {
    doSelection(growShape(start, end), false);
    selection = null;
  }

}
