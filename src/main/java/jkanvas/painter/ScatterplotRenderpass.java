package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.GenericPaintList;
import jkanvas.animation.PointList;
import jkanvas.table.CachedTable;

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
   * Creates a point list for the given features.
   * 
   * @param table The table.
   * @param f1 The first feature.
   * @param f2 The second feature.
   * @param size The size of the scatter plot.
   * @param pointSize The point size.
   * @return The point list.
   */
  private static PointList createPoints(final CachedTable table,
      final int f1, final int f2, final double size, final double pointSize) {
    final int rows = table.rows();
    final PointList pl = new PointList(rows, Color.BLACK, null);
    for(int el = 0; el < rows; ++el) {
      final int i = pl.addPoint(table.getMinMaxScaled(el, f1) * size,
          table.getMinMaxScaled(el, f2) * size, pointSize);
      if(i != el) throw new IllegalStateException(
          "unpredicted index: " + i + " != " + el);
    }
    return pl;
  }

  /**
   * Creates a scatter plot render pass.
   * 
   * @param table The table.
   * @param f1 The first feature.
   * @param f2 The second feature.
   * @param size The size of the scatter plot.
   * @param pointSize The point size.
   */
  public ScatterplotRenderpass(final CachedTable table,
      final int f1, final int f2, final double pointSize, final double size) {
    this(createPoints(table, f1, f2, size, pointSize), size);
  }

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
    final Rectangle2D rect = getBoundingBox();
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
