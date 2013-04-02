package jkanvas.util;

import static jkanvas.util.VecUtil.*;
import static junit.framework.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

/**
 * Tests the vector utility class.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class VecUtilTest {

  /** Tests the computation of orientation differences. */
  @Test
  public void angleTest() {
    final Point2D r = new Point2D.Double(1, 0);
    final Point2D ur = new Point2D.Double(1, 1);
    final Point2D u = new Point2D.Double(0, 1);
    final Point2D ul = new Point2D.Double(-1, 1);
    final Point2D l = new Point2D.Double(-1, 0);
    final Point2D dl = new Point2D.Double(-1, -1);
    final Point2D d = new Point2D.Double(0, -1);
    final Point2D dr = new Point2D.Double(1, -1);
    final Point2D[] counterClock = { r, ur, u, ul, l, dl, d, dr};
    final double[] angles = { 0, Math.PI * 0.25, Math.PI * 0.5, Math.PI * 0.75, Math.PI,
        Math.PI * 0.75, Math.PI * 0.5, Math.PI * 0.25};
    for(int i = 0; i < counterClock.length; ++i) {
      final Point2D a = counterClock[i];
      for(int k = 0; k < counterClock.length; ++k) {
        final Point2D b = counterClock[(i + k) % counterClock.length];
        assertEquals(angles[k], getOrientationDifference(a, b), 1e-9);
      }
    }
  }

  /**
   * Asserts that both points are the same with a very small grace threshold.
   * 
   * @param expected The expected point.
   * @param actual The actual point.
   */
  public static final void equals(final Point2D expected, final Point2D actual) {
    assertEquals(expected.getX(), actual.getX(), 1e-9);
    assertEquals(expected.getY(), actual.getY(), 1e-9);
  }

  /** Tests basic vector operations. */
  @Test
  public void vectorTest() {
    final Point2D a = new Point2D.Double(0, 0);
    final Point2D b = new Point2D.Double(2, 0);
    final Point2D c = new Point2D.Double(0, 4);
    final Point2D d = new Point2D.Double(-3, 6);
    equals(b, addVec(a, b));
    equals(new Point2D.Double(-1, 10), addVec(b, addVec(c, d)));
    equals(new Point2D.Double(-5, 10), subVec(addVec(c, d), b));
    equals(subVec(a, b), invVec(b));
    equals(new Point2D.Double(4, 0), mulVec(b, 2));
    equals(new Point2D.Double(1, 0), mulVec(b, 0.5));
    assertEquals(0, getLength(a), 1e-9);
    assertEquals(2, getLength(b), 1e-9);
    assertEquals(45, getLengthSq(d), 1e-9);
    assertEquals(Math.sqrt(45), getLength(d), 1e-9);
    equals(new Point2D.Double(1.0, 2), middleVec(b, c));
    equals(new Point2D.Double(2.0, 0), interpolate(b, c, 0));
    equals(new Point2D.Double(1.5, 1), interpolate(b, c, 0.25));
    equals(new Point2D.Double(1.0, 2), interpolate(b, c, 0.5));
    equals(new Point2D.Double(0.5, 3), interpolate(b, c, 0.75));
    equals(new Point2D.Double(0.0, 4), interpolate(b, c, 1));
    equals(new Point2D.Double(6, 3), getOrthoLeft(d));
    equals(new Point2D.Double(-6, -3), getOrthoRight(d));
    equals(new Point2D.Double(-6, 12), setLength(d, 2 * Math.sqrt(45)));
    assertEquals(Math.toRadians(60),
        getOrientation(new Point2D.Double(1, -Math.sqrt(3))), 1e-9);
    assertEquals(M_2_PI - Math.toRadians(60),
        getOrientation(new Point2D.Double(1, Math.sqrt(3))), 1e-9);
  }

  /** Tests rotation functions. */
  @Test
  public void rotations() {
    final Point2D v = new Point2D.Double(2, 0);
    equals(new Point2D.Double(0, -2), rotate(v, Math.PI * 0.5));
    equals(new Point2D.Double(-2, 0), rotate(v, Math.PI));
    equals(new Point2D.Double(0, 2), rotate(v, Math.PI * 1.5));
    equals(v, rotate(v, Math.PI * 2));
    final Point2D a = new Point2D.Double(2, 3);
    final Point2D b = new Point2D.Double(3, 3);
    final Point2D c = new Point2D.Double(2, 4);
    final Point2D d = new Point2D.Double(1, 3);
    final Point2D e = new Point2D.Double(2, 2);
    equals(b, rotateByAngle(b, a, Math.PI * 2));
    equals(c, rotateByAngle(b, a, Math.PI * 1.5));
    equals(d, rotateByAngle(b, a, Math.PI));
    equals(e, rotateByAngle(b, a, Math.PI * 0.5));
    equals(b, rotateByAngle(b, a, 0));
    equals(b, rotate(b, a, 0));
    equals(c, rotate(b, a, -Math.sqrt(2)));
    equals(d, rotate(b, a, 2));
    equals(e, rotate(b, a, Math.sqrt(2)));
    equals(d, rotate(b, a, 3));
  }

}
