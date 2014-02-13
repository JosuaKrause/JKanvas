package jkanvas.painter;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import jkanvas.AbstractKanvasContext;
import jkanvas.KanvasContext;

/**
 * The {@link KanvasContext} that is used during cache creation.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class CacheContext extends AbstractKanvasContext {

  /** The transformation to canvas coordinates. */
  private final AffineTransform toCanvas;
  /** The transformation to component coordinates. */
  private final AffineTransform toComponent;
  /** The bounding box of the render pass. */
  private final Rectangle2D bbox;

  /**
   * Creates the cache context with the identity as transformation.
   * 
   * @param bbox The bounding box.
   */
  public CacheContext(final Rectangle2D bbox) {
    this(0, 0, new AffineTransform(), new AffineTransform(), bbox);
  }

  /**
   * Copies a cache context.
   * 
   * @param offX The x offset.
   * @param offY The y offset.
   * @param toCanvas The transformation to canvas coordinates.
   * @param toComponent The transformation to component coordinates.
   * @param bbox The bounding box.
   */
  private CacheContext(final double offX, final double offY,
      final AffineTransform toCanvas, final AffineTransform toComponent,
      final Rectangle2D bbox) {
    super(true, offX, offY);
    this.toCanvas = toCanvas;
    this.toComponent = toComponent;
    this.bbox = bbox;
  }

  /**
   * Directly alters the scaling.
   * 
   * @param s Scales the transformation.
   */
  protected void doScale(final double s) {
    toComponent.scale(s, s);
    toCanvas.scale(1 / s, 1 / s);
  }

  /**
   * Directly alters the translation.
   * 
   * @param x Translates the transformation in x direction.
   * @param y Translates the transformation in y direction.
   */
  protected void doTranslate(final double x, final double y) {
    toComponent.translate(x, y);
    toCanvas.translate(-x, -y);
  }

  @Override
  protected KanvasContext create(final boolean inCanvasSpace,
      final double offX, final double offY) {
    if(!inCanvasSpace) throw new IllegalStateException();
    return new CacheContext(offX, offY, toCanvas, toComponent, bbox);
  }

  @Override
  protected Rectangle2D createVisibleComponent() {
    return toComponentCoordinates(bbox);
  }

  @Override
  protected void transform(final AffineTransform at) {
    at.concatenate(toComponent);
  }

  @Override
  protected void transformBack(final AffineTransform at) {
    at.concatenate(toCanvas);
  }

}
