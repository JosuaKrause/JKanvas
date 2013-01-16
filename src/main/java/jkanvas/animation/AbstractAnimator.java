package jkanvas.animation;

import jkanvas.Refreshable;
import jkanvas.SimpleRefreshManager;

/**
 * An abstract animator.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractAnimator extends SimpleRefreshManager implements Animator {

  /** The frame rate of the animator. */
  private long framerate;

  /** The waiting time resulting from the {@link #framerate}. */
  private long framewait;

  /** The animator thread. */
  private final Thread animator;

  /** Whether this object is already disposed or can still be used. */
  private volatile boolean disposed;

  /** Creates an animator. */
  public AbstractAnimator() {
    setFramerate(60);
    animator = new Thread() {

      @Override
      public void run() {
        try {
          while(!isInterrupted() && !isDisposed()) {
            synchronized(this) {
              try {
                wait(getFramewait());
              } catch(final InterruptedException e) {
                interrupt();
                continue;
              }
            }
            boolean needsRedraw;
            synchronized(this) {
              needsRedraw = step();
            }
            if(needsRedraw) {
              refreshAll();
            }
          }
        } finally {
          dispose();
        }
      }

    };
    animator.setDaemon(true);
    animator.start();
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

  /**
   * Getter.
   * 
   * @return The animation lock.
   */
  public Object getAnimationLock() {
    return animator;
  }

  /**
   * Simulates one step. The animator lock is acquired during the call.
   * 
   * @return Whether a redraw is necessary.
   */
  protected abstract boolean step();

  @Override
  public void addRefreshable(final Refreshable r) {
    if(disposed) throw new IllegalStateException("object already disposed");
    super.addRefreshable(r);
  }

  /**
   * Disposes the object by cleaning all refreshables and stopping the
   * simulation thread. The object cannot be used anymore after a call to this
   * method.
   */
  public void dispose() {
    disposed = true;
    clearRefreshables();
    animator.interrupt();
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
