package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.util.PaintUtil;

/**
 * A HUD showing the current frame-rate.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class FrameRateHUD extends HUDRenderpassAdapter {

  /** The canvas to measure the frame-rate. */
  private final Canvas canvas;

  /** The padding of the text box. */
  private final double padding = 5.0;

  /** The alpha value of the text box. */
  private final double alpha = 0.5;

  /** The text color. */
  private final Color TEXT = Color.WHITE;

  /** The text box color. */
  private final Color BACK = Color.BLACK;

  /**
   * Creates a frame-rate HUD for the given canvas.
   * 
   * @param canvas The canvas.
   */
  public FrameRateHUD(final Canvas canvas) {
    this.canvas = canvas;
    canvas.setMeasureFrameTime(true);
  }

  @Override
  public void setVisible(final boolean isVisible) {
    super.setVisible(isVisible);
    canvas.setMeasureFrameTime(isVisible);
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    final long time = canvas.getLastFrameTime();
    if(time == 0) return;
    final double fps = 1e9 / time;
    final Rectangle2D comp = ctx.getVisibleComponent();
    final Point2D pos = new Point2D.Double(
        comp.getMaxX() - padding, comp.getMinY() + padding);
    final StringDrawer sd = new StringDrawer(gfx, "fps: " + format(fps));
    final Graphics2D g = (Graphics2D) gfx.create();
    g.setColor(BACK);
    PaintUtil.setAlpha(g, alpha);
    g.fill(PaintUtil.toRoundRectangle(
        sd.getBounds(pos, StringDrawer.RIGHT, StringDrawer.TOP), padding));
    g.dispose();
    gfx.setColor(TEXT);
    sd.draw(pos, StringDrawer.RIGHT, StringDrawer.TOP);
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
