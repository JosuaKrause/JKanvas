package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * Paints and interacts with a canvas in component coordinates.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface HUDInteraction {

  /**
   * Draws a HUD (Head-Up-Display) on the canvas. This method draws over the
   * canvas and uses the components coordinate space.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  void drawHUD(Graphics2D gfx, KanvasContext ctx);

  /**
   * Is called when the user clicks at the component. The coordinates are in the
   * components coordinate space and therefore suitable for clicks on HUDs. This
   * method is the first in the order of click processing and other actions
   * (object clicking and dragging) won't happen if this method returns
   * <code>true</code>.
   * 
   * @param p The click position in component coordinates.
   * @return Whether the click was consumed.
   */
  boolean clickHUD(Point2D p);

  /**
   * Is called when the user moves the mouse over the component. This method
   * returns the tool-tip that should be displayed at the given position. This
   * method is called before {@link KanvasInteraction#getTooltip(Point2D)}.
   * 
   * @param p The mouse position in component coordinates.
   * @return The tool-tip text or <code>null</code> if no tool-tip should be
   *         displayed.
   */
  String getTooltipHUD(Point2D p);

}
