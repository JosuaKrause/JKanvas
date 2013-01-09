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

  private Rectangle2D getBounds(final double dx, final double dy) {
    return new Rectangle2D.Double(bbox.getX() + dx, bbox.getY() + dy,
        bbox.getWidth(), bbox.getHeight());
  }

  private Shape getBoundsVertical(final double dx, final double dy) {
    final AffineTransform at = AffineTransform.getTranslateInstance(
        dx + bbox.getX(), dy+ bbox.getY());
    at.rotate(ROT_V);
    at.translate(-bbox.getX(), -bbox.getY());
    return at.createTransformedShape(bbox);
  }

  private Shape getBoundsDiagonal(final double dx, final double dy) {
    final AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
    at.concatenate(AffineTransform.getRotateInstance(ROT_D));
    return at.createTransformedShape(bbox);
  }

  public Shape getBounds(final Point2D pos, final int hpos, final int vpos) {
    return getBounds(pos, hpos, vpos, Orientation.HORIZONTAL);
  }

  public Shape getBounds(final Point2D p,
      final int hpos, final int vpos, final Orientation o) {
    double dx;
    switch(hpos) {
      case LEFT:
        dx = p.getX() - bbox.getX();
        break;
      case CENTER_H:
        dx = p.getX() - bbox.getX() - bbox.getWidth() * 0.5;
        break;
      case RIGHT:
        dx = p.getX() - bbox.getX() - bbox.getWidth();
        break;
      default:
        throw new IllegalArgumentException("hpos: " + hpos);
    }
    double dy;
    switch(vpos) {
      case TOP:
        dy = p.getY() - bbox.getY();
        break;
      case CENTER_V:
        dy = p.getY() - bbox.getY() - bbox.getHeight() * 0.5;
        break;
      case BOTTOM:
        dy = p.getY() - bbox.getY() - bbox.getHeight();
        break;
      default:
        throw new IllegalArgumentException("vpos: " + vpos);
    }
    Shape res;
    switch(o) {
      case HORIZONTAL:
        res = getBounds(dx, dy);
        break;
      case DIAGONAL:
        res = getBoundsDiagonal(dx, dy);
        break;
      case VERTICAL:
        res = getBoundsVertical(dx, dy);
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
   */
  private void draw(final double dx, final double dy) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(dx - bbox.getX(), dy - bbox.getY() - bbox.getHeight());
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  private void drawVertical(final double dx, final double dy) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(dx, dy - bbox.getHeight());
    g2.rotate(ROT_V);
    g2.translate(-bbox.getX(), -bbox.getY());
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  private void drawDiagonal(final double dx, final double dy) {
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(dx, dy);
    g2.rotate(ROT_D);
    g2.translate(-bbox.getX(), -bbox.getY());
    g2.drawString(str, 0, 0);
    g2.dispose();
  }

  public void draw(final Point2D pos, final int hpos, final int vpos) {
    draw(pos, hpos, vpos, Orientation.HORIZONTAL);
  }

  public void draw(final Point2D pos, final int hpos, final int vpos, final Orientation o) {
    double dx;
    switch(hpos) {
      case LEFT:
        dx = pos.getX();
        break;
      case CENTER_H:
        dx = pos.getX() - bbox.getWidth() * 0.5;
        break;
      case RIGHT:
        dx = pos.getX() - bbox.getWidth();
        break;
      default:
        throw new IllegalArgumentException("hpos: " + hpos);
    }
    double dy;
    switch(vpos) {
      case BOTTOM:
        dy = pos.getY();
        break;
      case CENTER_V:
        dy = pos.getY() + bbox.getHeight() * 0.5;
        break;
      case TOP:
        dy = pos.getY() + bbox.getHeight();
        break;
      default:
        throw new IllegalArgumentException("vpos: " + vpos);
    }
    switch(o) {
      case HORIZONTAL:
        draw(dx, dy);
        break;
      case DIAGONAL:
        drawDiagonal(dx, dy);
        break;
      case VERTICAL:
        drawVertical(dx, dy);
        break;
    }
  }

}
