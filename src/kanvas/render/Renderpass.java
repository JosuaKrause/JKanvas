package kanvas.render;

import java.awt.Graphics2D;

import kanvas.Context;

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
  void render(Graphics2D gfx, Context ctx);

  /**
   * Getter.
   * 
   * @return When <code>true</code>, the context during rendering is set such
   *         that {@link Context#inCanvasCoordinates()} returns
   *         <code>false</code>.
   */
  boolean isHUD();

}
