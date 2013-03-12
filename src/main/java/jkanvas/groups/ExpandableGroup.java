package jkanvas.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;

/**
 * A group that can be expanded.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ExpandableGroup extends LinearGroup {

  /** Whether the group is expanded. */
  private boolean expanded = false;

  /**
   * Creates an expandable group.
   * 
   * @param animator The animator.
   * @param isHorizontal Whether the group is laid out horizontally.
   * @param space The space between render passes.
   * @param timing The animation timing.
   */
  public ExpandableGroup(final Animator animator, final boolean isHorizontal,
      final double space, final AnimationTiming timing) {
    super(animator, isHorizontal, space, timing);
  }

  @Override
  protected void doLayout(final List<RenderpassPosition> members) {
    if(expanded) {
      super.doLayout(members);
      return;
    }
    final AnimationTiming timing = getTiming();
    double w = 0;
    double h = 0;
    for(final RenderpassPosition m : members) {
      if(!m.pass.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = m.getPredictBBox();
      if(bbox == null) {
        continue;
      }
      w = Math.max(w, bbox.getWidth());
      h = Math.max(h, bbox.getHeight());
    }
    boolean first = true;
    for(final RenderpassPosition m : members) {
      final Rectangle2D bbox = m.getPredictBBox();
      if(bbox == null) {
        continue;
      }
      // TODO FIXME where to put the bounding box
      // position (ie getX(), getY()) consideration? see #13
      final Point2D dest = new Point2D.Double(
          (w - bbox.getWidth()) * 0.5,
          (h - bbox.getHeight()) * 0.5);
      if(!m.pass.isVisible()) {
        m.set(dest);
      } else if(!m.getPredict().equals(dest)) {
        m.startAnimationTo(dest, timing, first ? getOnFinish() : null);
        first = false;
      }
    }
  }

  /**
   * Setter.
   * 
   * @param expanded Whether the group is expanded.
   */
  public void setExpanded(final boolean expanded) {
    this.expanded = expanded;
    invalidate();
  }

  /**
   * Getter.
   * 
   * @return Whether the group is expanded.
   */
  public boolean isExpanded() {
    return expanded;
  }

}
