package jkanvas.selection;

import java.awt.Shape;
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

  /** The canvas to select on. */
  private final Canvas canvas;

  /** The list of selectabel render-passes. */
  private final List<SelectableRenderpass> selects = new ArrayList<>();

  /**
   * Creates an abstract selector.
   * 
   * @param canvas The canvas the selector operates on.
   */
  public AbstractSelector(final Canvas canvas) {
    this.canvas = Objects.requireNonNull(canvas);
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
    final KanvasContext ctx = canvas.getHUDContext();
    for(final SelectableRenderpass r : selects) {
      final KanvasContext c = RenderpassPainter.getContextFor(r, ctx);
      final AffineTransform at = c.toCanvasTransformation();
      final Shape selection = at.createTransformedShape(s);
      r.select(selection, preview);
    }
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
