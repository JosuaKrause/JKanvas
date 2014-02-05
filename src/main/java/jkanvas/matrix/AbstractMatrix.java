package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

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
  // TODO #43 -- Java 8 simplification
  public T get(final MatrixPosition pos) {
    return get(pos.row, pos.col);
  }

  @Override
  // TODO #43 -- Java 8 simplification
  public String[] getRowNames() {
    final String[] names = new String[rows()];
    for(int i = 0; i < names.length; ++i) {
      names[i] = getRowName(i);
    }
    return names;
  }

  @Override
  // TODO #43 -- Java 8 simplification
  public String[] getColumnNames() {
    final String[] names = new String[cols()];
    for(int i = 0; i < names.length; ++i) {
      names[i] = getColumnName(i);
    }
    return names;
  }

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    this.manager = manager;
  }

  @Override
  // TODO #43 -- Java 8 simplification
  public void getBoundingBox(final Rectangle2D bbox, final int row, final int col) {
    double x = 0;
    double y = 0;
    for(int i = 0; i < col; ++i) {
      x += getWidth(i);
    }
    for(int i = 0; i < row; ++i) {
      y += getHeight(i);
    }
    bbox.setFrame(x, y, getWidth(col), getHeight(row));
  }

}
