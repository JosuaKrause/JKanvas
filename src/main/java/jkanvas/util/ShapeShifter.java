package jkanvas.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * A wrapper for multiple shapes. An implementation should not create new shapes
 * when setting the active shape via {@link #setActiveShape(Shape)}. Possible
 * active shapes should be created once.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class ShapeShifter implements Shape {

  /** A shape that fails when calling any shape method. */
  private static final Shape FAIL = new Shape() {

    /**
     * Fails always.
     * 
     * @return Any type.
     * @throws IllegalStateException Always throws a runtime exception.
     */
    private <T> T fail() {
      throw new IllegalStateException("must set active shape first");
    }

    @Override
    public boolean intersects(
        final double x, final double y, final double w, final double h) {
      return fail();
    }

    @Override
    public boolean intersects(final Rectangle2D r) {
      return fail();
    }

    @Override
    public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
      return fail();
    }

    @Override
    public PathIterator getPathIterator(final AffineTransform at) {
      return fail();
    }

    @Override
    public Rectangle2D getBounds2D() {
      return fail();
    }

    @Override
    public Rectangle getBounds() {
      return fail();
    }

    @Override
    public boolean contains(final double x, final double y, final double w, final double h) {
      return fail();
    }

    @Override
    public boolean contains(final double x, final double y) {
      return fail();
    }

    @Override
    public boolean contains(final Rectangle2D r) {
      return fail();
    }

    @Override
    public boolean contains(final Point2D p) {
      return fail();
    }

  }; // FAIL

  /** The currently active shape. */
  private Shape active;

  /** Creates a shape shifter. */
  public ShapeShifter() {
    active = FAIL;
  }

  /**
   * Setter.
   * 
   * @param active Sets the active shape. This method must be called at least
   *          once before using the object as shape.
   */
  protected void setActiveShape(final Shape active) {
    this.active = Objects.requireNonNull(active);
  }

  /** Makes this shape invalid. */
  protected void makeInvalid() {
    active = FAIL;
  }

  @Override
  public Rectangle getBounds() {
    return active.getBounds();
  }

  @Override
  public Rectangle2D getBounds2D() {
    return active.getBounds2D();
  }

  @Override
  public boolean contains(final double x, final double y) {
    return active.contains(x, y);
  }

  @Override
  public boolean contains(final Point2D p) {
    return active.contains(p);
  }

  @Override
  public boolean intersects(final double x, final double y, final double w, final double h) {
    return active.intersects(x, y, w, h);
  }

  @Override
  public boolean intersects(final Rectangle2D r) {
    return active.intersects(r);
  }

  @Override
  public boolean contains(final double x, final double y, final double w, final double h) {
    return active.contains(x, y, w, h);
  }

  @Override
  public boolean contains(final Rectangle2D r) {
    return active.contains(r);
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at) {
    return active.getPathIterator(at);
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
    return active.getPathIterator(at, flatness);
  }

}
