package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

import jkanvas.RefreshManager;

/**
 * A quadratic matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
public interface QuadraticMatrix<T> {

  /**
   * Getter.
   * 
   * @param row The row.
   * @return The visual height of the row.
   */
  double getHeight(final int row);

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The visual width of the column.
   */
  double getWidth(final int col);

  /**
   * Getter.
   * 
   * @param row The row / column.
   * @return The name of the row / column.
   */
  String getName(final int row);

  /**
   * Getter.
   * 
   * @param row The row.
   * @param col The column.
   * @return The content of the cell.
   */
  T get(final int row, final int col);

  /**
   * Getter.
   * 
   * @return The number of rows / columns.
   */
  int size();

  /**
   * Sets the refresh manager of this quadratic matrix if the matrix supports
   * automatic refreshing. If the matrix does not support automatic refreshing
   * this method is a no-op.
   * 
   * @param manager The refresh manager or <code>null</code> if automatic
   *          refreshing is deactivated.
   * @see #supportsAutoRefreshing()
   * @see #isAutoRefreshing()
   */
  void setRefreshManager(RefreshManager manager);

  /**
   * Getter.
   * 
   * @return Whether this quadratic matrix supports automatic refreshing.
   *         Automatic refreshing means that whenever a value is set the refresh
   *         manager installed with {@link #setRefreshManager(RefreshManager)}
   *         is refreshed. For example a {@link MutableQuadraticMatrix} should
   *         support automatic refreshing with every setter refreshing the
   *         {@link RefreshManager}. A {@link QuadraticMatrix} whose data cannot
   *         be altered via the matrix itself needs not to support automatic
   *         refreshing. Consequently {@link #setRefreshManager(RefreshManager)}
   *         should be a no-op and {@link #isAutoRefreshing()} should also
   *         always return <code>false</code>.
   * @see #setRefreshManager(RefreshManager)
   * @see #isAutoRefreshing()
   */
  boolean supportsAutoRefreshing();

  /**
   * Getter.
   * 
   * @return Whether this quadratic matrix currently uses automatic refreshing.
   * @see #setRefreshManager(RefreshManager)
   * @see #supportsAutoRefreshing()
   */
  boolean isAutoRefreshing();

  /**
   * Getter.
   * 
   * @param row The row.
   * @param col The column.
   * @return The bounding box of the given cell.
   */
  Rectangle2D getBoundingBox(int row, int col);

}
