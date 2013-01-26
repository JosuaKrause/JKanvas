package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;

import jkanvas.KanvasContext;
import jkanvas.KanvasPainter;


/**
 * Provides meaningful default implementations for a {@link KanvasPainter}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class PainterAdapter implements KanvasPainter {

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    // draw nothing
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    // draw nothing
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    // the event is not consumed
    return false;
  }

  @Override
  public boolean clickHUD(final Point2D p) {
    // the event is not consumed
    return false;
  }

  @Override
  public String getTooltip(final Point2D p) {
    // no tool-tip
    return null;
  }

  @Override
  public String getTooltipHUD(final Point2D p) {
    // no tool-tip
    return null;
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    // the event is not consumed
    return false;
  }

  @Override
  public boolean isAllowingPan(final Point2D p, final MouseEvent e) {
    return SwingUtilities.isLeftMouseButton(e);
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    // do nothing
  }

  @Override
  public void endDrag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    drag(start, cur, dx, dy);
  }

  @Override
  public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
    // do nothing
    return false;
  }

  @Override
  public void dragHUD(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    // do nothing
  }

  @Override
  public void endDragHUD(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    dragHUD(start, end, dx, dy);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    // nothing to do
    return false;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return null;
  }

  @Override
  public void dispose() {
    // nothing to do
  }

}
