package jkanvas.matrix;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import jkanvas.RefreshManager;

/**
 * A permuted view on a matrix.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public class PermutedMatrix<T> implements MutableMatrix<T> {

  /** The underlying matrix. */
  private final MutableMatrix<T> matrix;
  /** The row permutations. */
  private final int[] rowPerm;
  /** The column permutations. */
  private final int[] colPerm;

  /**
   * Creates a permuted view on the given matrix.
   * 
   * @param matrix The matrix.
   */
  public PermutedMatrix(final MutableMatrix<T> matrix) {
    this.matrix = Objects.requireNonNull(matrix);
    rowPerm = new int[matrix.rows()];
    for(int i = 0; i < rowPerm.length; ++i) {
      rowPerm[i] = i;
    }
    colPerm = new int[matrix.cols()];
    for(int i = 0; i < colPerm.length; ++i) {
      colPerm[i] = i;
    }
  }

  /**
   * Swaps two rows.
   * 
   * @param a The first row.
   * @param b The second row.
   */
  public void swapRows(final int a, final int b) {
    final int tmp = rowPerm[a];
    rowPerm[a] = rowPerm[b];
    rowPerm[b] = tmp;
    refreshAll();
  }

  /**
   * Swaps two columns.
   * 
   * @param a The first column.
   * @param b The second column.
   */
  public void swapColumns(final int a, final int b) {
    final int tmp = colPerm[a];
    colPerm[a] = colPerm[b];
    colPerm[b] = tmp;
    refreshAll();
  }

  /**
   * Sorts the rows with the given comparator.
   * 
   * @param cmp The comparator. The index of the row is handed in.
   */
  public void sortRows(final Comparator<Integer> cmp) {
    final Integer[] arr = new Integer[rowPerm.length];
    for(int i = 0; i < arr.length; ++i) {
      arr[i] = rowPerm[i] = i; // unsort first
    }
    Arrays.sort(arr, cmp);
    for(int i = 0; i < rowPerm.length; ++i) {
      rowPerm[i] = arr[i];
    }
    refreshAll();
  }

  /**
   * Sorts the columns with the given comparator.
   * 
   * @param cmp The comparator. The index of the column is handed in.
   */
  public void sortColumns(final Comparator<Integer> cmp) {
    final Integer[] arr = new Integer[colPerm.length];
    for(int i = 0; i < arr.length; ++i) {
      arr[i] = colPerm[i] = i; // unsort first
    }
    Arrays.sort(arr, cmp);
    for(int i = 0; i < colPerm.length; ++i) {
      colPerm[i] = arr[i];
    }
    refreshAll();
  }

  @Override
  public int rows() {
    return matrix.rows();
  }

  @Override
  public int cols() {
    return matrix.cols();
  }

  @Override
  public T get(final int row, final int col) {
    return matrix.get(rowPerm[row], colPerm[col]);
  }

  @Override
  public void set(final int row, final int col, final T value) {
    matrix.set(rowPerm[row], colPerm[col], value);
    refreshAll();
  }

  @Override
  public double getWidth(final int col) {
    return matrix.getWidth(colPerm[col]);
  }

  @Override
  public void setWidth(final int col, final double value) {
    matrix.setWidth(colPerm[col], value);
    refreshAll();
  }

  @Override
  public double getHeight(final int row) {
    return matrix.getHeight(rowPerm[row]);
  }

  @Override
  public void setHeight(final int row, final double value) {
    matrix.setHeight(rowPerm[row], value);
    refreshAll();
  }

  @Override
  public String getRowName(final int row) {
    return matrix.getRowName(rowPerm[row]);
  }

  @Override
  public void setRowName(final int row, final String name) {
    matrix.setRowName(rowPerm[row], name);
    refreshAll();
  }

  @Override
  public String[] getRowNames() {
    final String[] res = new String[rows()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = getRowName(i);
    }
    return res;
  }

  @Override
  public String getColumnName(final int col) {
    return matrix.getColumnName(colPerm[col]);
  }

  @Override
  public void setColumnName(final int col, final String name) {
    matrix.setColumnName(colPerm[col], name);
    refreshAll();
  }

  @Override
  public String[] getColumnNames() {
    final String[] res = new String[cols()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = getColumnName(i);
    }
    return res;
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox, final int row, final int col) {
    matrix.getBoundingBox(bbox, rowPerm[row], colPerm[col]);
  }

  /** The refresh manager. */
  private RefreshManager manager;

  @Override
  public void setRefreshManager(final RefreshManager manager) {
    this.manager = manager;
  }

  /**
   * Refreshes all {@link jkanvas.Refreshable Refreshables} if a refresh manager
   * is installed.
   */
  protected void refreshAll() {
    if(manager == null) return;
    manager.refreshAll();
  }

}
