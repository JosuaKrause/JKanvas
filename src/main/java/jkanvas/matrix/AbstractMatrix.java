package jkanvas.matrix;

import jkanvas.RefreshManager;

/**
 * An abstract minimal implementation of a {@link Matrix}. If possible methods
 * should be overwritten for performance.
 *
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public abstract class AbstractMatrix<T> implements Matrix<T> {

  /** The refresh manager. */
  private RefreshManager manager;

  /**
   * Refreshes all {@link jkanvas.Refreshable Refreshables} if a refresh manager
   * is installed.
   */
  protected void refreshAll() {
    if(manager == null) return;
    manager.refreshAll();
  }

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    this.manager = manager;
  }

}
