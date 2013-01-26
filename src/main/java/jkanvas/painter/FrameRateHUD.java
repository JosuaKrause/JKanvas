package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;
import jkanvas.KanvasContext;

/**
 * A HUD showing the current frame-rate.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class FrameRateHUD extends HUDRenderpassAdapter {

  /** The canvas to measure the frame-rate. */
  private final Canvas canvas;

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
    final Point2D pos = new Point2D.Double(comp.getMaxX(), comp.getMinY());
    gfx.setColor(Color.BLACK);
    StringDrawer.drawText(gfx, "fps: " + fps, pos, StringDrawer.RIGHT, StringDrawer.TOP);
  }

}
