package jkanvas.util;

/**
 * An interpolator maps values from the range <code>[0,1]</code> to
 * <code>[0,1]</code>.
 * 
 * @author Joschi <josua.krause@googlemail.com>
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

  /** Smooth interpolation. */
  Interpolator SLOW_IN_OUT = new Interpolator() {

    /** The strength of the fast phase. */
    private final double STRENGTH = 4;

    /** The minimal value of the arc tangents. */
    private final double MIN_VAL = VecUtil.fastArcTan(-0.5 * STRENGTH);

    /** The range of the arc tangets. */
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

}
