package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import jkanvas.KanvasContext;

/**
 * An adapter for render-passes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class RenderpassAdapter extends AbstractRenderpass {

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    // do nothing
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    // do nothing when clicking
    return false;
  }

  @Override
  public String getTooltip(final Point2D p) {
    // no tool-tip
    return null;
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    // no dragging
    return false;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur, final double dx, final double dy) {
    // do nothing
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end, final double dx, final double dy) {
    // do nothing
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    // do nothing
    return false;
  }

}
