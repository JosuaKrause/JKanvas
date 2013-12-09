package jkanvas.nodelink.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationTiming;
import jkanvas.nodelink.NodeLinkView;
import jkanvas.util.VecUtil;

/**
 * Bouncing nodes.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public class BouncingLayouter<T extends AnimatedPosition> extends AbstractLayouter<T> {

  /** The movements of the nodes. */
  private final Map<Integer, Point2D> movements = new HashMap<>();

  /** Creates a bouncing layouter. */
  public BouncingLayouter() {
    setTiming(AnimationTiming.FAST);
  }

  /** The speed of the nodes. */
  private double speed = 20.0;

  /**
   * Setter.
   * 
   * @param speed Sets the speed.
   */
  public void setSpeed(final double speed) {
    this.speed = speed;
  }

  /**
   * Getter.
   * 
   * @return The speed of the nodes.
   */
  public double getSpeed() {
    return speed;
  }

  @Override
  protected boolean iterate() {
    return true;
  }

  @Override
  public void deregister() {
    super.deregister();
    movements.clear();
  }

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    final Rectangle2D rect = new Rectangle2D.Double();
    getBoundingBox(rect);
    for(int i = 0; i < view.nodeCount(); ++i) {
      final T node = view.getNode(i);
      if(!movements.containsKey(i)) {
        final double alpha = Math.random() * Math.PI * 2;
        movements.put(i, new Point2D.Double(
            speed * Math.cos(alpha), speed * Math.sin(alpha)));
      }
      final Point2D move = movements.get(i);
      final Point2D newPos = VecUtil.addVec(node.getPos(), move);
      if(newPos.getX() > rect.getMaxX() && move.getX() > 0) {
        move.setLocation(-move.getX(), move.getY());
      }
      if(newPos.getX() < rect.getMinX() && move.getX() < 0) {
        move.setLocation(-move.getX(), move.getY());
      }
      if(newPos.getY() > rect.getMaxY() && move.getY() > 0) {
        move.setLocation(move.getX(), -move.getY());
      }
      if(newPos.getY() < rect.getMinY() && move.getY() < 0) {
        move.setLocation(move.getX(), -move.getY());
      }
      setPosition(node, VecUtil.addVec(node.getPos(), move));
    }
    return true;
  }

}
