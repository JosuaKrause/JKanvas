package jkanvas;

/**
 * Manages refreshing of various resources.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface RefreshManager extends Refreshable {

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

  /**
   * Getter.
   * 
   * @return Whether a bulk operation is currently in progress.
   */
  boolean inBulkOperation();

}
