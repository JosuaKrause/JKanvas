package jkanvas.present;

import java.awt.geom.Rectangle2D;

import jkanvas.Canvas;
import jkanvas.animation.AnimationTiming;
import jkanvas.io.json.JSONElement;
import jkanvas.io.json.JSONKeyBindings;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.groups.LinearGroup;

/**
 * A presentation containing slides.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Presentation extends LinearGroup<Slide> {

  /** The canvas. */
  private final Canvas canvas;
  /** The animation timing. */
  private final AnimationTiming timing;

  /**
   * Creates a presentation.
   * 
   * @param canvas The canvas.
   * @param hSpace The horizontal space between slides.
   * @param timing The animation timing.
   */
  public Presentation(final Canvas canvas, final double hSpace,
      final AnimationTiming timing) {
    super(canvas.getAnimator(), true, hSpace, AnimationTiming.NO_ANIMATION);
    this.canvas = canvas;
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
      canvas.setRestriction(null, AnimationTiming.NO_ANIMATION, null);
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
    final Rectangle2D box = new Rectangle2D.Double();
    RenderpassPainter.getTopLevelBounds(box, slide);
    canvas.setRestriction(box, timing, null);
  }

  /**
   * Creates a presentation from the given JSON element.
   * 
   * @param c The canvas.
   * @param info An optional info HUD. May be <code>null</code>.
   * @param json The JSON element.
   * @param base The default slide metrics.
   * @return The presentation.
   */
  public static final Presentation fromJSON(final Canvas c, final SimpleTextHUD info,
      final JSONElement json, final SlideMetrics base) {
    json.expectObject();
    final SlideMetrics metric;
    if(json.hasValue("metric")) {
      metric = SlideMetrics.loadFromJSON(json.getValue("metric"), base);
    } else {
      metric = base;
    }
    final AnimationTiming timing;
    if(json.hasValue("timing")) {
      timing = AnimationTiming.loadFromJSON(json.getValue("timing"));
    } else {
      timing = AnimationTiming.NO_ANIMATION;
    }
    final Presentation res = new Presentation(c, metric.slideSpaceHor(), timing);
    if(json.hasValue("id")) {
      res.setIds(json.getString("id", null));
    }
    JSONKeyBindings.load(json, c, info);
    if(json.hasValue("slides")) {
      final JSONElement slides = json.getValue("slides");
      slides.expectArray();
      for(final JSONElement slide : slides) {
        final Slide s = new Slide(metric);
        s.parseJSON(slide);
        res.addRenderpass(s);
      }
    }
    return res;
  }

}
