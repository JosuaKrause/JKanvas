package kanvas.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import kanvas.Canvas;
import kanvas.Context;
import kanvas.Painter;

/**
 * Renders an arbitrary background that is completely filled (each pixel has its
 * own color).
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class BackgroundPainter implements Renderpass {

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
    if(resolution <= 0) throw new IllegalArgumentException("resolution must be >= 1: "+resolution)
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
    paintBackground(gfx, ctx);
  }

  /**
   * Paints the background.
   * 
   * @param g2
   * @param ctx
   */
  private void paintBackground(final Graphics2D g2, final Context ctx) {
    final double resolution = getResolution();
    final Rectangle2D view = ctx.getVisibleComponent();
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

  protected abstract Color getColorFor(double x, double y);

  protected Color getColorFor(final Rectangle2D r) {
    return getColorFor(r.getCenterX(), r.getCenterY());
  }

  public void clearCache() {
    // nothing to do yet
  }

  public static void main(final String[] args) {
    final JFrame f = new JFrame("test");
    final Painter p = new BackgroundPainter() {

      @Override
      protected Color getColorFor(final double x, final double y) {
        return Color.getHSBColor((float) (x % 1.0), (float) Math.abs(Math.sin(x * y)),
            (float) Math.abs(Math.cos(y)));
      }

      @Override
      protected void drawForeground(final Graphics2D gfx, final Context ctx) {
        // nothing to do
      }

    };
    f.add(new Canvas(p, 800, 600));
    f.pack();
    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }

}
