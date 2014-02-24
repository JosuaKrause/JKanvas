package jkanvas.painter.pod;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;

/**
 * Shows borders of render passes.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The innermost wrapped type.
 */
public class BorderRenderpass<T extends Renderpass> extends Renderpod<T> {

  /** The stroke width. */
  private double width;
  /** The border color. */
  private Color border;

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   */
  public BorderRenderpass(final T pass) {
    this(pass, Color.BLACK, 1.0);
  }

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   * @param border The border color or <code>null</code> if transparent.
   * @param width The stroke width.
   */
  public BorderRenderpass(final T pass, final Color border, final double width) {
    super(pass);
    this.border = border;
    this.width = width;
  }

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   * @param border The border color or <code>null</code> if transparent.
   * @param width The stroke width.
   */
  public BorderRenderpass(final Renderpod<T> pass,
      final Color border, final double width) {
    super(pass);
    this.border = border;
    this.width = width;
  }

  /**
   * Setter.
   * 
   * @param border The color of the border or <code>null</code> if transparent.
   */
  public void setColor(final Color border) {
    this.border = border;
  }

  /**
   * Getter.
   * 
   * @return The color of the border or <code>null</code> if transparent.
   */
  public Color getColor() {
    return border;
  }

  /**
   * Setter.
   * 
   * @param width The width of the border.
   */
  public void setBorderWidth(final double width) {
    this.width = width;
  }

  /**
   * Getter.
   * 
   * @return The width of the border.
   */
  public double getBorderWidth() {
    return width;
  }

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    if(border == null) return;
    final Rectangle2D box = new Rectangle2D.Double();
    getInnerBoundingBox(box);
    box.setFrame(box.getX() - width * 0.5, box.getY() - width * 0.5,
        box.getWidth() + width, box.getHeight() + width);
    g.setColor(border);
    g.setStroke(new BasicStroke((float) width));
    g.draw(box);
  }

  @Override
  protected void addOwnBox(final RectangularShape bbox) {
    bbox.setFrame(bbox.getX() - width, bbox.getY() - width,
        bbox.getWidth() + 2 * width, bbox.getHeight() + 2 * width);
  }

}
