package jkanvas.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Utility methods for painting.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class PaintUtil {

  /** Hidden default constructor. */
  private PaintUtil() {
    throw new AssertionError();
  }

  /**
   * Creates a rectangle to draw a point.
   * 
   * @param pos The point.
   * @param size The size of the point.
   * @return The rectangle.
   */
  public static Rectangle2D pixel(final Point2D pos, final double size) {
    final double s2 = size * 0.5;
    return new Rectangle2D.Double(pos.getX() - s2, pos.getY() - s2, size, size);
  }

  /**
   * Creates a rectangle to draw a point.
   * 
   * @param pos The point.
   * @return The rectangle.
   */
  public static Rectangle2D pixel(final Point2D pos) {
    return pixel(pos, 1);
  }

  /**
   * Adds padding to a rectangle.
   * 
   * @param rect The rectangle.
   * @param padding The padding.
   * @return The new rectangle.
   */
  public static Rectangle2D addPadding(final Rectangle2D rect, final double padding) {
    final double p2 = padding * 2;
    return new Rectangle2D.Double(rect.getX() - padding, rect.getY() - padding,
        rect.getWidth() + p2, rect.getHeight() + p2);
  }

  /**
   * Converts a rectangle to a round rectangle.
   * 
   * @param rect The rectangle.
   * @param radius The radius of the round rectangle.
   * @return The round rectangle.
   */
  public static RoundRectangle2D toRoundRectangle(
      final Rectangle2D rect, final double radius) {
    final double r2 = radius * 2;
    return new RoundRectangle2D.Double(rect.getX() - radius, rect.getY() - radius,
        rect.getWidth() + r2, rect.getHeight() + r2, radius, radius);
  }

  /**
   * Scales a rectangle so that the center point stays the same.
   * 
   * @param rect The rectangle to scale.
   * @param scale The scaling factor.
   * @return The scaled rectangle.
   */
  public static Rectangle2D scaleCenter(final Rectangle2D rect, final double scale) {
    final double w = rect.getWidth() * scale;
    final double h = rect.getHeight() * scale;
    return new Rectangle2D.Double(rect.getCenterX() - w * 0.5, rect.getCenterY() - h
        * 0.5, w, h);
  }

  /**
   * Fits a rectangle into the given rectangle. The aspect ratio of the
   * resulting rectangle is given by <code>w</code> and <code>h</code>.
   * 
   * @param rect The rectangle.
   * @param w The width for defining the aspect ratio.
   * @param h The height for defining the aspect ratio.
   * @return A rectangle with the given aspect ratio that fits into the given
   *         rectangle.
   */
  public static Rectangle2D fitInto(final Rectangle2D rect, final double w, final double h) {
    final double rw = rect.getWidth();
    final double rh = rect.getHeight();
    final double verWidth = w * rh / h;
    final boolean vertical = verWidth <= rw;
    final double px;
    final double py;
    final double nw;
    final double nh;
    if(vertical) {
      nw = verWidth;
      nh = rh;
      px = (rw - nw) * 0.5;
      py = 0;
    } else {
      nw = rw;
      nh = h * rw / w;
      px = 0;
      py = (rh - nh) * 0.5;
    }
    return new Rectangle2D.Double(rect.getX() + px, rect.getY() + py, nw, nh);
  }

  /**
   * Creates a circle with the given radius.
   * 
   * @param x The x position.
   * @param y The y position.
   * @param r The radius.
   * @return The circle.
   */
  public static Ellipse2D createEllipse(final double x, final double y, final double r) {
    final double r2 = r * 2;
    return new Ellipse2D.Double(x - r, y - r, r2, r2);
  }

  /**
   * Creates a line with a given width without using a stroke.
   * 
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @param width The width of the line.
   * @return The shape of the line.
   */
  public static Shape createLine(final double x1, final double y1,
      final double x2, final double y2, final double width) {
    final Point2D ortho = VecUtil.setLength(
        new Point2D.Double(y1 - y2, x2 - x1), width * 0.5);
    final GeneralPath gp = new GeneralPath();
    gp.moveTo(x1 + ortho.getX(), y1 + ortho.getY());
    gp.lineTo(x2 + ortho.getX(), y2 + ortho.getY());
    gp.lineTo(x2 - ortho.getX(), y2 - ortho.getY());
    gp.lineTo(x1 - ortho.getX(), y1 - ortho.getY());
    gp.closePath();
    return gp;
  }

  /**
   * Sets the alpha value of a color. Hint: Consider using
   * {@link #setAlpha(Graphics2D, double)}.
   * 
   * @param col The color.
   * @param alpha The new alpha value.
   * @return The color with transparency.
   */
  public static Color setAlpha(final Color col, final double alpha) {
    final float[] comp = col.getRGBComponents(null);
    return new Color(comp[0], comp[1], comp[2], (float) alpha);
  }

  /**
   * Multiplies the alpha value of a color. Hint: Consider using
   * {@link #setAlpha(Graphics2D, double)}.
   * 
   * @param col The color.
   * @param alpha The alpha value to multiply to the previous value.
   * @return The color with new transparency.
   */
  public static Color mulAlpha(final Color col, final double alpha) {
    final float[] comp = col.getRGBComponents(null);
    return new Color(comp[0], comp[1], comp[2], (float) (alpha * comp[3]));
  }

  /**
   * Creates a color with transparency. Hint: Consider using
   * {@link #setAlpha(Graphics2D, double)}.
   * 
   * @param h The hue value. <code>1</code> represents <code>360</code> degrees.
   * @param s The saturation value.
   * @param b The brightness value.
   * @param alpha The alpha value.
   * @return The color.
   */
  public static Color hsbaColor(final double h, final double s, final double b,
      final double alpha) {
    return setAlpha(Color.getHSBColor((float) h, (float) s, (float) b), alpha);
  }

  /**
   * Removes the transparency of the given color.
   * 
   * @param col The color.
   * @return The same color without transparency.
   */
  public static Color noAlpha(final Color col) {
    return noAlpha(col, null);
  }

  /**
   * Removes the transparency of the given color.
   * 
   * @param col The color.
   * @param alpha An optional array with a length of at least one that is used
   *          to store the previous alpha value of the color in slot 0.
   * @return The same color without transparency.
   */
  public static Color noAlpha(final Color col, final float[] alpha) {
    final float[] comp = col.getRGBComponents(null);
    if(alpha != null) {
      alpha[0] = comp[3];
    }
    if(comp[3] == 1) return col;
    return new Color(comp[0], comp[1], comp[2]);
  }

  /**
   * Sets the alpha value of the given graphics context via composite.
   * 
   * @param g The graphics context.
   * @param alpha The alpha value.
   */
  public static void setAlpha(final Graphics2D g, final double alpha) {
    if(alpha >= 1) return;
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
  }

}
