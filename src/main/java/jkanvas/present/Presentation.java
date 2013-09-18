package jkanvas.present;

import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.groups.LinearGroup;

/**
 * A presentation containing slides.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Presentation extends LinearGroup<Slide> {

  /** The metrics for the presentation. */
  private final SlideMetrics metrics;

  /**
   * Creates a presentation.
   * 
   * @param animator The animator.
   * @param metrics The slide metrics.
   */
  public Presentation(final Animator animator, final SlideMetrics metrics) {
    super(animator, true, metrics.slideSpaceHor(), AnimationTiming.NO_ANIMATION);
    this.metrics = metrics;
  }

  @Override
  protected void beforeAdding(final Slide slide) {
    super.beforeAdding(slide);
    slide.setMetrics(metrics);
  }

  @Override
  protected void removedRenderpass(final RenderpassPosition<Slide> rp) {
    super.removedRenderpass(rp);
    rp.pass.setMetrics(null);
  }

}
