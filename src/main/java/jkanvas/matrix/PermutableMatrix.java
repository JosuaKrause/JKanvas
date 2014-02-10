package jkanvas.matrix;

import java.util.Comparator;

/**
 * A matrix whose rows and columns can be rearranged. To easily add this
 * functionality to any matrix use {@link PermutedMatrix}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 * @see PermutedMatrix
 */
public interface PermutableMatrix<T> extends Matrix<T> {

  /**
   * Swaps two rows.
   * 
   * @param a The first row.
   * @param b The second row.
   */
  void swapRows(int a, int b);

  /**
   * Swaps two columns.
   * 
   * @param a The first column.
   * @param b The second column.
   */
  void swapColumns(int a, int b);

  /**
   * Sorts the rows with the given comparator.
   * 
   * @param cmp The comparator. The index of the rows is handed in.
   */
  void sortRows(Comparator<Integer> cmp);

  /**
   * Sorts the columns with the given comparator.
   * 
   * @param cmp The comparator. The index of the columns is handed in.
   */
  void sortColumns(final Comparator<Integer> cmp);

}
