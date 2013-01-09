package jkanvas.painter;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws and measures a string.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class StringDrawer {

  /**
   * Orientations to draw a string.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  public static enum Orientation {

    /** Horizontal text. */
    HORIZONTAL,

    /** Text going from bottom left to upper right with 45 degrees. */
    DIAGONAL,

    /**
     * Text rotated by 90 degrees counter-clockwise. Reading from bottom to top.
     */
    VERTICAL,

  }

  /** The point marks the left side of the text. */
  public static final int LEFT = 0;

  /** The point marks the horizontal center of the text. */
  public static final int CENTER_H = 1;

  /** The point marks the right side of the text. */
  public static final int RIGHT = 2;

  /** The point marks the top of the text. */
  public static final int TOP = 3;

  /** The point marks the vertical center of the text. */
  public static final int CENTER_V = 4;

  /** The point marks the bottom of the text. */
  public static final int BOTTOM = 5;

  /** Rotating by 90 degrees counter-clockwise. */
  private static final double ROT_V = -Math.PI / 2;

  /** Rotating by 45 degrees counter-clockwise. */
  private static final double ROT_D = -Math.PI / 4;

  /** The graphics context. */
  private final Graphics2D g;

  /** The string to draw. */
  private final String str;

  /** The bounding box of the string. */
  private final Rectangle2D bbox;

  /**
   * Creates a string drawer.
   * 
   * @param g The graphics context.
   * @param str The string.
   */
  public StringDrawer(final Graphics2D g, final String str) {
    this.g = g;
    this.str = str;
    final FontMetrics fm = g.getFontMetrics();
    bbox = fm.getStringBounds(str, g);
  }

  /**
   * Computes the bounds of the text.
   * 
   * @param pos The position of the text.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @param o The orientation of the text.
   * @return The bounds.
   */
  public Shape getBounds(final Point2D pos,
      final int hpos, final int vpos, final Orientation o) {
    Shape res;
    switch(o) {
      case HORIZONTAL:
        res = getBounds(pos, hpos, vpos);
        break;
      case DIAGONAL:
        res = getRotatedBounds(pos, hpos, vpos, ROT_D);
        break;
      case VERTICAL:
        res = getRotatedBounds(pos, hpos, vpos, ROT_V);
        break;
      default:
        throw new AssertionError();
    }
    return res;
  }

  /**
   * Computes the bounds of the text assuming horizontal orientation.
   * 
   * @param pos The position.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @return The bounds.
   */
  public Rectangle2D getBounds(final Point2D pos, final int hpos, final int vpos) {
    return new Rectangle2D.Double(pos.getX() + getHorizontalOffset(hpos) + bbox.getX(),
        pos.getY() + getVerticalOffset(vpos, true) + bbox.getY(),
        bbox.getWidth(), bbox.getHeight());
  }

  /**
   * Computes the bounds of rotated text.
   * 
   * @param pos The position of the text.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @param theta The clockwise rotation.
   * @return The bounds.
   */
  public Shape getRotatedBounds(final Point2D pos,
      final int hpos, final int vpos, final double theta) {
    final AffineTransform at = AffineTransform.getTranslateInstance(pos.getX(),
        pos.getY());
    at.rotate(theta);
    at.translate(getHorizontalOffset(hpos), getVerticalOffset(vpos, true));
    return at.createTransformedShape(bbox);
  }

  /**
   * Draws the text.
   * 
   * @param pos The position of the text.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @param o The orientation of the text.
   */
  public void draw(final Point2D pos, final int hpos, final int vpos, final Orientation o) {
    switch(o) {
      case HORIZONTAL:
        draw(pos, hpos, vpos);
        break;
      case DIAGONAL:
        drawRotated(pos, hpos, vpos, ROT_D);
        break;
      case VERTICAL:
        drawRotated(pos, hpos, vpos, ROT_V);
        break;
    }
  }

  /**
   * Draws the text assuming horizontal orientation.
   * 
   * @param pos The position.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   */
  public void draw(final Point2D pos, final int hpos, final int vpos) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(pos.getX() + getHorizontalOffset(hpos),
        pos.getY() - bbox.getHeight() + getVerticalOffset(vpos, false));
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  /**
   * Draws rotated text.
   * 
   * @param pos The position of the text.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @param theta The clockwise rotation.
   */
  public void drawRotated(final Point2D pos,
      final int hpos, final int vpos, final double theta) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(pos.getX(), pos.getY());
    g2.rotate(theta);
    g2.translate(getHorizontalOffset(hpos),
        getVerticalOffset(vpos, false) - bbox.getHeight());
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  /**
   * Computes the horizontal offset of the text.
   * 
   * @param hpos The horizontal alignment.
   * @return The offset.
   */
  private double getHorizontalOffset(final int hpos) {
    double dx;
    switch(hpos) {
      case LEFT:
        dx = 0;
        break;
      case CENTER_H:
        dx = -bbox.getWidth() * 0.5;
        break;
      case RIGHT:
        dx = -bbox.getWidth();
        break;
      default:
        throw new IllegalArgumentException("hpos: " + hpos);
    }
    return dx - bbox.getX();
  }

  /**
   * Computes the vertical offset of the text.
   * 
   * @param vpos The vertical alignment.
   * @param isBBox Whether the offset is used to compute the bounding box.
   * @return The offset.
   */
  private double getVerticalOffset(final int vpos, final boolean isBBox) {
    double dy;
    switch(vpos) {
      case BOTTOM:
        dy = 0;
        break;
      case CENTER_V:
        dy = bbox.getHeight() * 0.5;
        break;
      case TOP:
        dy = bbox.getHeight();
        break;
      default:
        throw new IllegalArgumentException("vpos: " + vpos);
    }
    return dy - bbox.getY() - (isBBox ? bbox.getHeight() : 0);
  }

}
