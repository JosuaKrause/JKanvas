package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

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

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    final Graphics2D g = (Graphics2D) gfx.create();
    final Rectangle2D view = ctx.getVisibleComponent();
    final Rectangle2D bbox = new Rectangle2D.Double();
    rp.getBoundingBox(bbox);
    final double s = PaintUtil.fitIntoPixelScale(
        (int) view.getWidth(), (int) view.getHeight(),
        bbox.getWidth(), bbox.getHeight(), true);
    final CacheContext gCtx = new CacheContext(bbox);
    PaintUtil.setAlpha(g, alpha.get());
    gCtx.doScale(s);
    g.scale(s, s);
    rp.draw(g, gCtx);
    g.dispose();
    gfx.setColor(Color.RED);
    final Rectangle2D canvasView = ctx.toCanvasCoordinates(view);
    final Rectangle2D vis = canvasView.createIntersection(bbox);
    vis.setFrame(vis.getX() * s, vis.getY() * s, vis.getWidth() * s, vis.getHeight() * s);
    gfx.draw(vis);
  }

  public static final void setupOverviewAndContext(final Canvas c,
      final AnimatedPainter ap, final Renderpass rp) {
    c.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        final Rectangle2D bbox = new Rectangle2D.Double();
        RenderpassPainter.getTopLevelBounds(bbox, rp);
        c.setRestriction(bbox, AnimationTiming.NO_ANIMATION, new AnimationAction() {

          @Override
          public void animationFinished() {
            c.setUserZoomable(false);
            c.showOnly(bbox);
          }

        });
        ap.addHUDPass(new OverviewHUD(c, rp));
        c.addComponentListener(new ComponentAdapter() {

          @Override
          public void componentResized(final ComponentEvent e) {
            final Rectangle2D bbox = new Rectangle2D.Double();
            RenderpassPainter.getTopLevelBounds(bbox, rp);
            c.showOnly(bbox);
          }

        });
      }

    }, 0);
  }

}
