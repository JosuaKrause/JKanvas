package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import java.util.concurrent.atomic.AtomicLong;

import jkanvas.FrameRateDisplayer;

/**
 * A HUD showing the current frame-rate.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class FrameRateHUD extends TextHUD implements FrameRateDisplayer {

  /** Creates a frame-rate HUD. */
  public FrameRateHUD() {
    this(RIGHT, TOP);
  }

  /**
   * Creates a frame-rate HUD.
   * 
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @see #LEFT
   * @see #RIGHT
   * @see #TOP
   * @see #BOTTOM
   */
  public FrameRateHUD(final int hpos, final int vpos) {
    super(hpos, vpos);
  }

  /**
   * Creates a frame-rate HUD.
   * 
   * @param text The text color.
   * @param back The background color.
   * @param alpha The alpha value of the background color.
   * @param padding The padding of the background.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @see #LEFT
   * @see #RIGHT
   * @see #TOP
   * @see #BOTTOM
   */
  public FrameRateHUD(final Color text, final Color back, final double alpha,
      final double padding, final int hpos, final int vpos) {
    super(text, back, alpha, padding, hpos, vpos);
  }

  @Override
  public int lineCount() {
    return 1;
  }

  /** The time it took to animate the most recent frame in nano-seconds. */
  private final AtomicLong lastAnimationTime = new AtomicLong();
  /** The time it took to draw the most recent frame in nano-seconds. */
  private long lastFrameTime;
  /** Whether the animation thread encountered lag recently. */
  private volatile boolean lag;

  @Override
  public void setLastFrameTime(final long time) {
    lastFrameTime = time;
  }

  @Override
  public void setLastAnimationTime(final long time, final boolean lag) {
    lastAnimationTime.set(time);
    this.lag = lag;
  }

  @Override
  public String getLine(final int i) {
    if(lastFrameTime == 0) return null;
    final double fps = 1e9 / (lastFrameTime + lastAnimationTime.get());
    return (lag ? "fps: *" : "fps: ") + format(fps);
  }

  /**
   * Formats a number with a fixed length after the decimal point.
   * 
   * @param value The number.
   * @return The string.
   */
  private static String format(final double value) {
    final String tmp = "" + Math.floor(value * 1e5) * 1e-5;
    if(tmp.indexOf('e') >= 0) return tmp;
    final String str = tmp + "00000";
    final int dot = str.indexOf('.');
    return str.substring(0, Math.min(dot + 6, str.length()));
  }

  @Override
  public void drawFrameRate(final Graphics2D g, final RectangularShape visibleRect) {
    draw(g, visibleRect);
  }

  @Override
  public boolean isActive() {
    return isVisible();
  }

}
