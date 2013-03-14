package jkanvas.animation;

import java.awt.geom.Point2D;

import jkanvas.util.VecUtil;

/**
 * An animated position. Operations are delayed until the next frame of
 * animation is computed. All public facing methods do copy the {@link Point2D}
 * objects to ensure that outside changes do not affect them afterwards.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimatedPosition extends Position2D implements Animated {

  /**
   * An animated {@link Point2D}. The values of this class are used in
   * {@link AnimatedPosition}. The values of {@link Position2D} in turn are set
   * to {@link Double#NaN} since they are not used.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private final class Point2DAnimated extends GenericAnimated<Point2D> {

    /**
     * Creates an animated {@link Point2D}.
     * 
     * @param pos The initial position.
     */
    public Point2DAnimated(final Point2D pos) {
      super(pos);
    }

    @Override
    protected Point2D interpolate(final Point2D from, final Point2D to, final double t) {
      return VecUtil.interpolate(from, to, t);
    }

  } // Point2DAnimated

  /** The actual animated position. */
  private final Point2DAnimated position;

  /**
   * Creates an animated position.
   * 
   * @param x The initial x position.
   * @param y The initial y position.
   */
  public AnimatedPosition(final double x, final double y) {
    super(Double.NaN, Double.NaN);
    position = new Point2DAnimated(copy(x, y));
  }

  /**
   * Creates an animated position.
   * 
   * @param pos The initial position.
   */
  public AnimatedPosition(final Point2D pos) {
    // this ensures copying the position
    this(pos.getX(), pos.getY());
  }

  /**
   * Copies the given position.
   * 
   * @param pos The position.
   * @return The copied position.
   */
  private static Point2D copy(final Point2D pos) {
    return copy(pos.getX(), pos.getY());
  }

  /**
   * Copies the given position into a {@link Point2D}.
   * 
   * @param x The x position.
   * @param y The y position.
   * @return The copied position.
   */
  private static Point2D copy(final double x, final double y) {
    return new Point2D.Double(x, y);
  }

  @Override
  public Point2D getPos() {
    return copy(position.get());
  }

  @Override
  public double getX() {
    // no copy needed
    return position.get().getX();
  }

  @Override
  public double getY() {
    // no copy needed
    return position.get().getY();
  }

  /**
   * Getter.
   * 
   * @return The final position after the animation has finished.
   */
  public Point2D getPredict() {
    return copy(position.getPredict());
  }

  /**
   * Getter.
   * 
   * @return The x final position after the animation has finished.
   */
  public double getPredictX() {
    // no copy needed
    return position.getPredict().getX();
  }

  /**
   * Getter.
   * 
   * @return The final y position after the animation has finished.
   */
  public double getPredictY() {
    // no copy needed
    return position.getPredict().getY();
  }

  @Override
  public void setPosition(final double x, final double y) {
    position.set(copy(x, y));
  }

  @Override
  public void setPosition(final Point2D pos) {
    position.set(copy(pos));
  }

  /**
   * Starts an animation to the given point.
   * 
   * @param pos The end point.
   * @param timing The animation timing.
   */
  public void startAnimationTo(final Point2D pos, final AnimationTiming timing) {
    position.startAnimationTo(copy(pos), timing);
  }

  /**
   * Starts an animation to the given point.
   * 
   * @param pos The end point.
   * @param timing The animation timing.
   * @param onFinish An action that is executed when the animation ends. This
   *          may be <code>null</code> when no action is required.
   */
  public void startAnimationTo(final Point2D pos,
      final AnimationTiming timing, final AnimationAction onFinish) {
    position.startAnimationTo(pos, timing, onFinish);
  }

  /**
   * Sets the current animation to a new destination. If no current animation is
   * active a new one is created with the given default values.
   * 
   * @param pos The new destination position.
   * @param defaultTiming The default timing that is used when no animation is
   *          active.
   */
  public void changeAnimationTo(final Point2D pos, final AnimationTiming defaultTiming) {
    position.changeAnimationTo(copy(pos), defaultTiming);
  }

  /** Aborts the current animation and keeps the current position. */
  public void clearAnimation() {
    position.clearAnimation();
  }

  @Override
  public boolean animate(final long currentTime) {
    return position.animate(currentTime);
  }

  /**
   * Getter.
   * 
   * @return Whether this node is in animation.
   */
  public boolean inAnimation() {
    return position.inAnimation();
  }

}
