package jkanvas.painter;

import java.awt.geom.Rectangle2D;

import jkanvas.KanvasInteraction;
import jkanvas.animation.Animated;

/**
 * Render passes can be used to dynamically change what is rendered on a canvas
 * and define an order.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface Renderpass extends KanvasInteraction {

  /**
   * Getter.
   * 
   * @return Whether this pass is currently visible.
   */
  boolean isVisible();

  /**
   * Getter.
   * 
   * @return The x offset of this pass in canvas coordinates.
   */
  double getOffsetX();

  /**
   * Getter.
   * 
   * @return The y offset of this pass in canvas coordinates.
   */
  double getOffsetY();

  /**
   * Getter.
   * 
   * @return An optional bounding box in canvas coordinates. This method does
   *         <em>not</em> have to account for the offset.
   */
  @Override
  Rectangle2D getBoundingBox();

  /**
   * Returns the animated object associated with this render pass. A render pass
   * is allowed to have up to one animated object associated. Return a
   * {@link jkanvas.animation.GroupAnimator} for more animated objects. The
   * associated object automatically gets registered and unregistered when this
   * render pass is added or removed to a
   * {@link jkanvas.animation.AnimatedPainter}. When this method returns an
   * object this object must always be the same over the life-time of the render
   * pass.
   * 
   * @return An animated object or <code>null</code>.
   */
  Animated getAnimated();

}
