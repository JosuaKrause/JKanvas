package jkanvas.animation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import jkanvas.Canvas;
import jkanvas.FrameRateDisplayer;
import jkanvas.Refreshable;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

/**
 * An animated painter.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimatedPainter extends RenderpassPainter implements Animator {

  /** The internal animator. */
  private AbstractAnimator animator;

  /** Whether the animation is stopped for now. */
  private AtomicBoolean isStopped;

  /** The time in milliseconds when the last stop occurred. */
  private AtomicLong lastStop;

  /** Creates an animated painter. */
  public AnimatedPainter() {
    final AtomicBoolean isStopped = new AtomicBoolean();
    this.isStopped = isStopped;
    final AtomicLong lastStop = new AtomicLong(getTime());
    this.lastStop = lastStop;
    // TODO #43 -- Java 8 simplification
    animator = new AbstractAnimator() {

      @Override
      protected boolean step() {
        final long currentTime;
        synchronized(isStopped) {
          if(isStopped.get()) return false;
          currentTime = getTime() - lastStop.get();
        }
        return getAnimationList().doAnimate(currentTime);
      }

    };
  }

  /**
   * Getter.
   * 
   * @return Returns the current time. This method can be overwritten to gain
   *         full control over animation timing. This method is called exactly
   *         once every step, once when the {@link #setStopped(boolean)} is
   *         called, and once during initialization.
   */
  protected long getTime() {
    return System.currentTimeMillis();
  }

  /**
   * Getter.
   * 
   * @return Whether the animation is currently paused.
   */
  public boolean isStopped() {
    return isStopped.get();
  }

  /**
   * Setter.
   * 
   * @param stopped Stops or resumes the animation.
   */
  public void setStopped(final boolean stopped) {
    synchronized(isStopped) {
      final long now = getTime();
      if(!stopped) {
        // nLast = last' + nT
        lastStop.addAndGet(now);
      } else {
        // last' = last - T
        lastStop.addAndGet(-now);
      }
      isStopped.set(stopped);
      if(!stopped) {
        animator.forceNextFrame();
      } else {
        animator.quickRefresh();
      }
    }
  }

  /**
   * Setter.
   * 
   * @param framerate Sets the framerate of the animator.
   */
  public void setFramerate(final long framerate) {
    animator.setFramerate(framerate);
  }

  @Override
  public void addPass(final Renderpass r) {
    super.addPass(r);
    r.setAnimationList(getAnimationList());
  }

  @Override
  public void setAnimationBarrier(final AnimationBarrier barrier, final Canvas canvas) {
    animator.setAnimationBarrier(barrier, canvas);
  }

  @Override
  public void setFrameRateDisplayer(final FrameRateDisplayer frd) {
    animator.setFrameRateDisplayer(frd);
  }

  @Override
  public AnimationList getAnimationList() {
    return animator.getAnimationList();
  }

  @Override
  public void addRefreshable(final Refreshable r) {
    animator.addRefreshable(r);
  }

  @Override
  public void removeRefreshable(final Refreshable r) {
    animator.removeRefreshable(r);
  }

  @Override
  public void forceNextFrame() {
    animator.forceNextFrame();
  }

  @Override
  public void quickRefresh() {
    animator.quickRefresh();
  }

  @Override
  public void startBulkOperation() {
    animator.startBulkOperation();
  }

  @Override
  public void endBulkOperation() {
    animator.endBulkOperation();
  }

  @Override
  public boolean inBulkOperation() {
    return animator.inBulkOperation();
  }

  @Override
  public Refreshable[] getRefreshables() {
    return animator.getRefreshables();
  }

  @Override
  public void refreshAll() {
    animator.refreshAll();
  }

  @Override
  // TODO #43 -- Java 8 simplification
  public void refresh() {
    refreshAll();
  }

  /** Disposes this painter and stops the animator. */
  @Override
  public void dispose() {
    super.dispose();
    if(animator.isDisposed()) return;
    animator.dispose();
  }

}
