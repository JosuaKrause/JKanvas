package jkanvas.animation;

import jkanvas.Refreshable;

/**
 * An animator refreshes a {@link Refreshable} successively.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Animator {

  /** Forces the next frame to be calculated. */
  void forceNextFrame();

  /** Forces a quick refresh without animating. */
  void quickRefresh();

  /**
   * Adds a refreshable that is refreshed each step.
   * 
   * @param r The refreshable.
   */
  void addRefreshable(final Refreshable r);

}
