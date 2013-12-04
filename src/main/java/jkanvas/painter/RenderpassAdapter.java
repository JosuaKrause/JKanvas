package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Objects;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimationList;
import jkanvas.animation.AnimationTiming;

/**
 * An adapter for render passes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class RenderpassAdapter implements Renderpass {

  /** Whether to use the double click default action. */
  public static boolean USE_DOUBLE_CLICK_DEFAULT = true;

  /** Whether caching is forced. */
  private boolean forceCache;

  @Override
  public void setForceCache(final boolean forceCache) {
    this.forceCache = forceCache;
  }

  @Override
  public boolean isForceCaching() {
    return forceCache;
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D p, final MouseEvent e) {
    if(!USE_DOUBLE_CLICK_DEFAULT) return false;
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    cam.toView(this, AnimationTiming.SMOOTH, null, true);
    return true;
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    // do nothing
  }

  @Override
  public boolean click(final Camera cam, final Point2D p, final MouseEvent e) {
    // do nothing when clicking
    return false;
  }

  @Override
  public String getTooltip(final Point2D p) {
    // no tool-tip
    return null;
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    // no dragging
    return false;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    // do nothing
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    drag(start, end, dx, dy);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    // do nothing
    return false;
  }

  @Override
  public double getOffsetX() {
    return 0;
  }

  @Override
  public double getOffsetY() {
    return 0;
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    // we do not need the animation list
  }

  @Override
  public boolean isVisible() {
    // visible by default
    return true;
  }

  @Override
  public Renderpass getParent() {
    // no parent
    return null;
  }

  @Override
  public boolean isChanging() {
    // be safe and always return true -- TODO is this necessary?
    return true;
  }

  /** The ids associated with this render pass. */
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
