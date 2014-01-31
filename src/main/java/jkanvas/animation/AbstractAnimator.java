package jkanvas.animation;

import java.util.concurrent.atomic.AtomicInteger;

import jkanvas.Canvas;
import jkanvas.FrameRateDisplayer;
import jkanvas.Refreshable;
import jkanvas.SimpleRefreshManager;
import jkanvas.animation.AnimationBarrier.CloseBlock;

/**
 * An abstract animator.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class AbstractAnimator extends SimpleRefreshManager implements Animator {

  /**
   * Whether the pauses between animation calculations should be adjusted to
   * compensate lag.
   */
  public static boolean COMPENSATE_LAG = false;

  /** The number of animation threads that were created. */
  private static final AtomicInteger ANIMATOR_COUNT = new AtomicInteger();

  /** The frame rate of the animator. */
  private long framerate;

  /** The waiting time resulting from the {@link #framerate}. */
  private long framewait;

  /** The animator thread. */
  private final Thread animator;

  /** The animation list containing all animated objects. */
  private final AnimationList list;

  /** Whether this object is already disposed or can still be used. */
  private volatile boolean disposed;

  /**
   * The animation barrier. If no animation barrier is installed no animation
   * will be performed and no redraw initiated.
   */
  private AnimationBarrier barrier;

  /** Creates an animator. */
  public AbstractAnimator() {
    setFramerate(60);
    // TODO #43 -- Java 8 simplification
    animator = new Thread("animation-thread-" + ANIMATOR_COUNT.getAndIncrement()) {

      @Override
      public void run() {
        try {
          long lastStep = 0;
          while(!isInterrupted() && !isDisposed()) {
            final long nextWait = getFramewait() - (COMPENSATE_LAG ? lastStep : 0L);
            lastStep = 0;
            if(nextWait > 0) {
              synchronized(this) {
                try {
                  wait(nextWait);
                } catch(final InterruptedException e) {
                  interrupt();
                  continue;
                }
              }
            }
            final long startStep = System.nanoTime();
            final boolean needsRedraw = doStep();
            if(needsRedraw) {
              refreshAll();
            }
            final long stepTime = System.nanoTime() - startStep;
            lastStep = stepTime / 1000000L;
            final FrameRateDisplayer frd = getFrameRateDisplayer();
            if(frd != null) {
              final boolean lag = lastStep > getFramewait();
              frd.setLastAnimationTime(stepTime, lag);
            }
          }
        } finally {
          dispose();
        }
      }

    };
    animator.setDaemon(true);
    animator.start();
    list = new AnimationList();
  }

  /**
   * Getter.
   * 
   * @return The waiting time for one frame.
   */
  protected long getFramewait() {
    return framewait;
  }

  /**
   * Setter.
   * 
   * @param framerate Sets the frame-rate.
   */
  public void setFramerate(final long framerate) {
    this.framerate = framerate;
    framewait = Math.max(1000 / this.framerate, 1);
  }

  @Override
  public void setAnimationBarrier(final AnimationBarrier barrier, final Canvas canvas) {
    if(barrier != null) {
      barrier.ensureValidity(canvas);
    }
    this.barrier = barrier;
  }

  @Override
  public AnimationList getAnimationList() {
    return list;
  }

  /**
   * Computes one step by calling {@link #step()}. If no animation barrier is
   * installed nothing will happen while returning <code>false</code>.
   * 
   * @return Whether a redraw is necessary.
   */
  protected final boolean doStep() {
    if(barrier == null) return false;
    try (CloseBlock b = barrier.openAnimationBlock()) {
      final boolean needsRedraw = step();
      if(needsRedraw) {
        // only animate further if redraw occurred
        b.setStateAfter(AnimationBarrier.ALLOW_DRAW);
      }
      return needsRedraw;
    }
  }

  /**
   * Simulates one step. No visible monitors are acquired during the call.
   * 
   * @return Whether a redraw is necessary.
   */
  protected abstract boolean step();

  /** The frame rate displayer. */
  private FrameRateDisplayer frd;

  @Override
  public void setFrameRateDisplayer(final FrameRateDisplayer frd) {
    this.frd = frd;
    forceNextFrame();
  }

  /**
   * Getter.
   * 
   * @return The current frame rate displayer or <code>null</code>.
   */
  public FrameRateDisplayer getFrameRateDisplayer() {
    return frd;
  }

  @Override
  public void addRefreshable(final Refreshable r) {
    if(disposed) throw new IllegalStateException("object already disposed");
    super.addRefreshable(r);
  }

  /**
   * Disposes the object by cleaning all {@link Refreshable Refreshables} and
   * stopping the simulation thread. The object cannot be used anymore after a
   * call to this method.
   */
  @Override
  public void dispose() {
    if(disposed) return;
    disposed = true;
    frd = null;
    clearRefreshables();
    animator.interrupt();
    list.dispose();
  }

  @Override
  public void forceNextFrame() {
    synchronized(animator) {
      animator.notifyAll();
    }
  }

  @Override
  public void quickRefresh() {
    refreshAll();
  }

  /**
   * Tests whether this animator is disposed.
   * 
   * @return If it is disposed.
   */
  public boolean isDisposed() {
    return disposed;
  }

}
