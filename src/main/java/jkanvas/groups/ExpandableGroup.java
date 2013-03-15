package jkanvas.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jkanvas.animation.AnimationAction;
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
  protected AnimationAction chooseLayout(final List<RenderpassPosition> members,
      final AnimationTiming timing, final boolean horizontal,
      final double alignmentFactor, final double space,
      final List<Rectangle2D> bboxes, final double max, final AnimationAction cof) {
    if(expanded) return linearLayout(members, timing, horizontal,
        alignmentFactor, space, bboxes, max, cof);
    return compactLayout(members, timing, bboxes, cof);
  }

  /**
   * The compact layout. All render passes are condensed in the space of the
   * largest render pass.
   * 
   * @param members The members.
   * @param timing The animation timing.
   * @param bboxes The bounding boxes.
   * @param cof The animation action.
   * @return The animation action if not used by <em>one</em> render pass
   *         animation. <code>null</code> otherwise.
   */
  protected AnimationAction compactLayout(
      final List<RenderpassPosition> members, final AnimationTiming timing,
      final List<Rectangle2D> bboxes, final AnimationAction cof) {
    AnimationAction c = cof;
    double w = 0;
    double h = 0;
    for(final Rectangle2D bbox : bboxes) {
      w = Math.max(w, bbox.getWidth());
      h = Math.max(h, bbox.getHeight());
    }
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
        m.startAnimationTo(dest, timing, c);
        c = null;
      }
    }
    return c;
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
   * Setter.
   * 
   * @param expanded Whether the group is expanded.
   * @param onFinish The action that will be performed after the laying out is
   *          complete.
   */
  public void setExpanded(final boolean expanded, final AnimationAction onFinish) {
    this.expanded = expanded;
    final AnimationAction of = getOnFinish();
    setOnFinish(onFinish);
    invalidate();
    setOnFinish(of);
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
