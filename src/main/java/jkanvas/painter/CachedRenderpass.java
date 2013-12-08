package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import jkanvas.AbstractKanvasContext;
import jkanvas.KanvasContext;

/**
 * Caches the current render pass as long as it is small in component
 * coordinates and does not change its content. Note that letting a render pass
 * extends this class only makes sense when {@link #isChanging()} not always
 * returns <code>true</code>. The render pass can also not work with the current
 * zoom level as it may be altered to create the cache. However visibility
 * checks against the visible canvas do work, even though the visible canvas is
 * assumed to be the cached render pass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class CachedRenderpass extends AbstractRenderpass {

  /** The visible size at which the caching comes into effect. */
  public static int CACHE_VISIBLE = 256;

  /** The orientation for the size of the cache. */
  public static int CACHE_SIZE = 512;

  /** The cached image. */
  private Image cache;

  /** The scaling of the image. */
  private double scale;

  /** Whether the render pass has changed during the last draw. */
  private boolean lastChanging;

  @Override
  public final void draw(final Graphics2D g, final KanvasContext ctx) {
    final Rectangle2D bbox = getBoundingBox();
    final boolean chg = isChanging();
    final boolean noCache = chg || lastChanging;
    lastChanging = chg;
    final Rectangle2D comp = ctx.toComponentCoordinates(bbox);
    final boolean drawSelf = noCache ||
        (comp.getWidth() >= CACHE_VISIBLE && comp.getHeight() >= CACHE_VISIBLE);
    if((!isForceCaching() && drawSelf) || jkanvas.Canvas.DISABLE_CACHING) {
      invalidateCache();
      doDraw(g, ctx);
      return;
    }
    createCache(bbox);
    g.scale(scale, scale);
    g.translate(bbox.getX(), bbox.getY());
    g.drawImage(cache, 0, 0, null);
    if(jkanvas.Canvas.DEBUG_CACHE) {
      jkanvas.util.PaintUtil.setAlpha(g, 0.3);
      g.setColor(java.awt.Color.MAGENTA);
      // we do not use a shape because we want to be as precise as the cache
      g.fillRect(0, 0, cache.getWidth(null), cache.getHeight(null));
    }
    return;
  }

  /**
   * Creates the cached image if it is not present.
   * 
   * @param bbox The bounding box of the render pass.
   */
  private void createCache(final Rectangle2D bbox) {
    if(cache != null) return;
    final double s = CACHE_SIZE / Math.max(bbox.getWidth(), bbox.getHeight());
    final double w = bbox.getWidth() * s;
    final double h = bbox.getHeight() * s;
    final BufferedImage img = new BufferedImage((int) Math.ceil(w), (int) Math.ceil(h),
        BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    final CacheContext cc = new CacheContext(bbox);
    g.translate(-bbox.getX(), -bbox.getY());
    cc.doTranslate(-bbox.getX(), -bbox.getY());
    g.scale(s, s);
    cc.doScale(s);
    g.clip(bbox);
    doDraw(g, cc);
    g.dispose();
    cache = img;
    scale = 1 / s;
  }

  /**
   * Draws the component.
   * 
   * @param g The graphics context.
   * @param ctx The kanvas context.
   * @see #draw(Graphics2D, KanvasContext)
   */
  protected abstract void doDraw(Graphics2D g, KanvasContext ctx);

  @Override
  public abstract boolean isChanging();

  /** Invalidates the cache. */
  protected void invalidate() {
    invalidateCache();
  }

  /** Invalidates only the cache. */
  protected final void invalidateCache() {
    if(cache != null) {
      cache.flush();
      cache = null;
    }
  }

  /**
   * The {@link KanvasContext} that is used during cache creation.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class CacheContext extends AbstractKanvasContext {

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

  } // CacheContext

}
