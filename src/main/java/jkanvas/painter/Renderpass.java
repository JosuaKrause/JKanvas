package jkanvas.painter;

import java.awt.geom.Rectangle2D;

import jkanvas.KanvasInteraction;
import jkanvas.animation.AnimationList;

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
   * Setter.
   * 
   * @param list Sets the animation list so that the render pass can add and
   *          remove animated objects. If the render pass has nothing to add to
   *          the list this method can be ignored.
   */
  void setAnimationList(AnimationList list);

  /**
   * Getter.
   * 
   * @return The parent of this render pass. The parent is used to calculate
   *         correct top level canvas positions when render passes are combined
   *         in groups.
   * @see jkanvas.painter.RenderpassPainter#getTopLevelBounds(Renderpass)
   * @see jkanvas.painter.RenderpassPainter#getTopLevelBounds(Renderpass,
   *      Rectangle2D)
   */
  Renderpass getParent();

  /**
   * Getter.
   * 
   * @return Whether the render pass may be altered until the next call of
   *         {@link #draw(java.awt.Graphics2D, jkanvas.KanvasContext)}.
   */
  boolean isChanging();

  /**
   * Setter.
   * 
   * @param forceCache Force caching. This can be useful when the render pass is
   *          moving and caching is supported.
   */
  public void setForceCache(final boolean forceCache);

  /**
   * Getter.
   * 
   * @return Whether to force caching when caching is supported.
   */
  public boolean isForceCaching();

  /** Disposes this render pass. */
  void dispose();

}
