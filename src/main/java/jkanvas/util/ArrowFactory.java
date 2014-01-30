package jkanvas.util;

import static jkanvas.util.VecUtil.*;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Objects;

/**
 * A factory for creating arrow shapes.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ArrowFactory {

  /**
   * The type of an arrow tip.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public static interface TipType {

    /**
     * Adds the tip to the given path.
     * 
     * @param path The path to append the tip to.
     * @param pos The position of the tip.
     * @param dir The direction of the arrow at the tip. The length determines
     *          the width of the tip.
     */
    public abstract void drawTip(Path2D path, Point2D pos, Point2D dir);

  } // TipType

  /** No tip. */
  // TODO #43 -- Java 8 simplification
  public static final TipType NONE = new TipType() {

    @Override
    public void drawTip(final Path2D path, final Point2D pos, final Point2D dir) {
      // nothing to draw
    }

  };

  /** A flat line orthogonal to the direction of the arrow. */
  // TODO #43 -- Java 8 simplification
  public static final TipType FLAT = new TipType() {

    @Override
    public void drawTip(final Path2D path, final Point2D pos, final Point2D dir) {
      final Point2D side = mulVec(getOrthoRight(dir), 0.5);
      final Point2D start = addVec(pos, side);
      final Point2D end = subVec(pos, side);
      path.moveTo(start.getX(), start.getY());
      path.lineTo(end.getX(), end.getY());
    }

  };

  /** A non filled arrow head. */
  // TODO #43 -- Java 8 simplification
  public static final TipType ARROW_THIN = new TipType() {

    @Override
    public void drawTip(final Path2D path, final Point2D pos, final Point2D dir) {
      final Point2D side = mulVec(getOrthoRight(dir), 0.5);
      final Point2D left = subVec(addVec(pos, side), dir);
      final Point2D right = subVec(subVec(pos, side), dir);
      path.moveTo(pos.getX(), pos.getY());
      path.lineTo(left.getX(), left.getY());
      path.moveTo(pos.getX(), pos.getY());
      path.lineTo(right.getX(), right.getY());
    }

  };

  /** A filled arrow head. */
  // TODO #43 -- Java 8 simplification
  public static final TipType ARROW_FULL = new TipType() {

    @Override
    public void drawTip(final Path2D path, final Point2D pos, final Point2D dir) {
      final Point2D side = mulVec(getOrthoRight(dir), 0.5);
      final Point2D left = subVec(addVec(pos, side), dir);
      final Point2D right = subVec(subVec(pos, side), dir);
      path.moveTo(pos.getX(), pos.getY());
      path.lineTo(left.getX(), left.getY());
      path.lineTo(right.getX(), right.getY());
      path.closePath();
    }

  };

  /** A circle as head. */
  // TODO #43 -- Java 8 simplification
  public static final TipType CIRCLE = new TipType() {

    @Override
    public void drawTip(final Path2D path, final Point2D pos, final Point2D dir) {
      path.append(PaintUtil.createCircle(pos.getX(), pos.getY(), getLength(dir)), false);
    }

  };

  /** The head of the arrow. */
  private TipType head;

  /** The tail of the arrow. */
  private TipType tail;

  /** The width of the tips. */
  private double width;

  /**
   * Creates an arrow factory for normal arrows (ie with a thin arrow head and
   * no tail) and a width of <code>10</code>.
   */
  public ArrowFactory() {
    this(10.0);
  }

  /**
   * Creates an arrow factory for normal arrows (ie with a thin arrow head and
   * no tail).
   * 
   * @param width The width of the tips.
   */
  public ArrowFactory(final double width) {
    this(ARROW_THIN, NONE, width);
  }

  /**
   * Creates an arrow factory.
   * 
   * @param head The head type.
   * @param tail The tail type.
   * @param width The width of the tips.
   */
  public ArrowFactory(final TipType head, final TipType tail, final double width) {
    this.head = Objects.requireNonNull(head);
    this.tail = Objects.requireNonNull(tail);
    this.width = width;
  }

  /**
   * Setter.
   * 
   * @param head The head type.
   */
  public void setHead(final TipType head) {
    this.head = Objects.requireNonNull(head);
  }

  /**
   * Getter.
   * 
   * @return The head type of the arrow.
   */
  public TipType getHead() {
    return head;
  }

  /**
   * Setter.
   * 
   * @param tail The tail type.
   */
  public void setTail(final TipType tail) {
    this.tail = Objects.requireNonNull(tail);
  }

  /**
   * Getter.
   * 
   * @return The tail type of the arrow.
   */
  public TipType getTail() {
    return tail;
  }

  /**
   * Setter.
   * 
   * @param width The width of the tips.
   */
  public void setWidth(final double width) {
    this.width = width;
  }

  /**
   * Getter.
   * 
   * @return The width of the tips.
   */
  public double getWidth() {
    return width;
  }

  /**
   * Creates a straight arrow.
   * 
   * @param start The start point.
   * @param end The end point.
   * @return The arrow shape.
   */
  public Shape createArrow(final Point2D start, final Point2D end) {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    return createArrow(start, end, null, null);
  }

  /**
   * Creates a curved arrow.
   * 
   * @param start The start point.
   * @param end The end point.
   * @param bend The bend of the arrow in the right direction as seen from the
   *          arrows perspective.
   * @return The arrow shape.
   */
  public Shape createArrow(final Point2D start, final Point2D end, final double bend) {
    if(bend == 0) return createArrow(start, end);
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    final Point2D mid = mulVec(subVec(end, start), 0.5);
    final Point2D right = setLength(getOrthoLeft(mid), bend);
    final Point2D bendPos = addVec(addVec(start, mid), right);
    return createArrow(start, end, bendPos, null);
  }

  /**
   * Creates a curved arrow.
   * 
   * @param start The start point.
   * @param end The end point.
   * @param bend The control point for bending.
   * @return The arrow shape.
   */
  public Shape createArrow(final Point2D start, final Point2D end, final Point2D bend) {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    Objects.requireNonNull(bend);
    return createArrow(start, end, bend, null);
  }

  /**
   * Creates a curved arrow.
   * 
   * @param start The start point.
   * @param end The end point.
   * @param bendFirst The first control point.
   * @param bendSecond The second control point.
   * @return The arrow shape.
   */
  public Shape createCurvedArrow(final Point2D start, final Point2D end,
      final Point2D bendFirst, final Point2D bendSecond) {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    Objects.requireNonNull(bendFirst);
    Objects.requireNonNull(bendSecond);
    return createArrow(start, end, bendFirst, bendSecond);
  }

  /**
   * Creates a curved arrow.
   * 
   * @param start The start point.
   * @param end The end point.
   * @param bendFirst The first control point.
   * @param bendSecond The second control point.
   * @return The arrow shape.
   */
  private Shape createArrow(final Point2D start, final Point2D end,
      final Point2D bendFirst, final Point2D bendSecond) {
    final Path2D path = new Path2D.Double();
    Point2D dirHead;
    Point2D dirTail;
    path.moveTo(start.getX(), start.getY());
    if(bendFirst == null) {
      path.lineTo(end.getX(), end.getY());
      dirHead = subVec(end, start);
      dirTail = subVec(start, end);
    } else if(bendSecond == null) {
      path.quadTo(bendFirst.getX(), bendFirst.getY(), end.getX(), end.getY());
      // directions of a quad curve at the end points
      // is the same as the directions to the control point
      dirHead = subVec(end, bendFirst);
      dirTail = subVec(bendFirst, start);
    } else {
      path.curveTo(bendFirst.getX(), bendFirst.getY(),
          bendSecond.getX(), bendSecond.getY(), end.getX(), end.getY());
      // directions of a bezier curve at the end points
      // is the same as the directions to the next control point
      dirHead = subVec(end, bendSecond);
      dirTail = subVec(bendFirst, start);
    }
    final Point2D ndirHead = setLength(dirHead, width);
    final Point2D ndirTail = setLength(dirTail, width);
    head.drawTip(path, end, ndirHead);
    tail.drawTip(path, start, ndirTail);
    return path;
  }

}
