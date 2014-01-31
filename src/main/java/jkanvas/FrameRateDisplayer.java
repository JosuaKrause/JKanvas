package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;

/**
 * Displays the frame rate.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface FrameRateDisplayer {

  /**
   * Sets the time it took to draw the current frame in nano-seconds. This
   * method is called by {@link Canvas} immediately before
   * {@link #drawFrameRate(Graphics2D, RectangularShape)}.
   * 
   * @param time The time it took to draw the most recent frame in nano-seconds.
   */
  void setLastFrameTime(long time);

  /**
   * Sets the time it took to animate the current frame in nano-seconds. This
   * method is called by an {@link jkanvas.animation.Animator}.
   * 
   * @param time The time it took to animate the most recent frame in
   *          nano-seconds.
   * @param lag Whether the animation computation took longer than expected.
   */
  void setLastAnimationTime(long time, boolean lag);

  /**
   * Draws the frame rate.
   * 
   * @param g The graphics context.
   * @param visibleRect The visible rectangle in component coordinates.
   */
  void drawFrameRate(Graphics2D g, RectangularShape visibleRect);

  /**
   * Getter.
   * 
   * @return Whether the frame rate displayer is currently active.
   */
  boolean isActive();

}
