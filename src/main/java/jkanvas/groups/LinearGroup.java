package jkanvas.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.animation.AnimationAction;
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

  /** The action that is executed when an animation ends. */
  private AnimationAction onFinish;

  /** The space between render-passes. */
  private double space;

  /** Whether the render-passes are laid out in horizontal direction. */
  private boolean horizontal;

  /** The alignment of the group. */
  private double alignmentFactor;

  /**
   * The alignment of a linear group. The alignment is meant relative to the
   * direction of the layout creation.
   * 
   * @author Manuel Hotz <manuel.hotz@uni-konstanz.de>
   */
  public static enum Alignment {

    /** Aligns on the left side. */
    LEFT(0),

    /** Aligns in the middle. */
    MIDDLE(0.5),

    /** Aligns on the right side. */
    RIGHT(1),

    ;

    /** The alignment factor of the alignment. */
    public final double alignmentFactor;

    /**
     * Creates an alignment.
     * 
     * @param alignmentFactor The alignment factor.
     */
    private Alignment(final double alignmentFactor) {
      this.alignmentFactor = alignmentFactor;
    }

  } // Alignment

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
    alignmentFactor = Alignment.MIDDLE.alignmentFactor;
    this.space = space;
    this.timing = Objects.requireNonNull(timing);
  }

  @Override
  protected void doLayout(final List<RenderpassPosition> members) {
    final AnimationTiming timing = getTiming();
    final boolean horizontal = isHorizontal();
    final double alignmentFactor = getAlignment();
    final double space = getSpace();
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
      final double opos = (max - v) * alignmentFactor;
      // TODO FIXME where to put the bounding
      // box position (ie getX(), getY()) consideration? see #13
      final Point2D dest = new Point2D.Double(
          (horizontal ? pos : opos),
          (horizontal ? opos : pos));
      p.startAnimationTo(dest, timing, i == 0 ? onFinish : null);
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
   * @param onFinish Sets the action that is executed when the animation ends.
   */
  public void setOnFinish(final AnimationAction onFinish) {
    this.onFinish = onFinish;
  }

  /**
   * Getter.
   * 
   * @return The action that is executed when the animation ends.
   */
  public AnimationAction getOnFinish() {
    return onFinish;
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

  /**
   * Setter.
   * 
   * @param alignment The alignment of the groups. <code>0</code> aligns to the
   *          left relative to the direction of the layout and <code>1</code>
   *          aligns to the right.
   */
  public void setAlignment(final double alignment) {
    alignmentFactor = alignment;
    invalidate();
  }

  /**
   * Setter.
   * 
   * @param alignment The alignment of the groups.
   */
  public void setAlignment(final Alignment alignment) {
    setAlignment(alignment.alignmentFactor);
  }

  /**
   * Getter.
   * 
   * @return The current alignment. <code>0</code> aligns to the left relative
   *         to the direction of the layout and <code>1</code> aligns to the
   *         right.
   */
  public double getAlignment() {
    return alignmentFactor;
  }

}
