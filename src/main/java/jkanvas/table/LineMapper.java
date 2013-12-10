package jkanvas.table;

import java.awt.Color;

import jkanvas.animation.LineList;

/**
 * Maps lines to the corresponding rows in a table. All lines are contained in a
 * cell going from the origin to the specified coordinates.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class LineMapper extends ListMapper<LineList> {

  /** The first feature. */
  private final int f1;
  /** The second feature. */
  private final int f2;
  /** The width of the cell. */
  private final double w;
  /** The height of the cell. */
  private final double h;
  /** The alpha value of the lines. */
  private final double alpha;

  /**
   * Creates a line map.
   * 
   * @param table The table.
   * @param f1 The first feature.
   * @param f2 The second feature.
   * @param w The width of the cell.
   * @param h The height of the cell.
   * @param alpha The alpha value of the lines.
   */
  public LineMapper(final DataTable table, final int f1, final int f2,
      final double w, final double h, final double alpha) {
    super(table);
    if(alpha < 0 || alpha > 1) throw new IllegalArgumentException("" + alpha);
    this.f1 = f1;
    this.f2 = f2;
    this.w = w;
    this.h = h;
    this.alpha = alpha;
  }

  @Override
  protected LineList createList() {
    return new LineList(getTable().rows(), Color.BLACK);
  }

  @Override
  protected int createForRow(final LineList ll, final int r) {
    final DataTable table = getTable();
    return ll.addLine(0, (1 - table.getMinMaxScaled(r, f1)) * h,
        w, (1 - table.getMinMaxScaled(r, f2)) * h, alpha);
  }

  /**
   * Getter.
   * 
   * @return The width of the cell.
   */
  public double getWidth() {
    return w;
  }

  /**
   * Getter.
   * 
   * @return The height of the cell.
   */
  public double getHeight() {
    return h;
  }

}
