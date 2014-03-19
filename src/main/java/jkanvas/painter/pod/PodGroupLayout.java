package jkanvas.painter.pod;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jkanvas.painter.Renderpass;
import jkanvas.painter.groups.RenderGroup.RenderpassPosition;
import jkanvas.painter.groups.RenderpassLayout;

public abstract class PodGroupLayout<T extends Renderpass>
    implements RenderpassLayout<PlaygroundPod<T>> {

  private int rows = 0;

  public void setRowCount(final int rows) {
    this.rows = rows;
  }

  protected abstract Comparator<T> order();

  public double getGap() {
    return 5;
  }

  @Override
  public void doLayout(
      final List<RenderpassPosition<PlaygroundPod<T>>> members) {
    final Map<String, Double> widths = new HashMap<>();
    final Map<String, Double> heights = new HashMap<>();
    final Map<String, List<RenderpassPosition<PlaygroundPod<T>>>> groups = new HashMap<>();
    final Rectangle2D rect = new Rectangle2D.Double();
    for(final RenderpassPosition<PlaygroundPod<T>> m : members) {
      final PlaygroundPod<T> p = m.pass;
      p.setVisible(true);
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
    final Comparator<RenderpassPosition<PlaygroundPod<T>>> cmp = new Comparator<RenderpassPosition<PlaygroundPod<T>>>() {

      private final Comparator<T> order = order();

      @Override
      public int compare(final RenderpassPosition<PlaygroundPod<T>> rp1,
          final RenderpassPosition<PlaygroundPod<T>> rp2) {
        final T t1 = rp1.pass.unwrap();
        final T t2 = rp2.pass.unwrap();
        return order.compare(t1, t2);
      }

    };
    for(final Entry<String, List<RenderpassPosition<PlaygroundPod<T>>>> e : groups.entrySet()) {
      final String group = e.getKey();
      final double w = widths.get(group);
      final double h = heights.get(group);
      int curCol = 0;
      final List<RenderpassPosition<PlaygroundPod<T>>> l = e.getValue();
      Collections.sort(l, cmp);
      for(final RenderpassPosition<PlaygroundPod<T>> m : l) {
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
