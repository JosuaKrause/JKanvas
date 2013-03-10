package jkanvas.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A small vector utility class.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class VecUtil {

  /** No constructor. */
  private VecUtil() {
    throw new AssertionError();
  }

  /**
   * Adds two points.
   * 
   * @param a Point.
   * @param b Point.
   * @return The sum vector.
   */
  public static Point2D addVec(final Point2D a, final Point2D b) {
    return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
  }

  /**
   * Subtracts two points. <code>a - b</code>
   * 
   * @param a Point.
   * @param b Point.
   * @return The difference vector.
   */
  public static Point2D subVec(final Point2D a, final Point2D b) {
    return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
  }

  /**
   * Multiplies a point with a scalar.
   * 
   * @param v Point.
   * @param s Scalar.
   * @return The scaled vector.
   */
  public static Point2D mulVec(final Point2D v, final double s) {
    return new Point2D.Double(v.getX() * s, v.getY() * s);
  }

  /**
   * Linearly interpolates between two points.
   * 
   * @param from The starting point.
   * @param to The ending point.
   * @param t The interpolation progress starting at 0 and going to 1.
   * @return The interpolated point.
   */
  public static Point2D interpolate(final Point2D from, final Point2D to, final double t) {
    return new Point2D.Double(from.getX() * (1 - t) + to.getX() * t,
        from.getY() * (1 - t) + to.getY() * t);
  }

  /**
   * Generates a vector that forms an angle of {@code 90} degrees with the given
   * vector.
   * 
   * @param a The given vector.
   * @return The orthogonal vector of the left side.
   */
  public static Point2D getOrthoLeft(final Point2D a) {
    return new Point2D.Double(-a.getY(), a.getX());
  }

  /**
   * Generates a vector that forms an angle of {@code -90} degrees with the
   * given vector.
   * 
   * @param a The given vector.
   * @return The orthogonal vector of the right side.
   */
  public static Point2D getOrthoRight(final Point2D a) {
    return new Point2D.Double(a.getY(), -a.getX());
  }

  /**
   * Generates a vector pointing in the opposite direction.
   * 
   * @param v The vector.
   * @return The inverse vector.
   */
  public static Point2D invVec(final Point2D v) {
    return mulVec(v, -1.0);
  }

  /**
   * Calculates the squared length of a vector.
   * 
   * @param v The vector.
   * @return The squared length.
   */
  public static double getLengthSq(final Point2D v) {
    return v.getX() * v.getX() + v.getY() * v.getY();
  }

  /**
   * Calculates the length of a vector.
   * 
   * @param v The vector.
   * @return The length.
   */
  public static double getLength(final Point2D v) {
    return Math.sqrt(getLengthSq(v));
  }

  /**
   * Sets the length of a vector.
   * 
   * @param v The vector.
   * @param l The new length.
   * @return The vector with the given length.
   */
  public static Point2D setLength(final Point2D v, final double l) {
    return mulVec(v, l / getLength(v));
  }

  /**
   * Calculates the absolute orientation of a given vector.
   * 
   * @param vec The vector.
   * @return The angle from this vector to the x-axis in counter-clockwise
   *         order. The range is from {@code 0.0 - 2.0 * Math.PI}. The vector
   *         (0, 0) results in an angle of <code>0</code>.
   */
  public static double getOrientation(final Point2D vec) {
    final double x = vec.getX();
    final double y = vec.getY();
    if(x == 0.0) return Math.PI * (y > 0.0 ? 0.5 : 1.5);
    return (x < 0 ? Math.PI : (y < 0 ? 2 * Math.PI : 0)) + Math.atan(y / x);
  }

  /**
   * Returns the difference of both orientations.
   * 
   * @param o1 The first orientation which ranges from
   *          {@code 0.0 - 2.0 * Math.PI}.
   * @param o2 The second orientation which ranges from
   *          {@code 0.0 - 2.0 * Math.PI}.
   * @return The absolute difference between those orientations which ranges
   *         from {@code 0.0 - Math.PI}.
   */
  public static double getOrientationDifference(final double o1, final double o2) {
    final double diff = Math.abs(o1 - o2);
    return diff > Math.PI ? 2.0 * Math.PI - diff : diff;
  }

  /**
   * Returns the difference of the orientations of the given vectors.
   * 
   * @param v1 The first vector.
   * @param v2 The second vector.
   * @return The absolute difference between the orientations of the vectors
   *         which ranges from {@code 0.0 - Math.PI}.
   */
  public static double getOrientationDifference(final Point2D v1, final Point2D v2) {
    return getOrientationDifference(getOrientation(v1), getOrientation(v2));
  }

  /**
   * Rotates a vector around the origin. The angle is measured in radians and
   * positive angles are counter-clockwise.
   * 
   * @param vec The vector to rotate.
   * @param theta The angle in radians.
   * @return The rotated vector.
   */
  public static Point2D rotate(final Point2D vec, final double theta) {
    final double cos = Math.cos(theta);
    final double sin = Math.sin(theta);
    final double x = vec.getX();
    final double y = vec.getY();
    return new Point2D.Double(x * cos + y * sin, -x * sin + y * cos);
  }

  /**
   * Rotates a point around the center such that the result has the given
   * distance to the original point.
   * 
   * @param pos The point to rotate.
   * @param center The center.
   * @param dist The distance. Positive values rotate in clockwise direction.
   * @return The point that has the given distance to the original point.
   * @throws IllegalArgumentException When the distance is longer than the
   *           diameter.
   */
  public static Point2D rotate(final Point2D pos, final Point2D center, final double dist) {
    final double f = dist > 0 ? 1 : -1;
    final double dSq = dist * dist;
    final Point2D rad = subVec(pos, center);
    final double radSq = getLengthSq(rad);
    if(dSq > 4 * radSq) return subVec(center, rad);
    return rotateByAngle(pos, center, f * Math.acos(1 - dSq * 0.5 / radSq));
  }

  /**
   * Rotates a point a given angle around the center.
   * 
   * @param pos The point to rotate.
   * @param center The center.
   * @param angle The angle.
   * @return The rotated point.
   */
  public static Point2D rotateByAngle(
      final Point2D pos, final Point2D center, final double angle) {
    final AffineTransform at = AffineTransform.getRotateInstance(
        angle, center.getX(), center.getY());
    return at.transform(pos, null);
  }

  /**
   * Whether point a must be rotated clockwise around the center point to align
   * with point b.
   * 
   * @param center The center of rotation.
   * @param a Point a.
   * @param b Point b.
   * @return Whether the rotation must be clockwise.
   */
  public static boolean isClockwiseOf(final Point2D center, final Point2D a,
      final Point2D b) {
    return Line2D.relativeCCW(center.getX(), center.getY(),
        a.getX(), a.getY(), b.getX(), b.getY()) < 0;
  }

  /**
   * Whether the given line contains NaNs.
   * 
   * @param line The line to check.
   * @return Whether the line contains NaNs.
   */
  public static boolean containsNaN(final Line2D line) {
    return Double.isNaN(line.getX1()) || Double.isNaN(line.getX2())
        || Double.isNaN(line.getY1()) || Double.isNaN(line.getY2());
  }

  /** The mean radius of the earth in meters. */
  public static final double RADIUS_EARTH_MEAN = 6371009;

  /**
   * Computes an approximation of the distance of two points on the earth in
   * meters using the mean radius of the earth.
   * 
   * @param latA The latitude of point A in degrees.
   * @param lonA The longitude of point A in degrees.
   * @param latB The latitude of point B in degrees.
   * @param lonB The longitude of point B in degrees.
   * @return The distance of the two points in meters.
   */
  public static double earthDistance(final double latA, final double lonA,
      final double latB, final double lonB) {
    // mean radius of the earth
    return sphereDistance(latA, lonA, latB, lonB, RADIUS_EARTH_MEAN);
  }

  /**
   * Computes the distance of two points on a sphere.
   * 
   * @param latA The latitude of point A in degrees.
   * @param lonA The longitude of point A in degrees.
   * @param latB The latitude of point B in degrees.
   * @param lonB The longitude of point B in degrees.
   * @param radius The radius of the sphere.
   * @return The distance.
   */
  public static double sphereDistance(final double latA, final double lonA,
      final double latB, final double lonB, final double radius) {
    final double dLat = Math.toRadians(latB - latA) * .5;
    final double dLon = Math.toRadians(lonB - lonA) * .5;
    final double rLatA = Math.toRadians(latA);
    final double rLatB = Math.toRadians(latB);
    final double a = Math.sin(dLat) * Math.sin(dLat) +
        Math.sin(dLon) * Math.sin(dLon) * Math.cos(rLatA) * Math.cos(rLatB);
    final double c = Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return 2 * radius * c;
  }

}
