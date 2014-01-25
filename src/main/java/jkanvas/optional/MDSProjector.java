package jkanvas.optional;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides a method to compute the MDS projection for a distance matrix. This
 * class depends on <code>mdsj.MDSJ</code> which must be included in the
 * class-path. The original page for MDSJ is located <a
 * href="http://www.inf.uni-konstanz.de/algo/software/mdsj/">here</a>.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class MDSProjector {

  /** No constructor. */
  private MDSProjector() {
    throw new AssertionError();
  }

  /** The MDSJ class. */
  private static Class<?> MDSJ;
  /** The actual projection method. */
  private static Method METHOD;
  /** Any exception during class loading. */
  private static Exception EXCEPTION;

  /** Loads the MDSJ class. */
  private static void loadMDSJ() {
    if((MDSJ != null && METHOD != null) || EXCEPTION != null) return;
    EXCEPTION = null;
    try {
      MDSJ = Class.forName("mdsj.MDSJ");
      METHOD = MDSJ.getMethod("classicalScaling", double[][].class);
    } catch(ClassNotFoundException | NoSuchMethodException | SecurityException e) {
      EXCEPTION = e;
      MDSJ = null;
      METHOD = null;
    }
  }

  /**
   * Getter.
   * 
   * @return Whether this class can be used.
   */
  public static boolean hasMDSJ() {
    loadMDSJ();
    return MDSJ != null && METHOD != null;
  }

  /** An exception is thrown when MDSJ could not be loaded. */
  public static void expectMDSJ() {
    loadMDSJ();
    if(MDSJ != null && METHOD != null) return;
    if(EXCEPTION == null) throw new IllegalStateException("could not load MDSJ");
    throw new IllegalStateException("error loading MDSJ", EXCEPTION);
  }

  /**
   * Computes the actual scaling.
   * 
   * @param m The input distance matrix.
   * @return The projection.
   */
  private static double[][] doScale(final double[][] m) {
    expectMDSJ();
    final Method scale = METHOD;
    try {
      // we must cast to Object because of vararg ambiguity otherwise
      return (double[][]) scale.invoke(null, (Object) m);
    } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException("error invoking MDSJ", e);
    }
  }

  /**
   * Canonicalizes the output of the MDS function.
   * 
   * @param mdsOutput The MDS output to manipulate.
   */
  private static void canonicalize(final double[][] mdsOutput) {
    final double xs[] = mdsOutput[0];
    final double ys[] = mdsOutput[1];
    double meanX = 0.0;
    double meanY = 0.0;
    for(int i = 0; i < xs.length; ++i) {
      if(!Double.isNaN(xs[i])) {
        meanX += xs[i];
      }
      if(!Double.isNaN(ys[i])) {
        meanY += ys[i];
      }
    }
    meanX /= xs.length;
    meanY /= ys.length;
    final Point2D mean = new Point2D.Double(meanX, meanY);
    double outwardMax = Double.NaN;
    final Point2D outward = new Point2D.Double();
    for(int i = 0; i < xs.length; ++i) {
      final double dist = mean.distanceSq(xs[i], ys[i]);
      if(Double.isNaN(outwardMax) || dist > outwardMax) {
        outward.setLocation(xs[i], ys[i]);
        outwardMax = dist;
      }
    }
    double farMax = Double.NaN;
    final Point2D far = new Point2D.Double();
    for(int i = 0; i < xs.length; ++i) {
      final double dist = outward.distanceSq(xs[i], ys[i]);
      if(Double.isNaN(farMax) || dist > farMax) {
        far.setLocation(xs[i], ys[i]);
        farMax = dist;
      }
    }
    // cannot find suitable points
    if(Double.isNaN(farMax) || Double.isNaN(outwardMax)) return;
    final AffineTransform rot = AffineTransform.getRotateInstance(
        far.getX() - outward.getX(), far.getY() - outward.getY(),
        outward.getX(), outward.getX());
    final Point2D dummy = new Point2D.Double();
    for(int i = 0; i < xs.length; ++i) {
      dummy.setLocation(xs[i], ys[i]);
      rot.transform(dummy, dummy);
      xs[i] = dummy.getX();
      ys[i] = dummy.getY();
    }
    rot.transform(outward, outward);
    rot.transform(far, far);
    final Line2D axis = new Line2D.Double(outward, far);
    boolean onTop = true;
    for(int i = 0; i < xs.length; ++i) {
      final int ccw = axis.relativeCCW(xs[i], ys[i]);
      if(ccw != 0) {
        onTop = ccw > 0;
        break;
      }
    }
    final AffineTransform aff = new AffineTransform();
    if(!onTop) {
      aff.scale(1, -1);
    }
    if(aff.isIdentity()) return;
    for(int i = 0; i < xs.length; ++i) {
      dummy.setLocation(xs[i], ys[i]);
      aff.transform(dummy, dummy);
      xs[i] = dummy.getX();
      ys[i] = dummy.getY();
    }
  }

  /**
   * Getter.
   * 
   * @param m The distance matrix.
   * @param maxDistance Restricts the maximal distance of two features.
   * @return Computes a projection for the distances.
   */
  public static double[][] getScaling(final double[][] m, final double maxDistance) {
    return getScaling(m, maxDistance, true);
  }

  /**
   * Getter.
   * 
   * @param m The distance matrix.
   * @param maxDistance Restricts the maximal distance of two features.
   * @param canonicalize Whether to canonicalize the resulting projection. The
   *          projection gets rotated and scaled in order to give consistent
   *          results.
   * @return Computes a projection for the distances.
   */
  public static double[][] getScaling(
      final double[][] m, final double maxDistance, final boolean canonicalize) {
    for(int r = 0; r < m.length; ++r) {
      for(int c = 0; c < m[r].length; ++c) {
        if(m[r][c] > maxDistance) {
          m[r][c] = maxDistance;
        }
      }
    }
    final double res[][] = doScale(m);
    if(canonicalize) {
      canonicalize(res);
    }
    return res;
  }

}
