package jkanvas.painter;

import java.awt.BasicStroke;
import java.awt.Color;
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
 * Shows borders of render passes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BorderRenderpass extends Renderpass {

  /** The render pass in a list for easier handling. */
  private final List<Renderpass> list;
  /** The render pass. */
  private final Renderpass pass;
  /** The stroke width. */
  private final double width;
  /** The border color. */
  private final Color border;

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   */
  public BorderRenderpass(final Renderpass pass) {
    this(pass, Color.BLACK, 1.0);
  }

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   * @param border The border color.
   * @param width The stroke width.
   */
  public BorderRenderpass(final Renderpass pass, final Color border, final double width) {
    this.pass = Objects.requireNonNull(pass);
    this.border = Objects.requireNonNull(border);
    this.width = width;
    list = Collections.singletonList(pass);
    pass.setParent(this);
    pass.setOffset(width * 0.5, width * 0.5);
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D box = new Rectangle2D.Double();
    pass.getBoundingBox(box);
    box.setFrame(box.getX(), box.getY(),
        box.getWidth() + width, box.getHeight() + width);
    final Graphics2D g = (Graphics2D) gfx.create();
    g.setColor(border);
    g.setStroke(new BasicStroke((float) width));
    g.draw(box);
    g.dispose();
    RenderpassPainter.draw(list, gfx, ctx);
    //
    // if(ctx.toCanvasLength(1) > 1) {
    // g.drawRect(0, 0, (int) rect.getWidth() - 1, (int) rect.getHeight() - 1);
    // } else {
    // g.draw(rect);
    // }
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox) {
    pass.getBoundingBox(bbox);
    bbox.setFrame(bbox.getX() - width, bbox.getY() - width,
        bbox.getWidth() + 2 * width, bbox.getHeight() + 2 * width);
  }

  @Override
  public boolean click(final Camera cam, final Point2D position, final MouseEvent e) {
    return RenderpassPainter.click(list, cam, position, e);
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D position, final MouseEvent e) {
    if(RenderpassPainter.doubleClick(list, cam, position, e)) return true;
    if(!USE_DOUBLE_CLICK_DEFAULT) return false;
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    cam.toView(this, AnimationTiming.SMOOTH, null, true);
    return true;
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
  public boolean isChanging() {
    return pass.isChanging();
  }

  @Override
  public void processMessage(final String[] ids, final String msg) {
    super.processMessage(ids, msg);
    pass.processMessage(ids, msg);
  }

}
