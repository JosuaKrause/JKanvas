package jkanvas.animation;

/**
 * An action that defines the behavior of an animated object when an animation
 * has finished.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface AnimationAction {

  /**
   * Is called when the animation with which it was registered terminates. This
   * may be when the animation terminates successfully or when the animation
   * gets overwritten.
   */
  void animationFinished();

}
