package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.LineList;
import jkanvas.table.LineMapper;
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
   * Creates a parallel coordinates cell from a table.
   * 
   * @param map The line map.
   * @param alpha The transparency of the lines.
   */
  public ParallelRenderpass(final LineMapper map, final double alpha) {
    this(map.getList(), map.getWidth(), map.getHeight(), alpha);
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
  public void getBoundingBox(final Rectangle2D bbox) {
    bbox.setFrame(0, 0, width, height);
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
