package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;

/**
 * A thin wrapper around a render pass. Thin wrapper do ignore each other
 * allowing to stack them on top of each other.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The innermost wrapped type.
 */
public abstract class ThinWrapperRenderpass<T extends Renderpass> extends Renderpass {

  /** The render pass in a list for easier handling. */
  private final List<Renderpass> list;
  /**
   * The decorated render pass or <code>null</code> if another wrapper was
   * handed in.
   */
  private final T pass;
  /** Another wrapper if render pass is <code>null</code>. */
  private final ThinWrapperRenderpass<T> wrapper;

  /**
   * Creates a thin wrapper around the given render pass. You may set the offset
   * of the render pass via {@link #setWrapOffset(double, double)} after the
   * initialization.
   * 
   * @param wrap The render pass to wrap.
   */
  public ThinWrapperRenderpass(final T wrap) {
    if(wrap instanceof ThinWrapperRenderpass) {
      wrapper = (ThinWrapperRenderpass<T>) Objects.requireNonNull(wrap);
      pass = null;
    } else {
      pass = Objects.requireNonNull(wrap);
      wrapper = null;
    }
    list = Collections.singletonList((Renderpass) wrap);
    list.get(0).setParent(this);
  }

  /**
   * Creates a thin wrapper around the given render pass. You may set the offset
   * of the render pass via {@link #setWrapOffset(double, double)} after the
   * initialization.
   * 
   * @param wrap The wrapper to wrap.
   */
  public ThinWrapperRenderpass(final ThinWrapperRenderpass<T> wrap) {
    wrapper = Objects.requireNonNull(wrap);
    wrapper.setParent(this);
    list = Collections.singletonList((Renderpass) wrapper);
    pass = null;
  }

  /**
   * Getter.
   * 
   * @return Gets the innermost wrapped render pass.
   */
  public T getWrapRenderpass() {
    ThinWrapperRenderpass<T> p = this;
    while(p.wrapper != null) {
      p = p.wrapper;
    }
    return p.pass;
  }

  /**
   * Sets the offset of the wrapped render pass.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  protected final void setWrapOffset(final double x, final double y) {
    final Renderpass pass = list.get(0);
    pass.setOffset(x, y);
  }

  /**
   * Computes the inner bounding box ignoring all other wrap render passes.
   * 
   * @param bbox The bounding box in which can be drawn.
   */
  protected final void getInnerBoundingBox(final Rectangle2D bbox) {
    double x = 0;
    double y = 0;
    ThinWrapperRenderpass<T> t = this;
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
  protected abstract void addOwnBox(Rectangle2D bbox);

  @Override
  public final void draw(final Graphics2D g, final KanvasContext ctx) {
    RenderpassPainter.draw(list, g, ctx);
    drawOwn(g, ctx);
  }

  /**
   * Draws the wrapper.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   */
  protected abstract void drawOwn(Graphics2D g, KanvasContext ctx);

  @Override
  public final void getBoundingBox(final Rectangle2D bbox) {
    final Renderpass pass = list.get(0);
    pass.getBoundingBox(bbox);
    addOwnBox(bbox);
  }

  @Override
  public final boolean click(final Camera cam, final Point2D position, final MouseEvent e) {
    return RenderpassPainter.click(list, cam, position, e);
  }

  @Override
  public final boolean doubleClick(final Camera cam, final Point2D position,
      final MouseEvent e) {
    if(RenderpassPainter.doubleClick(list, cam, position, e)) return true;
    if(!USE_DOUBLE_CLICK_DEFAULT) return false;
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    cam.toView(this, AnimationTiming.SMOOTH, null, true);
    return true;
  }

  @Override
  public final String getTooltip(final Point2D position) {
    return RenderpassPainter.getTooltip(list, position);
  }

  @Override
  public final boolean moveMouse(final Point2D cur) {
    return RenderpassPainter.moveMouse(list, cur);
  }

  /** The start position of the drag in the render pass coordinates. */
  private Point2D start = null;

  @Override
  public final boolean acceptDrag(final Point2D position, final MouseEvent e) {
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
  public final void drag(final Point2D _, final Point2D cur,
      final double dx, final double dy) {
    // dx and dy do not change
    final Renderpass pass = list.get(0);
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, cur);
    pass.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _, final Point2D end,
      final double dx, final double dy) {
    // dx and dy do not change
    final Renderpass pass = list.get(0);
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, end);
    pass.endDrag(start, pos, dx, dy);
  }

  @Override
  public final boolean isChanging() {
    final Renderpass pass = list.get(0);
    return pass.isChanging();
  }

  @Override
  public final void processMessage(final String[] ids, final String msg) {
    super.processMessage(ids, msg);
    final Renderpass pass = list.get(0);
    pass.processMessage(ids, msg);
  }

}
