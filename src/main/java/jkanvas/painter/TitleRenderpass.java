package jkanvas.painter;

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
import jkanvas.util.StringDrawer;

/**
 * Adds a title to the given renderpass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class TitleRenderpass extends AbstractRenderpass {

  /** The render pass in a list for easier handling. */
  private final List<Renderpass> list;
  /** The decorated render pass. */
  private final AbstractRenderpass pass;
  /** The text height. */
  private final double textHeight;
  /** The space between render pass and text. */
  private final double space;
  /** The title text. */
  private String title;

  /**
   * Creates a title for the given render pass.
   * 
   * @param title The initial title.
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public TitleRenderpass(final String title, final AbstractRenderpass pass,
      final double textHeight, final double space) {
    this.textHeight = textHeight;
    this.space = space;
    this.pass = Objects.requireNonNull(pass);
    this.title = Objects.requireNonNull(title);
    list = Collections.singletonList((Renderpass) pass);
    pass.setParent(this);
    pass.setOffset(0, space + textHeight);
  }

  /**
   * Setter.
   * 
   * @param title The title.
   */
  public void setTitle(final String title) {
    this.title = Objects.requireNonNull(title);
  }

  /**
   * Getter.
   * 
   * @return The current title.
   */
  public String getTitle() {
    return title;
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    final Rectangle2D box = new Rectangle2D.Double();
    getBoundingBox(box);
    box.setFrame(box.getX(), box.getY(), box.getWidth(), textHeight);
    g.setColor(Color.BLACK);
    StringDrawer.drawInto(g, title, box);
    RenderpassPainter.draw(list, g, ctx);
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox) {
    pass.getBoundingBox(bbox);
    bbox.setFrame(bbox.getX(), bbox.getY(),
        bbox.getWidth(), bbox.getHeight() + space + textHeight);
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
