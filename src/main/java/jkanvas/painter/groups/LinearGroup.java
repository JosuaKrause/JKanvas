package jkanvas.painter.groups;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;

/**
 * A group linearly ordering the members. Render passes without bounding boxes
 * are <em>not</em> allowed.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of layouted render passes.
 */
public class LinearGroup<T extends Renderpass> extends RenderGroup<T> {

  /** The on finish action to use when layouting. */
  private final AtomicReference<AnimationAction> curOnFinish;

  /** The animation timing. */
  private AnimationTiming timing;

  /** The action that is executed when an animation ends. */
  private AnimationAction onFinish;

  /** The space between render passes. */
  private double space;

  /** Whether the render passes are laid out in horizontal direction. */
  private boolean horizontal;

  /**
   * The position where to break the line. If the value is smaller or equal than
   * zero no line break is performed.
   */
  private double breakPoint;

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
    CENTER(0.5),

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
   * @param isHorizontal Whether render passes are laid out in horizontal
   *          direction.
   * @param space The space between two render passes.
   * @param timing The animation timing.
   */
  public LinearGroup(final Animator animator, final boolean isHorizontal,
      final double space, final AnimationTiming timing) {
    super(animator);
    horizontal = isHorizontal;
    alignmentFactor = Alignment.CENTER.alignmentFactor;
    this.space = space;
    this.timing = Objects.requireNonNull(timing);
    curOnFinish = new AtomicReference<>();
  }

  @Override
  public void invalidate() {
    super.invalidate();
    final AnimationAction cof = curOnFinish.getAndSet(onFinish);
    getAnimator().getAnimationList().scheduleAction(cof, timing);
  }

  /**
   * Clears the current on finish action.
   * 
   * @return The on finish action to use.
   */
  protected AnimationAction clearCurrentOnFinish() {
    return curOnFinish.getAndSet(null);
  }

  @Override
  protected final void doLayout(final List<RenderpassPosition<T>> members) {
    final AnimationTiming timing = getTiming();
    final boolean horizontal = isHorizontal();
    final double alignmentFactor = getAlignment();
    final double space = getSpace();
    final List<Rectangle2D> bboxes = new ArrayList<>(members.size());
    double maxH = 0;
    double maxV = 0;
    for(final RenderpassPosition<T> p : members) {
      final Renderpass pass = p.pass;
      final Rectangle2D bbox = new Rectangle2D.Double();
      pass.getBoundingBox(bbox);
      bboxes.add(bbox);
      if(!pass.isVisible()) {
        continue;
      }
      final double w = bbox.getWidth();
      final double h = bbox.getHeight();
      if(w > maxH) {
        maxH = w;
      }
      if(h > maxV) {
        maxV = h;
      }
    }
    final AnimationAction cof = clearCurrentOnFinish();
    getAnimator().getAnimationList().scheduleAction(cof, timing);
    chooseLayout(members, timing, horizontal, alignmentFactor, space, bboxes, maxH, maxV);
  }

  /**
   * Chooses the layout algorithm.
   * 
   * @param members The members.
   * @param timing The animation timing.
   * @param horizontal Whether the primary direction is horizontal.
   * @param alignmentFactor The alignment factor.
   * @param space The space between render passes.
   * @param bboxes The bounding boxes.
   * @param maxH The maximal horizontal space a render pass uses.
   * @param maxV The maximal vertical space a render pass uses.
   */
  protected void chooseLayout(final List<RenderpassPosition<T>> members,
      final AnimationTiming timing, final boolean horizontal,
      final double alignmentFactor, final double space, final List<Rectangle2D> bboxes,
      final double maxH, final double maxV) {
    linearLayout(members, timing, horizontal, alignmentFactor, space, bboxes, maxH, maxV);
  }

  /**
   * The linear layout.
   * 
   * @param members The members.
   * @param timing The animation timing.
   * @param horizontal Whether the primary direction is horizontal.
   * @param alignmentFactor The alignment factor.
   * @param space The space between render passes.
   * @param bboxes The bounding boxes.
   * @param maxH The maximal horizontal space a render pass uses.
   * @param maxV The maximal vertical space a render pass uses.
   */
  protected void linearLayout(final List<RenderpassPosition<T>> members,
      final AnimationTiming timing, final boolean horizontal,
      final double alignmentFactor, final double space, final List<Rectangle2D> bboxes,
      final double maxH, final double maxV) {
    final double max = horizontal ? maxV : maxH;
    final boolean breakLines = breakPoint > 0;
    double pos = 0;
    double ortho = 0;
    for(int i = 0; i < members.size(); ++i) {
      final RenderpassPosition<T> p = members.get(i);
      final Rectangle2D bbox = bboxes.get(i);
      if(!p.pass.isVisible()) {
        continue;
      }
      final double v = horizontal ? bbox.getHeight() : bbox.getWidth();
      final double opos = (max - v) * alignmentFactor;
      // TODO FIXME where to put the bounding
      // box position (ie getX(), getY()) consideration? see #13
      final Point2D dest = new Point2D.Double(
          (horizontal ? pos : ortho + opos),
          (horizontal ? ortho + opos : pos));
      p.startAnimationTo(dest, timing, null);
      pos += (horizontal ? bbox.getWidth() : bbox.getHeight()) + space;
      if(breakLines && pos >= breakPoint) {
        pos = 0;
        ortho += (horizontal ? bbox.getHeight() : bbox.getWidth()) + space;
      }
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
   * @param space The space between render passes.
   */
  public void setSpace(final double space) {
    this.space = space;
    invalidate();
  }

  /**
   * Getter.
   * 
   * @return The space between render passes.
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

  /**
   * Setter.
   * 
   * @param breakPoint Sets the maximal width of a linear ordering. Additional
   *          items are then placed in the next line.
   */
  public void setBreakPoint(final double breakPoint) {
    this.breakPoint = breakPoint;
  }

  /**
   * Getter.
   * 
   * @return The point where to break lines. This is the maximal width of a
   *         linear ordering.
   */
  public double getBreakPoint() {
    return breakPoint;
  }

}
