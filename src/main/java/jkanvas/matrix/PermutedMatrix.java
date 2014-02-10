package jkanvas.matrix;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import jkanvas.util.ArrayUtil;

/**
 * A permuted view on a matrix. The size of the underlying matrix must not
 * change.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public class PermutedMatrix<T> extends AbstractMatrix<T> implements PermutableMatrix<T> {

  /** The underlying matrix. */
  private final Matrix<T> matrix;
  /** The row permutations. */
  private final int[] rowPerm;
  /** The column permutations. */
  private final int[] colPerm;

  /**
   * Creates a permuted view on the given matrix.
   * 
   * @param matrix The matrix.
   */
  public PermutedMatrix(final Matrix<T> matrix) {
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

  @Override
  public void swapRows(final int a, final int b) {
    ArrayUtil.swap(rowPerm, a, b);
    refreshAll();
  }

  @Override
  public void swapColumns(final int a, final int b) {
    ArrayUtil.swap(colPerm, a, b);
    refreshAll();
  }

  @Override
  public void sortRows(final Comparator<Integer> cmp) {
    final Integer[] arr = new Integer[rowPerm.length];
    for(int i = 0; i < arr.length; ++i) {
      arr[i] = rowPerm[i];
    }
    Arrays.sort(arr, cmp);
    for(int i = 0; i < rowPerm.length; ++i) {
      rowPerm[i] = arr[i];
    }
    refreshAll();
  }

  @Override
  public void sortColumns(final Comparator<Integer> cmp) {
    final Integer[] arr = new Integer[colPerm.length];
    for(int i = 0; i < arr.length; ++i) {
      arr[i] = colPerm[i];
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

  /**
   * Computes the permutation of the given position.
   * 
   * @param pos The position.
   * @return The actual position in the underlying matrix.
   */
  public MatrixPosition permute(final MatrixPosition pos) {
    if(pos == null) return null;
    return new MatrixPosition(rowPerm[pos.row], colPerm[pos.col]);
  }

  @Override
  public T get(final int row, final int col) {
    return matrix.get(rowPerm[row], colPerm[col]);
  }

  @Override
  public double getWidth(final int col) {
    return matrix.getWidth(colPerm[col]);
  }

  @Override
  public double getHeight(final int row) {
    return matrix.getHeight(rowPerm[row]);
  }

  @Override
  public String getRowName(final int row) {
    return matrix.getRowName(rowPerm[row]);
  }

  @Override
  public String getColumnName(final int col) {
    return matrix.getColumnName(colPerm[col]);
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox, final int row, final int col) {
    matrix.getBoundingBox(bbox, rowPerm[row], colPerm[col]);
  }

}
