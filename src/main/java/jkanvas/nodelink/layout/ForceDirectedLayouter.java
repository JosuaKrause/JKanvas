package jkanvas.nodelink.layout;

import static jkanvas.util.VecUtil.*;

import java.awt.geom.Point2D;

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

  /** Creates a force directed layouter. */
  public ForceDirectedLayouter() {
    setTiming(AnimationTiming.FAST);
  }

  /** The preferred length of an edge. */
  private double preferredLength = 200;

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

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    boolean chg = false;
    final int count = view.nodeCount();
    final Point2D[] positions = new Point2D[count];
    for(int n = 0; n < count; ++n) {
      positions[n] = view.getNode(n).getPos();
    }
    for(int n = 0; n < count; ++n) {
      final Point2D pos = positions[n];
      Point2D force = new Point2D.Double();
      for(int e = 0; e < count; ++e) {
        if(view.areConnected(n, e)) {
          final Point2D f = calcImpulse(pos, positions[e]);
          force = addVec(force, f);
        } else {
          force = subVec(force, new Point2D.Double());
        }
      }
      force = mulVec(force, 1.0 / count);
      final T node = view.getNode(n);
      chg = setPosition(node, addVec(pos, force)) || chg;
    }
    return chg;
  }

  /**
   * Calculates the impulse created by another node.
   * 
   * @param from The node to be impacted.
   * @param to The node creating the impulse.
   * @return The impulse.
   */
  private Point2D calcImpulse(final Point2D from, final Point2D to) {
    final Point2D diff = subVec(to, from);
    final double len = getLength(diff);
    if(len < 1e-9) return new Point2D.Double();
    return setLength(diff, (preferredLength + len) * 0.5);
  }
}
