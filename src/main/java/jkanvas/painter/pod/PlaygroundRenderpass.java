package jkanvas.painter.pod;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;
import jkanvas.painter.groups.RenderGroup;

public class PlaygroundRenderpass<T extends Renderpass> extends
    RenderGroup<PlaygroundPod<T>> {

  private int rows = 0;

  public PlaygroundRenderpass(final Animator animator) {
    super(animator);
  }

  public void setRowCount(final int rows) {
    this.rows = rows;
  }

  public double getGap() {
    return 5;
  }

  @Override
  protected void doLayout(
      final List<RenderpassPosition<PlaygroundPod<T>>> members) {
    final Map<String, Double> widths = new HashMap<>();
    final Map<String, Double> heights = new HashMap<>();
    final Map<String, List<RenderpassPosition<PlaygroundPod<T>>>> groups = new HashMap<>();
    final Rectangle2D rect = new Rectangle2D.Double();
    for(final RenderpassPosition<PlaygroundPod<T>> m : members) {
      final PlaygroundPod<T> p = m.pass;
      final String group = p.getGroup();
      if(group == null) {
        continue;
      }
      if(!groups.containsKey(group)) {
        groups.put(group, new ArrayList<RenderpassPosition<PlaygroundPod<T>>>());
      }
      groups.get(group).add(m);
      p.getBoundingBox(rect);
      final Double w = widths.get(group);
      if(w == null || w < rect.getWidth()) {
        widths.put(group, rect.getWidth());
      }
      final Double h = heights.get(group);
      if(h == null || h < rect.getHeight()) {
        heights.put(group, rect.getHeight());
      }
    }
    final double gap = getGap();
    final Point2D topRight = new Point2D.Double();
    final Point2D cur = new Point2D.Double();
    int curCol = 0;
    for(final Entry<String, List<RenderpassPosition<PlaygroundPod<T>>>> e : groups.entrySet()) {
      final String group = e.getKey();
      final double w = widths.get(group);
      final double h = heights.get(group);
      curCol = 0;
      for(final RenderpassPosition<PlaygroundPod<T>> m : e.getValue()) {
        if(curCol == 0) {
          cur.setLocation(topRight);
          topRight.setLocation(topRight.getX(), topRight.getY() + h + gap);
        }
        m.pass.getBoundingBox(rect);
        m.set(new Point2D.Double(cur.getX() + (w - rect.getWidth()) * 0.5,
            cur.getY() + (h - rect.getHeight()) * 0.5));
        cur.setLocation(cur.getX() + w + gap, cur.getY());
        ++curCol;
        if(rows > 0 && curCol >= rows) {
          curCol = 0;
        }
      }
    }
  }

}
