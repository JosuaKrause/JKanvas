package jkanvas.painter.groups;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.List;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.groups.RenderGroup.RenderpassPosition;

public abstract class RenderpassLayout<T extends Renderpass> {

  public abstract void doLayout(List<RenderpassPosition<T>> members);

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

  public void drawBackground(final Graphics2D g, final KanvasContext ctx,
      final Rectangle2D bbox, final List<RenderpassPosition<T>> members) {
    // nothing to draw
  }

}
