package kanvas.util;

public final class MathUtil {

  private MathUtil() {
    throw new AssertionError();
  }

  public static double clamp(final double d, final double min, final double max) {
    return d < min ? min : (d > max ? max : d);
  }

  public static double clamp(final double d) {
    return clamp(d, 0, 1);
  }

}
