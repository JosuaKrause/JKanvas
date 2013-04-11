package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

import jkanvas.Canvas;
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
  public boolean clickHUD(final Point2D p, final MouseEvent e) {
    // ignore clicks
    return false;
  }

  @Override
  public boolean doubleClickHUD(final Point2D p, final MouseEvent e) {
    // ignore double clicks
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

  /** The ids associated with this HUD render pass. */
  private String ids = "";

  @Override
  public void setIds(final String ids) {
    this.ids = " " + Objects.requireNonNull(ids) + " ";
  }

  @Override
  public String getIds() {
    return ids;
  }

  @Override
  public void processMessage(final String[] ids, final String msg) {
    for(final String id : ids) {
      if(this.ids.contains(id) && this.ids.contains(" " + id + " ")) {
        processMessage(msg);
        return;
      }
    }
  }

  /**
   * Processes a message handed in via the {@link Canvas#postMessage(String)}
   * method. The message ids are already processed at this point.
   * 
   * @param msg The message to be processed. Due to technical reasons the
   *          character '<code>#</code>' cannot be in messages. Messages cannot
   *          be the empty string.
   */
  protected void processMessage(@SuppressWarnings("unused") final String msg) {
    // nothing to do
  }

}
