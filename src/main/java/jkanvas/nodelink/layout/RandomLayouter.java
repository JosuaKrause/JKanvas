package jkanvas.nodelink.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.NodeLinkView;

/**
 * Randomly positions nodes in the given frame.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public class RandomLayouter<T extends AnimatedPosition> extends AbstractLayouter<T> {

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    boolean chg = false;
    final Rectangle2D rect = new Rectangle2D.Double();
    getBoundingBox(rect);
    final double w = rect.getWidth();
    final double h = rect.getHeight();
    for(final T node : view.nodes()) {
      final double x = rect.getX() + Math.random() * w;
      final double y = rect.getY() + Math.random() * h;
      chg = setPosition(node, new Point2D.Double(x, y)) || chg;
    }
    return chg;
  }

}
