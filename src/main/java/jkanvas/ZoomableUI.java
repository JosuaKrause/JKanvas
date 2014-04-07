package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

/**
 * The implementation of a zoom-able user interface that can be translated.
 * Zooming can also be performed.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class ZoomableUI implements ZoomableView {

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

  @Override
  public void setOffset(final double x, final double y) {
    if(isRestricted()) {
      setRestrictedOffset(x, y);
    } else {
      offX = x;
      offY = y;
    }
    refreshee.refresh();
  }

  /**
   * Sets the offset while keeping restrictions.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  private void setRestrictedOffset(final double x, final double y) {
    final Rectangle2D bbox = restriction.getBoundingRect();
    offX = x;
    offY = y;
    if(bbox == null) return;
    final Rectangle2D comp = restriction.getComponentView();
    final Rectangle2D visBB = toCanvas(comp);
    // snap back
    if(bbox.contains(visBB)) return;
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
  }

  @Override
  public void zoomTicks(final double x, final double y, final double zooming) {
    final double factor = Math.pow(1.1, -zooming);
    zoomTo(x, y, factor);
  }

  @Override
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
      if(bbox != null) {
        final Rectangle2D visBB = restriction.getComponentView();
        // load class only when restricting the view
        final double min = jkanvas.util.PaintUtil.fitIntoScale(
            visBB, bbox.getWidth(), bbox.getHeight());
        if(newZoom < min) {
          newZoom = min;
          f = newZoom / zoom;
        }
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

  @Override
  public void zoom(final double factor, final RectangularShape box) {
    zoomTo(box.getCenterX(), box.getCenterY(), factor);
  }

  @Override
  public void resetView(final RectangularShape screen) {
    zoom = 1;
    // does repaint
    setOffset(screen.getCenterX(), screen.getCenterY());
  }

  @Override
  public void showRectangle(final RectangularShape view, final RectangularShape screen,
      final double margin, final boolean fit) {
    final int nw = (int) (screen.getWidth() - 2 * margin);
    final int nh = (int) (screen.getHeight() - 2 * margin);
    final double factor = jkanvas.util.PaintUtil.fitIntoPixelScale(
        nw, nh, view.getWidth(), view.getHeight(), fit);
    // must compute initial values by hand, because of possible
    // intermediate views that are out of the restriction
    zoom = 1;
    offX = margin + (nw - view.getWidth()) / 2 - view.getMinX();
    offY = margin + (nh - view.getHeight()) / 2 - view.getMinY();
    zoom(factor, screen);
    setOffset(offX, offY);
  }

  @Override
  public double getOffsetX() {
    return offX;
  }

  @Override
  public double getOffsetY() {
    return offY;
  }

  @Override
  public void transform(final Graphics2D g) {
    g.translate(offX, offY);
    g.scale(zoom, zoom);
  }

  @Override
  public void transform(final AffineTransform at) {
    at.translate(offX, offY);
    at.scale(zoom, zoom);
  }

  @Override
  public void transformBack(final AffineTransform at) {
    at.scale(1 / zoom, 1 / zoom);
    at.translate(-offX, -offY);
  }

  @Override
  public double inReal(final double s) {
    return s / zoom;
  }

  @Override
  public double fromReal(final double s) {
    return s * zoom;
  }

  @Override
  public double getXForScreen(final double x) {
    return inReal(x - offX);
  }

  @Override
  public double getYForScreen(final double y) {
    return inReal(y - offY);
  }

  @Override
  public double getXFromCanvas(final double x) {
    return fromReal(x) + offX;
  }

  @Override
  public double getYFromCanvas(final double y) {
    return fromReal(y) + offY;
  }

  @Override
  public Point2D getForScreen(final Point2D p) {
    return new Point2D.Double(getXForScreen(p.getX()), getYForScreen(p.getY()));
  }

  @Override
  public Rectangle2D toCanvas(final RectangularShape rect) {
    return new Rectangle2D.Double(
        getXForScreen(rect.getMinX()), getYForScreen(rect.getMinY()),
        inReal(rect.getWidth()), inReal(rect.getHeight()));
  }

  @Override
  public boolean isRestricted() {
    return restriction != null;
  }

  @Override
  public double getMinZoom() {
    return minZoom;
  }

  @Override
  public boolean hasMinZoom() {
    return minZoom > 0;
  }

  @Override
  public void setMinZoom(final double zoom) {
    minZoom = zoom;
  }

  @Override
  public double getMaxZoom() {
    return maxZoom;
  }

  @Override
  public boolean hasMaxZoom() {
    return maxZoom > 0;
  }

  @Override
  public void setMaxZoom(final double zoom) {
    maxZoom = zoom;
  }

}
