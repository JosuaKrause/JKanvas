package jkanvas.painter;

import static jkanvas.util.ArrayUtil.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jkanvas.KanvasContext;
import jkanvas.util.PaintUtil;

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
   * Removes a render pass.
   * 
   * @param r The render pass.
   */
  public void removePass(final Renderpass r) {
    back.remove(r);
  }

  /**
   * Adds a new HUD render pass on top.
   * 
   * @param r The HUD render pass.
   */
  public void addHUDPass(final HUDRenderpass r) {
    front.add(r);
  }

  /**
   * Removes a HUD render pass.
   * 
   * @param r The HUD render pass.
   */
  public void removeHUDPass(final HUDRenderpass r) {
    front.remove(r);
  }

  @Override
  public final void draw(final Graphics2D g, final KanvasContext ctx) {
    g.setColor(Color.GRAY);
    draw(back, g, ctx);
  }

  /**
   * Draws all render passes given by the list. This method obeys
   * {@link jkanvas.Canvas#DEBUG_BBOX} by using the current color to draw the
   * bounding boxes.
   * 
   * @param passes The render passes to draw.
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   * @see #draw(Graphics2D, KanvasContext)
   */
  public static final void draw(
      final List<Renderpass> passes, final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleCanvas();
    for(final Renderpass r : passes) {
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
    if(jkanvas.Canvas.DEBUG_BBOX) {
      final Graphics2D g = (Graphics2D) gfx.create();
      PaintUtil.setAlpha(g, 0.3);
      for(final Renderpass r : passes) {
        if(!r.isVisible()) {
          continue;
        }
        final Rectangle2D bbox = getPassBoundingBox(r);
        if(bbox == null || !view.intersects(bbox)) {
          continue;
        }
        g.fill(bbox);
      }
      g.dispose();
    }
  }

  @Override
  public final boolean click(final Point2D p, final MouseEvent e) {
    return click(back, p, e);
  }

  /**
   * Clicks on render passes.
   * 
   * @param passes The list of render passes.
   * @param p The clicked point in canvas coordinates.
   * @param e The mouse event.
   * @return Whether the click was consumed.
   * @see #click(Point2D, MouseEvent)
   */
  public static final boolean click(
      final List<Renderpass> passes, final Point2D p, final MouseEvent e) {
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, p);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.click(pos, e)) return true;
    }
    return false;
  }

  @Override
  public final String getTooltip(final Point2D p) {
    return getTooltip(back, p);
  }

  /**
   * Returns the tool-tip from the first render pass returning one.
   * 
   * @param passes The render passes.
   * @param p The point in canvas coordinates.
   * @return The tool-tip text or null if no tool-tip was returned by any render
   *         pass.
   * @see #getTooltip(Point2D)
   */
  public static final String getTooltip(final List<Renderpass> passes, final Point2D p) {
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, p);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      final String tooltip = r.getTooltip(pos);
      if(tooltip != null) return tooltip;
    }
    return null;
  }

  @Override
  public final boolean moveMouse(final Point2D cur) {
    return moveMouse(back, cur);
  }

  /**
   * Moves the mouse.
   * 
   * @param passes The render passes.
   * @param cur The current position.
   * @return Whether any render pass has consumed the mouse move.
   * @see #moveMouse(Point2D)
   */
  public static final boolean moveMouse(final List<Renderpass> passes, final Point2D cur) {
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, cur);
      if(bbox != null && !bbox.contains(pos)) {
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
    for(final Renderpass r : reverseList(back)) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = getPositionFromCanvas(r, p);
      if(bbox != null && !bbox.contains(pos)) {
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
    for(final HUDRenderpass r : reverseList(front)) {
      if(!r.isVisible()) {
        continue;
      }
      if(r.clickHUD(p)) return true;
    }
    return false;
  }

  @Override
  public final String getTooltipHUD(final Point2D p) {
    for(final HUDRenderpass r : reverseList(front)) {
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
    for(final HUDRenderpass r : reverseList(front)) {
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
  public static final Rectangle2D getPassBoundingBox(final Renderpass r) {
    final Rectangle2D rect = r.getBoundingBox();
    if(rect == null) return null;
    return new Rectangle2D.Double(rect.getX() + r.getOffsetX(),
        rect.getY() + r.getOffsetY(), rect.getWidth(), rect.getHeight());
  }

  /**
   * Calculates the joined bounding boxes of the given render passes.
   * 
   * @param passes The render passes.
   * @return The joined bounding box or <code>null</code> if no bounding box was
   *         present.
   * @see #getBoundingBox()
   */
  public static final Rectangle2D getBoundingBox(final Iterable<Renderpass> passes) {
    Rectangle2D bbox = null;
    for(final Renderpass r : passes) {
      if(!r.isVisible()) {
        continue;
      }
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

  @Override
  public final Rectangle2D getBoundingBox() {
    return getBoundingBox(back);
  }

  /**
   * Computes the top level bounding box position in canvas coordinates of the
   * given render pass.
   * 
   * @param pass The render pass.
   * @return The bounding box of the render pass in top level canvas
   *         coordinates.
   */
  public static final Rectangle2D getTopLevelBounds(final Renderpass pass) {
    final Rectangle2D rect = pass.getBoundingBox();
    if(rect == null) return null;
    return getTopLevelBounds(pass, rect);
  }

  /**
   * Converts a rectangle in render pass local coordinates into top level canvas
   * coordinates.
   * 
   * @param pass The render pass.
   * @param rect The rectangle in local render pass canvas coordinates.
   * @return The rectangle in top level canvas coordinates.
   */
  public static final Rectangle2D getTopLevelBounds(
      final Renderpass pass, final Rectangle2D rect) {
    final Rectangle2D box = new Rectangle2D.Double(
        rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    Renderpass p = pass;
    do {
      box.setRect(box.getX() + p.getOffsetX(),
          box.getY() + p.getOffsetY(),
          box.getWidth(), box.getHeight());
      p = p.getParent();
    } while(p != null);
    return box;
  }

}
