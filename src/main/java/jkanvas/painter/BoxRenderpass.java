package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import jkanvas.KanvasContext;

/**
 * A simple box render pass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class BoxRenderpass extends Renderpass {

  /** The box. */
  private final Rectangle2D rect;
  /** The border color or <code>null</code> if the border is not drawn. */
  private Color border;
  /** The fill color or <code>null</code> if the filling is not drawn. */
  private Color fill;

  /**
   * Creates a box render pass.
   * 
   * @param rect The rectangle.
   */
  public BoxRenderpass(final Rectangle2D rect) {
    this(rect, null, null);
  }

  /**
   * Creates a box render pass.
   * 
   * @param rect The rectangle.
   * @param fill The filling color or <code>null</code> if the filling is not
   *          drawn.
   * @param border The border color or <code>null</code> if the border is not
   *          drawn.
   */
  public BoxRenderpass(final Rectangle2D rect, final Color fill, final Color border) {
    this.rect = new Rectangle2D.Double();
    this.fill = fill;
    this.border = border;
    setBoundingBox(rect);
  }

  /**
   * Setter.
   * 
   * @param rect The bounding box.
   */
  public void setBoundingBox(final Rectangle2D rect) {
    this.rect.setFrame(rect);
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    bbox.setFrame(rect);
  }

  /**
   * Setter.
   * 
   * @param fill The filling color or <code>null</code> if the filling is not
   *          drawn.
   */
  public void setFill(final Color fill) {
    this.fill = fill;
  }

  /**
   * Getter.
   * 
   * @return The filling color or <code>null</code> if the filling is not drawn.
   */
  public Color getFill() {
    return fill;
  }

  /**
   * Setter.
   * 
   * @param border The border color or <code>null</code> if the border is not
   *          drawn.
   */
  public void setBorder(final Color border) {
    this.border = border;
  }

  /**
   * Getter.
   * 
   * @return The border color or <code>null</code> if the border is not drawn.
   */
  public Color getBorder() {
    return border;
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    if(fill == border && fill == null) return;
    if(fill != null) {
      g.setColor(fill);
      g.fill(rect);
    }
    if(border == null) return;
    g.setColor(border);
    if(ctx.toCanvasLength(1) > 1) {
      g.drawRect(0, 0, (int) rect.getWidth() - 1, (int) rect.getHeight() - 1);
    } else {
      g.draw(rect);
    }
  }

}
