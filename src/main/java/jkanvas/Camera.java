package jkanvas;

import java.awt.geom.Rectangle2D;

import jkanvas.animation.AnimationTiming;

/**
 * A camera is way to control the viewport of a {@link jkanvas.Canvas}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Camera {

  /**
   * Scrolls to the specified view. The rectangle will be completely visible
   * afterwards.
   * 
   * @param rect The rectangle in canvas coordinates.
   * @param timing The timing of the scrolling.
   */
  void toView(Rectangle2D rect, AnimationTiming timing);

  /**
   * Getter.
   * 
   * @return The current view in canvas coordinates.
   */
  Rectangle2D getView();

  /**
   * Getter.
   * 
   * @return The destination of the current scroll animation in canvas
   *         coordinates. If no scroll animation is active the result is the
   *         same as {@link #getView()}.
   */
  Rectangle2D getPredictView();

}
