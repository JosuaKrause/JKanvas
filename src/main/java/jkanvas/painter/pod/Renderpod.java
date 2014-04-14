package jkanvas.painter.pod;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimationList;
import jkanvas.painter.CachedRenderpass;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

/**
 * A thin wrapper around a render pass. Thin wrapper do ignore each other
 * allowing to stack them on top of each other.
 *
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The innermost wrapped type.
 */
public abstract class Renderpod<T extends Renderpass> extends CachedRenderpass {

  /** The render pass in a list for easier handling. */
  private final List<Renderpass> list;
  /**
   * The decorated render pass or <code>null</code> if another wrapper was
   * handed in.
   */
  private final T pass;
  /** The cached render pass responsible for drawing. */
  private final CachedRenderpass cache;
  /** Another wrapper if render pass is <code>null</code>. */
  private final Renderpod<T> wrapper;
  /** Whether this pod is active. */
  private boolean active = true;

  /**
   * Creates a thin wrapper around the given render pass. You may set the offset
   * of the render pass via {@link #setChildOffset(double, double)} after the
   * initialization.
   *
   * @param wrap The render pass to wrap.
   */
  @SuppressWarnings("unchecked")
  public Renderpod(final T wrap) {
    if(wrap instanceof Renderpod) {
      wrapper = (Renderpod<T>) Objects.requireNonNull(wrap);
      cache = wrapper.hasCache() ? wrapper : null;
      pass = null;
    } else {
      pass = Objects.requireNonNull(wrap);
      cache = wrap instanceof CachedRenderpass ? (CachedRenderpass) wrap : null;
      wrapper = null;
    }
    list = Collections.singletonList((Renderpass) wrap);
    list.get(0).setParent(this);
  }

  /**
   * Creates a thin wrapper around the given render pass. You may set the offset
   * of the render pass via {@link #setChildOffset(double, double)} after the
   * initialization.
   *
   * @param wrap The wrapper to wrap.
   */
  public Renderpod(final Renderpod<T> wrap) {
    wrapper = Objects.requireNonNull(wrap);
    wrapper.setParent(this);
    cache = wrap.hasCache() ? wrap : null;
    list = Collections.singletonList((Renderpass) wrapper);
    pass = null;
  }

  /**
   * Getter.
   *
   * @return Whether this pod is active.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Setter.
   *
   * @param active Whether this pod is active.
   */
  public void setActive(final boolean active) {
    this.active = active;
    invalidateCache();
  }

  @Override
  public boolean hasCache() {
    return cache != null;
  }

  /**
   * Getter.
   *
   * @return Gets the innermost wrapped render pass.
   */
  public T unwrap() {
    Renderpod<T> p = this;
    while(p.wrapper != null) {
      p = p.wrapper;
    }
    return p.pass;
  }

  /**
   * Getter.
   *
   * @return The x offset of the inner render pass in relation to the parent of
   *         this render pod.
   */
  public double unwrapOffsetX() {
    double x = 0;
    Renderpod<T> p = this;
    while(p.wrapper != null) {
      p = p.wrapper;
      x += p.getOffsetX();
    }
    final Renderpass rp = p.list.get(0);
    final Renderpass pass = list.get(0);
    x += rp.getOffsetX() - pass.getOffsetX();
    return x + getOffsetX();
  }

  /**
   * Getter.
   *
   * @return The y offset of the inner render pass in relation to the parent of
   *         this render pod.
   */
  public double unwrapOffsetY() {
    double y = 0;
    Renderpod<T> p = this;
    while(p.wrapper != null) {
      p = p.wrapper;
      y += p.getOffsetY();
    }
    final Renderpass rp = p.list.get(0);
    final Renderpass pass = list.get(0);
    y += rp.getOffsetY() - pass.getOffsetY();
    return y + getOffsetY();
  }

  /**
   * Computes the bounding box of the inner render pass in relation to the
   * parent of this render pod.
   *
   * @param bbox The bounding box to store the result in.
   */
  public void unwrapBoundingBox(final RectangularShape bbox) {
    unwrap().getBoundingBox(bbox);
  }

  /**
   * Getter.
   *
   * @return The parent pod or <code>null</code> if this is the final pod.
   */
  public Renderpod<T> getChildPod() {
    return wrapper;
  }

  /**
   * Sets the offset of the wrapped render pass.
   *
   * @param x The x offset.
   * @param y The y offset.
   */
  protected final void setChildOffset(final double x, final double y) {
    final Renderpass pass = list.get(0);
    pass.setOffset(x, y);
  }

  /**
   * Computes the inner bounding box ignoring all other wrap render passes.
   *
   * @param bbox The bounding box in which can be drawn.
   */
  public final void getInnerBoundingBox(final Rectangle2D bbox) {
    double x = 0;
    double y = 0;
    Renderpod<T> t = this;
    while(t.pass == null) {
      t = t.wrapper;
      x += t.getOffsetX();
      y += t.getOffsetY();
    }
    final Renderpass p = t.list.get(0);
    p.getBoundingBox(bbox);
    final Renderpass pass = list.get(0);
    x += p.getOffsetX() - pass.getOffsetX();
    y += p.getOffsetY() - pass.getOffsetY();
    bbox.setFrame(bbox.getX() + x, bbox.getY() + y, bbox.getWidth(), bbox.getHeight());
  }

  /**
   * Adds the own bounds to the given bounding box.
   *
   * @param bbox The bounding box of the wrapped render passed.
   */
  protected abstract void addOwnBox(RectangularShape bbox);

  @Override
  public void beforeDraw() {
    if(cache != null) {
      cache.beforeDraw();
    }
  }

  @Override
  protected void doDraw(final Graphics2D g, final KanvasContext ctx) {
    if(isActive()) {
      drawOwn(g, ctx);
    }
    if(cache != null) {
      CachedRenderpass.doDraw(cache, g, ctx);
    } else {
      RenderpassPainter.draw(list, g, ctx);
    }
  }

  /**
   * Draws the wrapper.
   *
   * @param g The graphics context.
   * @param ctx The canvas context.
   */
  protected abstract void drawOwn(Graphics2D g, KanvasContext ctx);

  @Override
  public final void getBoundingBox(final RectangularShape bbox) {
    final Renderpass pass = list.get(0);
    pass.getBoundingBox(bbox);
    addOwnBox(bbox);
  }

  @Override
  public boolean click(final Camera cam, final Point2D position, final MouseEvent e) {
    return RenderpassPainter.click(list, cam, position, e);
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D position, final MouseEvent e) {
    if(RenderpassPainter.doubleClick(list, cam, position, e)) return true;
    if(USE_DOUBLE_CLICK_DEFAULT) return defaultDoubleClick(this, cam, e);
    return false;
  }

  @Override
  public String getTooltip(final Point2D position) {
    return RenderpassPainter.getTooltip(list, position);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    return RenderpassPainter.moveMouse(list, cur);
  }

  /** The start position of the drag in the render pass coordinates. */
  private Point2D start = null;

  @Override
  public boolean acceptDrag(final Point2D position, final MouseEvent e) {
    final Renderpass pass = list.get(0);
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, position);
    final Rectangle2D bbox = new Rectangle2D.Double();
    pass.getBoundingBox(bbox);
    if(!bbox.contains(pos)) return false;
    if(!pass.acceptDrag(pos, e)) return false;
    start = pos;
    return true;
  }

  @Override
  public void drag(final Point2D _start, final Point2D cur,
      final double dx, final double dy) {
    // dx and dy do not change
    final Renderpass pass = list.get(0);
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, cur);
    pass.drag(start, pos, dx, dy);
  }

  @Override
  public void endDrag(final Point2D _start, final Point2D end,
      final double dx, final double dy) {
    // dx and dy do not change
    final Renderpass pass = list.get(0);
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, end);
    pass.endDrag(start, pos, dx, dy);
  }

  @Override
  public boolean isChanging() {
    final Renderpass pass = list.get(0);
    return pass.isChanging();
  }

  @Override
  public void processMessage(final String[] ids, final String msg) {
    super.processMessage(ids, msg);
    RenderpassPainter.processMessage(list, ids, msg);
  }

  @Override
  public void setAnimationList(final AnimationList al) {
    super.setAnimationList(al);
    list.get(0).setAnimationList(al);
  }

}
