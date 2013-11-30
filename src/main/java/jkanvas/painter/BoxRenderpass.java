package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

public class BoxRenderpass extends AbstractRenderpass {

  private Rectangle2D rect;

  private Color border;

  private Color fill;

  public BoxRenderpass(final Rectangle2D rect) {
    this(rect, null, null);
  }

  public BoxRenderpass(final Rectangle2D rect, final Color fill, final Color border) {
    setBoundingBox(rect);
    this.fill = fill;
    this.border = border;
  }

  public void setBoundingBox(final Rectangle2D rect) {
    this.rect = new Rectangle2D.Double(
        rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(
        rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
  }

  public void setFill(final Color fill) {
    this.fill = fill;
  }

  public Color getFill() {
    return fill;
  }

  public void setBorder(final Color border) {
    this.border = border;
  }

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
