package jkanvas.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the integrity of the known {@link Interpolator} implementations.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class InterpolatorTest {

  /**
   * Tests the inverse interpolation of the given {@link Interpolator}.
   * 
   * @param pol The interpolator.
   */
  private static void testInverse(final Interpolator pol) {
    for(double t = 0; t < 1; t += 1e-5) {
      assertEquals(t, pol.inverseInterpolate(pol.interpolate(t)), 1e-3);
      assertEquals(t, pol.interpolate(pol.inverseInterpolate(t)), 1e-3);
    }
    assertEquals(1.0, pol.inverseInterpolate(pol.interpolate(1)), 1e-3);
    assertEquals(1.0, pol.interpolate(pol.inverseInterpolate(1)), 1e-3);
  }

  /**
   * Tests the inverse interpolation of known {@link Interpolator}
   * implementations.
   */
  @Test
  public void testInverse() {
    testInverse(Interpolator.LINEAR);
    testInverse(Interpolator.SMOOTH);
    testInverse(Interpolator.SLOW_IN_OUT);
    testInverse(Interpolator.QUAD_IN_OUT);
    testInverse(Interpolator.LOG);
    testInverse(Interpolator.SQRT);
  }

  /**
   * Tests start and end points.
   * 
   * @param pol The interpolation.
   */
  private static void testStartEnd(final Interpolator pol) {
    assertEquals(0, pol.interpolate(0), 1e-3);
    assertEquals(1, pol.interpolate(1), 1e-3);
  }

  /** Checks whether start and end points are interpolated correctly. */
  @Test
  public void testStartEnd() {
    testStartEnd(Interpolator.LINEAR);
    testStartEnd(Interpolator.SMOOTH);
    testStartEnd(Interpolator.SLOW_IN_OUT);
    testStartEnd(Interpolator.QUAD_IN_OUT);
    testStartEnd(Interpolator.LOG);
    testStartEnd(Interpolator.SQRT);
  }

}
