package jkanvas.util;

/**
 * A data structure that allows swaps on its indices.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Swapable {

  /**
   * Swaps the elements with the given index.
   * 
   * @param a The first index.
   * @param b The second index.
   */
  void swap(int a, int b);

}
