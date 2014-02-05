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
  T get(MatrixPosition pos);

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
  // TODO #43 -- Java 8 simplification
  String[] getRowNames();

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
  // TODO #43 -- Java 8 simplification
  String[] getColumnNames();

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
  void getBoundingBox(Rectangle2D bbox, int row, int col);

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
