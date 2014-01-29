package jkanvas;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Paints and interacts with a {@link Canvas}.
 * 
 * @author Joschi <josua.krause@gmail.com>
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

  /** {@inheritDoc} */
  @Override
  void processMessage(String[] ids, String msg);

  /** Disposes this painter. */
  void dispose();

}
