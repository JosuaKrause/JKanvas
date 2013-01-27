package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
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
  private final List<HUDRenderpass> front;

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
    back.add(r);
  }

  /**
   * Adds a new HUD render pass on top.
   * 
   * @param r The HUD render pass.
   */
  public void addHUDPass(final HUDRenderpass r) {
    front.add(r);
  }

  @Override
  public final void draw(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleCanvas();
    for(final Renderpass r : back) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = getPassBoundingBox(r);
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
      final KanvasContext c = getContextFor(r, ctx);
      r.draw(g, c);
      g.dispose();
    }
  }

  @Override
  public final boolean click(final Point2D p, final MouseEvent e) {
    for(final Renderpass r : back) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, p);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.click(pos, e)) return true;
    }
    return false;
  }

  @Override
  public final String getTooltip(final Point2D p) {
    for(final Renderpass r : back) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, p);
      if(!bbox.contains(pos)) {
        continue;
      }
      final String tooltip = r.getTooltip(pos);
      if(tooltip != null) return tooltip;
    }
    return null;
  }

  @Override
  public final boolean moveMouse(final Point2D cur) {
    for(final Renderpass r : back) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, cur);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.moveMouse(pos)) return true;
    }
    return false;
  }

  /** The render-pass currently responsible for dragging. */
  private Renderpass dragging = null;

  /** The start position of the drag in the render-pass coordinates. */
  private Point2D start = null;

  @Override
  public final boolean acceptDrag(final Point2D p, final MouseEvent e) {
    for(final Renderpass r : back) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, p);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.acceptDrag(pos, e)) {
        start = pos;
        dragging = r;
        return true;
      }
    }
    return false;
  }

  @Override
  public final void drag(final Point2D _, final Point2D cur,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = getPositionFromCanvas(dragging, cur);
    dragging.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _, final Point2D end,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = getPositionFromCanvas(dragging, end);
    dragging.endDrag(start, pos, dx, dy);
    dragging = null;
  }

  @Override
  public final void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    for(final HUDRenderpass r : front) {
      if(!r.isVisible()) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      r.drawHUD(g, ctx);
      g.dispose();
    }
  }

  @Override
  public final boolean clickHUD(final Point2D p) {
    for(final HUDRenderpass r : front) {
      if(!r.isVisible()) {
        continue;
      }
      if(r.clickHUD(p)) return true;
    }
    return false;
  }

  @Override
  public final String getTooltipHUD(final Point2D p) {
    for(final HUDRenderpass r : front) {
      if(!r.isVisible()) {
        continue;
      }
      final String tooltip = r.getTooltipHUD(p);
      if(tooltip != null) return tooltip;
    }
    return null;
  }

  /** The HUD-render-pass currently responsible for dragging. */
  private HUDRenderpass draggingHUD = null;

  @Override
  public final boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
    for(final HUDRenderpass r : front) {
      if(!r.isVisible()) {
        continue;
      }
      if(r.acceptDragHUD(p, e)) {
        draggingHUD = r;
        return true;
      }
    }
    return false;
  }

  @Override
  public final void dragHUD(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    if(draggingHUD == null) return;
    draggingHUD.dragHUD(start, cur, dx, dy);
  }

  @Override
  public final void endDragHUD(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    if(draggingHUD == null) return;
    draggingHUD.endDragHUD(start, end, dx, dy);
    draggingHUD = null;
  }

  /**
   * Converts a position in canvas coordinates to the position of the given
   * render-pass.
   * 
   * @param r The render-pass.
   * @param pos The position in canvas coordinates.
   * @return The position in render-pass coordinates.
   */
  public static final Point2D getPositionFromCanvas(final Renderpass r, final Point2D pos) {
    return new Point2D.Double(pos.getX() - r.getOffsetX(), pos.getY() - r.getOffsetY());
  }

  /**
   * Converts a position in component coordinates to the position of the given
   * render-pass.
   * 
   * @param r The render-pass.
   * @param ctx The current (non render-pass adjusted) context.
   * @param pos The position in components coordinates.
   * @return The position in render-pass coordinates.
   */
  public static final Point2D getPositionFromComponent(final Renderpass r,
      final KanvasContext ctx, final Point2D pos) {
    return getPositionFromCanvas(r, ctx.toCanvasCoordinates(pos));
  }

  /**
   * Converts the given context to represent the context of the given
   * render-pass.
   * 
   * @param r The render-pass.
   * @param ctx The context.
   * @return The transformed context.
   */
  public static final KanvasContext getContextFor(
      final Renderpass r, final KanvasContext ctx) {
    return ctx.translate(r.getOffsetX(), r.getOffsetY());
  }

  /**
   * Getter.
   * 
   * @param r The render-pass.
   * @return The bounding box of the render-pass in canvas coordinates.
   */
  private static Rectangle2D getPassBoundingBox(final Renderpass r) {
    final Rectangle2D rect = r.getBoundingBox();
    if(rect == null) return null;
    return new Rectangle2D.Double(rect.getX() + r.getOffsetX(),
        rect.getY() + r.getOffsetY(), rect.getWidth(), rect.getHeight());
  }

  @Override
  public final Rectangle2D getBoundingBox() {
    Rectangle2D bbox = null;
    for(final Renderpass r : back) {
      final Rectangle2D b = getPassBoundingBox(r);
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
