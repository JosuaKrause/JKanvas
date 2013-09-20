package jkanvas.present;

import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;
import jkanvas.animation.AnimationTiming;
import jkanvas.groups.LinearGroup;
import jkanvas.painter.RenderpassPainter;

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

  /** Whether the presentation mode is active. */
  private boolean inPresentation = false;

  /**
   * Setter.
   * 
   * @param inPresentation Whether to activate the presentation mode.
   */
  public void setPresentationMode(final boolean inPresentation) {
    this.inPresentation = inPresentation;
    if(inPresentation) {
      if(curSlide < 0) {
        setSlide(curSlide, AnimationTiming.NO_ANIMATION);
      } else {
        setSlide(curSlide);
      }
    } else {
      canvas.setRestriction(null, AnimationTiming.NO_ANIMATION);
    }
  }

  /**
   * Getter.
   * 
   * @return Whether the presentation mode is active.
   */
  public boolean inPresentationMode() {
    return inPresentation;
  }

  /** The current slide. */
  private int curSlide = -1;

  /**
   * Getter.
   * 
   * @return The current slide.
   */
  public int currentSlide() {
    return Math.max(curSlide, 0);
  }

  /** Advances to the next slide. */
  public void nextSlide() {
    setSlide(Math.max(curSlide + 1, 1));
  }

  /** Advances to the previous slide. */
  public void prevSlide() {
    setSlide(curSlide - 1);
  }

  /**
   * Setter.
   * 
   * @param no Directly sets the slide.
   */
  public void setSlide(final int no) {
    setSlide(no, timing);
  }

  /**
   * Setter.
   * 
   * @param no Directly sets the slide.
   * @param timing The animation timing.
   */
  private void setSlide(final int no, final AnimationTiming timing) {
    curSlide = Math.min(Math.max(0, no), renderpassCount() - 1);
    if(!inPresentationMode()) return;
    final Slide slide = getRenderpass(curSlide);
    final Rectangle2D box = RenderpassPainter.getTopLevelBounds(slide);
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
