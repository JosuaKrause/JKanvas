package jkanvas.nodelink.layout;

import static jkanvas.util.VecUtil.*;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
  /** The movement decay. */
  private double decay = 3;

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
    if(preferredLength < 0 || preferredLength * preferredLength <= MIN_MOVEMENT_SQ) throw new IllegalArgumentException(
        "Illegal preferred length: " + preferredLength);
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
   * @param decay The decay of movement. If the decay is <code>&lt;= 0</code>
   *          the movement is not gradually slowed down.
   */
  public void setDecay(final double decay) {
    this.decay = decay;
  }

  /**
   * Getter.
   * 
   * @return The decay of movement.If the decay is <code>&lt;= 0</code> the
   *         movement is not gradually slowed down.
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

  /** The current decay. */
  private double curDecay;

  /** The squared minimal movement. */
  public static double MIN_MOVEMENT_SQ = 1e-4;

  @Override
  public void layout(final boolean deregisterOnEnd) {
    curDecay = decay > 0 ? decay : 0;
    super.layout(deregisterOnEnd);
  }

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    final double pl = preferredLength;
    final double plSq = pl * pl;
    final int count = view.nodeCount();
    boolean chg = false;
    for(int n = 0; n < count; ++n) {
      boolean needsMovement = false;
      final T node = view.getNode(n);
      final Point2D pos = node.getPos();
      Point2D force = new Point2D.Double();
      for(int e = 0; e < count; ++e) {
        if(n == e) {
          continue;
        }
        final T other = view.getNode(e);
        final Point2D diff = subVec(pos, other.getPos());
        final double lenSq = getLengthSq(diff);
        if(lenSq < MIN_MOVEMENT_SQ) {
          needsMovement = true;
        } else {
          final double spring = view.areConnected(n, e) ? Math.sqrt(lenSq) / plSq : 0;
          final double rep = pl / lenSq;
          force = addVec(force, mulVec(diff, rep - spring));
        }
      }
      if(curDecay > 0) {
        force = mulVec(force, curDecay);
      }
      if(needsMovement && getLengthSq(force) < MIN_MOVEMENT_SQ) {
        final Random r = ThreadLocalRandom.current();
        final double x = r.nextGaussian();
        final double y = r.nextGaussian();
        force = new Point2D.Double(x * pl, y * pl);
      }
      chg = setPosition(node, addVec(pos, force)) || chg;
    }
    if(curDecay > 0) {
      curDecay *= 0.99;
    }
    return chg;
  }

}
