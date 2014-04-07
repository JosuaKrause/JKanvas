package jkanvas.table.bin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;

/**
 * A render pass to paint histograms for {@link ColumnBinner column bins}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class HistogramRenderpass extends Renderpass {

  /** The width of the render pass. */
  private final double width;
  /** The height of the render pass. */
  private final double height;
  /** The column bins. */
  private ColumnBinner binner;

  /**
   * Creates a histogram for the given column bins.
   * 
   * @param binner The column bins.
   * @param width The width of the render pass.
   * @param height The height of the render pass.
   */
  public HistogramRenderpass(final ColumnBinner binner,
      final double width, final double height) {
    this.binner = binner;
    if(width <= 0 || height <= 0) throw new IllegalArgumentException(
        "invalid size: " + width + "x" + height);
    this.width = width;
    this.height = height;
  }

  /**
   * Setter.
   * 
   * @param binner The column bins or <code>null</code> if only an empty
   *          rectangle should be shown.
   */
  public void setBinner(final ColumnBinner binner) {
    this.binner = binner;
  }

  /**
   * Getter.
   * 
   * @return The current column bins or <code>null</code> if only an empty
   *         rectangle is shown.
   */
  public ColumnBinner getBinner() {
    return binner;
  }

  /** The filling color. */
  private Color color = new Color(0x43a2ca);

  /**
   * Setter.
   * 
   * @param color The filling color. <code>null</code> is no filling.
   */
  public void setColor(final Color color) {
    this.color = color;
  }

  /**
   * Getter.
   * 
   * @return The filling color or <code>null</code> if the bars are not filled.
   */
  public Color getColor() {
    return color;
  }

  /** The border color. */
  private Color border = Color.BLACK;

  /**
   * Setter.
   * 
   * @param border The border color or <code>null</code> if no border is drawn.
   */
  public void setBorder(final Color border) {
    this.border = border;
  }

  /**
   * Getter.
   * 
   * @return The border color or <code>null</code> if no border is drawn.
   */
  public Color getBorder() {
    return border;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    bbox.setFrame(0, 0, width, height);
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    if(border == null && color == null) return;
    if(binner != null) {
      final Rectangle2D rect = new Rectangle2D.Double();
      final int bins = binner.bins();
      final double binWidth = binner.getTotalWidth();
      final double binHeight = binner.getMaxCount();
      double curMin = binner.getMinValueOf(0);
      double x = 0.0;
      for(int b = 0; b < bins; ++b) {
        final double curMax = binner.getMinValueOf(b + 1);
        final double w = (curMax - curMin) * width / binWidth;
        final double h = binner.getCountOf(b) * height / binHeight;
        rect.setFrame(x, height - h, w, h);
        if(color != null) {
          g.setColor(color);
          g.fill(rect);
        }
        if(border != null) {
          g.setColor(border);
          g.draw(rect);
        }
        x += w;
        curMin = curMax;
      }
    }
  }

}
