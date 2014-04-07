package jkanvas;

import java.util.Objects;

import jkanvas.painter.HUDRenderpass;
import jkanvas.painter.Renderpass;

/**
 * Stops the current scene graph traversal for interaction methods in order to
 * call the corresponding
 * {@link KanvasInteraction#acceptDrag(java.awt.geom.Point2D, java.awt.event.MouseEvent)}
 * or
 * {@link HUDInteraction#acceptDragHUD(java.awt.geom.Point2D, java.awt.event.MouseEvent)}
 * method for the given render or HUD item.
 * <p>
 * This exception can only be used before the actual action has taken place.
 * 
 * @see Canvas#acceptDragging(Renderpass)
 * @see Canvas#acceptHUDDragging(HUDRenderpass)
 * @see HUDInteraction#clickHUD(Camera, java.awt.geom.Point2D,
 *      java.awt.event.MouseEvent)
 * @see HUDInteraction#acceptDragHUD(java.awt.geom.Point2D,
 *      java.awt.event.MouseEvent)
 * @see KanvasInteraction#click(Camera, java.awt.geom.Point2D,
 *      java.awt.event.MouseEvent)
 * @author Joschi <josua.krause@gmail.com>
 */
public class AcceptDraggingException extends RuntimeException {

  /** The render item or <code>null</code>. */
  private final Renderpass rp;
  /** The HUD item or <code>null</code>. */
  private final HUDRenderpass hrp;

  /**
   * Creates an exception for the given render item.
   * 
   * @param rp The render item.
   */
  AcceptDraggingException(final Renderpass rp) {
    this.rp = Objects.requireNonNull(rp);
    hrp = null;
  }

  /**
   * Creates an exception for the given HUD item.
   * 
   * @param hrp The HUD item.
   */
  AcceptDraggingException(final HUDRenderpass hrp) {
    this.hrp = Objects.requireNonNull(hrp);
    rp = null;
  }

  /**
   * Getter.
   * 
   * @return Whether the render item is not <code>null</code>. On of both is
   *         always non-<code>null</code>.
   */
  public boolean isRenderpass() {
    return rp != null;
  }

  /**
   * Getter.
   * 
   * @return The render item if existent.
   */
  public Renderpass getRenderpass() {
    return rp;
  }

  /**
   * Getter.
   * 
   * @return The HUD item if existent.
   */
  public HUDRenderpass getHUDRenderpass() {
    return hrp;
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    if(Canvas.ALLOW_INTERACTION_DIAGNOSTIC) return super.fillInStackTrace();
    // we wont use the stack trace so we save the costs of creating it
    return this;
  }

}
