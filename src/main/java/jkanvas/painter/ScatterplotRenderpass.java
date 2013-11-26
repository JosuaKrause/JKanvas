package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.GenericPaintList;

/**
 * A render pass for a scatter plot.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ScatterplotRenderpass extends CachedRenderpass {

  /** The list of shapes in the scatter plot. */
  private final GenericPaintList<? extends Shape> list;
  /** The size of the scatter plot. */
  private final double size;

  /**
   * Creates a scatter plot render pass.
   * 
   * @param list The content list.
   * @param size The size of the render pass.
   */
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
  public void doDraw(final Graphics2D g, final KanvasContext ctx) {
    g.setColor(Color.BLACK);
    g.draw(getBoundingBox());
    list.paintAll(g);
  }

  /** Whether the underlying data structure has been changed. */
  private boolean hasChanged;

  /** Signals that the underlying data source has changed. */
  public void change() {
    hasChanged = true;
  }

  @Override
  public boolean isChanging() {
    final boolean res = hasChanged;
    hasChanged = false;
    return res;
  }

}
