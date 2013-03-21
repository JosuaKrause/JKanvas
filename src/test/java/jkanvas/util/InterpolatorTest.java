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
    for(double t = 0; t < 1; t += 0.001) {
      assertEquals(t, pol.inverseInterpolate(pol.interpolate(t)), 0.00001);
      assertEquals(t, pol.interpolate(pol.inverseInterpolate(t)), 0.00001);
    }
    assertEquals(1.0, pol.inverseInterpolate(pol.interpolate(1)), 0.00001);
    assertEquals(1.0, pol.interpolate(pol.inverseInterpolate(1)), 0.00001);
  }

  /**
   * Tests the inverse interpolation of known {@link Interpolator}
   * implementations.
   */
  @Test
  public void testInverse() {
    testInverse(Interpolator.LINEAR);
    testInverse(Interpolator.SMOOTH);
  }

}
