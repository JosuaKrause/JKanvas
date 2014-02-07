package jkanvas.animation;

public class AnimatedDouble extends GenericAnimated<Double> {

  public AnimatedDouble(final double init) {
    super(init);
  }

  @Override
  protected Double interpolate(final Double from, final Double to, final double t) {
    return to * t + from * (1.0 - t);
  }

}
