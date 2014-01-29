package jkanvas.animation;

/**
 * An animatable object.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Animated {

  /**
   * Animates the object.
   * 
   * @param currentTime The current time in milliseconds.
   * @return Whether this object has been changed.
   */
  boolean animate(long currentTime);

}
