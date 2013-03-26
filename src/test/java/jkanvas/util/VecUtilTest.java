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

  /** Tests basic vector operations. */
  @Test
  public void vectorTest() {
    final Point2D a = new Point2D.Double(0, 0);
    final Point2D b = new Point2D.Double(2, 0);
    final Point2D c = new Point2D.Double(0, 4);
    final Point2D d = new Point2D.Double(-3, 6);
    assertEquals(b, addVec(a, b));
    assertEquals(new Point2D.Double(-1, 10), addVec(b, addVec(c, d)));
    assertEquals(new Point2D.Double(-5, 10), subVec(addVec(c, d), b));
    assertEquals(subVec(a, b), invVec(b));
    assertEquals(new Point2D.Double(4, 0), mulVec(b, 2));
    assertEquals(new Point2D.Double(1, 0), mulVec(b, 0.5));
    assertEquals(0, getLength(a), 1e-9);
    assertEquals(2, getLength(b), 1e-9);
    assertEquals(45, getLengthSq(d), 1e-9);
    assertEquals(Math.sqrt(45), getLength(d), 1e-9);
    assertEquals(new Point2D.Double(1.0, 2), middleVec(b, c));
    assertEquals(new Point2D.Double(2.0, 0), interpolate(b, c, 0));
    assertEquals(new Point2D.Double(1.5, 1), interpolate(b, c, 0.25));
    assertEquals(new Point2D.Double(1.0, 2), interpolate(b, c, 0.5));
    assertEquals(new Point2D.Double(0.5, 3), interpolate(b, c, 0.75));
    assertEquals(new Point2D.Double(0.0, 4), interpolate(b, c, 1));
    assertEquals(new Point2D.Double(6, 3), getOrthoRight(d));
    assertEquals(new Point2D.Double(-6, -3), getOrthoLeft(d));
    assertEquals(new Point2D.Double(-6, 12), setLength(d, 2 * Math.sqrt(45)));
    Point2D rot = rotate(b, Math.PI * 0.5);
    assertEquals(0, rot.getX(), 1e-9);
    assertEquals(2, rot.getY(), 1e-9);
    rot = rotate(b, Math.PI);
    assertEquals(-2, rot.getX(), 1e-9);
    assertEquals(0, rot.getY(), 1e-9);
    rot = rotate(b, Math.PI * 1.5);
    assertEquals(0, rot.getX(), 1e-9);
    assertEquals(-2, rot.getY(), 1e-9);
  }

}
