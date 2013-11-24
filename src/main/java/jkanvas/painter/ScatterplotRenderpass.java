package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.GenericPaintList;

public class ScatterplotRenderpass extends AbstractRenderpass {

  private final GenericPaintList<? extends Shape> list;
  private final double size;

  public ScatterplotRenderpass(
      final GenericPaintList<? extends Shape> list, final double size) {
    if(size <= 0.0) throw new IllegalArgumentException("" + size);
    this.list = Objects.requireNonNull(list);
    this.size = size;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(0, 0, size, size);
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    g.setColor(Color.BLACK);
    g.draw(getBoundingBox());
    list.paintAll(g);
  }

}
