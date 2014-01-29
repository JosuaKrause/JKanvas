package jkanvas;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;

import org.junit.Test;

/**
 * Tests the zoom-able user interface.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ZoomableUITest {

  /**
   * Whether two rectangles are the same.
   * 
   * @param a The expected rectangle.
   * @param b The actual rectangle.
   */
  private static void sameRect(final Rectangle2D a, final Rectangle2D b) {
    try {
      assertEquals(a.getX(), b.getX(), 1e-10);
      assertEquals(a.getY(), b.getY(), 1e-10);
      assertEquals(a.getWidth(), b.getWidth(), 1e-10);
      assertEquals(a.getHeight(), b.getHeight(), 1e-10);
    } catch(final AssertionError ae) {
      System.out.println("expected[x:" + a.getX() + " y:" + a.getY()
          + " w:" + a.getWidth() + " h:" + a.getHeight() + "]");
      System.out.println("actual[x:" + b.getX() + " y:" + b.getY()
          + " w:" + b.getWidth() + " h:" + b.getHeight() + "]");
      throw ae;
    }
  }

  /**
   * A {@link Refreshable} that counts the number of refreshes.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private class DummyRefreshable implements Refreshable {

    /** The number of refreshes. */
    private int refreshed;

    /** Creates a {@link DummyRefreshable}. */
    public DummyRefreshable() {
      refreshed = 0;
    }

    @Override
    public void refresh() {
      ++refreshed;
    }

    /**
     * Checks whether the expected number of refreshes has happened. The number
     * is reset afterwards.
     * 
     * @param expected The number of expected refreshes.
     */
    public void testRefreshed(final int expected) {
      assertEquals(expected, refreshed);
      refreshed = 0;
    }

  } // DummyRefreshable

  /** Tests the capability of setting the view to an given rectangle. */
  @Test
  public void resetTest() {
    final DummyRefreshable dummy = new DummyRefreshable();
    final ZoomableUI zui = new ZoomableUI(dummy, null);
    final Rectangle2D view = new Rectangle2D.Double(0, 0, 1, 1);
    final Rectangle2D rect = new Rectangle2D.Double(2, 3, 4, 5);

    zui.showRectangle(rect, view, 0, true);
    dummy.testRefreshed(2);
    final Rectangle2D canvasView1 = zui.toCanvas(view);
    final Rectangle2D expected1 = new Rectangle2D.Double(1.5, 3, 5, 5);
    sameRect(expected1, canvasView1);

    zui.showRectangle(rect, view, 0, false);
    dummy.testRefreshed(2);
    final Rectangle2D canvasView2 = zui.toCanvas(view);
    final Rectangle2D expected2 = new Rectangle2D.Double(2, 3.5, 4, 4);
    sameRect(expected2, canvasView2);
  }

  /** Tests zooming with a restricted canvas. */
  @Test
  public void zoomTest() {
    final Rectangle2D comp = new Rectangle2D.Double(0, 0, 1, 1);
    final Rectangle2D canvas = new Rectangle2D.Double(1, 1, 3, 3);
    final DummyRefreshable dummy = new DummyRefreshable();
    final ZoomableUI zui = new ZoomableUI(dummy, new RestrictedCanvas() {

      @Override
      public Rectangle2D getComponentView() {
        return comp;
      }

      @Override
      public Rectangle2D getBoundingRect() {
        return canvas;
      }

    });
    zui.zoom(
        Math.min(comp.getWidth() / canvas.getWidth(),
            comp.getHeight() / canvas.getHeight()), comp);
    zui.setOffset(zui.getOffsetX(), zui.getOffsetY() - 10);
    zui.setOffset(zui.getOffsetX() - 10, zui.getOffsetY());
    dummy.testRefreshed(3);
    sameRect(canvas, zui.toCanvas(comp));

    zui.zoomTo(0, 0, 2);
    zui.setOffset(zui.getOffsetX() + 10, zui.getOffsetY());
    zui.setOffset(zui.getOffsetX(), zui.getOffsetY() + 10);
    dummy.testRefreshed(3);
    sameRect(new Rectangle2D.Double(1, 1, 1.5, 1.5), zui.toCanvas(comp));

    zui.setOffset(zui.getOffsetX() - 10, zui.getOffsetY());
    zui.setOffset(zui.getOffsetX(), zui.getOffsetY() - 10);
    dummy.testRefreshed(2);
    sameRect(new Rectangle2D.Double(2.5, 2.5, 1.5, 1.5), zui.toCanvas(comp));

    zoomTest(new Rectangle2D.Double(0, 0, 2, 1));
    zoomTest(new Rectangle2D.Double(0, 0, 1, 2));
  }

  /**
   * Tests with arbitrary formed component sizes.
   * 
   * @param comp The component size.
   */
  private void zoomTest(final Rectangle2D comp) {
    final Rectangle2D canvas = new Rectangle2D.Double(1, 1, 3, 3);
    final DummyRefreshable dummy = new DummyRefreshable();
    final ZoomableUI zui = new ZoomableUI(dummy, new RestrictedCanvas() {

      @Override
      public Rectangle2D getComponentView() {
        return comp;
      }

      @Override
      public Rectangle2D getBoundingRect() {
        return canvas;
      }

    });
    zui.zoomTo(0, 0, Math.min(comp.getWidth() / canvas.getWidth(),
        comp.getHeight() / canvas.getHeight()) * 0.01);
    dummy.testRefreshed(1);
    assertEquals(canvas.getCenterX(), zui.toCanvas(comp).getCenterX(), 1e-10);
    assertEquals(canvas.getCenterY(), zui.toCanvas(comp).getCenterY(), 1e-10);
  }

  /**
   * Validates that the canvas can always be moved when restricted without
   * bounding box.
   */
  @Test
  public void moveTest() {
    final Rectangle2D comp = new Rectangle2D.Double(0, 0, 1, 1);
    final DummyRefreshable dummy = new DummyRefreshable();
    final ZoomableUI zui = new ZoomableUI(dummy, new RestrictedCanvas() {

      @Override
      public Rectangle2D getComponentView() {
        // should not be used
        fail();
        return null;
      }

      @Override
      public Rectangle2D getBoundingRect() {
        return null;
      }

    });
    zui.setOffset(2, 2);
    zui.zoomTo(2, 2, 2);
    dummy.testRefreshed(2);
    sameRect(new Rectangle2D.Double(-1, -1, 0.5, 0.5), zui.toCanvas(comp));
  }

  /** Checks the functionality of zooming bounds. */
  @Test
  public void zoomBoundsTest() {
    final Rectangle2D rect = new Rectangle2D.Double(0, 0, 1, 1);
    final DummyRefreshable dummy = new DummyRefreshable();
    final ZoomableUI zui = new ZoomableUI(dummy, null);
    zui.setMaxZoom(10);
    zui.zoom(20, rect);
    zui.setMinZoom(0.1);
    zui.zoom(0.05 * 0.05, rect);
    zui.zoom(10, rect);
    sameRect(rect, zui.toCanvas(rect));
    dummy.testRefreshed(3);
  }

}
