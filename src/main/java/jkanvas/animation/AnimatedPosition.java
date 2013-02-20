package jkanvas.animation;

import java.awt.geom.Point2D;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jkanvas.util.Interpolator;

/**
 * An animated position. Operations are delayed until the next frame of
 * animation is computed.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimatedPosition extends Position2D {

  /** The animation start point. */
  private Point2D start;

  /** The animation end point. */
  private Point2D end;

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
   * @return The x final position after the animation has finished.
   */
  public double getPredictX() {
    return end != null ? end.getX() : getX();
  }

  /**
   * Getter.
   * 
   * @return The final y position after the animation has finished.
   */
  public double getPredictY() {
    return end != null ? end.getY() : getY();
  }

  @Override
  public void setPosition(final double x, final double y) {
    setPosition(new Point2D.Double(x, y));
  }

  @Override
  public void setPosition(final Point2D pos) {
    // ensures that every previous animation is cleared
    pendingOperations.add(new PendingOp(pos));
    // set position directly for immediate feed-back
    doSetPosition(pos.getX(), pos.getY());
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

  /** The long animation duration. */
  public static final int LONG = 2000;

  /** The standard animation duration. */
  public static final int NORMAL = 1000;

  /** Fast animation duration. */
  public static final int FAST = 100;

  /**
   * Starts an animation to the given point.
   * 
   * @param currentTime The current time in milliseconds.
   * @param pos The end point.
   * @param pol The interpolation method.
   * @param duration The duration in milliseconds.
   */
  private void startAnimationTo(final long currentTime, final Point2D pos,
      final Interpolator pol, final int duration) {
    clearAnimation(currentTime);
    if(duration <= 0) {
      doSetPosition(pos.getX(), pos.getY());
      return;
    }
    startTime = currentTime;
    endTime = currentTime + duration;
    this.pol = pol;
    start = getPos();
    end = pos;
  }

  /**
   * Starts an animation to the given point.
   * 
   * @param pos The end point.
   * @param pol The interpolation method.
   * @param duration The duration in milliseconds.
   */
  public void startAnimationTo(final Point2D pos,
      final Interpolator pol, final int duration) {
    pendingOperations.add(new PendingOp(pos, pol, duration, true));
  }

  /**
   * Sets the current animation to a new destination. If no current animation is
   * active a new one is created with the given default values.
   * 
   * @param currentTime The current time in milliseconds.
   * @param pos The new destination position.
   * @param defaultPol The default interpolation that is used when no animation
   *          is active.
   * @param defaultDuration The default duration that is used when no animation
   *          is active in milliseconds.
   */
  private void changeAnimationTo(final long currentTime, final Point2D pos,
      final Interpolator defaultPol, final int defaultDuration) {
    final Interpolator p = pol;
    final long et = endTime;
    doAnimate(currentTime);
    if(!inAnimation()) {
      startAnimationTo(currentTime, pos, defaultPol, defaultDuration);
      return;
    }
    start = getPos();
    end = pos;
    pol = p;
    startTime = currentTime;
    endTime = et;
  }

  /**
   * Sets the current animation to a new destination. If no current animation is
   * active a new one is created with the given default values.
   * 
   * @param pos The new destination position.
   * @param defaultPol The default interpolation that is used when no animation
   *          is active.
   * @param defaultDuration The default duration that is used when no animation
   *          is active in milliseconds.
   */
  public void changeAnimationTo(final Point2D pos,
      final Interpolator defaultPol, final int defaultDuration) {
    pendingOperations.add(new PendingOp(pos, defaultPol, defaultDuration, false));
  }

  /**
   * Aborts the current animation and keeps the current position.
   * 
   * @param currentTime The current time in milliseconds.
   */
  private void clearAnimation(final long currentTime) {
    doAnimate(currentTime);
    pol = null;
    start = null;
    end = null;
  }

  /** Aborts the current animation and keeps the current position. */
  public void clearAnimation() {
    pendingOperations.add(new PendingOp());
  }

  /**
   * Animates the position.
   * 
   * @param currentTime The current time in milliseconds.
   */
  public void animate(final long currentTime) {
    PendingOp op = pendingOperations.poll();
    if(op == null) {
      doAnimate(currentTime);
      return;
    }
    // process pending operations
    do {
      switch(op.operation) {
        case OP_CLEAR:
          clearAnimation(currentTime);
          break;
        case OP_START:
          startAnimationTo(currentTime, op.destination, op.interpolator, op.duration);
          break;
        case OP_CHANGE:
          changeAnimationTo(currentTime, op.destination, op.interpolator, op.duration);
          break;
        case OP_SET: {
          clearAnimation(currentTime);
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

  /**
   * Does the actual animation calculation.
   * 
   * @param currentTime The current time.
   */
  private void doAnimate(final long currentTime) {
    if(!inAnimation()) return;
    if(currentTime >= endTime) {
      doSetPosition(end.getX(), end.getY());
      start = null;
      end = null;
      pol = null;
      return;
    }
    final double t = ((double) currentTime - startTime) / ((double) endTime - startTime);
    final double f = pol.interpolate(t);
    doSetPosition(start.getX() * (1 - f) + end.getX() * f,
        start.getY() * (1 - f) + end.getY() * f);
    // no need to animate when end position is reached
    if(getX() == end.getX() && getY() == end.getY()) {
      pol = null;
      start = null;
      end = null;
    }
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

    /** The interpolator for the animation. */
    public final Interpolator interpolator;

    /** The duration of the animation. */
    public final int duration;

    /** Creates an animation clearing operation. */
    public PendingOp() {
      operation = OP_CLEAR;
      destination = null;
      interpolator = null;
      duration = 0;
    }

    /**
     * Creates a position setting operation. This clears animations.
     * 
     * @param pos The position.
     */
    public PendingOp(final Point2D pos) {
      operation = OP_SET;
      destination = pos;
      interpolator = null;
      duration = 0;
    }

    /**
     * Creates an animation initiating operation.
     * 
     * @param destination The destination of the animation.
     * @param interpolator The interpolation.
     * @param duration The duration in milliseconds.
     * @param start Whether this operation starts the animation or changes the
     *          current animation.
     */
    public PendingOp(final Point2D destination,
        final Interpolator interpolator, final int duration, final boolean start) {
      operation = start ? OP_START : OP_CHANGE;
      this.destination = destination;
      this.interpolator = interpolator;
      this.duration = duration;
    }

  }

  /**
   * Getter.
   * 
   * @return Whether this node is in animation.
   */
  public boolean inAnimation() {
    return pol != null;
  }

  /** Whether this position has been changed. */
  private boolean changed;

  /** Signals a change. */
  private void change() {
    changed = true;
  }

  /**
   * Getter.
   * 
   * @return Whether this position has been changed. The change flag is cleared
   *         by this method.
   */
  public boolean hasChanged() {
    final boolean res = changed;
    changed = false;
    return res;
  }

}
