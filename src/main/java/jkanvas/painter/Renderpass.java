package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.KanvasInteraction;
import jkanvas.animation.AnimationList;
import jkanvas.animation.AnimationTiming;
import jkanvas.painter.pod.Renderpod;

/**
 * Render passes can be used to dynamically change what is rendered on a canvas
 * and define an order.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class Renderpass implements KanvasInteraction {

  /** Whether caching is forced. */
  private boolean forceCache;

  /**
   * Setter.
   * 
   * @param forceCache Force caching. This can be useful when the render pass is
   *          moving and caching is supported.
   */
  public void setForceCache(final boolean forceCache) {
    this.forceCache = forceCache;
  }

  /**
   * Getter.
   * 
   * @return Whether to force caching when caching is supported.
   */
  public boolean isForceCaching() {
    return forceCache;
  }

  /** Whether to use the double click default action. */
  public static boolean USE_DOUBLE_CLICK_DEFAULT = true;

  /**
   * The double click default action.
   * 
   * @param rp The render pass.
   * @param cam The camera.
   * @param e The mouse event.
   * @return Whether the click was consumed.
   */
  public static final boolean defaultDoubleClick(
      final Renderpass rp, final Camera cam, final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    Renderpass r = rp;
    while(r.getParent() instanceof Renderpod) {
      r = r.getParent();
    }
    cam.toView(r, AnimationTiming.SMOOTH, null, true);
    return true;
  }

  /**
   * The double click default action.
   * 
   * @param rect The rectangle.
   * @param cam The camera.
   * @param e The mouse event.
   * @return Whether the click was consumed.
   */
  public static final boolean defaultDoubleClick(
      final Rectangle2D rect, final Camera cam, final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    cam.toView(rect, AnimationTiming.SMOOTH, null, true);
    return true;
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D p, final MouseEvent e) {
    if(USE_DOUBLE_CLICK_DEFAULT) return defaultDoubleClick(this, cam, e);
    return false;
  }

  /** Whether the pass is visible. */
  private boolean isVisible = true;

  /**
   * Getter.
   * 
   * @return Whether this pass is currently visible.
   */
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * Setter. Implementations may override this method with an
   * {@link UnsupportedOperationException} when they provide the value by
   * themselves.
   * 
   * @param isVisible Sets the visibility of this pass.
   */
  public void setVisible(final boolean isVisible) {
    this.isVisible = isVisible;
  }

  /** The x offset in canvas coordinates. */
  private double x;

  /** The y offset in canvas coordinates. */
  private double y;

  /**
   * Setter.
   * 
   * @param x Sets the x offset in canvas coordinates.
   * @param y Sets the y offset in canvas coordinates.
   */
  public void setOffset(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Getter.
   * 
   * @return The x offset of this pass in canvas coordinates.
   */
  public double getOffsetX() {
    return x;
  }

  /**
   * Getter.
   * 
   * @return The y offset of this pass in canvas coordinates.
   */
  public double getOffsetY() {
    return y;
  }

  /**
   * Calculates the bounding box of the render pass. The method does
   * <em>not</em> account for the offset. To get information about the offset of
   * the render pass use {@link #getOffsetX()} and {@link #getOffsetY()}
   * respectively.
   * 
   * @param bbox The rectangle in which the bounding box is stored.
   * @see #getOffsetX()
   * @see #getOffsetY()
   */
  @Override
  public abstract void getBoundingBox(RectangularShape bbox);

  /** The parent. */
  private Renderpass parent;

  /**
   * Setter.
   * 
   * @param parent Sets the parent of this render pass. Parents can not be
   *          switched directly.
   */
  public void setParent(final Renderpass parent) {
    if(parent != null && this.parent != null) throw new IllegalStateException(
        "tried to set two parents");
    this.parent = parent;
  }

  /**
   * Getter.
   * 
   * @return The parent of this render pass. The parent is used to calculate
   *         correct top level canvas positions when render passes are combined
   *         in groups.
   * @see jkanvas.painter.RenderpassPainter#getTopLevelBounds(Rectangle2D,
   *      Renderpass)
   * @see jkanvas.painter.RenderpassPainter#convertToTopLevelBounds(Rectangle2D,
   *      Renderpass)
   */
  public Renderpass getParent() {
    return parent;
  }

  /** The ids associated with this render pass. */
  private String ids = "";

  /**
   * Setter.
   * 
   * @param ids The ids associated with this render pass. Multiple ids may be
   *          separated with space '<code> </code>'.
   */
  public void setIds(final String ids) {
    this.ids = " " + Objects.requireNonNull(ids).trim() + " ";
  }

  /**
   * Getter.
   * 
   * @return The ids associated with this render pass. Multiple ids may be
   *         separated with space '<code> </code>'.
   */
  public String getIds() {
    return ids;
  }

  @Override
  // TODO #43 -- Java 8 simplification
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
   * <p>
   * This implementation handles the messages "<code>visible:true</code> ", "
   * <code>visible:false</code>", and "<code>visible:toggle</code>".
   * 
   * @param msg The message to be processed. Due to technical reasons the
   *          character '<code>#</code>' cannot be in messages. Messages cannot
   *          be the empty string.
   */
  protected void processMessage(final String msg) {
    switch(msg) {
      case "visible:true":
        setVisible(true);
        break;
      case "visible:false":
        setVisible(false);
        break;
      case "visible:toggle":
        setVisible(!isVisible());
        break;
    }
  }

  @Override
  public abstract void draw(final Graphics2D g, final KanvasContext ctx);

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
  // TODO #43 -- Java 8 simplification
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    drag(start, end, dx, dy);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    // do nothing
    return false;
  }

  /**
   * Setter.
   * 
   * @param list Sets the animation list so that the render pass can add and
   *          remove animated objects. If the render pass has nothing to add to
   *          the list this method can be ignored.
   */
  public void setAnimationList(@SuppressWarnings("unused") final AnimationList list) {
    // we do not need the animation list
  }

  /**
   * Getter.
   * 
   * @return Whether the render pass may be altered until the next call of
   *         {@link #draw(java.awt.Graphics2D, jkanvas.KanvasContext)}.
   */
  public boolean isChanging() {
    // be safe and always return true -- TODO is this necessary?
    return true;
  }

}
