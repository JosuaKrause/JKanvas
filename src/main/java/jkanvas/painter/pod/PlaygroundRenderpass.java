package jkanvas.painter.pod;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;
import jkanvas.painter.groups.RenderGroup;

public class PlaygroundRenderpass<T extends Renderpass> extends
    RenderGroup<PlaygroundPod<T>> {

  public PlaygroundRenderpass(final Animator animator) {
    super(animator);
  }

  public double getBorder() {
    return 5;
  }

  @Override
  protected void doLayout(
      final List<RenderpassPosition<PlaygroundPod<T>>> members) {
    final Map<String, Double> widths = new HashMap<>();
    final Rectangle2D rect = new Rectangle2D.Double();
    for(final RenderpassPosition<PlaygroundPod<T>> m : members) {
      final PlaygroundPod<T> p = m.pass;
      final String group = p.getGroup();
      if(group == null) {
        continue;
      }
      p.getBoundingBox(rect);
      final Double d = widths.get(group);
      if(d == null || d < rect.getWidth()) {
        widths.put(group, rect.getWidth());
      }
    }
    final double border = getBorder();
    final Point2D topRight = new Point2D.Double();
    final Map<String, Point2D> groups = new HashMap<>();
    for(final RenderpassPosition<PlaygroundPod<T>> m : members) {
      final PlaygroundPod<T> p = m.pass;
      final String group = p.getGroup();
      if(group == null) {
        continue;
      }
      Point2D pos = groups.get(group);
      final double w = widths.get(group);
      if(pos == null) {
        pos = new Point2D.Double(topRight.getX(), topRight.getY());
        topRight.setLocation(topRight.getX() + w + border, topRight.getY());
      }
      p.getBoundingBox(rect);
      final double x = pos.getX();
      pos.setLocation(pos.getX() + (w - rect.getWidth()) * 0.5, pos.getY());
      m.set(pos);
      groups.put(group, new Point2D.Double(x, pos.getY() + border + rect.getHeight()));
    }
  }

}
