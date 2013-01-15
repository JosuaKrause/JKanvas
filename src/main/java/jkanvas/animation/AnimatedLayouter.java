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
   * @return Animatable positions. Do <em>not</em> call
   *         {@link Iterator#remove()}.
   */
  Iterable<? extends AnimatedPosition> getPositions();

}
