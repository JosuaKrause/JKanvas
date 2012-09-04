package kanvas.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import kanvas.Canvas;
import kanvas.Context;
import kanvas.painter.RenderpassPainter;
import kanvas.util.MathUtil;

/**
 * Renders an arbitrary background that is completely filled (each pixel has its
 * own color).
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class DenseBackgroundPass implements Renderpass {

  /** The resolution in screen pixels. */
  private int resolution = 10;

  /**
   * Setter.
   * 
   * @param resolution The resolution of the background in screen pixels.
   * @throws IllegalArgumentException When the resolution is smaller than 1
   *           pixel.
   */
  public void setResolution(final int resolution) {
    if(resolution <= 0) throw new IllegalArgumentException(
        "resolution must be >= 1: " + resolution);
    this.resolution = resolution;
  }

  /**
   * Getter.
   * 
   * @return The resolution of the background in screen pixels.
   */
  public double getResolution() {
    return resolution;
  }

  @Override
  public void render(final Graphics2D gfx, final Context ctx) {
    paintBackground(gfx, ctx.getVisibleComponent(), ctx);
  }

  @Override
  public boolean isHUD() {
    return false;
  }

  /**
   * Paints the background in the given rectangle.
   * 
   * @param g2 The graphics context.
   * @param view The rectangle.
   * @param ctx The context.
   */
  private void paintBackground(final Graphics2D g2, final Rectangle2D view,
      final Context ctx) {
    final double resolution = getResolution();
    for(double x = view.getMinX(); x <= view.getMaxX(); x += resolution) {
      for(double y = view.getMinY(); y <= view.getMaxY(); y += resolution) {
        final Rectangle2D pixel = ctx.toCanvasCoordinates(
            new Rectangle2D.Double(x, y, resolution, resolution));
        final Color c = getColorFor(pixel);
        g2.setColor(c);
        g2.fill(pixel);
      }
    }
  }

  /**
   * Returns the color for the given position. This method is guaranteed to be
   * referential transparent, thus returning the same result for same arguments.
   * However after a call to {@link #clearCache()} the results may change.
   * 
   * @param x The x coordinate in canvas coordinates.
   * @param y The y coordinate in canvas coordinates.
   * @return The color at the given position.
   */
  protected abstract Color getColorFor(double x, double y);

  /**
   * Determines the color of a given rectangle. This method may use super
   * sampling to generate more accurate results.
   * 
   * @param r The rectangle.
   * @return The color.
   */
  protected Color getColorFor(final Rectangle2D r) {
    return getColorFor(r.getCenterX(), r.getCenterY());
  }

  /** Clears a color cache if any. */
  public void clearCache() {
    // nothing to do yet
  }

  /**
   * A small testing application.
   * 
   * @param args No args.
   */
  public static void main(final String[] args) {
    final JFrame f = new JFrame("test");
    final Renderpass p = new DenseBackgroundPass() {

      @Override
      protected Color getColorFor(final double x, final double y) {
        final double nx = (x - 400) / 100;
        final double ny = (y - 300) / 100;
        final double h = 120.0;
        final double v = Math.sin(nx) * Math.sin(ny) / (Math.sqrt(nx * nx + ny * ny));
        return Color.getHSBColor((float) h, .8f, (float) (MathUtil.clamp(v)));
      }

    };
    final RenderpassPainter rp = new RenderpassPainter();
    rp.addPass(p);
    f.add(new Canvas(rp, 800, 600));
    f.pack();
    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }

}
