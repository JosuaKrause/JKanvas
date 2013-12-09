package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.GenericPaintList;
import jkanvas.table.PointMapper;

/**
 * A render pass for a scatter plot.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ScatterplotRenderpass extends CachedRenderpass {

  /** The size of the scatter plot. */
  private final double size;
  /** The list of shapes in the scatter plot. */
  protected final GenericPaintList<? extends Shape> list;
  /** The map if any. */
  protected final PointMapper pm;

  /**
   * Creates a scatter plot render pass.
   * 
   * @param pm The point map.
   */
  public ScatterplotRenderpass(final PointMapper pm) {
    this(pm, pm.getList(), pm.getSize());
  }

  /**
   * Creates a scatter plot render pass.
   * 
   * @param list The content list.
   * @param size The size of the render pass.
   */
  public ScatterplotRenderpass(final GenericPaintList<? extends Shape> list,
      final double size) {
    this(null, list, size);
  }

  /**
   * Creates a scatter plot render pass.
   * 
   * @param pm The point map if any.
   * @param list The content list.
   * @param size The size of the render pass.
   */
  private ScatterplotRenderpass(final PointMapper pm,
      final GenericPaintList<? extends Shape> list, final double size) {
    if(size <= 0.0) throw new IllegalArgumentException("" + size);
    this.list = Objects.requireNonNull(list);
    this.size = size;
    this.pm = pm;
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox) {
    bbox.setFrame(0, 0, size, size);
  }

  @Override
  public void doDraw(final Graphics2D g, final KanvasContext ctx) {
    g.setColor(Color.BLACK);
    final Rectangle2D rect = new Rectangle2D.Double();
    getBoundingBox(rect);
    if(ctx.toCanvasLength(1) > 1) {
      g.drawRect(0, 0, (int) rect.getWidth() - 1, (int) rect.getHeight() - 1);
    } else {
      g.draw(rect);
    }
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
