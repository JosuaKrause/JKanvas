package jkanvas.util;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;

import org.junit.Test;

/**
 * Tests some paint utility methods.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class PaintUtilTest {

  /**
   * Whether two rectangles are the same.
   * 
   * @param a The expected rectangle.
   * @param b The actual rectangle.
   */
  private static void sameRect(final Rectangle2D a, final Rectangle2D b) {
    assertEquals(a.getX(), b.getX(), Math.ulp(a.getX()));
    assertEquals(a.getY(), b.getY(), Math.ulp(a.getY()));
    assertEquals(a.getWidth(), b.getWidth(), Math.ulp(a.getWidth()));
    assertEquals(a.getHeight(), b.getHeight(), Math.ulp(a.getHeight()));
  }

  /**
   * Tests the method
   * {@link PaintUtil#scaleCenter(java.awt.geom.RectangularShape, double)}.
   */
  @Test
  public void zoomRectTest() {
    final Rectangle2D r = new Rectangle2D.Double(1, 2, 4, 6);
    final Rectangle2D r0p5 = new Rectangle2D.Double(2, 3.5, 2, 3);
    final Rectangle2D r2p0 = new Rectangle2D.Double(-1, -1, 8, 12);
    sameRect(r0p5, PaintUtil.scaleCenter(r, 0.5));
    sameRect(r2p0, PaintUtil.scaleCenter(r, 2.0));
  }

  /**
   * Tests the method
   * {@link PaintUtil#fitInto(java.awt.geom.RectangularShape, double, double)}.
   */
  @Test
  public void fitIntoTest() {
    // h > w
    final Rectangle2D outer0 = new Rectangle2D.Double(1, 2, 4, 6);
    final Rectangle2D inner0h = new Rectangle2D.Double(1, 4, 4, 2);
    sameRect(inner0h, PaintUtil.fitInto(outer0, 4, 2));
    sameRect(inner0h, PaintUtil.fitInto(outer0, 8, 4));
    sameRect(inner0h, PaintUtil.fitInto(outer0, 2, 1));
    final Rectangle2D inner0v = new Rectangle2D.Double(2, 2, 2, 6);
    sameRect(inner0v, PaintUtil.fitInto(outer0, 2, 6));
    sameRect(inner0v, PaintUtil.fitInto(outer0, 4, 12));
    sameRect(inner0v, PaintUtil.fitInto(outer0, 1, 3));
    final Rectangle2D inner0e = new Rectangle2D.Double(1, 2, 4, 6);
    sameRect(inner0e, PaintUtil.fitInto(outer0, 4, 6));
    sameRect(inner0e, PaintUtil.fitInto(outer0, 2, 3));
    sameRect(inner0e, PaintUtil.fitInto(outer0, 8, 12));
    final Rectangle2D inner0q = new Rectangle2D.Double(1, 3, 4, 4);
    sameRect(inner0q, PaintUtil.fitInto(outer0, 4, 4));
    sameRect(inner0q, PaintUtil.fitInto(outer0, 1, 1));
    // h < w
    final Rectangle2D outer1 = new Rectangle2D.Double(1, 2, 6, 4);
    final Rectangle2D inner1h = new Rectangle2D.Double(3, 2, 2, 4);
    sameRect(inner1h, PaintUtil.fitInto(outer1, 2, 4));
    sameRect(inner1h, PaintUtil.fitInto(outer1, 4, 8));
    sameRect(inner1h, PaintUtil.fitInto(outer1, 1, 2));
    final Rectangle2D inner1v = new Rectangle2D.Double(1, 3, 6, 2);
    sameRect(inner1v, PaintUtil.fitInto(outer1, 6, 2));
    sameRect(inner1v, PaintUtil.fitInto(outer1, 12, 4));
    sameRect(inner1v, PaintUtil.fitInto(outer1, 3, 1));
    final Rectangle2D inner1e = new Rectangle2D.Double(1, 2, 6, 4);
    sameRect(inner1e, PaintUtil.fitInto(outer1, 6, 4));
    sameRect(inner1e, PaintUtil.fitInto(outer1, 3, 2));
    sameRect(inner1e, PaintUtil.fitInto(outer1, 12, 8));
    final Rectangle2D inner1q = new Rectangle2D.Double(2, 2, 4, 4);
    sameRect(inner1q, PaintUtil.fitInto(outer1, 4, 4));
    sameRect(inner1q, PaintUtil.fitInto(outer1, 1, 1));
    // h == w
    final Rectangle2D outer2 = new Rectangle2D.Double(1, 1, 2, 2);
    sameRect(new Rectangle2D.Double(1.5, 1, 1, 2), PaintUtil.fitInto(outer2, 2, 4));
    sameRect(new Rectangle2D.Double(1, 1.5, 2, 1), PaintUtil.fitInto(outer2, 4, 2));
    sameRect(outer2, PaintUtil.fitInto(outer2, 1, 1));
    sameRect(outer2, PaintUtil.fitInto(outer2, 3, 3));
    sameRect(outer2, PaintUtil.fitInto(outer2, 4, 4));
  }

}
