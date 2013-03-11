package jkanvas.animation;

/**
 * An action that defines the behavior of an animated object when an animation
 * has finished.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface AnimationAction {

  /**
   * Is called when the animation with which it was registered terminates
   * successfully. That means that this method is only called when the animation
   * was not changed or cancelled before it would have finished.
   */
  void animationFinished();

}
