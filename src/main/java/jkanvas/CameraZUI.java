package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.animation.Animated;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.GenericAnimated;
import jkanvas.painter.Renderpass;

/**
 * A zoom-able view with an attached camera.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
class CameraZUI implements ZoomableView, Camera, Animated {

  /** The underlying zoom-able UI. */
  private final ZoomableUI zui;

  /**
   * The animation of the current viewport. This value is meant to be write-only
   * since the used viewport is always that from {@link #zui}.
   */
  private GenericAnimated<Rectangle2D> view;

  /** The canvas. */
  private final Canvas canvas;

  /**
   * Creates a zoom-able camera for the given canvas. This method should only be
   * called by the constructor of the canvas.
   * 
   * @param canvas The canvas.
   * @param restriction The restriction.
   */
  CameraZUI(final Canvas canvas, final RestrictedCanvas restriction) {
    zui = new ZoomableUI(canvas, restriction);
    this.canvas = canvas;
  }

  /** Ensures that {@link #view} is non-<code>null</code>. */
  private void ensureView() {
    if(view != null) return;
    // TODO #43 -- Java 8 simplification
    view = new GenericAnimated<Rectangle2D>(getView()) {

      @Override
      protected Rectangle2D interpolate(
          final Rectangle2D from, final Rectangle2D to, final double t) {
        return jkanvas.util.VecUtil.interpolate(from, to, t);
      }

    };
  }

  /**
   * Clears the current animation. This method must always be called when the
   * viewport is set directly.
   */
  private void stopAnimation() {
    if(view == null) return;
    view.clearAnimation();
  }

  /**
   * Getter.
   * 
   * @return The size of the canvas in component coordinates.
   */
  public Rectangle2D getCanvasRect() {
    return canvas.getCanvasRect();
  }

  @Override
  public Rectangle2D getView() {
    return zui.toCanvas(canvas.getCanvasRect());
  }

  @Override
  public Rectangle2D getPredictView() {
    if(view == null || !view.inAnimation()) return getView();
    return view.getPredict();
  }

  @Override
  public boolean inAnimation() {
    return view != null && view.inAnimation();
  }

  @Override
  public void move(final double dx, final double dy) {
    setOffset(getOffsetX() - dx, getOffsetY() - dy);
  }

  @Override
  public void toView(final Rectangle2D rect, final AnimationTiming timing,
      final AnimationAction onFinish, final boolean useMargin) {
    Objects.requireNonNull(rect);
    Objects.requireNonNull(timing);
    final Rectangle2D r =
        useMargin ? jkanvas.util.PaintUtil.addPadding(rect, canvas.getMargin()) : rect;
    if(r.isEmpty()) {
      canvas.scheduleAction(onFinish, timing);
      return;
    }
    ensureView();
    view.set(getView());
    view.startAnimationTo(r, timing, onFinish);
  }

  @Override
  public void toView(final Renderpass pass, final AnimationTiming timing,
      final AnimationAction onFinish, final boolean useMargin) {
    final Rectangle2D box = new Rectangle2D.Double();
    jkanvas.painter.RenderpassPainter.getTopLevelBounds(box, pass);
    toView(box, timing, onFinish, useMargin);
  }

  /** Is used to delay animation until the canvas is displayed the first time. */
  private Rectangle2D toBeSet;

  @Override
  public boolean animate(final long currentTime) {
    if(toBeSet != null) {
      final Rectangle2D vis = canvas.getCanvasRect();
      if(!vis.isEmpty()) {
        zui.showRectangle(toBeSet, vis, 0, true);
        toBeSet = null;
      }
      return true;
    }
    if(view == null) return false;
    if(!view.animate(currentTime)) return false;
    final Rectangle2D vis = canvas.getCanvasRect();
    final Rectangle2D v = view.get();
    if(v.isEmpty()) return true;
    if(vis.isEmpty()) {
      toBeSet = v;
      return true;
    }
    zui.showRectangle(v, vis, 0, true);
    return true;
  }

  @Override
  public void showRectangle(final RectangularShape view,
      final RectangularShape screen, final double margin, final boolean fit) {
    stopAnimation();
    zui.showRectangle(view, screen, margin, fit);
  }

  @Override
  public void resetView(final RectangularShape screen) {
    stopAnimation();
    zui.resetView(screen);
  }

  @Override
  public void setOffset(final double x, final double y) {
    stopAnimation();
    zui.setOffset(x, y);
  }

  @Override
  public void zoom(final double factor, final RectangularShape box) {
    stopAnimation();
    zui.zoom(factor, box);
  }

  @Override
  public void zoomTicks(final double x, final double y, final double zooming) {
    stopAnimation();
    zui.zoomTicks(x, y, zooming);
  }

  @Override
  public void zoomTo(final double x, final double y, final double factor) {
    stopAnimation();
    zui.zoomTo(x, y, factor);
  }

  @Override
  public void setMaxZoom(final double zoom) {
    zui.setMaxZoom(zoom);
  }

  @Override
  public void setMinZoom(final double zoom) {
    zui.setMinZoom(zoom);
  }

  @Override
  public void transform(final AffineTransform at) {
    zui.transform(at);
  }

  @Override
  public void transform(final Graphics2D g) {
    zui.transform(g);
  }

  @Override
  public void transformBack(final AffineTransform at) {
    zui.transformBack(at);
  }

  @Override
  public Rectangle2D toCanvas(final RectangularShape rect) {
    return zui.toCanvas(rect);
  }

  @Override
  public boolean isRestricted() {
    return zui.isRestricted();
  }

  @Override
  public double fromReal(final double s) {
    return zui.fromReal(s);
  }

  @Override
  public double inReal(final double s) {
    return zui.inReal(s);
  }

  @Override
  public Point2D getForScreen(final Point2D p) {
    return zui.getForScreen(p);
  }

  @Override
  public double getXForScreen(final double x) {
    return zui.getXForScreen(x);
  }

  @Override
  public double getXFromCanvas(final double x) {
    return zui.getXFromCanvas(x);
  }

  @Override
  public double getYForScreen(final double y) {
    return zui.getYForScreen(y);
  }

  @Override
  public double getYFromCanvas(final double y) {
    return zui.getYFromCanvas(y);
  }

  @Override
  public double getOffsetX() {
    return zui.getOffsetX();
  }

  @Override
  public double getOffsetY() {
    return zui.getOffsetY();
  }

  @Override
  public boolean hasMaxZoom() {
    return zui.hasMaxZoom();
  }

  @Override
  public double getMaxZoom() {
    return zui.getMaxZoom();
  }

  @Override
  public boolean hasMinZoom() {
    return zui.hasMinZoom();
  }

  @Override
  public double getMinZoom() {
    return zui.getMinZoom();
  }

}
