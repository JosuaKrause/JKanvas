package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;


/**
 * Render passes can be used to dynamically change what is rendered on a canvas
 * and define an order.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Renderpass {

  /**
   * Renders the current pass.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  void render(Graphics2D gfx, KanvasContext ctx);

  /**
   * Getter.
   * 
   * @return When <code>true</code>, the context during rendering is set such
   *         that {@link KanvasContext#inCanvasCoordinates()} returns
   *         <code>false</code>. The value should be fixed since this method is
   *         only called once to determine whether it is a HUD.
   */
  boolean isHUD();

  /**
   * Getter.
   * 
   * @return Whether this pass is currently visible.
   */
  boolean isVisible();

  /**
   * Getter.
   * 
   * @return The x offset of this pass in canvas coordinates.
   */
  double getOffsetX();

  /**
   * Getter.
   * 
   * @return The y offset of this pass in canvas coordinates.
   */
  double getOffsetY();

  /**
   * Getter.
   * 
   * @return An optional bounding box in canvas coordinates. This method does
   *         <em>not</em> have to account for the offset. If the pass is a HUD
   *         the bounding box is ignored.
   */
  Rectangle2D getBoundingBox();

}
