package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jkanvas.KanvasContext;


/**
 * A render pass painter renders render passes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class RenderpassPainter extends PainterAdapter {

  /** The HUD render passes. */
  private final List<Renderpass> front;

  /** The normal render passes. */
  private final List<Renderpass> back;

  /** Creates an empty render pass painter. */
  public RenderpassPainter() {
    front = new ArrayList<>();
    back = new ArrayList<>();
  }

  /**
   * Adds a new render pass on top.
   * 
   * @param r The render pass.
   */
  public void addPass(final Renderpass r) {
    if(!r.isHUD()) {
      back.add(r);
    } else {
      front.add(r);
    }
  }

  @Override
  public final void draw(final Graphics2D gfx, final KanvasContext ctx) {
    render(gfx, ctx, back);
  }

  @Override
  public final void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    render(gfx, ctx, front);
  }

  /**
   * Renders a list of render-passes.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   * @param list The list of render-passes.
   */
  private static void render(final Graphics2D gfx, final KanvasContext ctx,
      final List<Renderpass> list) {
    final Rectangle2D view = ctx.getVisibleCanvas();
    for(final Renderpass r : list) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = getBoundingBox(r);
      if(bbox != null && !view.intersects(bbox)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      if(bbox != null) {
        g.setClip(bbox);
      }
      final double dx = r.getOffsetX();
      final double dy = r.getOffsetY();
      g.translate(dx, dy);
      final KanvasContext c = ctx.translate(dx, dy);
      r.render(g, c);
      g.dispose();
    }
  }

  /**
   * Converts a position to the real position of the given render-pass.
   * 
   * @param r The render-pass.
   * @param pos The position.
   * @return The real position.
   */
  public Point2D getRealPosition(final Renderpass r, final Point2D pos) {
    return new Point2D.Double(pos.getX() - r.getOffsetX(), pos.getY() - r.getOffsetY());
  }

  /**
   * Getter.
   * 
   * @param r The render-pass.
   * @return The bounding box of the render-pass in canvas coordinates.
   */
  private static Rectangle2D getBoundingBox(final Renderpass r) {
    final Rectangle2D rect = r.getBoundingBox();
    if(rect == null) return null;
    return new Rectangle2D.Double(rect.getX() + r.getOffsetX(),
        rect.getY() + r.getOffsetY(), rect.getWidth(), rect.getHeight());
  }

  @Override
  public Rectangle2D getBoundingBox() {
    Rectangle2D bbox = null;
    for(final Renderpass r : back) {
      final Rectangle2D b = getBoundingBox(r);
      if(b == null) {
        continue;
      }
      if(bbox == null) {
        bbox = b;
      } else {
        bbox.add(b);
      }
    }
    return bbox;
  }

}
