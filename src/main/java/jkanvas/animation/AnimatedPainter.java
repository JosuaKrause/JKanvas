package jkanvas.animation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import jkanvas.Refreshable;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

/**
 * An animated painter.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimatedPainter extends RenderpassPainter implements Animator {

  /** The internal animator. */
  private AbstractAnimator animator;

  /** Whether the animation is stopped for now. */
  private AtomicBoolean isStopped;

  /** The time in milli-seconds when the last stop occured. */
  private AtomicLong lastStop;

  /** Creates an animated painter. */
  public AnimatedPainter() {
    final AtomicBoolean isStopped = new AtomicBoolean();
    final AtomicLong lastStop = new AtomicLong(getTime());
    animator = new AbstractAnimator() {

      @Override
      protected boolean step() {
        if(isStopped.get()) return false;
        final long currentTime = getTime() - lastStop.get();
        return getAnimationList().doAnimate(currentTime);
      }

    };
    this.isStopped = isStopped;
    this.lastStop = lastStop;
  }

  /**
   * Getter.
   * 
   * @return Returns the current time. This method can be overwritten to gain
   *         full control over animation timing.
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
    synchronized(animator.getAnimationLock()) {
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

  @Override
  public void addPass(final Renderpass r) {
    super.addPass(r);
    r.setAnimationList(getAnimationList());
  }

  @Override
  public Object getAnimationLock() {
    return animator.getAnimationLock();
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
  public Refreshable[] getRefreshables() {
    return animator.getRefreshables();
  }

  @Override
  public void refreshAll() {
    animator.refreshAll();
  }

  /** Disposes this painter and stops the animator. */
  @Override
  public void dispose() {
    super.dispose();
    if(animator.isDisposed()) return;
    animator.dispose();
  }

}
