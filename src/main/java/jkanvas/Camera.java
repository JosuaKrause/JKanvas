package jkanvas;

import java.awt.geom.Rectangle2D;

import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.painter.Renderpass;

/**
 * A camera is way to control the viewport of a {@link jkanvas.Canvas}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Camera {

  /**
   * Scrolls to the specified viewport. The rectangle will be completely visible
   * afterwards.
   * 
   * @param rect The rectangle in canvas coordinates.
   * @param timing The timing of the scrolling.
   * @param onFinish The action that is executed after the animation has
   *          finished.
   * @param useMargin Whether to add the canvas margin.
   */
  void toView(Rectangle2D rect, AnimationTiming timing,
      AnimationAction onFinish, boolean useMargin);

  /**
   * Moves the view by the given amount.
   * 
   * @param dx The horizontal movement in component coordinates.
   * @param dy The vertical movement in component coordinates.
   */
  void move(double dx, double dy);

  /**
   * Scrolls to the specified render pass. The bounding box of the render pass
   * will be completely visible afterwards. If the render pass has no bounding
   * box the finish action will be scheduled normally but otherwise nothing
   * happens.
   * 
   * @param pass The render pass.
   * @param timing The timing of the scrolling.
   * @param onFinish The action that is executed after the animation has
   *          finished.
   * @param useMargin Whether to add the canvas margin.
   */
  // TODO #43 -- Java 8 simplification
  void toView(Renderpass pass, AnimationTiming timing,
      AnimationAction onFinish, boolean useMargin);

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

  /**
   * Getter.
   * 
   * @return Whether the camera is in animation.
   */
  boolean inAnimation();

}
