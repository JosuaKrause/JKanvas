package jkanvas.selection;

import java.awt.AlphaComposite;
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

/**
 * Allows for arbitrary shaped selections.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractSelector implements HUDRenderpass {

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

  /** The list of selectabel render-passes. */
  private final List<SelectableRenderpass> selects = new ArrayList<>();

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
    this.inner = Objects.requireNonNull(inner);
    this.alphaInner = (float) alphaInner;
    this.outer = Objects.requireNonNull(outer);
    this.alphaOuter = (float) alphaOuter;
  }

  /** The displayed selection. */
  private Shape selection;

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

  /** Whether this selector is active. */
  private boolean active = true;

  /**
   * Setter.
   * 
   * @param isActive Whether this selector is active.
   */
  public void setActive(final boolean isActive) {
    active = isActive;
  }

  /**
   * Getter.
   * 
   * @return Whether this selector is active.
   */
  public boolean isActive() {
    return active;
  }

  @Override
  public boolean isVisible() {
    return active;
  }

  /**
   * Adds a selectable render-pass. If the render-pass is already added nothing
   * happens.
   * 
   * @param selectable The render-pass.
   */
  public void addSelectable(final SelectableRenderpass selectable) {
    if(selects.contains(selectable)) return;
    selects.add(selectable);
  }

  /**
   * Removes a selectable render-pass. If the render-pass is not contained
   * nothing happens.
   * 
   * @param selectable The render-pass.
   */
  public void removeSelectable(final SelectableRenderpass selectable) {
    selects.remove(selectable);
  }

  /**
   * Does the actual selection of the given shape on all select-able
   * render-passes.
   * 
   * @param s The shape in component coordinates.
   * @param preview Whether the selection should only be a preview.
   */
  protected void doSelection(final Shape s, final boolean preview) {
    selection = s;
    final KanvasContext ctx = canvas.getHUDContext();
    for(final SelectableRenderpass r : selects) {
      final KanvasContext c = RenderpassPainter.getContextFor(r, ctx);
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
    if(selection == null) {
      selection = beginShape(start, cur);
    } else {
      selection = growShape(start, cur);
    }
    doSelection(selection, true);
  }

  @Override
  public void endDragHUD(final Point2D start, final Point2D end, final double dx,
      final double dy) {
    doSelection(growShape(start, end), false);
    selection = null;
  }

  @Override
  public boolean clickHUD(final Point2D p) {
    // no clicking
    return false;
  }

  @Override
  public String getTooltipHUD(final Point2D p) {
    // no tool-tip -- showing what, exactly?
    return null;
  }

}
