package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

import jkanvas.RefreshManager;
import jkanvas.nodelink.GraphView;

/**
 * An adjacency matrix using a {@link GraphView} as data storage.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The graph view type.
 */
public class GraphMatrix<T extends GraphView> implements QuadraticMatrix<Boolean> {

  /** The underlying view. */
  private final T view;

  /** The size of cells. */
  private final double size;

  /**
   * Creates an adjacency matrix using the given graph view.
   * 
   * @param view The view.
   * @param size The size of cells.
   */
  public GraphMatrix(final T view, final double size) {
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
  public String getRowName(final int row) {
    return getName(row);
  }

  @Override
  public String getColumnName(final int col) {
    return getName(col);
  }

  @Override
  public String[] getNames() {
    final String[] names = new String[cols()];
    for(int i = 0; i < names.length; ++i) {
      names[i] = getName(i);
    }
    return names;
  }

  @Override
  public String[] getRowNames() {
    return getNames();
  }

  @Override
  public String[] getColumnNames() {
    return getNames();
  }

  @Override
  public int size() {
    return view.nodeCount();
  }

  @Override
  public int rows() {
    return size();
  }

  @Override
  public int cols() {
    return size();
  }

  @Override
  public Boolean get(final int row, final int col) {
    return areConnected(row, col);
  }

  @Override
  // TODO #43 -- Java 8 simplification
  public Boolean get(final MatrixPosition pos) {
    return get(pos.row, pos.col);
  }

  /**
   * Getter.
   * 
   * @return Whether the underlying graph is directed.
   */
  public boolean isDirected() {
    return view.isDirected();
  }

  /**
   * Getter.
   * 
   * @param a The edge start id ie the row.
   * @param b The edge end id ie the column.
   * @return Whether there is an edge.
   */
  public boolean areConnected(final int a, final int b) {
    return view.areConnected(a, b);
  }

  /**
   * Getter.
   * 
   * @return The view.
   */
  protected final T getView() {
    return view;
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox, final int row, final int col) {
    bbox.setFrame(row * size, col * size, size, size);
  }

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    // nothing to do
  }

}
