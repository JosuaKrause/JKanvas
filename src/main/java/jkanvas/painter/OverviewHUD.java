package jkanvas.painter;

import java.awt.Graphics2D;
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
    final Rectangle2D canvasView = ctx.toCanvasCoordinates(view);
    final Rectangle2D bbox = new Rectangle2D.Double();
    rp.getBoundingBox(bbox);
    final double s = PaintUtil.fitIntoPixelScale((int) view.getWidth(),
        (int) view.getHeight(),
        bbox.getWidth(), bbox.getHeight(), true);
    final CacheContext gCtx = new CacheContext(canvasView);
    gCtx.doScale(s);
    g.scale(s, s);
    PaintUtil.setAlpha(g, alpha.get());
    rp.draw(g, gCtx);
    g.dispose();
    // TODO draw viewport
  }

  public static final void setupOverviewAndContext(final Canvas c,
      final AnimatedPainter ap, final Renderpass rp) {
    c.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        final Rectangle2D bbox = new Rectangle2D.Double();
        rp.getBoundingBox(bbox);
        c.setRestriction(bbox, AnimationTiming.NO_ANIMATION);
        c.setUserZoomable(false);
        c.showOnly(bbox);
        ap.addHUDPass(new OverviewHUD(c, rp));
      }

    }, 0);
  }

}
