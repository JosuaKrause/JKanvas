package jkanvas.adjacency;

import java.awt.geom.Rectangle2D;

import jkanvas.RefreshManager;

/**
 * An adjacency matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
public interface AdjacencyMatrix<T> {

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
   * Sets the refresh manager of this adjacency matrix if the matrix supports
   * automatic refreshing. Otherwise this method is a no-op.
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
   * @return Whether this adjacency matrix supports automatic refreshing.
   */
  boolean supportsAutoRefreshing();

  /**
   * Getter.
   * 
   * @return Whether this adjacency matrix currently uses automatic refreshing.
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
