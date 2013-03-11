package jkanvas.animation;

import java.util.ArrayList;
import java.util.List;
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
    final AtomicLong lastStop = new AtomicLong(System.currentTimeMillis());
    animator = new AbstractAnimator() {

      @Override
      protected boolean step() {
        if(isStopped.get()) return false;
        final long currentTime = System.currentTimeMillis() - lastStop.get();
        return doStep(currentTime);
      }

    };
    this.isStopped = isStopped;
    this.lastStop = lastStop;
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
      final long now = System.currentTimeMillis();
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
    final Animated a = r.getAnimated();
    if(a != null) {
      addAnimated(a);
    }
  }

  @Override
  public void removePass(final Renderpass r) {
    super.removePass(r);
    final Animated a = r.getAnimated();
    if(a != null) {
      removeAnimated(a);
    }
  }

  /** All registered animated objects. */
  private final List<Animated> animated = new ArrayList<>();

  /**
   * Adds an animatable object.
   * 
   * @param animate The object.
   */
  public void addAnimated(final Animated animate) {
    synchronized(animator.getAnimationLock()) {
      if(animated.contains(animate)) throw new IllegalArgumentException(
          "animated object already added: " + animate);
      animated.add(animate);
    }
  }

  /**
   * Removes an animatable object.
   * 
   * @param animate The object.
   */
  public void removeAnimated(final Animated animate) {
    synchronized(animator.getAnimationLock()) {
      animated.remove(animate);
    }
  }

  /**
   * Computes one step for all animated.
   * 
   * @param currentTime The current time in milliseconds.
   * @return Whether a redraw is needed.
   */
  protected boolean doStep(final long currentTime) {
    boolean needsRedraw = false;
    // animation lock is already acquired
    for(final Animated a : animated) {
      needsRedraw = a.animate(currentTime) || needsRedraw;
    }
    return needsRedraw;
  }

  @Override
  public Object getAnimationLock() {
    return animator.getAnimationLock();
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
