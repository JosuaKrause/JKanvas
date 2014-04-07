package jkanvas.painter.groups;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.List;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.groups.RenderGroup.RenderpassPosition;

/**
 * A layout for render passes in a render group.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The render pass type.
 */
public abstract class RenderpassLayout<T extends Renderpass> {

  /**
   * Computes the actual layout.
   * 
   * @param members The elements of the group.
   */
  public abstract void doLayout(List<RenderpassPosition<T>> members);

  /**
   * Adds the bounding boxes of the members to the given bounding box.
   * 
   * @param bbox The bounding box storing the result. An empty rectangle is
   *          handled correctly.
   * @param members The members to add to the bounding box.
   * @return Whether some bounding boxes were added.
   */
  public boolean addBoundingBox(
      final RectangularShape bbox, final List<RenderpassPosition<T>> members) {
    boolean change = false;
    for(final RenderpassPosition<T> p : members) {
      if(!p.pass.isVisible()) {
        continue;
      }
      if(p.checkBBoxChange()) {
        change = true;
      }
      RenderpassPainter.addToRect(bbox, p.getPassBBox());
    }
    return change;
  }

  /**
   * Draws the background for the render group.
   * 
   * @param g The graphics context.
   * @param ctx The context.
   * @param bbox The bounding box.
   * @param members The members.
   */
  public void drawBackground(
      @SuppressWarnings("unused") final Graphics2D g,
      @SuppressWarnings("unused") final KanvasContext ctx,
      @SuppressWarnings("unused") final Rectangle2D bbox,
      @SuppressWarnings("unused") final List<RenderpassPosition<T>> members) {
    // nothing to draw
  }

}
