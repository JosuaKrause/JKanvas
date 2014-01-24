package jkanvas.optional;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.BitSet;

import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.NodeLinkView;
import jkanvas.nodelink.layout.AbstractLayouter;
import jkanvas.util.ArrayUtil;
import jkanvas.util.BitSetIterable;

/**
 * Computes a node-link layout based on MDS. The use of this class is optional
 * and only possible when <code>mdsj.MDSJ</code> is in the class-path. See
 * {@link MDSProjector} for more details.
 * 
 * @see MDSProjector
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node type.
 */
public class MDSLayouter<T extends AnimatedPosition> extends AbstractLayouter<T> {

  /** The margin. */
  private final double margin;

  /**
   * Creates a MDS layouter.
   * 
   * @param margin The margin.
   */
  public MDSLayouter(final double margin) {
    this.margin = margin;
  }

  @Override
  protected boolean doLayout(final NodeLinkView<T> view) {
    final int count = view.nodeCount();
    final double[][] dist = new double[count][count];
    final double maxDistance = computeDistances(view, dist);
    final double[][] xy = MDSProjector.getScaling(dist, maxDistance + 1.0);
    final double[] x = xy[0];
    final double[] y = xy[1];
    final double minX = ArrayUtil.min(x);
    final double maxX = ArrayUtil.max(x);
    final double minY = ArrayUtil.min(y);
    final double maxY = ArrayUtil.max(y);
    final Rectangle2D rect = new Rectangle2D.Double();
    getBoundingBox(rect);
    final double ox = rect.getX() + margin;
    final double oy = rect.getY() + margin;
    final double w = rect.getWidth() - 2 * margin;
    final double h = rect.getHeight() - 2 * margin;
    boolean chg = false;
    final Point2D pos = new Point2D.Double();
    for(int i = 0; i < count; ++i) {
      pos.setLocation((x[i] - minX) / (maxX - minX) * w + ox,
          (y[i] - minY) / (maxY - minY) * h + oy);
      chg = setPosition(view.getNode(i), pos) || chg;
    }
    return chg;
  }

  /**
   * Computes the distance matrix for the graph.
   * 
   * @param view The graph view.
   * @param dist The result distance matrix.
   * @return The maximum distance.
   */
  private double computeDistances(final NodeLinkView<T> view, final double[][] dist) {
    for(int i = 0; i < dist.length; ++i) {
      Arrays.fill(dist[i], Double.POSITIVE_INFINITY);
    }
    final BitSet visited = new BitSet();
    final BitSet nextRound = new BitSet();
    final BitSet currentRound = new BitSet();
    final BitSetIterable curItr = new BitSetIterable(currentRound);
    double maxDistance = 0.0;
    for(int i = 0; i < dist.length; ++i) {
      visited.clear();
      currentRound.clear();
      currentRound.set(i);
      double distance = 0.0;
      while(visited.cardinality() < dist.length && !currentRound.isEmpty()) {
        nextRound.clear();
        for(final int n : curItr) {
          visited.set(n);
          if(dist[i][n] > distance) {
            dist[i][n] = distance;
          }
          // we must check all neighbors -- not only those with higher index
          for(int c = 0; c < dist.length; ++c) {
            if(c == n || !view.areConnected(n, c)) {
              continue;
            }
            if(visited.get(c) || currentRound.get(c)) {
              continue;
            }
            nextRound.set(c);
          }
        }
        distance += 1.0;
        if(distance > maxDistance) {
          maxDistance = distance;
        }
        currentRound.clear();
        currentRound.or(nextRound);
      }
    }
    return maxDistance;
  }

}
