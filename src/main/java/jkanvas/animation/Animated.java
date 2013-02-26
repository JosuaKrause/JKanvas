package jkanvas.animation;

/**
 * An animatable object.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Animated {

  /**
   * Animates the position.
   * 
   * @param currentTime The current time in milliseconds.
   */
  void animate(long currentTime);

  /**
   * Getter.
   * 
   * @return Whether this position has been changed. The change flag is cleared
   *         by this method.
   */
  boolean hasChanged();

}
