package jkanvas.table;

import java.awt.Color;
import java.awt.Shape;

import jkanvas.animation.CircleList;
import jkanvas.animation.PointList;

/**
 * Maps points to rows in a table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class PointMapper extends ListMapper<PointList<? extends Shape>> {

  /** The first feature. */
  private final int f1;
  /** The second feature. */
  private final int f2;
  /** The size of the area of points. */
  private final double size;
  /** The size of the points. */
  private final double pointSize;

  /**
   * Creates a point map.
   * 
   * @param table The table.
   * @param f1 The first feature.
   * @param f2 The second feature.
   * @param size The size of the area of points.
   * @param pointSize The size of the points.
   */
  public PointMapper(final DataTable table, final int f1, final int f2,
      final double size, final double pointSize) {
    super(table);
    this.f1 = f1;
    this.f2 = f2;
    this.size = size;
    this.pointSize = pointSize;
  }

  /** The point list factory if any. */
  private PointListFactory factory;

  /**
   * Setter.
   * 
   * @param factory The point list factory or <code>null</code> if a circle list
   *          should be created.
   */
  public void setPointListFactory(final PointListFactory factory) {
    this.factory = factory;
  }

  @Override
  protected PointList<? extends Shape> createList() {
    final DataTable table = getTable();
    final int rows = table.rows();
    if(factory == null) return new CircleList(rows, Color.BLACK, null);
    return factory.createPointList(table, rows, Color.BLACK, null);
  }

  @Override
  protected int createForRow(final PointList<? extends Shape> pl, final int r) {
    final DataTable table = getTable();
    return pl.addPoint(table.getMinMaxScaled(r, f1) * size,
        table.getMinMaxScaled(r, f2) * size, pointSize);
  }

  /**
   * Getter.
   * 
   * @return The size of the area of points.
   */
  public double getSize() {
    return size;
  }

}
