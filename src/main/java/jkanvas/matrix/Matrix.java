package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

import jkanvas.RefreshManager;

/**
 * A matrix.
 *
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public interface Matrix<T> {

  /**
   * Getter.
   *
   * @param row The row.
   * @param col The column.
   * @return The content of the cell.
   */
  T get(int row, int col);

  /**
   * Getter.
   *
   * @param pos The position in the matrix.
   * @return The content of the cell.
   */
  default T get(final MatrixPosition pos) {
    return get(pos.row, pos.col);
  }

  /**
   * Getter.
   *
   * @return The number of rows.
   */
  int rows();

  /**
   * Getter.
   *
   * @return The number of columns.
   */
  int cols();

  /**
   * Getter.
   *
   * @param row The row.
   * @return The name of the row.
   */
  String getRowName(int row);

  /**
   * Getter.
   *
   * @return The names of the matrix rows.
   */
  default String[] getRowNames() {
    final String[] names = new String[rows()];
    for(int i = 0; i < names.length; ++i) {
      names[i] = getRowName(i);
    }
    return names;
  }

  /**
   * Getter.
   *
   * @param col The column.
   * @return The name of the column.
   */
  String getColumnName(int col);

  /**
   * Getter.
   *
   * @return The names of the matrix columns.
   */
  default String[] getColumnNames() {
    final String[] names = new String[cols()];
    for(int i = 0; i < names.length; ++i) {
      names[i] = getColumnName(i);
    }
    return names;
  }

  /**
   * Getter.
   *
   * @param row The row.
   * @return The visual height of the row.
   */
  double getHeight(int row);

  /**
   * Getter.
   *
   * @param col The column.
   * @return The visual width of the column.
   */
  double getWidth(int col);

  /**
   * Getter.
   *
   * @param bbox The rectangle to store the bounding box of the given cell in.
   * @param row The row.
   * @param col The column.
   */
  default void getBoundingBox(final Rectangle2D bbox, final int row, final int col) {
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

  /**
   * Sets the refresh manager of this quadratic matrix. Whenever a value of this
   * matrix changes the refresh manager gets notified. If the manager is
   * <code>null</code> value changes are not reported. The currently installed
   * refresh manager can be notified when values in the matrix change. This
   * operation is optional.
   *
   * @param manager The refresh manager or <code>null</code>.
   */
  void setRefreshManager(RefreshManager manager);

}
