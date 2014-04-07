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
import java.util.Objects;
import java.util.TreeMap;

import jkanvas.animation.AnimationTiming;
import jkanvas.painter.Renderpass;
import jkanvas.painter.groups.RenderGroup.RenderpassPosition;
import jkanvas.painter.groups.RenderpassLayout;

/**
 * Lays out card pods.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public abstract class CardGroupLayout<T extends Renderpass>
    extends RenderpassLayout<CardPod<T>> {

  /** The number of columns or zero if there is no restriction. */
  private int columns = 0;

  /**
   * Setter.
   * 
   * @param cols The number of columns or zero if there is no restriction.
   */
  public void setColumnCount(final int cols) {
    this.columns = cols;
  }

  /**
   * Getter.
   * 
   * @return The order of the cards.
   */
  protected abstract Comparator<T> order();

  /**
   * Getter.
   * 
   * @return The gap between cards.
   */
  public double getGap() {
    return 5;
  }

  @Override
  public void doLayout(
      final List<RenderpassPosition<CardPod<T>>> members) {
    final Map<String, Double> widths = new HashMap<>();
    final Map<String, Double> heights = new HashMap<>();
    final Map<String, List<RenderpassPosition<CardPod<T>>>> groups = new TreeMap<>();
    final Rectangle2D rect = new Rectangle2D.Double();
    for(final RenderpassPosition<CardPod<T>> m : members) {
      final CardPod<T> p = m.pass;
      p.setVisible(true);
      final String group = p.getGroup();
      if(group == null) {
        continue;
      }
      if(!groups.containsKey(group)) {
        groups.put(group, new ArrayList<RenderpassPosition<CardPod<T>>>());
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
    final Comparator<RenderpassPosition<CardPod<T>>> cmp = new Comparator<RenderpassPosition<CardPod<T>>>() {

      private final Comparator<T> order = order();

      @Override
      public int compare(final RenderpassPosition<CardPod<T>> rp1,
          final RenderpassPosition<CardPod<T>> rp2) {
        final T t1 = rp1.pass.unwrap();
        final T t2 = rp2.pass.unwrap();
        return order.compare(t1, t2);
      }

    };
    for(final Entry<String, List<RenderpassPosition<CardPod<T>>>> e : groups.entrySet()) {
      final String group = e.getKey();
      final double w = widths.get(group);
      final double h = heights.get(group);
      int curCol = 0;
      final List<RenderpassPosition<CardPod<T>>> l = e.getValue();
      Collections.sort(l, cmp);
      for(final RenderpassPosition<CardPod<T>> m : l) {
        if(curCol == 0) {
          cur.setLocation(topRight);
          topRight.setLocation(topRight.getX(), topRight.getY() + h + gap);
        }
        m.pass.getBoundingBox(rect);
        final Point2D pos = new Point2D.Double(cur.getX() + (w - rect.getWidth()) * 0.5,
            cur.getY() + (h - rect.getHeight()) * 0.5);
        m.startAnimationTo(pos, timing);
        cur.setLocation(cur.getX() + w + gap, cur.getY());
        ++curCol;
        if(columns > 0 && curCol >= columns) {
          curCol = 0;
        }
      }
    }
  }

  /** The animation timing. */
  private AnimationTiming timing = AnimationTiming.NO_ANIMATION;

  /**
   * Setter.
   * 
   * @param timing The animation timing.
   */
  public void setTiming(final AnimationTiming timing) {
    this.timing = Objects.requireNonNull(timing);
  }

  /**
   * Getter.
   * 
   * @return The animation timing.
   */
  public AnimationTiming getTiming() {
    return timing;
  }

}
