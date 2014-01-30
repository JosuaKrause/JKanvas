package jkanvas.matrix;

/**
 * A mutable quadratic matrix.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public interface MutableQuadraticMatrix<T> extends MutableMatrix<T>, QuadraticMatrix<T> {

  /**
   * Setter.
   * 
   * @param row The row / column.
   * @param name The name of the row / column.
   */
  void setName(int row, String name);

}
