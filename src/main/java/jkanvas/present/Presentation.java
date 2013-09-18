package jkanvas.present;

import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;
import jkanvas.animation.AnimationTiming;
import jkanvas.groups.LinearGroup;

/**
 * A presentation containing slides.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Presentation extends LinearGroup<Slide> {

  /** The metrics for the presentation. */
  private final SlideMetrics metrics;
  /** The canvas. */
  private final Canvas canvas;
  /** The animation timing. */
  private final AnimationTiming timing;

  /**
   * Creates a presentation.
   * 
   * @param canvas The canvas.
   * @param metrics The slide metrics.
   * @param timing The animation timing.
   */
  public Presentation(final Canvas canvas, final SlideMetrics metrics,
      final AnimationTiming timing) {
    super(canvas.getAnimator(), true, metrics.slideSpaceHor(),
        AnimationTiming.NO_ANIMATION);
    this.canvas = canvas;
    this.metrics = metrics;
    this.timing = timing;
  }

  @Override
  protected void processMessage(final String msg) {
    super.processMessage(msg);
    switch(msg) {
      case "slide:next":
        nextSlide();
        break;
      case "slide:prev":
        prevSlide();
        break;
      case "present:true":
        setPresentationMode(true);
        break;
      case "present:false":
        setPresentationMode(false);
        break;
      case "present:toggle":
        setPresentationMode(!inPresentationMode());
        break;
    }
  }

  private boolean inPresentation = false;

  public void setPresentationMode(final boolean inPresentation) {
    this.inPresentation = inPresentation;
    if(inPresentation) {
      setSlide(curSlide);
    } else {
      canvas.setRestriction(null, AnimationTiming.NO_ANIMATION);
    }
  }

  public boolean inPresentationMode() {
    return inPresentation;
  }

  private int curSlide = 0;

  public int currentSlide() {
    return curSlide;
  }

  public void nextSlide() {
    setSlide(curSlide + 1);
  }

  public void prevSlide() {
    setSlide(curSlide - 1);
  }

  public void setSlide(final int no) {
    curSlide = Math.min(Math.max(0, no), renderpassCount() - 1);
    if(!inPresentationMode()) return;
    final Slide slide = getRenderpass(curSlide);
    final Rectangle2D box = slide.getBoundingBox();
    canvas.setRestriction(box, timing);
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
