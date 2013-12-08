package jkanvas.nodelink.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.DefaultNodeRealizer;
import jkanvas.nodelink.NodeLinkView;

/**
 * A layout positioning the nodes in a circle.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node position type.
 */
public class CircleLayouter<T extends AnimatedPosition> extends AbstractLayouter<T> {

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    boolean chg = false;
    final Rectangle2D rect = new Rectangle2D.Double();
    getBoundingBox(rect);
    final double w = rect.getWidth();
    final double h = rect.getHeight();
    final double r = Math.min(w, h) / 2 - DefaultNodeRealizer.RADIUS;
    final int count = view.nodeCount();
    final double step = 2 * Math.PI / count;
    double angle = 0;
    for(int i = 0; i < count; ++i) {
      final double x = rect.getCenterX() + Math.sin(angle) * r;
      final double y = rect.getCenterY() + Math.cos(angle) * r;
      chg = setPosition(view.getNode(i), new Point2D.Double(x, y)) || chg;
      angle += step;
    }
    return chg;
  }

}
