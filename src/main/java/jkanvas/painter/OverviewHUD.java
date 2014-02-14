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
import jkanvas.animation.AnimatedDouble;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.util.PaintUtil;

public class OverviewHUD extends HUDRenderpass {

  private final Canvas c;
  private final Renderpass rp;
  private final AnimatedDouble alpha;

  protected OverviewHUD(final Canvas c, final Renderpass rp) {
    this.c = Objects.requireNonNull(c);
    this.rp = Objects.requireNonNull(rp);
    alpha = new AnimatedDouble(1.0);
    c.getAnimator().getAnimationList().addAnimated(alpha);
  }

  public double getCurrentAlpha() {
    return alpha.get();
  }

  public double getPredictedAlpha() {
    return alpha.getPredict();
  }

  public void setAlpha(final double a, final AnimationTiming timing) {
    alpha.startAnimationTo(a, timing);
  }

  private double getScale(final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleComponent();
    final Rectangle2D bbox = new Rectangle2D.Double();
    rp.getBoundingBox(bbox);
    return PaintUtil.fitIntoPixelScale(
        (int) view.getWidth(), (int) view.getHeight(),
        bbox.getWidth(), bbox.getHeight(), true);
  }

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
    final Rectangle2D view = getViewFrame(c.getHUDContext());
    if(view.contains(p)) return false; // let dragging handle it
    final Rectangle2D frame = new Rectangle2D.Double();
    getFullFrame(frame, c.getHUDContext());
    if(!frame.contains(p)) return false;
    setView(p.getX());
    return true;
  }

  protected void setView(final double x) {
    final KanvasContext ctx = c.getHUDContext();
    final Rectangle2D view = ctx.getVisibleCanvas();
    final Rectangle2D comp = ctx.getVisibleComponent();
    final Rectangle2D bbox = new Rectangle2D.Double();
    RenderpassPainter.getTopLevelBounds(bbox, rp);
    final double offX = x * bbox.getWidth() / comp.getWidth();
    final double w = view.getWidth();
    final double h = view.getHeight();
    view.setFrame(offX - w * 0.5, view.getY(), w, h);
    c.showOnly(view);
  }

  private double originalDrag;

  @Override
  public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
    final Rectangle2D view = getViewFrame(c.getHUDContext());
    if(!view.contains(p)) return false;
    System.out.println("hi");
    originalDrag = view.getCenterX();
    return true;
  }

  @Override
  public void dragHUD(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    setView(originalDrag + dx);
  }

  public static final void setupOverviewAndContext(final Canvas c,
      final AnimatedPainter ap, final Renderpass rp) {
    c.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        setup(c, ap, rp);
      }

    }, 0);
  }

  static final void setup(final Canvas c, final RenderpassPainter ap, final Renderpass rp) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    RenderpassPainter.getTopLevelBounds(bbox, rp);
    final OverviewHUD overview = new OverviewHUD(c, rp);
    c.setRestriction(bbox, AnimationTiming.NO_ANIMATION, new AnimationAction() {

      @Override
      public void animationFinished() {
        c.setUserZoomable(false);
        final Rectangle2D bbox = new Rectangle2D.Double();
        RenderpassPainter.getTopLevelBounds(bbox, rp);
        c.showOnly(bbox);
        overview.setView(0);
      }

    });
    ap.addHUDPass(overview);
    c.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(final ComponentEvent e) {
        final Rectangle2D bbox = new Rectangle2D.Double();
        RenderpassPainter.getTopLevelBounds(bbox, rp);
        c.showOnly(bbox);
      }

    });
  }

}
