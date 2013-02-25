package jkanvas.animation;

import java.util.Iterator;

/**
 * An animatable layouter.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface AnimatedLayouter {

  /**
   * Getter.
   * 
   * @return Animatable objects. Do <em>not</em> call {@link Iterator#remove()}.
   */
  Iterable<? extends Animated> getPositions();

}
