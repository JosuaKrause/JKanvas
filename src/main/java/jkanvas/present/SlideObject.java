package jkanvas.present;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

/**
 * An object that can be added to a slide. A {@link SlideObject} can be added to
 * a {@link Slide} once and has undefined behavior otherwise.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class SlideObject {

  /** The slide that owns the object. */
  private final Slide slide;
  /** The vertical alignment. */
  private final VerticalSlideAlignment vAlign;
  /** The horizontal alignment behavior of the text. */
  private HorizontalSlideAlignment hAlign;

  /**
   * Creates a slide object.
   * 
   * @param slide The slide that owns the object.
   * @param hAlign The initial horizontal alignment of the object.
   * @param vAlign The vertical alignment of the object.
   */
  public SlideObject(final Slide slide, final HorizontalSlideAlignment hAlign,
      final VerticalSlideAlignment vAlign) {
    this.slide = Objects.requireNonNull(slide);
    this.vAlign = Objects.requireNonNull(vAlign);
    this.hAlign = Objects.requireNonNull(hAlign);
    slide.add(this);
  }

  /**
   * Getter.
   * 
   * @return The slide that owns the object.
   */
  protected Slide getSlide() {
    return slide;
  }

  /**
   * Computes the offset of the object with the given metric.
   * 
   * @param metric The metric.
   * @return The offset of the object.
   */
  public Point2D getOffset(final SlideMetrics metric) {
    final VerticalSlideAlignment v = getVerticalAlignment();
    final double h = getSlide().getTotalHeight(v);
    return metric.getOffsetFor(getIndent(), getWidth(),
        getHorizontalAlignment(), getTop(), getHeight(), h, v);
  }

  /**
   * Getter.
   * 
   * @return The top offset.
   * @throws IllegalStateException When the object has not been initialized yet.
   */
  public abstract double getTop() throws IllegalStateException;

  /**
   * Getter.
   * 
   * @return The height of the object.
   * @throws IllegalStateException When the object has not been initialized yet.
   */
  public abstract double getHeight() throws IllegalStateException;

  /**
   * Getter.
   * 
   * @return The width of the object.
   * @throws IllegalStateException When the object has not been initialized yet.
   */
  public abstract double getWidth() throws IllegalStateException;

  /**
   * Getter.
   * 
   * @return The indentation of the object.
   */
  public abstract int getIndent();

  /**
   * Getter.
   * 
   * @return The horizontal alignment.
   */
  public HorizontalSlideAlignment getHorizontalAlignment() {
    return hAlign;
  }

  /**
   * Setter.
   * 
   * @param hAlign Sets the horizontal alignment.
   */
  public void setHorizontalAlignment(final HorizontalSlideAlignment hAlign) {
    this.hAlign = Objects.requireNonNull(hAlign);
  }

  /**
   * Getter.
   * 
   * @return The vertical alignment.
   */
  public VerticalSlideAlignment getVerticalAlignment() {
    return vAlign;
  }

  /**
   * Getter.
   * 
   * @param bbox The rectangle in which the bounding box of the object without
   *          the offset is stored.
   * @throws IllegalStateException When the object has not been drawn yet.
   */
  public void getBoundingBox(final Rectangle2D bbox) throws IllegalStateException {
    bbox.setFrame(0, 0, getWidth(), getHeight());
  }

  /**
   * Draws the object.
   * 
   * @param g The graphics context which is already at the right position.
   * @param ctx The canvas context.
   */
  public abstract void draw(Graphics2D g, KanvasContext ctx);

  /**
   * This method is called as first step before the object is drawn. It can be
   * used to compute geometry that relies on graphic contexts.
   * 
   * @param gfx The graphic context. This context is not yet positioned
   *          correctly and may not be changed.
   * @param metric The current metrics.
   */
  public abstract void beforeDraw(Graphics2D gfx, SlideMetrics metric);

}
