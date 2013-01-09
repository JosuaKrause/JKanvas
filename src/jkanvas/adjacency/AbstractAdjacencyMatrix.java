package jkanvas.adjacency;

import java.util.ArrayList;
import java.util.List;

import jkanvas.Refreshable;

public abstract class AbstractAdjacencyMatrix<T> implements AdjacencyMatrix<T> {

  private final String[] names;

  private final T[][] matrix;

  private final double[] widths;

  private final double[] heights;

  public AbstractAdjacencyMatrix(final int size) {
    names = new String[size];
    widths = new double[size];
    heights = new double[size];
    matrix = createMatrix(size);
  }

  protected abstract T[][] createMatrix(int size);

  private final List<Refreshable> refreshables = new ArrayList<>();

  public void addRefreshable(final Refreshable r) {
    if(refreshables.contains(r)) throw new IllegalArgumentException(
        r + " already added");
    refreshables.add(r);
  }

  public void removeRefreshable(final Refreshable r) {
    if(!refreshables.remove(r)) throw new IllegalArgumentException(
        r + " was not contained");
  }

  protected void refreshAll() {
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

}
