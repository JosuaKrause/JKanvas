package jkanvas.table;

/**
 * Defines a metric between objects. It must obey the following rules for all
 * <code>a</code> and <code>b</code> with type <code>T</code>:
 * <ul>
 * <li><em>positivity</em>: <code>distance(a, b) >= 0.0</code>
 * <li><em>reflexivity</em>: <code>(a == b) <==> (distance(a, b) == 0.0)</code>
 * <li><em>symmetry</em>: <code>distance(a, b) == distance(b, a)</code>
 * </ul>
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The object type.
 */
public interface Metric<T> {

  /**
   * The distance between two elements.
   * 
   * @param a The first element.
   * @param b The second element.
   * @return The distance between them.
   */
  double distance(T a, T b);

}
