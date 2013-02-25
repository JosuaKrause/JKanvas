package jkanvas.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;

/**
 * A group linearly ordering the members. Render-passes without bounding boxes
 * are <em>not</em> allowed.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class LinearGroup extends RenderGroup {

  /** The animation timing. */
  private AnimationTiming timing;

  /** The space between render-passes. */
  private double space;

  /** Whether the render-passes are laid out in horizontal direction. */
  private boolean horizontal;

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
    final List<Rectangle2D> bboxes = new ArrayList<>(members.size());
    double max = 0;
    for(final RenderpassPosition p : members) {
      final Renderpass pass = p.pass;
      final Rectangle2D bbox = pass.getBoundingBox();
      if(bbox == null) throw new IllegalStateException("bbox must not be null");
      bboxes.add(bbox);
      if(!pass.isVisible()) {
        continue;
      }
      final double v = horizontal ? bbox.getHeight() : bbox.getWidth();
      if(v > max) {
        max = v;
      }
    }
    double pos = 0;
    for(int i = 0; i < members.size(); ++i) {
      final RenderpassPosition p = members.get(i);
      final Rectangle2D bbox = bboxes.get(i);
      if(!p.pass.isVisible()) {
        continue;
      }
      final double v = horizontal ? bbox.getHeight() : bbox.getWidth();
      final double opos = (max - v) * 0.5;
      final Point2D dest = new Point2D.Double(
          horizontal ? pos : opos, horizontal ? opos : pos);
      p.changeAnimationTo(dest, timing);
      pos += (horizontal ? bbox.getWidth() : bbox.getHeight()) + space;
    }
  }

  /**
   * Setter.
   * 
   * @param timing The timing of the animations.
   */
  public void setTiming(final AnimationTiming timing) {
    this.timing = Objects.requireNonNull(timing);
    invalidate();
  }

  /**
   * Getter.
   * 
   * @return The timing of the animations.
   */
  public AnimationTiming getTiming() {
    return timing;
  }

  /**
   * Setter.
   * 
   * @param space The space between render-passes.
   */
  public void setSpace(final double space) {
    this.space = space;
    invalidate();
  }

  /**
   * Getter.
   * 
   * @return The space between render-passes.
   */
  public double getSpace() {
    return space;
  }

  /**
   * Setter.
   * 
   * @param horizontal Whether the orientation of the groups is horizontal.
   */
  public void setOrientation(final boolean horizontal) {
    this.horizontal = horizontal;
    invalidate();
  }

  /**
   * Getter.
   * 
   * @return Whether the orientation of the groups is horizontal.
   */
  public boolean isHorizontal() {
    return horizontal;
  }

}
