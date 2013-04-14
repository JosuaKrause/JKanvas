package jkanvas.nodelink.layout;

import static jkanvas.util.VecUtil.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationTiming;
import jkanvas.nodelink.NodeLinkView;

/**
 * A force directed layout.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public class ForceDirectedLayouter<T extends AnimatedPosition> extends
    AbstractLayouter<T> {

  /** The preferred length of an edge. */
  private double preferredLength = 200;
  /** The spring factor. */
  private double factor = 0.2;
  /** The movement decay. */
  private double decay = 0.3;

  /** Creates a force directed layouter. */
  public ForceDirectedLayouter() {
    setTiming(AnimationTiming.FAST);
  }

  /**
   * Setter.
   * 
   * @param preferredLength The preferred length of an edge.
   */
  public void setPreferredLength(final double preferredLength) {
    this.preferredLength = preferredLength;
  }

  /**
   * Getter.
   * 
   * @return The preferred length of an edge.
   */
  public double getPreferredLength() {
    return preferredLength;
  }

  /**
   * Setter.
   * 
   * @param factor The spring constant.
   */
  public void setFactor(final double factor) {
    this.factor = factor;
  }

  /**
   * Getter.
   * 
   * @return The spring constant.
   */
  public double getFactor() {
    return factor;
  }

  /**
   * Setter.
   * 
   * @param decay The decay of movement.
   */
  public void setDecay(final double decay) {
    this.decay = decay;
  }

  /**
   * Getter.
   * 
   * @return The decay of movement.
   */
  public double getDecay() {
    return decay;
  }

  @Override
  protected boolean iterate() {
    return true;
  }

  @Override
  public void setTiming(final AnimationTiming timing) {
    if(timing.duration <= 0) throw new IllegalArgumentException(
        "needs duration > 0: " + timing.duration);
    super.setTiming(timing);
  }

  /** The list of node velocities. */
  private final List<Point2D> velocity = new ArrayList<>();

  @Override
  public void deregister() {
    super.deregister();
    velocity.clear();
  }

  /** The current decay. */
  private double curDecay;

  /** The minimal movement. */
  public static double MIN_MOVEMENT = 1e-9;

  @Override
  public void layout(final boolean deregisterOnEnd) {
    velocity.clear();
    curDecay = decay;
    super.layout(deregisterOnEnd);
  }

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    final int count = view.nodeCount();
    while(velocity.size() < count) {
      velocity.add(new Point2D.Double());
    }
    Point2D all = new Point2D.Double();
    for(int n = 0; n < count; ++n) {
      boolean needsMovement = false;
      final T node = view.getNode(n);
      final Point2D pos = node.getPos();
      Point2D force = new Point2D.Double();
      for(int e = 0; e < count; ++e) {
        final T other = view.getNode(e);
        final Point2D diff = subVec(pos, other.getPos());
        final double lenSq = getLengthSq(diff);
        if(view.areConnected(n, e) || lenSq < preferredLength * preferredLength) {
          if(lenSq < MIN_MOVEMENT) {
            needsMovement = true;
          } else {
            force = addVec(force, spring(diff));
          }
        }
      }
      Point2D move = velocity.get(n);
      if(needsMovement && force.getX() < MIN_MOVEMENT && force.getY() < MIN_MOVEMENT) {
        final double x = Math.random() - 0.5;
        final double y = Math.random() - 0.5;
        force = new Point2D.Double(x * preferredLength, y * preferredLength);
      }
      move = mulVec(addVec(move, force), curDecay);
      all = addVec(all, move);
      velocity.set(n, move);
    }
    all = mulVec(all, 1.0 / count);
    boolean chg = false;
    for(int n = 0; n < count; ++n) {
      final T node = view.getNode(n);
      final Point2D pos = node.getPos();
      final Point2D move = subVec(velocity.get(n), all);
      velocity.set(n, move);
      chg = setPosition(node, addVec(pos, move)) || chg;
    }
    curDecay *= 0.99;
    return chg;
  }

  /**
   * Computes the spring acceleration for the given difference.
   * 
   * @param diff The difference vector of two nodes.
   * @return The acceleration.
   */
  private Point2D spring(final Point2D diff) {
    final double len = getLength(diff) - preferredLength;
    return setLength(diff, -len * factor);
  }

}
