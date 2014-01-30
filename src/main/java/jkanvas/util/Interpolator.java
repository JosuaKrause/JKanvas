package jkanvas.util;

/**
 * An interpolator maps values from the range <code>[0,1]</code> to
 * <code>[0,1]</code>.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Interpolator {

  /**
   * Maps the input to a value from 0 to 1.
   * 
   * @param t The input value from 0 to 1.
   * @return The mapped value from 0 to 1.
   */
  double interpolate(double t);

  /**
   * Inversely maps the input to a value from 0 to 1.
   * 
   * @param t The input value from 0 to 1.
   * @return The mapped value from 0 to 1.
   */
  double inverseInterpolate(double t);

  /** Smooth interpolation. */
  Interpolator SMOOTH = new Interpolator() {

    @Override
    public double interpolate(final double t) {
      return Math.sin((t - 0.5) * Math.PI) * 0.5 + 0.5;
    }

    @Override
    public double inverseInterpolate(final double t) {
      return 0.5 - Math.asin(1 - 2 * t) / Math.PI;
    }

  };

  /** Arc tangents slow in slow out interpolation. */
  Interpolator SLOW_IN_OUT = new Interpolator() {

    /** The strength of the fast phase. */
    private final double STRENGTH = 4;

    /** The minimal value of the arc tangents. */
    private final double MIN_VAL = VecUtil.fastArcTan(-0.5 * STRENGTH);

    /** The range of the arc tangents. */
    private final double RANGE = -MIN_VAL * 2;

    @Override
    public double interpolate(final double t) {
      return (VecUtil.fastArcTan((t - 0.5) * STRENGTH) - MIN_VAL) / RANGE;
    }

    @Override
    public double inverseInterpolate(final double t) {
      return 0.5 - Math.tan(-MIN_VAL - RANGE * t) / STRENGTH;
    }

  };

  /** Quadratic slow in slow out interpolation. */
  Interpolator QUAD_IN_OUT = new Interpolator() {

    @Override
    public double interpolate(final double t) {
      return t <= 0.5 ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
    }

    @Override
    public double inverseInterpolate(final double t) {
      return t <= 0.5 ? Math.sqrt(t * 0.5) : 1 - Math.sqrt(1 - t) / Math.sqrt(2);
    }

  };

  /** Linear interpolation. */
  Interpolator LINEAR = new Interpolator() {

    @Override
    public double interpolate(final double t) {
      return t;
    }

    @Override
    public double inverseInterpolate(final double t) {
      return t;
    }

  };

  /** Logarithmic interpolation. */
  Interpolator LOG = new Interpolator() {

    private final double LD = Math.log(2.0);

    @Override
    public double interpolate(final double t) {
      return Math.log(t + 1) / LD;
    }

    @Override
    public double inverseInterpolate(final double t) {
      return Math.exp(t * LD) - 1.0;
    }

  };

  /** Square root interpolation. */
  Interpolator SQRT = new Interpolator() {

    @Override
    public double interpolate(final double t) {
      return Math.sqrt(t);
    }

    @Override
    public double inverseInterpolate(final double t) {
      return t * t;
    }

  };

}
