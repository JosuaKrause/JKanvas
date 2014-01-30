package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

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

}
