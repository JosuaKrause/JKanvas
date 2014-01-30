package jkanvas.matrix;

import java.awt.geom.Rectangle2D;
import java.util.Objects;

public abstract class PermutedMatrix<T> implements Matrix<T> {

  private final Matrix<T> matrix;

  private final int[] rowPerm;

  private final int[] colPerm;

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

  public void swapRows(final int a, final int b) {
    final int tmp = rowPerm[a];
    rowPerm[a] = rowPerm[b];
    rowPerm[b] = tmp;
  }

  public void swapColumns(final int a, final int b) {
    final int tmp = colPerm[a];
    colPerm[a] = colPerm[b];
    colPerm[b] = tmp;
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

}
