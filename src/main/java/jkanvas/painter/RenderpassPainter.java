package jkanvas.painter;

import static jkanvas.util.ArrayUtil.*;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.KanvasPainter;
import jkanvas.util.PaintUtil;

/**
 * A render pass painter renders render passes.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class RenderpassPainter implements KanvasPainter {

  /** The HUD render passes. */
  private final List<HUDRenderpass> front;

  /** The normal render passes. */
  private final List<Renderpass> back;

  /** Creates an empty render pass painter. */
  public RenderpassPainter() {
    front = new ArrayList<>();
    back = new ArrayList<>();
  }

  @Override
  public boolean isAllowingPan(final Point2D p, final MouseEvent e) {
    return SwingUtilities.isLeftMouseButton(e);
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
    g.setColor(java.awt.Color.GRAY);
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
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final Renderpass r : passes) {
      if(!r.isVisible()) {
        continue;
      }
      getPassBoundingBox(bbox, r);
      if(!view.intersects(bbox)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      g.clip(bbox);
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
        getPassBoundingBox(bbox, r);
        if(!view.intersects(bbox)) {
          continue;
        }
        g.fill(bbox);
      }
      g.dispose();
    }
  }

  @Override
  public final boolean click(final Camera cam, final Point2D p, final MouseEvent e) {
    return click(back, cam, p, e);
  }

  /**
   * Clicks on render passes.
   *
   * @param passes The list of render passes.
   * @param cam The camera on which the interaction happened.
   * @param p The clicked point in canvas coordinates.
   * @param e The mouse event.
   * @return Whether the click was consumed.
   * @see #click(Camera, Point2D, MouseEvent)
   */
  public static final boolean click(
      final List<Renderpass> passes, final Camera cam, final Point2D p, final MouseEvent e) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
      final Point2D pos = getPositionFromCanvas(r, p);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.click(cam, pos, e)) return true;
    }
    return false;
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D p, final MouseEvent e) {
    if(doubleClick(back, cam, p, e)) return true;
    if(!Renderpass.USE_DOUBLE_CLICK_DEFAULT) return false;
    final Rectangle2D box = new Rectangle2D.Double();
    getBoundingBox(box);
    return Renderpass.defaultDoubleClick(box, cam, e);
  }

  /**
   * Double clicks on render passes.
   *
   * @param passes The list of render passes.
   * @param cam The camera on which the interaction happened.
   * @param p The double clicked point in canvas coordinates.
   * @param e The mouse event.
   * @return Whether the click was consumed.
   * @see #doubleClick(Camera, Point2D, MouseEvent)
   */
  public static final boolean doubleClick(
      final List<Renderpass> passes, final Camera cam, final Point2D p, final MouseEvent e) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
      final Point2D pos = getPositionFromCanvas(r, p);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.doubleClick(cam, pos, e)) return true;
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
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
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
    return moveMouse(back, cur);
  }

  /**
   * Moves the mouse. This method is always called on every render pass.
   *
   * @param passes The render passes.
   * @param cur The current position.
   * @return Whether any render pass has been affected by the mouse move.
   * @see #moveMouse(Point2D)
   */
  public static final boolean moveMouse(final List<Renderpass> passes, final Point2D cur) {
    boolean moved = false;
    for(final Renderpass r : reverseList(passes)) {
      if(!r.isVisible()) {
        continue;
      }
      final Point2D pos = getPositionFromCanvas(r, cur);
      if(r.moveMouse(pos)) {
        moved = true;
      }
    }
    return moved;
  }

  /** The render pass currently responsible for dragging. */
  private Renderpass dragging = null;

  /** The start position of the drag in the render pass coordinates. */
  private Point2D start = null;

  @Override
  public final boolean acceptDrag(final Point2D p, final MouseEvent e) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final Renderpass r : reverseList(back)) {
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
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
  public final void drag(final Point2D _start, final Point2D cur,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = getPositionFromCanvas(dragging, cur);
    dragging.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _start, final Point2D end,
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
  public final boolean clickHUD(final Camera cam, final Point2D p, final MouseEvent e) {
    for(final HUDRenderpass r : reverseList(front)) {
      if(!r.isVisible()) {
        continue;
      }
      if(r.clickHUD(cam, p, e)) return true;
    }
    return false;
  }

  @Override
  public final boolean doubleClickHUD(
      final Camera cam, final Point2D p, final MouseEvent e) {
    for(final HUDRenderpass r : reverseList(front)) {
      if(!r.isVisible()) {
        continue;
      }
      if(r.doubleClickHUD(cam, p, e)) return true;
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

  /** The HUD render pass currently responsible for dragging. */
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
   * render pass.
   *
   * @param r The render pass.
   * @param pos The position in canvas coordinates.
   * @return The position in render pass coordinates.
   */
  public static final Point2D getPositionFromCanvas(final Renderpass r, final Point2D pos) {
    return new Point2D.Double(pos.getX() - r.getOffsetX(), pos.getY() - r.getOffsetY());
  }

  /**
   * Converts a position in component coordinates to the position of the given
   * render pass.
   *
   * @param r The render pass.
   * @param ctx The current (non render pass adjusted) context.
   * @param pos The position in components coordinates.
   * @return The position in render pass coordinates.
   */
  public static final Point2D getPositionFromComponent(
      final Renderpass r, final KanvasContext ctx, final Point2D pos) {
    return getPositionFromCanvas(r, ctx.toCanvasCoordinates(pos));
  }

  /**
   * Converts the given context to represent the context of the given render
   * pass.
   *
   * @param r The render pass.
   * @param ctx The context.
   * @return The transformed context.
   */
  public static final KanvasContext getContextFor(
      final Renderpass r, final KanvasContext ctx) {
    return ctx.translate(r.getOffsetX(), r.getOffsetY());
  }

  /**
   * Converts the given context to represent the context of the given render
   * pass from the top level.
   *
   * @param r The render pass.
   * @param ctx The context.
   * @return The transformed context.
   */
  public static final KanvasContext getRecursiveContextFor(
      final Renderpass r, final KanvasContext ctx) {
    final Renderpass p = r.getParent();
    final KanvasContext c = p != null ? getRecursiveContextFor(p, ctx) : ctx;
    return getContextFor(r, c);
  }

  /**
   * Getter.
   *
   * @param rect The rectangle in which the bounding box of the render pass in
   *          canvas coordinates is stored.
   * @param r The render pass.
   */
  public static final void getPassBoundingBox(
      final Rectangle2D rect, final Renderpass r) {
    r.getBoundingBox(rect);
    rect.setFrame(rect.getX() + r.getOffsetX(),
        rect.getY() + r.getOffsetY(), rect.getWidth(), rect.getHeight());
  }

  /**
   * Calculates the joined bounding boxes of the given render passes.
   *
   * @param rect The rectangle where the result is stored. Note that the result
   *          may be empty.
   * @param passes The render passes.
   */
  public static final void getBoundingBox(
      final RectangularShape rect, final Iterable<Renderpass> passes) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    // clear rect
    rect.setFrame(bbox);
    for(final Renderpass r : passes) {
      if(!r.isVisible()) {
        continue;
      }
      getPassBoundingBox(bbox, r);
      addToRect(rect, bbox);
    }
  }

  /**
   * Adds the other rectangle to the given rectangular shape.
   *
   * @param rect The rectangle to extend.
   * @param other The rectangle to add.
   */
  public static final void addToRect(final RectangularShape rect, final Rectangle2D other) {
    if(rect.isEmpty()) {
      rect.setFrame(other);
      return;
    } else if(other.isEmpty()) return;
    final double x1 = Math.min(rect.getMinX(), other.getMinX());
    final double x2 = Math.max(rect.getMaxX(), other.getMaxX());
    final double y1 = Math.min(rect.getMinY(), other.getMinY());
    final double y2 = Math.max(rect.getMaxY(), other.getMaxY());
    rect.setFrame(x1, y1, x2 - x1, y2 - y1);
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    getBoundingBox(bbox, back);
  }

  @Override
  public void processMessage(final String[] ids, final String msg) {
    processMessage(back, ids, msg);
    processHUDMessage(front, ids, msg);
  }

  /**
   * Processes a message handed in via the
   * {@link jkanvas.Canvas#postMessage(String)} method. This method forwards the
   * message to all visible children.
   *
   * @param passes The children.
   * @param ids The ids that accept the message.
   * @param msg The message.
   */
  public static final void processMessage(
      final Iterable<Renderpass> passes, final String[] ids, final String msg) {
    for(final Renderpass r : passes) {
      r.processMessage(ids, msg);
    }
  }

  /**
   * Processes a message handed in via the
   * {@link jkanvas.Canvas#postMessage(String)} method. This method forwards the
   * message to all visible children.
   *
   * @param passes The children.
   * @param ids The ids that accept the message.
   * @param msg The message.
   */
  public static final void processHUDMessage(
      final Iterable<HUDRenderpass> passes, final String[] ids, final String msg) {
    for(final HUDRenderpass r : passes) {
      r.processMessage(ids, msg);
    }
  }

  /**
   * Whether the render pass is visible at top level.
   *
   * @param pass The render pass.
   * @return Whether it is actually visible.
   */
  public static final boolean isTopLevelVisible(final Renderpass pass) {
    if(pass == null) return true;
    return pass.isVisible() && isTopLevelVisible(pass.getParent());
  }

  /**
   * Computes the top level bounding box position of the given render pass in
   * canvas coordinates.
   *
   * @param bbox The rectangle in which the result will be stored.
   * @param pass The render pass.
   */
  public static final void getTopLevelBounds(final Rectangle2D bbox, final Renderpass pass) {
    pass.getBoundingBox(bbox);
    convertToTopLevelBounds(bbox, pass);
  }

  /**
   * Converts a rectangle in render pass local coordinates into top level canvas
   * coordinates.
   *
   * @param rect The rectangle to transform.
   * @param pass The render pass.
   */
  public static final void convertToTopLevelBounds(
      final Rectangle2D rect, final Renderpass pass) {
    Renderpass p = pass;
    do {
      rect.setFrame(rect.getX() + p.getOffsetX(), rect.getY() + p.getOffsetY(),
          rect.getWidth(), rect.getHeight());
      p = p.getParent();
    } while(p != null);
  }

  /**
   * Computes the top level offset of the given render pass in canvas
   * coordinates.
   *
   * @param pass The render pass.
   * @return The offset of the render pass in top level canvas coordinates.
   */
  public static final Point2D getTopLevelOffset(final Renderpass pass) {
    Renderpass p = pass;
    final Point2D res = new Point2D.Double();
    do {
      res.setLocation(res.getX() + pass.getOffsetX(), res.getY() + pass.getOffsetY());
      p = p.getParent();
    } while(p != null);
    return res;
  }

  @Override
  public void dispose() {
    front.clear();
    back.clear();
    dragging = null;
    draggingHUD = null;
  }

}
