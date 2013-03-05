package jkanvas.animation;

import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jkanvas.util.Interpolator;

/**
 * An animated position. Operations are delayed until the next frame of
 * animation is computed.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimatedPosition extends Position2D implements Animated {

  /** The animation start point. */
  private Point2D start;

  /** The animation end point. */
  private Point2D end;

  /** The predicted end point. */
  private Point2D pred;

  /** The interpolation method or <code>null</code> if no animation is active. */
  private Interpolator pol;

  /** The start time. */
  private long startTime;

  /** The end time. */
  private long endTime;

  /**
   * Creates an animated position.
   * 
   * @param x The initial x position.
   * @param y The initial y position.
   */
  public AnimatedPosition(final double x, final double y) {
    super(x, y);
  }

  /**
   * Creates an animated position.
   * 
   * @param pos The initial position.
   */
  public AnimatedPosition(final Point2D pos) {
    super(pos);
  }

  /**
   * Getter.
   * 
   * @return The final position after the animation has finished.
   */
  public Point2D getPredict() {
    final Point2D pred = this.pred;
    if(pred == null) {
      getPos();
    }
    return new Point2D.Double(pred.getX(), pred.getY());
  }

  /**
   * Getter.
   * 
   * @return The x final position after the animation has finished.
   */
  public double getPredictX() {
    return pred != null ? pred.getX() : getX();
  }

  /**
   * Getter.
   * 
   * @return The final y position after the animation has finished.
   */
  public double getPredictY() {
    return pred != null ? pred.getY() : getY();
  }

  @Override
  public void setPosition(final double x, final double y) {
    setPosition(new Point2D.Double(x, y));
  }

  @Override
  public void setPosition(final Point2D pos) {
    Objects.requireNonNull(pos);
    // ensures that every previous animation is cleared
    pendingOperations.add(new PendingOp(pos));
    // set position directly for immediate feed-back
    doSetPosition(pos.getX(), pos.getY());
    pred = null;
  }

  /**
   * Worm-hole for setting the actual position. Be sure to call
   * <code>super.doSetPosition(x, y);</code> when overwriting this method.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  protected void doSetPosition(final double x, final double y) {
    change();
    super.setPosition(x, y);
  }

  /**
   * Starts an animation to the given point.
   * 
   * @param currentTime The current time in milliseconds.
   * @param pos The end point.
   * @param timing The timing.
   */
  private void startAnimationTo(final long currentTime,
      final Point2D pos, final AnimationTiming timing) {
    doAnimate(currentTime);
    doClearAnimation();
    startTime = currentTime;
    endTime = currentTime + timing.duration;
    pol = timing.pol;
    start = getPos();
    end = pos;
    pred = pos;
  }

  /**
   * Starts an animation to the given point.
   * 
   * @param pos The end point.
   * @param timing The animation timing.
   */
  public void startAnimationTo(final Point2D pos, final AnimationTiming timing) {
    Objects.requireNonNull(timing);
    if(timing.duration <= 0) {
      setPosition(pos);
      return;
    }
    Objects.requireNonNull(pos);
    pendingOperations.add(new PendingOp(pos, timing, true));
    pred = pos;
  }

  /**
   * Sets the current animation to a new destination. If no current animation is
   * active a new one is created with the given default values.
   * 
   * @param currentTime The current time in milliseconds.
   * @param pos The new destination position.
   * @param defaultTiming The default timing that is used when no animation is
   *          active.
   */
  private void changeAnimationTo(final long currentTime,
      final Point2D pos, final AnimationTiming defaultTiming) {
    doAnimate(currentTime);
    final Interpolator p = pol;
    // not using inAnimation() because of possible pred racing conditions
    if(p == null) {
      startAnimationTo(currentTime, pos, defaultTiming);
      return;
    }
    final long et = endTime;
    start = getPos();
    end = pos;
    pred = pos;
    pol = p;
    startTime = currentTime;
    endTime = et;
  }

  /**
   * Sets the current animation to a new destination. If no current animation is
   * active a new one is created with the given default values.
   * 
   * @param pos The new destination position.
   * @param defaultTiming The default timing that is used when no animation is
   *          active.
   */
  public void changeAnimationTo(final Point2D pos,
      final AnimationTiming defaultTiming) {
    Objects.requireNonNull(defaultTiming);
    if(!inAnimation() && defaultTiming.duration <= 0) {
      setPosition(pos);
      return;
    }
    Objects.requireNonNull(pos);
    pendingOperations.add(new PendingOp(pos, defaultTiming, false));
    pred = pos;
  }

  /** Aborts the current animation and keeps the current position. */
  private void doClearAnimation() {
    pol = null;
    start = null;
    end = null;
    pred = null;
  }

  /** Aborts the current animation and keeps the current position. */
  public void clearAnimation() {
    pendingOperations.add(new PendingOp());
    doClearAnimation();
  }

  /**
   * Dispatches all pending operations.
   * 
   * @param currentTime The current time in milliseconds.
   */
  private void dispatchAll(final long currentTime) {
    PendingOp op = pendingOperations.poll();
    if(op == null) {
      doAnimate(currentTime);
      return;
    }
    // process pending operations
    do {
      switch(op.operation) {
        case OP_CLEAR:
          // clear was called prior to animation so we
          // do not compute the next position
          doClearAnimation();
          break;
        case OP_START:
          startAnimationTo(currentTime, op.destination, op.timing);
          break;
        case OP_CHANGE:
          changeAnimationTo(currentTime, op.destination, op.timing);
          break;
        case OP_SET: {
          doClearAnimation();
          final Point2D pos = op.destination;
          doSetPosition(pos.getX(), pos.getY());
          break;
        }
        default:
          throw new InternalError();
      }
      op = pendingOperations.poll();
    } while(op != null);
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
      doSetPosition(end.getX(), end.getY());
      start = null;
      end = null;
      pred = null;
      pol = null;
      return;
    }
    final double t = ((double) currentTime - startTime) / ((double) endTime - startTime);
    final double f = pol.interpolate(t);
    doSetPosition(start.getX() * (1 - f) + end.getX() * f,
        start.getY() * (1 - f) + end.getY() * f);
  }

  /** The queue of pending operations. */
  private final Queue<PendingOp> pendingOperations = new ConcurrentLinkedQueue<>();

  /** Animation clearing operation. */
  private static final int OP_CLEAR = 0;

  /** Animation start operation. */
  private static final int OP_START = 1;

  /** Animation change operation. */
  private static final int OP_CHANGE = 2;

  /** Position setting operation. */
  private static final int OP_SET = 3;

  /**
   * A pending animation operation.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private final class PendingOp {

    /** The type of operation. */
    public final int operation;

    /** The destination of the animation. */
    public final Point2D destination;

    /** The timing for the animation. */
    public final AnimationTiming timing;

    /** Creates an animation clearing operation. */
    public PendingOp() {
      operation = OP_CLEAR;
      destination = null;
      timing = null;
    }

    /**
     * Creates a position setting operation. This clears animations.
     * 
     * @param pos The position.
     */
    public PendingOp(final Point2D pos) {
      operation = OP_SET;
      destination = pos;
      timing = null;
    }

    /**
     * Creates an animation initiating operation.
     * 
     * @param destination The destination of the animation.
     * @param timing The timing.
     * @param start Whether this operation starts the animation or changes the
     *          current animation.
     */
    public PendingOp(final Point2D destination,
        final AnimationTiming timing, final boolean start) {
      operation = start ? OP_START : OP_CHANGE;
      this.destination = destination;
      this.timing = timing;
    }

  }

  /**
   * Getter.
   * 
   * @return Whether this node is in animation.
   */
  public boolean inAnimation() {
    return pol != null || pred != null;
  }

  /** Whether this position has been changed. */
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
