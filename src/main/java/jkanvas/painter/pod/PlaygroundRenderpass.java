package jkanvas.painter.pod;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;
import jkanvas.painter.groups.RenderGroup;

public class PlaygroundRenderpass<T extends Renderpass> extends
    RenderGroup<PlaygroundPod<T>> {

  public PlaygroundRenderpass(final Animator animator) {
    super(animator);
  }

  @Override
  protected void doLayout(
      final List<RenderpassPosition<PlaygroundPod<T>>> members) {
    // no layout
  }

  private final Point2D last = new Point2D.Double();

  @Override
  protected void addedRenderpass(final RenderpassPosition<PlaygroundPod<T>> rp) {
    super.addedRenderpass(rp);
    rp.set(new Point2D.Double(last.getX(), last.getY()));
    final Rectangle2D rect = new Rectangle2D.Double();
    rp.pass.getBoundingBox(rect);
    last.setLocation(last.getX(), last.getY() + rect.getHeight() + 5);
  }

}
