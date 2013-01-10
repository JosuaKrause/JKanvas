package jkanvas.adjacency;

import java.util.ArrayList;
import java.util.List;

import jkanvas.Refreshable;

/**
 * A straight forward implementation of a dense {@link AdjacencyMatrix}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
public abstract class AbstractAdjacencyMatrix<T> implements AdjacencyMatrix<T> {

  /** The row / column names. */
  private final String[] names;

  /** The actual matrix. */
  private final T[][] matrix;

  /** The widths of the columns. */
  private final double[] widths;

  /** The heights of the rows. */
  private final double[] heights;

  /** The number of active bulk operations. */
  private int bulkOps = 0;

  /**
   * Creates an adjacency matrix with the given size.
   * 
   * @param size The size.
   */
  public AbstractAdjacencyMatrix(final int size) {
    names = new String[size];
    widths = new double[size];
    heights = new double[size];
    matrix = createMatrix(size);
  }

  /**
   * Creates a quadratic array with the given number of rows / columns.
   * 
   * @param size The number of rows / columns.
   * @return An quadratic array.
   */
  protected abstract T[][] createMatrix(int size);

  /** The list containing all refreshables. */
  private final List<Refreshable> refreshables = new ArrayList<>();

  @Override
  public void addRefreshable(final Refreshable r) {
    if(refreshables.contains(r)) return;
    refreshables.add(r);
  }

  @Override
  public void removeRefreshable(final Refreshable r) {
    refreshables.remove(r);
  }

  @Override
  public Refreshable[] getRefreshables() {
    return refreshables.toArray(new Refreshable[refreshables.size()]);
  }

  @Override
  public void inheritRefreshables(final AdjacencyMatrix<T> matrix) {
    for(final Refreshable r : matrix.getRefreshables()) {
      addRefreshable(r);
    }
  }

  @Override
  public void refreshAll() {
    if(bulkOps > 0) return;
    for(final Refreshable r : refreshables) {
      r.refresh();
    }
  }

  @Override
  public double getHeight(final int row) {
    return heights[row];
  }

  @Override
  public double getWidth(final int col) {
    return widths[col];
  }

  @Override
  public void setHeight(final int row, final double value) {
    heights[row] = value;
    refreshAll();
  }

  @Override
  public void setWidth(final int col, final double value) {
    widths[col] = value;
    refreshAll();
  }

  @Override
  public String getName(final int row) {
    return names[row] == null ? "" : names[row];
  }

  @Override
  public void setName(final int row, final String name) {
    names[row] = name;
    refreshAll();
  }

  @Override
  public T get(final int row, final int col) {
    return matrix[row][col];
  }

  @Override
  public void set(final int row, final int col, final T value) {
    matrix[row][col] = value;
    refreshAll();
  }

  @Override
  public int size() {
    return names.length;
  }

  @Override
  public void startBulkOperation() {
    ++bulkOps;
  }

  @Override
  public void endBulkOperation() {
    --bulkOps;
    refreshAll();
  }

}
