package jkanvas.adjacency;

import jkanvas.Refreshable;

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
   * Setter.
   * 
   * @param row The row.
   * @param value Sets the visual height of the row.
   */
  void setHeight(final int row, final double value);

  /**
   * Setter.
   * 
   * @param col The column.
   * @param value Sets the visual width of the column.
   */
  void setWidth(final int col, final double value);

  /**
   * Getter.
   * 
   * @param row The row / column.
   * @return The name of the row / column.
   */
  String getName(final int row);

  /**
   * Setter.
   * 
   * @param row The row / column.
   * @param name Sets the name of the row / column.
   */
  void setName(final int row, final String name);

  /**
   * Getter.
   * 
   * @param row The row.
   * @param col The column.
   * @return The content of the cell.
   */
  T get(final int row, final int col);

  /**
   * Setter.
   * 
   * @param row The row.
   * @param col The column.
   * @param value Sets the content of the cell.
   */
  void set(final int row, final int col, final T value);

  /**
   * Getter.
   * 
   * @return The number of rows / columns.
   */
  int size();

  /**
   * Adds a {@link Refreshable} that is refreshed each time a value gets
   * updated. If the {@link Refreshable} is already in the list this is a no-op.
   * 
   * @param r The {@link Refreshable}.
   */
  void addRefreshable(Refreshable r);

  /**
   * Removes a {@link Refreshable}. If the {@link Refreshable} is not in the
   * list this is a no-op.
   * 
   * @param r The {@link Refreshable}.
   */
  void removeRefreshable(Refreshable r);

  /**
   * Getter.
   * 
   * @return An array of the installed {@link Refreshable Refreshables}.
   *         Modifications to the array are not reflected in the actual
   *         {@link Refreshable Refreshables}.
   */
  Refreshable[] getRefreshables();

  /**
   * Adds all {@link Refreshable Refreshables} from the other matrix.
   * 
   * @param matrix The other matrix.
   */
  void inheritRefreshables(AdjacencyMatrix<T> matrix);

  /**
   * Refreshes all {@link Refreshable Refreshables}. This method gets called
   * from all setters. If the matrix is currently in a bulk operation this
   * method is a no-op.
   */
  void refreshAll();

  /**
   * Starts a bulk operation. During a bulk operation every call to
   * {@link #refreshAll()} is a no-op. Bulk operations can stack.
   */
  void startBulkOperation();

  /**
   * Ends a bulk operation. After the last bulk operation has been terminated
   * all {@link Refreshable Refreshables} get updated.
   */
  void endBulkOperation();

}
