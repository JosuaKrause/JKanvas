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

}
