package jkanvas.matrix;

/**
 * A quadratic matrix.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public interface QuadraticMatrix<T> extends Matrix<T> {

  /**
   * Getter.
   * 
   * @param row The row / column.
   * @return The name of the row / column.
   */
  String getName(int row);

  /**
   * Getter.
   * 
   * @return The names of the matrix rows / columns.
   */
  // TODO #43 -- Java 8 simplification
  String[] getNames();

  /**
   * Getter.
   * 
   * @return The number of rows / columns.
   */
  int size();

}
