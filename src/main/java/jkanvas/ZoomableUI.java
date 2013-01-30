package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.util.PaintUtil;

/**
 * A zoom-able user interface can be translated and zooming can be performed.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class ZoomableUI {

  /** The {@link Refreshable} to be notified when the transformation changes. */
  private final Refreshable refreshee;

  /** An optional restriction of the canvas. */
  private final RestrictedCanvas restriction;

  /** The x offset. */
  private double offX;

  /** The y offset. */
  private double offY;

  /** The zoom level. */
  private double zoom = 1;

  /** The minimal zoom value. */
  private double minZoom;

  /** The maximal zoom value. */
  private double maxZoom;

  /**
   * Creates a zoom-able user interface.
   * 
   * @param refreshee Will be notified when the transformation changes.
   * @param restriction An optional restriction for the canvas.
   */
  public ZoomableUI(final Refreshable refreshee, final RestrictedCanvas restriction) {
    this.refreshee = Objects.requireNonNull(refreshee);
    this.restriction = restriction;
  }

  /**
   * Setter.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  public void setOffset(final double x, final double y) {
    if(!isRestricted()) {
      offX = x;
      offY = y;
    } else if(!setRestrictedOffset(x, y)) return;
    refreshee.refresh();
  }

  /**
   * Sets the offset while keeping restrictions.
   * 
   * @param x The x offset.
   * @param y The y offset.
   * @return Whether repainting is needed.
   */
  private boolean setRestrictedOffset(final double x, final double y) {
    final Rectangle2D bbox = restriction.getBoundingRect();
    if(bbox == null) return false;
    offX = x;
    offY = y;
    final Rectangle2D comp = restriction.getComponentView();
    final Rectangle2D visBB = toCanvas(comp);
    // snap back
    if(bbox.contains(visBB)) return true;
    if(visBB.getWidth() > bbox.getWidth()) {
      offX = comp.getCenterX() - fromReal(bbox.getCenterX());
    } else {
      final double transX;
      if(visBB.getMaxX() > bbox.getMaxX()) {
        // too far right
        transX = bbox.getMaxX() - visBB.getMaxX();
      } else if(visBB.getMinX() < bbox.getMinX()) {
        // too far left
        transX = bbox.getMinX() - visBB.getMinX();
      } else {
        transX = 0;
      }
      offX -= fromReal(transX);
    }
    if(visBB.getHeight() > bbox.getHeight()) {
      offY = comp.getCenterY() - fromReal(bbox.getCenterY());
    } else {
      final double transY;
      if(visBB.getMaxY() > bbox.getMaxY()) {
        // too far down
        transY = bbox.getMaxY() - visBB.getMaxY();
      } else if(visBB.getMinY() < bbox.getMinY()) {
        // too far up
        transY = bbox.getMinY() - visBB.getMinY();
      } else {
        transY = 0;
      }
      offY -= fromReal(transY);
    }
    return true;
  }

  /**
   * Zooms to the on screen (in component coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param zooming The amount of zooming.
   */
  public void zoomTicks(final double x, final double y, final int zooming) {
    final double factor = Math.pow(1.1, -zooming);
    zoomTo(x, y, factor);
  }

  /**
   * Zooms to the on screen (in component coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param factor The factor to alter the zoom level. Must be
   *          <code>&gt;0</code>.
   */
  public void zoomTo(final double x, final double y, final double factor) {
    double f = factor;
    double newZoom = zoom * factor;
    if(newZoom <= 0) throw new IllegalArgumentException(
        "factor: " + factor + " zoom: " + newZoom);
    if(newZoom < minZoom) {
      // minZoom > 0 since newZoom > 0
      newZoom = minZoom;
      f = newZoom / zoom;
    }
    if(hasMaxZoom() && newZoom > maxZoom) {
      newZoom = maxZoom;
      f = newZoom / zoom;
    }
    if(isRestricted()) {
      final Rectangle2D bbox = restriction.getBoundingRect();
      if(bbox == null) return; // change nothing
      final Rectangle2D visBB = restriction.getComponentView();
      final double min = PaintUtil.fitIntoScale(visBB, bbox.getWidth(), bbox.getHeight());
      if(newZoom < min) {
        newZoom = min;
        f = newZoom / zoom;
      }
    }
    // P = (off - mouse) / zoom
    // P = (newOff - mouse) / newZoom
    // newOff = (off - mouse) / zoom * newZoom + mouse
    // newOff = (off - mouse) * factor + mouse
    zoom = newZoom;
    // does repaint
    setOffset((offX - x) * f + x, (offY - y) * f + y);
  }

  /**
   * Zooms towards the center of the given rectangle.
   * 
   * @param factor The zoom factor.
   * @param box The rectangle to zoom to in component coordinates.
   */
  public void zoom(final double factor, final Rectangle2D box) {
    zoomTo(box.getCenterX(), box.getCenterY(), factor);
  }

  /**
   * Resets the view such that the origin of canvas coordinates lies in the
   * center of the given rectangle in component coordinates.
   * 
   * @param screen The screen rectangle in component coordinates.
   */
  public void resetView(final Rectangle2D screen) {
    zoom = 1;
    // does repaint
    setOffset(screen.getCenterX(), screen.getCenterY());
  }

  /**
   * Transforms the view such that the rectangle <code>view</code> in canvas
   * coordinates matches the rectangle <code>screen</code> in component
   * coordinates. An additional margin can be added.
   * 
   * @param view The rectangle in canvas coordinates.
   * @param screen The rectangle in component coordinates.
   * @param margin The margin.
   * @param fit If set to <code>true</code> the <code>view</code> will be
   *          completely visible. When set to <code>false</code> as much as
   *          possible from <code>view</code> will be visible without showing
   *          anything else.
   */
  public void showRectangle(final Rectangle2D view, final Rectangle2D screen,
      final double margin, final boolean fit) {
    final int nw = (int) (screen.getWidth() - 2 * margin);
    final int nh = (int) (screen.getHeight() - 2 * margin);
    final double rw = nw / view.getWidth();
    final double rh = nh / view.getHeight();
    final double factor = fit ? Math.min(rw, rh) : Math.max(rw, rh);
    zoom = 1;
    setOffset(margin + (nw - view.getWidth()) / 2 - view.getMinX(), margin
        + (nh - view.getHeight()) / 2 - view.getMinY());
    zoom(factor, screen);
  }

  /**
   * Getter.
   * 
   * @return the x offset
   */
  public double getOffsetX() {
    return offX;
  }

  /**
   * Getter.
   * 
   * @return the y offset
   */
  public double getOffsetY() {
    return offY;
  }

  /**
   * Transforms the given graphics object.
   * 
   * @param gfx The graphics object.
   */
  public void transform(final Graphics2D gfx) {
    gfx.translate(offX, offY);
    gfx.scale(zoom, zoom);
  }

  /**
   * Transforms the given affine transformation.
   * 
   * @param at The affine transformation.
   */
  public void transform(final AffineTransform at) {
    at.translate(offX, offY);
    at.scale(zoom, zoom);
  }

  /**
   * Transforms the given affine transformation back.
   * 
   * @param at The affine transformation.
   */
  public void transformBack(final AffineTransform at) {
    at.scale(1 / zoom, 1 / zoom);
    at.translate(-offX, -offY);
  }

  /**
   * Calculates the real coordinate of the given input in component coordinates.
   * 
   * @param s The coordinate in component coordinates. Due to uniform zooming
   *          both horizontal and vertical coordinates can be converted.
   * @return In real coordinates.
   */
  public double inReal(final double s) {
    return s / zoom;
  }

  /**
   * Calculates the component coordinate of the given input in real coordinates.
   * 
   * @param s The coordinate in real coordinates. Due to uniform zooming both
   *          horizontal and vertical coordinates can be converted.
   * @return In screen coordinates.
   */
  public double fromReal(final double s) {
    return s * zoom;
  }

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param x The components x coordinate.
   * @return The real coordinate.
   */
  public double getXForScreen(final double x) {
    return inReal(x - offX);
  }

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param y The components y coordinate.
   * @return The real coordinate.
   */
  public double getYForScreen(final double y) {
    return inReal(y - offY);
  }

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param x The real x coordinate.
   * @return The component coordinate.
   */
  public double getXFromCanvas(final double x) {
    return fromReal(x) + offX;
  }

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param y The real y coordinate.
   * @return The component coordinate.
   */
  public double getYFromCanvas(final double y) {
    return fromReal(y) + offY;
  }

  /**
   * Converts a point in component coordinates to canvas coordinates.
   * 
   * @param p The point.
   * @return The point in the canvas coordinates.
   */
  public Point2D getForScreen(final Point2D p) {
    return new Point2D.Double(getXForScreen(p.getX()), getYForScreen(p.getY()));
  }

  /**
   * Converts a rectangle in component coordinates to canvas coordinates.
   * 
   * @param rect The rectangle.
   * @return The rectangle in canvas coordinates.
   */
  public Rectangle2D toCanvas(final Rectangle2D rect) {
    return new Rectangle2D.Double(
        getXForScreen(rect.getMinX()), getYForScreen(rect.getMinY()),
        inReal(rect.getWidth()), inReal(rect.getHeight()));
  }

  /**
   * Getter.
   * 
   * @return Whether a restriction is set.
   */
  public boolean isRestricted() {
    return restriction != null;
  }

  /**
   * Returns the minimal zoom value.
   * 
   * @return The minimal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  public double getMinZoom() {
    return minZoom;
  }

  /**
   * Getter.
   * 
   * @return Whether zoom has a minimum.
   */
  public boolean hasMinZoom() {
    return minZoom > 0;
  }

  /**
   * Sets the current minimal zoom value.
   * 
   * @param zoom The new minimal zoom value. Non-positive values indicate no
   *          restriction.
   */
  public void setMinZoom(final double zoom) {
    minZoom = zoom;
  }

  /**
   * Returns the maximal zoom value.
   * 
   * @return The maximal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  public double getMaxZoom() {
    return maxZoom;
  }

  /**
   * Getter.
   * 
   * @return Whether zoom has a maximum.
   */
  public boolean hasMaxZoom() {
    return maxZoom > 0;
  }

  /**
   * Sets the current maximal zoom value.
   * 
   * @param zoom The new maximal zoom value. Non-positive values indicate no
   *          restriction.
   */
  public void setMaxZoom(final double zoom) {
    maxZoom = zoom;
  }

}
