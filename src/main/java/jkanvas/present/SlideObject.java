package jkanvas.present;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;

/**
 * An object that can be added to a slide. A {@link SlideObject} can be added to
 * a {@link Slide} once and has undefined behavior otherwise.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class SlideObject {

  /** The slide that owns the object. */
  private final Slide slide;

  /**
   * Creates a slide object.
   * 
   * @param slide The slide that owns the object.
   */
  public SlideObject(final Slide slide) {
    this.slide = Objects.requireNonNull(slide);
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
  public abstract Point2D getOffset(SlideMetrics metric);

  /**
   * Getter.
   * 
   * @return The bounding box of the object without the offset.
   * @throws IllegalStateException When the object has not been drawn yet.
   */
  public abstract Rectangle2D getBoundingBox() throws IllegalStateException;

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
