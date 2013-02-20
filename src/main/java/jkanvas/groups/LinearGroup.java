package jkanvas.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;

/**
 * A group linearly ordering the members. Render-passes without bounding boxes
 * are <em>not</em> allowed.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class LinearGroup extends RenderGroup {

  /** The animation timing. */
  private final AnimationTiming timing;

  /** The space between render-passes. */
  private final double space;

  /** Whether the render-passes are laid out in horizontal direction. */
  private final boolean horizontal;

  /**
   * Creates a linear group.
   * 
   * @param animator The underlying animator.
   * @param isHorizontal Whether render-passes are laid out in horizontal
   *          direction.
   * @param space The space between two render-passes.
   * @param timing The animation timing.
   */
  public LinearGroup(final Animator animator, final boolean isHorizontal,
      final double space, final AnimationTiming timing) {
    super(animator);
    horizontal = isHorizontal;
    this.space = space;
    this.timing = Objects.requireNonNull(timing);
  }

  @Override
  protected void doLayout(final List<RenderpassPosition> members) {
    double pos = 0;
    for(final RenderpassPosition p : members) {
      final Point2D dest = new Point2D.Double(horizontal ? pos : 0, horizontal ? 0 : pos);
      p.changeAnimationTo(dest, timing);
      final Rectangle2D bbox = p.pass.getBoundingBox();
      if(bbox == null) throw new IllegalStateException("bbox must not be null");
      pos += (horizontal ? bbox.getWidth() : bbox.getHeight()) + space;
    }
  }

}
