package jkanvas.present;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

/**
 * An object that can be added to a slide. A {@link SlideObject} can be added to
 * a {@link Slide} once and has undefined behavior otherwise.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface SlideObject {

  /**
   * Configures the object for the given slide. This method may be called
   * multiple times and further calls must not change the state when the object
   * is already configured. The configuration must not depend on the given
   * metric which can change over time.
   * 
   * @param slide The slide.
   * @param metric The metric.
   */
  void configure(Slide slide, SlideMetrics metric);

  /**
   * Computes the offset of the object with the given metric.
   * 
   * @param metric The metric.
   * @return The offset of the object.
   */
  Point2D getOffset(SlideMetrics metric);

  /**
   * Getter.
   * 
   * @return The bounding box of the object without the offset.
   */
  Rectangle2D getBoundingBox();

  /**
   * Draws the object.
   * 
   * @param g The graphics context which is already at the right position.
   * @param ctx The canvas context.
   */
  void draw(Graphics2D g, KanvasContext ctx);

  /**
   * This method is called as first step before the object is drawn. It can be
   * used to compute geometry that relies on graphic contexts.
   * 
   * @param gfx The graphic context. This context is not yet positioned
   *          correctly and may not be changed.
   */
  void beforeDraw(Graphics2D gfx);

}
