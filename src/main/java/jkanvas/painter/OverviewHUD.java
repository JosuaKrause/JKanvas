package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.ViewConfiguration;
import jkanvas.animation.AnimatedDouble;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.util.PaintUtil;

/**
 * A HUD that shows a render item fully as overlay. The user also can navigate
 * in the overview.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class OverviewHUD extends HUDRenderpass {

  /** The canvas. */
  private final Canvas c;
  /** The render item to show. */
  private final Renderpass rp;
  /** The alpha value of the overview. */
  private final AnimatedDouble alpha;
  /** The maximal allowed height. */
  private final double maxHeight;

  /**
   * Creates an overview.
   * 
   * @param c The canvas.
   * @param rp The render item.
   * @param maxHeight The maximal allowed height.
   */
  protected OverviewHUD(final Canvas c, final Renderpass rp, final double maxHeight) {
    this.c = Objects.requireNonNull(c);
    this.rp = Objects.requireNonNull(rp);
    this.maxHeight = maxHeight;
    alpha = new AnimatedDouble(1.0);
    c.getAnimator().getAnimationList().addAnimated(alpha);
  }

  /**
   * Getter.
   * 
   * @return The current alpha value.
   */
  public double getCurrentAlpha() {
    return alpha.get();
  }

  /**
   * Getter.
   * 
   * @return The predicted alpha value when the alpha value is changing.
   */
  public double getPredictedAlpha() {
    return alpha.getPredict();
  }

  /**
   * Setter.
   * 
   * @param a The new alpha value.
   * @param timing The timing.
   */
  public void setAlpha(final double a, final AnimationTiming timing) {
    alpha.startAnimationTo(a, timing);
  }

  /**
   * Computes the scale of the render item.
   * 
   * @param ctx The HUD canvas context.
   * @return The scale.
   */
  private double getScale(final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleComponent();
    final Rectangle2D bbox = new Rectangle2D.Double();
    rp.getBoundingBox(bbox);
    final double s = PaintUtil.fitIntoPixelScale(
        (int) view.getWidth(), (int) view.getHeight(),
        bbox.getWidth(), bbox.getHeight(), true);
    if(bbox.getHeight() * s > maxHeight) return maxHeight / bbox.getHeight();
    return s;
  }

  /**
   * Computes the currently visible portion of the render item in overlay
   * coordinates.
   * 
   * @param ctx The HUD canvas context.
   * @return The view rectangle.
   */
  private Rectangle2D getViewFrame(final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleComponent();
    final Rectangle2D bbox = new Rectangle2D.Double();
    rp.getBoundingBox(bbox);
    final Rectangle2D canvasView = ctx.toCanvasCoordinates(view);
    final Rectangle2D vis = canvasView.createIntersection(bbox);
    final double s = getScale(ctx);
    vis.setFrame(vis.getX() * s, vis.getY() * s, vis.getWidth() * s, vis.getHeight() * s);
    return vis;
  }

  /**
   * Computes the bounding box of the render item in overlay coordinates.
   * 
   * @param rect The rectangle to store the result in.
   * @param ctx The HUD canvas context.
   */
  private void getFullFrame(final Rectangle2D rect, final KanvasContext ctx) {
    rp.getBoundingBox(rect);
    final double s = getScale(ctx);
    rect.setFrame(rect.getX() * s, rect.getY() * s,
        rect.getWidth() * s, rect.getHeight() * s);
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    final Graphics2D g = (Graphics2D) gfx.create();
    final Rectangle2D bbox = new Rectangle2D.Double();
    rp.getBoundingBox(bbox);
    final CacheContext gCtx = new CacheContext(bbox);
    PaintUtil.setAlpha(g, alpha.get());
    final double s = getScale(ctx);
    gCtx.doScale(s);
    g.scale(s, s);
    rp.draw(g, gCtx);
    g.dispose();
    gfx.setColor(Color.RED);
    gfx.draw(getViewFrame(ctx));
  }

  @Override
  public boolean clickHUD(final Camera cam, final Point2D p, final MouseEvent e) {
    final KanvasContext ctx = c.getHUDContext();
    final Rectangle2D view = getViewFrame(ctx);
    if(view.contains(p)) return false; // let dragging handle it
    final Rectangle2D frame = new Rectangle2D.Double();
    getFullFrame(frame, ctx);
    if(!frame.contains(p)) return false;
    setView(p.getX());
    Canvas.acceptHUDDragging(this);
    return true;
  }

  /**
   * Setter.
   * 
   * @param x Sets the current view centered around this horizontal coordinate
   *          on the overlay.
   */
  protected void setView(final double x) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    RenderpassPainter.getTopLevelBounds(bbox, rp);
    final KanvasContext ctx = c.getHUDContext();
    final double s = getScale(ctx);
    final double offX = x / s;
    final double w = bbox.getWidth() / s;
    final double h = bbox.getHeight();
    bbox.setFrame(offX - w * 0.5, 0, w, h);
    c.showOnly(bbox);
  }

  /**
   * Getter.
   * 
   * @return The current horizontal coordinate on which the view is centered on.
   */
  protected double getView() {
    final KanvasContext ctx = c.getHUDContext();
    final Rectangle2D view = getViewFrame(ctx);
    return view.getCenterX();
  }

  /** Whether the view is moved or set directly. */
  private boolean moveView;
  /** The original dragging position. */
  private double originalDrag;
  /** The last x coordinate. */
  private double lastX;
  /** The last y coordinate. */
  private double lastY;

  @Override
  public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
    final Rectangle2D frame = new Rectangle2D.Double();
    final KanvasContext ctx = c.getHUDContext();
    getFullFrame(frame, ctx);
    if(!frame.contains(p)) return false;
    final Rectangle2D view = getViewFrame(ctx);
    if(view.contains(p)) {
      moveView = true;
      lastX = 0;
      lastY = 0;
    } else {
      originalDrag = view.getCenterX();
    }
    return true;
  }

  @Override
  public void dragHUD(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    if(moveView) {
      final ViewConfiguration cfg = c.getViewConfiguration();
      final KanvasContext ctx = cfg.getHUDContext();
      final Rectangle2D view = getViewFrame(ctx);
      final Rectangle2D rect = ctx.getVisibleComponent();
      c.getCamera().move((dx - lastX) * rect.getWidth() / view.getWidth(),
          (dy - lastY) * rect.getHeight() / view.getHeight());
      lastX = dx;
      lastY = dy;
    } else {
      setView(originalDrag + dx);
    }
  }

  /**
   * Adds an overview to the given painter.
   * 
   * @param c The canvas.
   * @param ap The painter.
   * @param rp The render item to use as overview.
   * @param preventUserZoom Whether to prevent the user from using zoom.
   */
  public static final void setupOverviewAndContext(final Canvas c,
      final AnimatedPainter ap, final Renderpass rp, final boolean preventUserZoom) {
    c.scheduleAction(new AnimationAction() {

      @Override
      // TODO #43 -- Java 8 simplification
      public void animationFinished() {
        setup(c, ap, rp, preventUserZoom);
      }

    }, 0);
  }

  /**
   * Sets up the overview.
   * 
   * @param c The canvas.
   * @param ap The painter.
   * @param rp The render item.
   * @param preventUserZoom Whether to prevent user initiated zoom.
   */
  static final void setup(final Canvas c, final RenderpassPainter ap,
      final Renderpass rp, final boolean preventUserZoom) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    RenderpassPainter.getTopLevelBounds(bbox, rp);
    final OverviewHUD overview = new OverviewHUD(c, rp, 100.0);
    c.setRestriction(bbox, AnimationTiming.NO_ANIMATION, new AnimationAction() {

      @Override
      // TODO #43 -- Java 8 simplification
      public void animationFinished() {
        if(preventUserZoom) {
          c.setUserZoomable(false);
        }
        overview.setView(0);
      }

    });
    ap.addHUDPass(overview);
    c.addComponentListener(new ComponentAdapter() {

      @Override
      // TODO #43 -- Java 8 simplification
      public void componentResized(final ComponentEvent e) {
        overview.setView(overview.getView());
      }

    });
  }

}
