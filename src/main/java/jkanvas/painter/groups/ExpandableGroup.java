package jkanvas.painter.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;

/**
 * A group that can be expanded.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of layouted render passes.
 */
public class ExpandableGroup<T extends Renderpass> extends LinearGroup<T> {

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
  protected void chooseLayout(final List<RenderpassPosition<T>> members,
      final AnimationTiming timing, final boolean horizontal,
      final double alignmentFactor, final double space,
      final List<Rectangle2D> bboxes, final double maxH, final double maxV) {
    if(expanded) {
      linearLayout(members, timing, horizontal, alignmentFactor,
          space, bboxes, maxH, maxV);
    } else {
      compactLayout(members, timing);
    }
  }

  /**
   * The compact layout. All render passes are condensed in the space of the
   * largest render pass.
   * 
   * @param members The members.
   * @param timing The animation timing.
   */
  protected void compactLayout(final List<RenderpassPosition<T>> members,
      final AnimationTiming timing) {
    for(final RenderpassPosition<T> m : members) {
      // TODO FIXME where to put the bounding box
      // position (ie getX(), getY()) consideration? see #13
      final Point2D dest = new Point2D.Double(0, 0);
      if(!m.pass.isVisible()) {
        m.set(dest);
      } else if(!m.getPredict().equals(dest)) {
        m.startAnimationTo(dest, timing, null);
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
