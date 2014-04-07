package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.LineList;
import jkanvas.table.LineMapper;

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

  /**
   * Creates a parallel coordinates cell from a table.
   * 
   * @param map The line map.
   */
  public ParallelRenderpass(final LineMapper map) {
    this(map.getList(), map.getWidth(), map.getHeight());
  }

  /**
   * Creates a parallel coordinates cell from a list.
   * 
   * @param list The line list.
   * @param width The width of the cell.
   * @param height The height of the cell.
   */
  public ParallelRenderpass(final LineList list,
      final double width, final double height) {
    if(width <= 0.0) throw new IllegalArgumentException("" + width);
    if(height <= 0.0) throw new IllegalArgumentException("" + height);
    this.list = Objects.requireNonNull(list);
    list.optimize();
    this.width = width;
    this.height = height;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
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
