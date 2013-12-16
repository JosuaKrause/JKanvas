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
 */
public abstract class ThinWrapperRenderpass extends Renderpass {

  /** The render pass in a list for easier handling. */
  private final List<Renderpass> list;
  /** The decorated render pass. */
  private final Renderpass pass;

  /**
   * Creates a thin wrapper around the given render pass. You may set the offset
   * of the render pass via {@link #setWrapOffset(double, double)} after the
   * initialization.
   * 
   * @param wrap The render pass to wrap.
   */
  public ThinWrapperRenderpass(final Renderpass wrap) {
    pass = Objects.requireNonNull(wrap);
    list = Collections.singletonList(pass);
    pass.setParent(this);
  }

  /**
   * Sets the offset of the wrapped render pass.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  protected final void setWrapOffset(final double x, final double y) {
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
    Renderpass p = pass;
    while(p instanceof ThinWrapperRenderpass) {
      final ThinWrapperRenderpass t = (ThinWrapperRenderpass) p;
      x += t.getOffsetX();
      y += t.getOffsetY();
      p = t.pass;
    }
    p.getBoundingBox(bbox);
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
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, cur);
    pass.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _, final Point2D end,
      final double dx, final double dy) {
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, end);
    pass.endDrag(start, pos, dx, dy);
  }

  @Override
  public final boolean isChanging() {
    return pass.isChanging();
  }

  @Override
  public final void processMessage(final String[] ids, final String msg) {
    super.processMessage(ids, msg);
    pass.processMessage(ids, msg);
  }

}
