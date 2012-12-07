package jkanvas.painter;

import java.awt.Graphics2D;

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
   *         <code>false</code>.
   */
  boolean isHUD();

}
