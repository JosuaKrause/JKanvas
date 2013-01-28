package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;

/**
 * Displays the frame rate.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface FrameRateDisplayer {

  /**
   * Sets the time it took to draw the current frame in nano-seconds. This
   * method is called by {@link Canvas} immediately before
   * {@link #drawFrameRate(Graphics2D, Rectangle2D)}.
   * 
   * @param time The time it took to draw the most recent frame in nano-seconds.
   */
  void setLastFrameTime(long time);

  /**
   * Draws the frame rate.
   * 
   * @param gfx The graphics context.
   * @param visibleRect The visible rectangle in component coordinates.
   */
  void drawFrameRate(Graphics2D gfx, Rectangle2D visibleRect);

  /**
   * Getter.
   * 
   * @return Whether the frame rate displayer is currently active.
   */
  boolean isActive();

}
