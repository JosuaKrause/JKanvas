package jkanvas.adjacency;

import jkanvas.RefreshManager;
import jkanvas.nodelink.GraphView;

/**
 * An adjacency matrix using a {@link GraphView} as data storage.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class GraphMatrix implements AdjacencyMatrix<Boolean> {

  /** The underlying view. */
  private final GraphView view;

  /** The size of cells. */
  private final double size;

  /**
   * Creates an adjacency matrix using the given graph view.
   * 
   * @param view The view.
   * @param size The size of cells.
   */
  public GraphMatrix(final GraphView view, final double size) {
    this.view = view;
    this.size = size;
  }

  @Override
  public double getWidth(final int col) {
    return size;
  }

  @Override
  public double getHeight(final int row) {
    return size;
  }

  @Override
  public String getName(final int row) {
    return view.getName(row);
  }

  @Override
  public int size() {
    return view.nodeCount();
  }

  @Override
  public Boolean get(final int row, final int col) {
    return view.areConnected(row, col);
  }

  @Override
  public boolean isAutoRefreshing() {
    return false;
  }

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    // no-op
  }

  @Override
  public boolean supportsAutoRefreshing() {
    return false;
  }

}
