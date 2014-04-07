package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;

/**
 * A HUD showing a color key.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ColorKeyHUD extends SimpleTextHUD {

  /**
   * Creates a color key HUD.
   * 
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @see #LEFT
   * @see #RIGHT
   * @see #TOP
   * @see #BOTTOM
   */
  public ColorKeyHUD(final int hpos, final int vpos) {
    super(hpos, vpos);
  }

  /**
   * Creates a color key HUD.
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
  public ColorKeyHUD(final Color text, final Color back, final double alpha,
      final double padding, final int hpos, final int vpos) {
    super(text, back, alpha, padding, hpos, vpos);
  }

  /** The padding for the text. */
  private final String pad = "    ";

  /** The list of colors. */
  private final List<Color> colors = new ArrayList<>();

  @Override
  public void insertLine(final int pos, final String line) {
    insertKey(pos, line, null);
  }

  @Override
  public void addLine(final String line) {
    addKey(line, null);
  }

  /**
   * Inserts a color key at the given position.
   * 
   * @param pos The index.
   * @param line The text line to add.
   * @param color The color.
   */
  public void insertKey(final int pos, final String line, final Color color) {
    colors.add(pos, color);
    super.insertLine(pos, pad + line);
  }

  /**
   * Adds a color key.
   * 
   * @param line The text line.
   * @param color The color.
   */
  public void addKey(final String line, final Color color) {
    colors.add(color);
    super.addLine(pad + line);
  }

  @Override
  protected void drawLine(final Graphics2D gfx, final Point2D cur,
      final StringDrawer sd, final int index, final int hpos, final int vpos) {
    super.drawLine(gfx, cur, sd, index, hpos, vpos);
    final Color color = colors.get(index);
    if(color == null) return;
    final Rectangle2D rect = sd.getBounds(cur, hpos, vpos);
    final double h = rect.getHeight();
    final Rectangle2D box = new Rectangle2D.Double(rect.getX(), rect.getY(), h, h);
    PaintUtil.addPaddingInplace(box, -1);
    final Graphics2D g = (Graphics2D) gfx.create();
    g.setColor(color);
    g.fill(box);
    g.setColor(Color.BLACK);
    g.draw(box);
    g.dispose();
  }

}
