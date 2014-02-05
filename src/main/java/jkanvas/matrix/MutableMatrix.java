package jkanvas.matrix;


/**
 * A mutable quadratic matrix.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public interface MutableMatrix<T> extends Matrix<T> {

  /**
   * Setter.
   * 
   * @param col The column.
   * @param value Sets the visual width of the column.
   */
  void setWidth(int col, double value);

  /**
   * Setter.
   * 
   * @param row The row.
   * @param value Sets the visual height of the row.
   */
  void setHeight(int row, double value);

  /**
   * Setter.
   * 
   * @param row The row.
   * @param name Sets the name of the row.
   */
  void setRowName(int row, String name);

  /**
   * Setter.
   * 
   * @param col The column.
   * @param name Sets the name of the column.
   */
  void setColumnName(int col, String name);

  /**
   * Setter.
   * 
   * @param row The row.
   * @param col The column.
   * @param value Sets the content of the cell.
   */
  void set(int row, int col, T value);

}
