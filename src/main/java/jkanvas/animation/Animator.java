package jkanvas.animation;

import jkanvas.Canvas;
import jkanvas.FrameRateDisplayer;
import jkanvas.RefreshManager;
import jkanvas.Refreshable;

/**
 * An animator refreshes a {@link Refreshable} successively.
 * 
 * @author Joschi <josua.krause@gmail.com>
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
  void addRefreshable(Refreshable r);

  /**
   * Getter.
   * 
   * @return The animation list.
   */
  AnimationList getAnimationList();

  /**
   * Installs the given animation barrier for the given canvas. The validity of
   * the barrier must be checked with
   * {@link AnimationBarrier#ensureValidity(Canvas)}.
   * 
   * @param barrier The animation barrier to install.
   * @param canvas The canvas connected to the barrier.
   */
  void setAnimationBarrier(AnimationBarrier barrier, Canvas canvas);

  /**
   * Setter.
   * 
   * @param frd Sets the frame rate displayer. <code>null</code> stops time
   *          measuring.
   */
  void setFrameRateDisplayer(FrameRateDisplayer frd);

  /** Disposes this animator. The animator cannot be used afterwards. */
  void dispose();

}
