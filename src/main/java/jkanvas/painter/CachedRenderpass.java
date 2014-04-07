package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import jkanvas.KanvasContext;
import jkanvas.util.PaintUtil;

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
public abstract class CachedRenderpass extends Renderpass {

  /** The visible size at which the caching comes into effect. */
  public static int CACHE_VISIBLE = 256;

  /** The orientation for the size of the cache. */
  public static int CACHE_SIZE = 512;

  /** The cached image. */
  private BufferedImage cache;

  /** The scaling of the image. */
  private double scale;

  /** Whether the render pass has changed during the last draw. */
  private boolean lastChanging;

  /**
   * This method is called before any drawing of the cached render pass happens.
   */
  public void beforeDraw() {
    // nothing to do
  }

  @Override
  public final void draw(final Graphics2D g, final KanvasContext ctx) {
    beforeDraw();
    if(!hasCache() || jkanvas.Canvas.DISABLE_CACHING) {
      invalidateCache();
      doDraw(g, ctx);
      return;
    }
    final Rectangle2D bbox = new Rectangle2D.Double();
    getBoundingBox(bbox);
    final boolean chg = isChanging();
    final boolean noCache = chg || lastChanging;
    lastChanging = chg;
    final Rectangle2D comp = ctx.toComponentCoordinates(bbox);
    if(noCache) {
      invalidateCache();
    }
    final boolean drawSelf = noCache ||
        (comp.getWidth() >= CACHE_VISIBLE && comp.getHeight() >= CACHE_VISIBLE);
    if(!isForceCaching() && drawSelf) {
      doDraw(g, ctx);
      return;
    }
    createCache(bbox);
    g.translate(bbox.getX(), bbox.getY());
    final AffineTransform sc = AffineTransform.getScaleInstance(scale, scale);
    // FIXME experimental
    final AffineTransformOp op = new AffineTransformOp(sc,
        AffineTransformOp.TYPE_BILINEAR);
    g.drawImage(cache, op, 0, 0);
    if(jkanvas.Canvas.DEBUG_CACHE) {
      jkanvas.util.PaintUtil.setAlpha(g, 0.3);
      g.setColor(java.awt.Color.MAGENTA);
      // we do not use a shape because we want to be as precise as the cache
      g.transform(sc);
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
    g.scale(s, s);
    cc.doScale(s);
    g.translate(-bbox.getX(), -bbox.getY());
    cc.doTranslate(-bbox.getX(), -bbox.getY());
    g.clip(bbox);
    doDraw(g, cc);
    g.dispose();
    cache = img;
    scale = 1 / s;
  }

  /**
   * Performs the actual drawing.
   * 
   * @param r The render pass.
   * @param gfx The graphics context.
   * @param ctx The context.
   */
  public static final void doDraw(
      final CachedRenderpass r, final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleCanvas();
    final Rectangle2D bbox = new Rectangle2D.Double();
    if(!r.isVisible()) return;
    RenderpassPainter.getPassBoundingBox(bbox, r);
    if(!view.intersects(bbox)) return;
    final Graphics2D g = (Graphics2D) gfx.create();
    g.clip(bbox);
    final double dx = r.getOffsetX();
    final double dy = r.getOffsetY();
    g.translate(dx, dy);
    final KanvasContext c = RenderpassPainter.getContextFor(r, ctx);
    r.doDraw(g, c);
    g.dispose();
    if(jkanvas.Canvas.DEBUG_BBOX) {
      final Graphics2D g2 = (Graphics2D) gfx.create();
      PaintUtil.setAlpha(g2, 0.3);
      g2.fill(bbox);
      g2.dispose();
    }
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

  /**
   * Getter.
   * 
   * @return Whether this render pass uses caching.
   */
  protected boolean hasCache() {
    return true;
  }

  /** Invalidates the cache. */
  protected void invalidate() {
    invalidateCache();
  }

  /** Invalidates only the cache. */
  protected final void invalidateCache() {
    if(cache == null) return;
    cache.flush();
    cache = null;
  }

}
