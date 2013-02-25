package jkanvas.matrix;

import jkanvas.RefreshManager;

/**
 * A mutable quadratic matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
public interface MutableQuadraticMatrix<T> extends QuadraticMatrix<T> {

  /**
   * Setter.
   * 
   * @param col The column.
   * @param value Sets the visual width of the column.
   */
  void setWidth(final int col, final double value);

  /**
   * Setter.
   * 
   * @param row The row.
   * @param value Sets the visual height of the row.
   */
  void setHeight(final int row, final double value);

  /**
   * Setter.
   * 
   * @param row The row / column.
   * @param name Sets the name of the row / column.
   */
  void setName(final int row, final String name);

  /**
   * Setter.
   * 
   * @param row The row.
   * @param col The column.
   * @param value Sets the content of the cell.
   */
  void set(final int row, final int col, final T value);

  /**
   * Sets the refresh manager of this quadratic matrix. Whenever a value of this
   * matrix changes the refresh manager gets notified. If the manager is
   * <code>null</code> value changes are not reported.
   * 
   * @param manager The refresh manager or <code>null</code>.
   */
  void setRefreshManager(RefreshManager manager);

}
