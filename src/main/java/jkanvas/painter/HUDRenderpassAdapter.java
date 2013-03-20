package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import jkanvas.KanvasContext;

/**
 * An adapter for a HUD render pass.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class HUDRenderpassAdapter implements HUDRenderpass {

  /** Whether this render pass is visible. */
  private boolean visible = true;

  /**
   * Setter.
   * 
   * @param isVisible Whether this render pass is visible.
   */
  public void setVisible(final boolean isVisible) {
    visible = isVisible;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void drawHUD(final Graphics2D g, final KanvasContext ctx) {
    // nothing to do
  }

  @Override
  public boolean clickHUD(final Point2D p) {
    // ignore clicks
    return false;
  }

  @Override
  public String getTooltipHUD(final Point2D p) {
    // no tool-tips
    return null;
  }

  @Override
  public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
    // ignore drags
    return false;
  }

  @Override
  public void dragHUD(final Point2D start, final Point2D cur, final double dx,
      final double dy) {
    // nothing to do
  }

  @Override
  public void endDragHUD(final Point2D start, final Point2D end, final double dx,
      final double dy) {
    dragHUD(start, end, dx, dy);
  }

}
