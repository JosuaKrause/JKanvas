package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * A HUD showing the current frame-rate.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class FrameRateHUD extends TextHUD implements FrameRateDisplayer {

  /** The padding of the text box. */
  public static final double PADDING = 5.0;

  /** The alpha value of the text box. */
  public static final double ALPHA = 0.5;

  /** The text color. */
  public static final Color TEXT = Color.WHITE;

  /** The text box color. */
  public static final Color BACK = Color.BLACK;

  /**
   * Creates a frame-rate HUD.
   */
  public FrameRateHUD() {
    super(TEXT, BACK, ALPHA, PADDING, RIGHT, TOP);
  }

  @Override
  public int lineCount() {
    return 1;
  }

  /** The time it took to draw the most recent frame in nano-seconds. */
  private long lastFrameTime;

  @Override
  public void setLastFrameTime(final long time) {
    lastFrameTime = time;
  }

  @Override
  public String getLine(final int i) {
    if(lastFrameTime == 0) return null;
    final double fps = 1e9 / lastFrameTime;
    return "fps: " + format(fps);
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
  public void drawFrameRate(final Graphics2D gfx, final Rectangle2D visibleRect) {
    draw(gfx, visibleRect);
  }

  @Override
  public boolean isActive() {
    return isVisible();
  }

}
