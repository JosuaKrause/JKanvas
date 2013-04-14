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
  private double factor = 0.4;
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

  public void setFactor(final double factor) {
    this.factor = factor;
  }

  public double getFactor() {
    return factor;
  }

  public void setDecay(final double decay) {
    this.decay = decay;
  }

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

  private final List<Point2D> velocity = new ArrayList<>();

  @Override
  public void deregister() {
    super.deregister();
    velocity.clear();
  }

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    boolean chg = false;
    final int count = view.nodeCount();
    while(velocity.size() < count) {
      velocity.add(new Point2D.Double());
    }
    for(int n = 0; n < count; ++n) {
      final T node = view.getNode(n);
      final Point2D pos = node.getPos();
      Point2D force = new Point2D.Double();
      for(int e = 0; e < count; ++e) {
        final T other = view.getNode(e);
        final Point2D diff = subVec(pos, other.getPos());
        final double lenSq = getLengthSq(diff);
        if(view.areConnected(n, e) || lenSq < preferredLength * preferredLength) {
          if(lenSq < 1e-9) {
            force = addVec(force, mulVec(
                new Point2D.Double(Math.random() - 0.5, Math.random() - 0.5), factor));
          } else {
            force = addVec(force, spring(diff));
          }
        }
      }
      Point2D move = velocity.get(n);
      move = mulVec(addVec(move, force), decay);
      velocity.set(n, move);
      chg = setPosition(node, addVec(pos, move)) || chg;
    }
    return chg;
  }

  private Point2D spring(final Point2D diff) {
    final double len = getLength(diff) - preferredLength;
    return setLength(diff, -len * factor);
  }

}
