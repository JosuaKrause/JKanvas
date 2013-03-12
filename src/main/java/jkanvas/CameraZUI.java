package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.animation.Animated;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.GenericAnimated;
import jkanvas.util.VecUtil;

class CameraZUI implements ZoomableView, Camera, Animated {

  private final ZoomableUI zui;

  private GenericAnimated<Rectangle2D> view;

  private final Canvas canvas;

  CameraZUI(final Canvas canvas, final boolean restricted) {
    zui = new ZoomableUI(canvas, restricted ? canvas : null);
    this.canvas = canvas;
  }

  private void ensureView() {
    if(view != null) return;
    view = new GenericAnimated<Rectangle2D>(getView()) {

      @Override
      protected Rectangle2D interpolate(final Rectangle2D from, final Rectangle2D to,
          final double t) {
        return VecUtil.interpolate(from, to, t);
      }

    };
  }

  private void stopAnimation() {
    if(view == null) return;
    view.clearAnimation();
  }

  @Override
  public Rectangle2D getView() {
    return zui.toCanvas(canvas.getVisibleRect());
  }

  @Override
  public Rectangle2D getPredictView() {
    if(view == null || !view.inAnimation()) return getView();
    return view.getPredict();
  }

  @Override
  public void toView(final Rectangle2D rect, final AnimationTiming timing) {
    Objects.requireNonNull(rect);
    Objects.requireNonNull(timing);
    ensureView();
    view.set(getView());
    view.startAnimationTo(rect, timing);
  }

  @Override
  public boolean animate(final long currentTime) {
    if(view == null) return false;
    if(!view.inAnimation()) return false;
    final boolean chg = view.animate(currentTime);
    zui.showRectangle(view.get(), canvas.getVisibleRect(), 0, true);
    return chg;
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
  public void zoomTicks(final double x, final double y, final int zooming) {
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
