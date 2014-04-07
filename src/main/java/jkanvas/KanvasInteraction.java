package jkanvas;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
 * Paints and interacts with a Kanvas in canvas coordinates.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface KanvasInteraction {

  /**
   * Draw on a canvas. The panning and zooming of the canvas is transparent to
   * this method and needs no further investigation.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   */
  void draw(Graphics2D g, KanvasContext ctx);

  /**
   * Is called when the user clicks at the component and the HUD action does not
   * consume the event. The coordinates are in the {@link KanvasPainter
   * Painters} coordinate space and therefore suitable for clicks on objects on
   * the canvas. This method is the second in the order of click processing and
   * no dragging is performed, when this method returns <code>true</code>. In
   * this method peers can be prevented to get processed by calling
   * {@link Canvas#preventPeerInteraction()}. In contrast to returning
   * <code>true</code> other interaction types still get processed.
   * 
   * @param cam The camera on which the interaction happened.
   * @param p The click position in canvas coordinates.
   * @param e The original event.
   * @return Whether the click was consumed.
   * @see Canvas#preventPeerInteraction()
   */
  boolean click(Camera cam, Point2D p, MouseEvent e);

  /**
   * Is called when the user performs a double click at the component and the
   * HUD action does not consume the event. The coordinates are in the
   * {@link KanvasPainter Painters} coordinate space and therefore suitable for
   * clicks on the objects on the canvas. This method does not interfere with
   * other clicks or drags. In this method peers can be prevented to get
   * processed by calling {@link Canvas#preventPeerInteraction()}. In contrast
   * to returning <code>true</code> other interaction types still get processed.
   * 
   * @param cam The camera on which the interaction happened.
   * @param p The double click position in canvas coordinates.
   * @param e The original event.
   * @return Whether the double click was consumed.
   * @see Canvas#preventPeerInteraction()
   */
  boolean doubleClick(Camera cam, Point2D p, MouseEvent e);

  /**
   * Is called when the user moves the mouse over the component and
   * {@link HUDInteraction#getTooltipHUD(Point2D)} has returned
   * <code>null</code>. This method returns the tool-tip that should be
   * displayed at the given canvas position.
   * 
   * @param p The mouse position in canvas coordinates.
   * @return The tool-tip text or <code>null</code> if no tool-tip should be
   *         displayed.
   */
  String getTooltip(Point2D p);

  /**
   * Is called when the user starts a dragging operation on the canvas. The
   * coordinates are in the {@link KanvasPainter Painters} coordinate space and
   * therefore suitable for dragging of objects on the canvas. In this method
   * peers can be prevented to get processed by calling
   * {@link Canvas#preventPeerInteraction()}. In contrast to returning
   * <code>true</code> other interaction types still get processed.
   * 
   * @param p The position where the drag starts in canvas coordinates.
   * @param e The mouse event.
   * @return Whether the drag is accepted and the dragging should start.
   * @see Canvas#preventPeerInteraction()
   */
  boolean acceptDrag(Point2D p, MouseEvent e);

  /**
   * Is called subsequently after {@link #acceptDrag(Point2D, MouseEvent)}
   * returned <code>true</code> on every mouse movement until the user releases
   * the mouse button.
   * 
   * @param start The position where the drag started in canvas coordinates.
   * @param cur The current drag position in canvas coordinates.
   * @param dx The x distance of the drag in canvas coordinates.
   * @param dy The y distance of the drag in canvas coordinates.
   */
  void drag(Point2D start, Point2D cur, double dx, double dy);

  /**
   * Is called when the user releases the mouse in drag operation.
   * 
   * @param start The position where the drag started in canvas coordinates.
   * @param end The end position of the drag in canvas coordinates.
   * @param dx The x distance of the drag in canvas coordinates.
   * @param dy The y distance of the drag in canvas coordinates.
   */
  // TODO #43 -- Java 8 simplification
  void endDrag(Point2D start, Point2D end, double dx, double dy);

  /**
   * Is called when the mouse was moved. In this method peers can be prevented
   * to get processed by calling {@link Canvas#preventPeerInteraction()}.
   * Returning <code>true</code> on the other hand does not prevent peers from
   * being executed in this method.
   * 
   * @param cur The current position in canvas coordinates.
   * @return Whether this event has affected the render pass.
   * @see Canvas#preventPeerInteraction()
   */
  boolean moveMouse(Point2D cur);

  /**
   * Calculates the bounding box of the canvas.
   * 
   * @param bbox The rectangle where the bounding box is stored.
   */
  void getBoundingBox(RectangularShape bbox);

  /**
   * Processes a message handed in via the {@link Canvas#postMessage(String)}
   * method.
   * 
   * @param ids A list of ids the message is for. Only objects with at least one
   *          of those ids should consume the message. Due to technical reasons
   *          the characters '<code>#</code>' and '<code> </code>' cannot be in
   *          ids. The list may be empty. Ids cannot be the empty string.
   * @param msg The message to be processed. The message must be handed to all
   *          children even when consumed. Due to technical reasons the
   *          character '<code>#</code>' cannot be in messages. Messages cannot
   *          be the empty string.
   */
  void processMessage(String[] ids, String msg);

}
