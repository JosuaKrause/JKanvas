package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.LineList;
import jkanvas.table.CachedTable;
import jkanvas.util.PaintUtil;

/**
 * A render pass for one parallel coordinates cell.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ParallelRenderpass extends CachedRenderpass {

  /** The width of the cell. */
  private final double width;
  /** The height of the cell. */
  private final double height;
  /** The list of lines. */
  private final LineList list;
  /** The transparency of the lines. */
  private final double alpha;

  /**
   * Creates lines from a table.
   * 
   * @param table The table.
   * @param f1 The feature of the left side.
   * @param f2 The feature of the right side.
   * @param width The width of the cell.
   * @param height The height of the cell.
   * @return The line list.
   */
  private static LineList createLines(final CachedTable table,
      final int f1, final int f2, final double width, final double height) {
    final int rows = table.rows();
    final LineList pl = new LineList(rows, Color.BLACK);
    for(int el = 0; el < rows; ++el) {
      final int i = pl.addLine(0, (1 - table.getMinMaxScaled(el, f1)) * height,
          width, (1 - table.getMinMaxScaled(el, f2)) * height);
      if(i != el) throw new IllegalStateException(
          "unpredicted index: " + i + " != " + el);
    }
    return pl;
  }

  /**
   * Creates a parallel coordinates cell from a table.
   * 
   * @param table The table.
   * @param f1 The left feature.
   * @param f2 The right feature.
   * @param width The width of the cell.
   * @param height The height of the cell.
   * @param alpha The transparency of the lines.
   */
  public ParallelRenderpass(final CachedTable table, final int f1, final int f2,
      final double width, final double height, final double alpha) {
    this(createLines(table, f1, f2, width, height), width, height, alpha);
  }

  /**
   * Creates a parallel coordinates cell from a list.
   * 
   * @param list The line list.
   * @param width The width of the cell.
   * @param height The height of the cell.
   * @param alpha The transparency of the lines.
   */
  public ParallelRenderpass(final LineList list,
      final double width, final double height, final double alpha) {
    if(width <= 0.0) throw new IllegalArgumentException("" + width);
    if(height <= 0.0) throw new IllegalArgumentException("" + height);
    if(alpha <= 0 || alpha > 1) throw new IllegalArgumentException("" + alpha);
    this.list = Objects.requireNonNull(list);
    this.width = width;
    this.height = height;
    this.alpha = alpha;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(0, 0, width, height);
  }

  /**
   * Getter.
   * 
   * @return The lines.
   */
  public LineList getList() {
    return list;
  }

  @Override
  protected void doDraw(final Graphics2D g, final KanvasContext ctx) {
    if(alpha < 1) {
      PaintUtil.setAlpha(g, alpha);
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
