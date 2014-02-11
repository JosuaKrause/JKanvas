package jkanvas.animation;

/**
 * Animates a double value.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimatedDouble extends GenericAnimated<Double> {

  /**
   * Creates an animated double value.
   * 
   * @param init The initial value.
   */
  public AnimatedDouble(final double init) {
    super(init);
  }

  @Override
  protected Double interpolate(final Double from, final Double to, final double t) {
    return to * t + from * (1.0 - t);
  }

}
