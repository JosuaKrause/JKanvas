package jkanvas.animation;

import jkanvas.Canvas;
import jkanvas.RefreshManager;
import jkanvas.Refreshable;

/**
 * An animator refreshes a {@link Refreshable} successively.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Animator extends RefreshManager {

  /** Forces the next frame to be calculated. */
  void forceNextFrame();

  /** Forces a quick refresh without animating. */
  void quickRefresh();

  /**
   * Adds a {@link Refreshable} that is refreshed each step. {@inheritDoc}
   * 
   * @param r The {@link Refreshable}.
   */
  @Override
  void addRefreshable(final Refreshable r);

  /**
   * Getter.
   * 
   * @return The animation lock. This lock is always held when an animation is
   *         processing. In order to avoid concurrent positions during drawing
   *         {@link Canvas#setPaintLock(Object)} should be called with this
   *         object. You can also use this lock to avoid concurrent
   *         modifications on animation lists.
   */
  Object getAnimationLock();

}
