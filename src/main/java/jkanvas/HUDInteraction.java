package jkanvas;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
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
   * @param g The graphics context.
   * @param ctx The canvas context.
   */
  void drawHUD(Graphics2D g, KanvasContext ctx);

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

  /**
   * Is called when the user starts a dragging operation on the canvas. The
   * coordinates are in the component coordinate space.
   * 
   * @param p The position where the drag starts in component coordinates.
   * @param e The mouse event.
   * @return Whether the drag is accepted and the dragging should start.
   */
  boolean acceptDragHUD(Point2D p, MouseEvent e);

  /**
   * Is called subsequently after {@link #acceptDragHUD(Point2D, MouseEvent)}
   * returned <code>true</code> on every mouse movement until the user releases
   * the mouse button.
   * 
   * @param start The position where the drag started in component coordinates.
   * @param cur The current drag position in component coordinates.
   * @param dx The x distance of the drag in component coordinates.
   * @param dy The y distance of the drag in component coordinates.
   */
  void dragHUD(Point2D start, Point2D cur, double dx, double dy);

  /**
   * Is called when the user releases the mouse in drag operation.
   * 
   * @param start The position where the drag started in component coordinates.
   * @param end The end position of the drag in component coordinates.
   * @param dx The x distance of the drag in component coordinates.
   * @param dy The y distance of the drag in component coordinates.
   */
  void endDragHUD(Point2D start, Point2D end, double dx, double dy);

}
