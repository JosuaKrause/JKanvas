package jkanvas.animation;

import java.awt.geom.Point2D;

import jkanvas.util.VecUtil;

/**
 * An animated position. Operations are delayed until the next frame of
 * animation is computed. All public facing methods do copy the {@link Point2D}
 * objects to ensure that outside changes do not affect them afterwards.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimatedPosition extends Position2D implements Animated {

  /**
   * An animated {@link Point2D}. The values of this class are used in
   * {@link AnimatedPosition}. The values of {@link Position2D} in turn are set
   * to {@link Double#NaN} since they are not used.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  // TODO #43 -- Java 8 simplification
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
  private final GenericAnimated<Point2D> position;

  /**
   * Creates an animated position.
   * 
   * @param x The initial x position.
   * @param y The initial y position.
   */
  public AnimatedPosition(final double x, final double y) {
    super(Double.NaN, Double.NaN);
    position = createAnimated(copy(x, y));
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
   * Creates an animated point. Implementations must not use the state of this
   * {@link AnimatedPosition} since this method is called during initialization
   * of the object.
   * 
   * @param pos The initial position.
   * @return The generic animated point for the given position.
   */
  protected GenericAnimated<Point2D> createAnimated(final Point2D pos) {
    return new Point2DAnimated(pos);
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
    setPosition(copy(x, y), null);
  }

  @Override
  public void setPosition(final Point2D pos) {
    setPosition(pos, null);
  }

  /**
   * Setter.
   * 
   * @param pos The new position.
   * @param onFinish An action that is executed when the position is set. This
   *          may be <code>null</code> when no action is required.
   */
  public void setPosition(final Point2D pos, final AnimationAction onFinish) {
    position.set(copy(pos), onFinish);
  }

  /**
   * Starts an animation to the given point.
   * 
   * @param pos The end point.
   * @param timing The animation timing.
   */
  public void startAnimationTo(final Point2D pos, final AnimationTiming timing) {
    startAnimationTo(pos, timing, null);
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
    position.startAnimationTo(copy(pos), timing, onFinish);
  }

  /** Aborts the current animation and keeps the current position. */
  public void clearAnimation() {
    clearAnimation(null);
  }

  /**
   * Aborts the current animation and keeps the current position.
   * 
   * @param onFinish An action that is executed when the animation is cleared.
   *          This may be <code>null</code> when no action is required.
   */
  public void clearAnimation(final AnimationAction onFinish) {
    position.clearAnimation(onFinish);
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
