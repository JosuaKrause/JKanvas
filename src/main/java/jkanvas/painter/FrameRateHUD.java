package jkanvas.painter;

import java.awt.Color;

import jkanvas.Canvas;

/**
 * A HUD showing the current frame-rate.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class FrameRateHUD extends TextHUD {

  /** The padding of the text box. */
  public static final double PADDING = 5.0;

  /** The alpha value of the text box. */
  public static final double ALPHA = 0.5;

  /** The text color. */
  public static final Color TEXT = Color.WHITE;

  /** The text box color. */
  public static final Color BACK = Color.BLACK;

  /** The canvas to measure the frame-rate. */
  private final Canvas canvas;

  /**
   * Creates a frame-rate HUD for the given canvas.
   * 
   * @param canvas The canvas.
   */
  public FrameRateHUD(final Canvas canvas) {
    super(TEXT, BACK, ALPHA, PADDING, RIGHT, TOP);
    this.canvas = canvas;
    canvas.setMeasureFrameTime(true);
  }

  @Override
  public void setVisible(final boolean isVisible) {
    super.setVisible(isVisible);
    canvas.setMeasureFrameTime(isVisible);
  }

  @Override
  public int lineCount() {
    return 1;
  }

  @Override
  public String getLine(final int i) {
    final long time = canvas.getLastFrameTime();
    if(time == 0) return null;
    final double fps = 1e9 / time;
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

}
