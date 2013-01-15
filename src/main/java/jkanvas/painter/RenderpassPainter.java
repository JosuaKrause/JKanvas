package jkanvas.painter;

import java.awt.Graphics2D;
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
    for(final Renderpass r : list) {
      final Graphics2D g = (Graphics2D) gfx.create();
      final double dx = r.getOffsetX();
      final double dy = r.getOffsetY();
      g.translate(dx, dy);
      final KanvasContext c = ctx.translate(dx, dy);
      r.render(g, c);
      g.dispose();
    }
  }

  @Override
  public Rectangle2D getBoundingBox() {
    Rectangle2D bbox = null;
    for(final Renderpass r : back) {
      final Rectangle2D tmp = r.getBoundingBox();
      if(tmp == null) {
        continue;
      }
      final Rectangle2D b = new Rectangle2D.Double(tmp.getX() + r.getOffsetX(),
          tmp.getY() + r.getOffsetY(), tmp.getWidth(), tmp.getHeight());
      if(bbox == null) {
        bbox = b;
      } else {
        bbox.add(b);
      }
    }
    return bbox;
  }

}
