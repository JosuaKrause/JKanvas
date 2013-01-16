package jkanvas.animation;

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
   * Adds a refreshable that is refreshed each step. {@inheritDoc}
   * 
   * @param r The refreshable.
   */
  @Override
  void addRefreshable(final Refreshable r);

}
