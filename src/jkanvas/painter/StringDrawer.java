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

  public static enum Orientation {

    HORIZONTAL,

    DIAGONAL,

    VERTICAL,

  }

  public static final int LEFT = 0;

  public static final int CENTER_H = 1;

  public static final int RIGHT = 2;

  public static final int TOP = 3;

  public static final int CENTER_V = 4;

  public static final int BOTTOM = 5;

  private static final double ROT_V = -Math.PI / 2;

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

  private Rectangle2D getBounds(final double dx, final double dy, final int hpos,
      final int vpos) {
    return new Rectangle2D.Double(dx + getHorizontalOffset(hpos) + bbox.getX(),
        dy + getVerticalOffset(vpos, true) + bbox.getY(),
        bbox.getWidth(), bbox.getHeight());
  }

  private Shape getBoundsVertical(final double dx, final double dy, final int hpos,
      final int vpos) {
    final AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
    at.rotate(ROT_V);
    at.translate(getHorizontalOffset(hpos),
        getVerticalOffset(vpos, true));
    return at.createTransformedShape(bbox);
  }

  private Shape getBoundsDiagonal(final double dx, final double dy, final int hpos,
      final int vpos) {
    final AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
    at.rotate(ROT_D);
    at.translate(getHorizontalOffset(hpos), getVerticalOffset(vpos, true));
    return at.createTransformedShape(bbox);
  }

  public Shape getBounds(final Point2D pos, final int hpos, final int vpos) {
    return getBounds(pos, hpos, vpos, Orientation.HORIZONTAL);
  }

  public Shape getBounds(final Point2D pos,
      final int hpos, final int vpos, final Orientation o) {
    Shape res;
    switch(o) {
      case HORIZONTAL:
        res = getBounds(pos.getX(), pos.getY(), hpos, vpos);
        break;
      case DIAGONAL:
        res = getBoundsDiagonal(pos.getX(), pos.getY(), hpos, vpos);
        break;
      case VERTICAL:
        res = getBoundsVertical(pos.getX(), pos.getY(), hpos, vpos);
        break;
      default:
        throw new AssertionError();
    }
    return res;
  }

  /**
   * Draws the string at the given position.
   * 
   * @param dx The x position.
   * @param dy The y position.
   * @param vpos
   * @param hpos
   */
  private void draw(final double dx, final double dy, final int hpos, final int vpos) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(dx + getHorizontalOffset(hpos),
        dy - bbox.getHeight() + getVerticalOffset(vpos, false));
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  private void drawVertical(final double dx, final double dy, final int hpos,
      final int vpos) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(dx, dy);
    g2.rotate(ROT_V);
    g2.translate(getHorizontalOffset(hpos),
        getVerticalOffset(vpos, false) - bbox.getHeight());
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  private void drawDiagonal(final double dx, final double dy, final int hpos,
      final int vpos) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(dx, dy);
    g2.rotate(ROT_D);
    g2.translate(getHorizontalOffset(hpos),
        getVerticalOffset(vpos, false) - bbox.getHeight());
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  public void draw(final Point2D pos, final int hpos, final int vpos) {
    draw(pos, hpos, vpos, Orientation.HORIZONTAL);
  }

  public void draw(final Point2D pos, final int hpos, final int vpos, final Orientation o) {
    switch(o) {
      case HORIZONTAL:
        draw(pos.getX(), pos.getY(), hpos, vpos);
        break;
      case DIAGONAL:
        drawDiagonal(pos.getX(), pos.getY(), hpos, vpos);
        break;
      case VERTICAL:
        drawVertical(pos.getX(), pos.getY(), hpos, vpos);
        break;
    }
  }

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
