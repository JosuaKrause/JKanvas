package jkanvas.animation;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jkanvas.util.Interpolator;

/**
 * Animates values. How values are interpolated must be implemented in a
 * sub-class. Values passed into this object or returned by it must not be
 * externally altered afterwards.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of animated values.
 */
public abstract class GenericAnimated<T> implements Animated {

  /** The current value. */
  private T cur;

  /** The animation start value. */
  private T start;

  /** The animation end value. */
  private T end;

  /** The predicted end value. */
  private T pred;

  /** The interpolation method or <code>null</code> if no animation is active. */
  private Interpolator pol;

  /** The start time. */
  private long startTime;

  /** The end time. */
  private long endTime;

  /** The action that is executed when an animation ends. */
  private AnimationAction onFinish;

  /**
   * Creates an animated value.
   * 
   * @param t The initial value.
   */
  public GenericAnimated(final T t) {
    cur = Objects.requireNonNull(t);
  }

  /**
   * Interpolates values.
   * 
   * @param from The starting value.
   * @param to The end value.
   * @param t The interpolation progress starting at <code>0</code> and going to
   *          <code>1</code>.
   * @return The interpolated value.
   */
  protected abstract T interpolate(T from, T to, double t);

  /**
   * Getter.
   * 
   * @return The final value after the animation has finished.
   */
  public T getPredict() {
    final T pred = this.pred;
    if(pred == null) return get();
    return pred;
  }

  /**
   * Getter.
   * 
   * @return The current value.
   */
  public T get() {
    return cur;
  }

  /**
   * This method is called every time an animation is started.
   * 
   * @param timing The used timing.
   * @param onFinish The action that was handed in by the public methods. Note
   *          that this might be <code>null</code>.
   * @return The action that will be executed when the animation ends. Note that
   *         this may substitute the original action.
   */
  protected AnimationAction beforeAnimation(
      @SuppressWarnings("unused") final AnimationTiming timing,
      final AnimationAction onFinish) {
    return onFinish;
  }

  /**
   * Setter.
   * 
   * @param t Immediately sets the current value.
   */
  public void set(final T t) {
    set(t, null);
  }

  /**
   * Setter.
   * 
   * @param t Immediately sets the current value.
   * @param onFinish The action that is executed when the position is set. This
   *          may be <code>null</code> when no action has to be executed.
   */
  public void set(final T t, final AnimationAction onFinish) {
    Objects.requireNonNull(t);
    // ensures that every previous animation is cleared
    pendingOperations.add(
        new PendingOp<>(t, beforeAnimation(AnimationTiming.NO_ANIMATION, onFinish)));
    // set value directly for immediate feed-back
    doSet(t);
    pred = null;
  }

  /**
   * Worm-hole for setting the actual value. Be sure to call
   * <code>super.doSet(t)</code> when overwriting this method.
   * 
   * @param t The value.
   */
  protected void doSet(final T t) {
    change();
    this.cur = t;
  }

  /**
   * Starts an animation to the given value.
   * 
   * @param currentTime The current time in milliseconds.
   * @param t The end value.
   * @param timing The timing.
   */
  private void startAnimationTo(final long currentTime,
      final T t, final AnimationTiming timing) {
    doAnimate(currentTime);
    doClearAnimation();
    startTime = currentTime;
    endTime = currentTime + timing.duration;
    pol = timing.pol;
    start = cur;
    end = t;
    pred = t;
  }

  /**
   * Starts an animation to the given value.
   * 
   * @param t The end value.
   * @param timing The animation timing.
   */
  public void startAnimationTo(final T t, final AnimationTiming timing) {
    startAnimationTo(t, timing, null);
  }

  /**
   * Starts an animation to the given value.
   * 
   * @param t The end value.
   * @param timing The animation timing.
   * @param onFinish An action that is executed when the animation ends. This
   *          may be <code>null</code> when no action is required.
   */
  public void startAnimationTo(
      final T t, final AnimationTiming timing, final AnimationAction onFinish) {
    Objects.requireNonNull(timing);
    if(timing.duration <= 0) {
      set(t, onFinish);
      return;
    }
    Objects.requireNonNull(t);
    pendingOperations.add(
        new PendingOp<>(t, timing, beforeAnimation(timing, onFinish)));
    pred = t;
  }

  /** Aborts the current animation and keeps the current value. */
  private void doClearAnimation() {
    pol = null;
    start = null;
    end = null;
    pred = null;
  }

  /**
   * Aborts the current animation and keeps the current value. Actions to
   * execute when the animation ends will be executed.
   */
  public void clearAnimation() {
    clearAnimation(null);
  }

  /**
   * Aborts the current animation and keeps the current value. Actions to
   * execute when the animation ends will be executed.
   * 
   * @param onFinish The action that is executed when the animation is cleared.
   *          This may be <code>null</code> when no action has to be executed.
   */
  public void clearAnimation(final AnimationAction onFinish) {
    pendingOperations.add(new PendingOp<T>(
        beforeAnimation(AnimationTiming.NO_ANIMATION, onFinish)));
    doClearAnimation();
  }

  /**
   * Dispatches all pending operations.
   * 
   * @param currentTime The current time in milliseconds.
   */
  private void dispatchAll(final long currentTime) {
    PendingOp<T> op = pendingOperations.poll();
    if(op == null) {
      doAnimate(currentTime);
      return;
    }
    // whether to clear on finish actions when last operation in queue
    boolean clearOnFinish;
    // process pending operations
    do {
      switch(op.operation) {
        case OP_CLEAR:
          // clear was called prior to animation so we
          // do not compute the next value
          doClearAnimation();
          clearOnFinish = true;
          break;
        case OP_START:
          startAnimationTo(currentTime, op.destination, op.timing);
          clearOnFinish = false;
          break;
        case OP_SET: {
          doClearAnimation();
          doSet(op.destination);
          clearOnFinish = true;
          break;
        }
        default:
          throw new InternalError();
      }
      // call action which will otherwise be overwritten
      if(onFinish != null) {
        AnimationAction.enqueue(onFinish);
      }
      onFinish = op.onFinish;
      op = pendingOperations.poll();
    } while(op != null);
    if(onFinish != null && clearOnFinish) {
      AnimationAction.enqueue(onFinish);
      onFinish = null;
    }
  }

  @Override
  public boolean animate(final long currentTime) {
    dispatchAll(currentTime);
    return clearChangeFlag();
  }

  /**
   * Does the actual animation calculation.
   * 
   * @param currentTime The current time.
   */
  private void doAnimate(final long currentTime) {
    // not using inAnimation() because of possible pred racing conditions
    if(pol == null) return;
    if(currentTime >= endTime) {
      doSet(end);
      start = null;
      end = null;
      pred = null;
      pol = null;
      if(onFinish != null) {
        AnimationAction.enqueue(onFinish);
        onFinish = null;
      }
      return;
    }
    final double t = ((double) currentTime - startTime) / ((double) endTime - startTime);
    final double f = pol.interpolate(t);
    doSet(interpolate(start, end, f));
  }

  /** The queue of pending operations. */
  private final Queue<PendingOp<T>> pendingOperations = new ConcurrentLinkedQueue<>();

  /** Animation clearing operation. */
  private static final int OP_CLEAR = 0;

  /** Animation start operation. */
  private static final int OP_START = 1;

  /** Value setting operation. */
  private static final int OP_SET = 2;

  /**
   * A pending animation operation.
   * 
   * @author Joschi <josua.krause@gmail.com>
   * @param <T> The animation type.
   */
  private static final class PendingOp<T> {

    /** The type of operation. */
    public final int operation;

    /** The destination of the animation. */
    public final T destination;

    /** The timing for the animation. */
    public final AnimationTiming timing;

    /** The associated action. */
    public final AnimationAction onFinish;

    /**
     * Creates an animation clearing operation.
     * 
     * @param onFinish The action that will be executed when the clearing is
     *          performed.
     */
    public PendingOp(final AnimationAction onFinish) {
      operation = OP_CLEAR;
      this.onFinish = onFinish;
      destination = null;
      timing = null;
    }

    /**
     * Creates a value setting operation. This clears animations.
     * 
     * @param val The value.
     * @param onFinish The action that will be executed when the position is
     *          set.
     */
    public PendingOp(final T val, final AnimationAction onFinish) {
      operation = OP_SET;
      this.onFinish = onFinish;
      destination = val;
      timing = null;
    }

    /**
     * Creates an animation initiating operation.
     * 
     * @param destination The destination of the animation.
     * @param timing The timing.
     * @param onFinish The action that will be executed when the animation ends.
     */
    public PendingOp(final T destination,
        final AnimationTiming timing, final AnimationAction onFinish) {
      operation = OP_START;
      this.destination = destination;
      this.timing = timing;
      this.onFinish = onFinish;
    }

  } // PendingOp

  /**
   * Getter.
   * 
   * @return Whether this object is in animation.
   */
  public boolean inAnimation() {
    return pol != null || pred != null;
  }

  /** Whether the value has been changed. */
  private boolean changed;

  /** Signals a change. */
  private void change() {
    changed = true;
  }

  /**
   * Clears the change flag.
   * 
   * @return Whether the change flag was set before.
   */
  private boolean clearChangeFlag() {
    final boolean res = changed;
    changed = false;
    return res;
  }

}
