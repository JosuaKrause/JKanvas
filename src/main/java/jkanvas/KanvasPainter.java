package jkanvas;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Paints and interacts with a {@link Canvas}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface KanvasPainter extends KanvasInteraction, HUDInteraction {

  /**
   * Whether the mouse event should start panning the canvas.
   * 
   * @param p The position of the mouse event in canvas coordinates.
   * @param e The mouse event.
   * @return Whether to start panning due to the mouse event.
   */
  boolean isAllowingPan(Point2D p, MouseEvent e);

  /**
   * Processes a message handed in via the {@link Canvas#postMessage(String)}
   * method.
   * 
   * @param ids A list of ids the message is for. Only objects with at least one
   *          of those ids should consume the message. Due to technical reasons
   *          the characters '<code>#</code>' and '<code>&#20;</code>' cannot be
   *          in ids. The list may be empty. Ids cannot be the empty string.
   * @param msg The message to be processed. The message must be handed to all
   *          children even when consumed. Due to technical reasons the
   *          character '<code>#</code>' cannot be in messages. Messages cannot
   *          be the empty string.
   */
  @Override
  void processMessage(String[] ids, String msg);

  /** Disposes this painter. */
  void dispose();

}
