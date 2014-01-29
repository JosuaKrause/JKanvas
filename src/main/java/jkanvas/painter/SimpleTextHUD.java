package jkanvas.painter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link TextHUD} implementation.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleTextHUD extends TextHUD {

  /** The list containing the lines. */
  private final List<String> lines = new ArrayList<>();

  /**
   * Creates a simple text HUD.
   * 
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @see #LEFT
   * @see #RIGHT
   * @see #TOP
   * @see #BOTTOM
   */
  public SimpleTextHUD(final int hpos, final int vpos) {
    super(hpos, vpos);
  }

  /**
   * Creates a simple text HUD.
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
  public SimpleTextHUD(final Color text, final Color back, final double alpha,
      final double padding, final int hpos, final int vpos) {
    super(text, back, alpha, padding, hpos, vpos);
  }

  @Override
  public int lineCount() {
    return lines.size();
  }

  @Override
  public String getLine(final int i) {
    return i < lines.size() ? lines.get(i) : null;
  }

  /**
   * Inserts a line to the text box.
   * 
   * @param pos The position.
   * @param line The line.
   */
  public void insertLine(final int pos, final String line) {
    lines.add(pos, line);
  }

  /**
   * Adds a line at the end of the text box.
   * 
   * @param line The line.
   */
  public void addLine(final String line) {
    lines.add(line);
  }

}
