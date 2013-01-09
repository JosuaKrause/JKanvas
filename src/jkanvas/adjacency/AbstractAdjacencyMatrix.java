package jkanvas.adjacency;

import java.util.ArrayList;
import java.util.List;

import jkanvas.Refreshable;

public abstract class AbstractAdjacencyMatrix<T> implements AdjacencyMatrix<T> {

  private final String[] names;

  private final T[][] matrix;

  private final double[] widths;

  private final double[] heights;

  private int bulkOps = 0;

  public AbstractAdjacencyMatrix(final int size) {
    names = new String[size];
    widths = new double[size];
    heights = new double[size];
    matrix = createMatrix(size);
  }

  protected abstract T[][] createMatrix(int size);

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
    return heights[col];
  }

  @Override
  public void setHeight(final int row, final double value) {
    heights[row] = value;
    refreshAll();
  }

  @Override
  public void setWidth(final int col, final double value) {
    heights[col] = value;
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
