package jkanvas.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import jkanvas.KanvasContext;

/**
 * Utility methods for painting.
 * 
 * @author Joschi <josua.krause@gmail.com>
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
   * Creates a circle to draw a point.
   * 
   * @param pos The point.
   * @param size The size of the point.
   * @return The circle.
   */
  public static Ellipse2D pixelRound(final Point2D pos, final double size) {
    return createCircle(pos.getX(), pos.getY(), size * 0.5);
  }

  /**
   * Adds padding to a rectangle.
   * 
   * @param rect The rectangle.
   * @param padding The padding.
   * @return The rectangle with the padding.
   */
  public static Rectangle2D addPadding(final RectangularShape rect, final double padding) {
    final double p2 = padding * 2;
    return new Rectangle2D.Double(rect.getX() - padding, rect.getY() - padding,
        rect.getWidth() + p2, rect.getHeight() + p2);
  }

  /**
   * Adds padding to a rectangle.
   * 
   * @param rect The rectangle. The result will also be stored in the rectangle.
   * @param padding The padding.
   */
  public static void addPaddingInplace(final RectangularShape rect, final double padding) {
    final double p2 = padding * 2;
    rect.setFrame(rect.getX() - padding, rect.getY() - padding,
        rect.getWidth() + p2, rect.getHeight() + p2);
  }

  /** Factor for changing radii correctly. */
  private static final double RADIUS_FACTOR = Math.sqrt(2) - 1;

  /**
   * Encloses the given rectangle with a rounded rectangle whose rounded corners
   * touch the corners of the rectangle.
   * 
   * @param dest The destination rectangle.
   * @param rect The rectangle to enclose.
   * @param border The size of the border.
   */
  public static void encloseRect(final RoundRectangle2D dest,
      final RectangularShape rect, final double border) {
    final double b2 = border * 2;
    final double arc = border / RADIUS_FACTOR;
    dest.setRoundRect(rect.getX() - border, rect.getY() - border,
        rect.getWidth() + b2, rect.getHeight() + b2, arc, arc);
  }

  /**
   * Sets the arc of a rounded rectangle.
   * 
   * @param rect The rounded rectangle to alter.
   * @param border The border. The arc touches the corner of a rectangle that
   *          would be created by adding the border as padding.
   */
  public static void setArc(final RoundRectangle2D rect, final double border) {
    final double arc = border / RADIUS_FACTOR;
    rect.setRoundRect(rect.getX(), rect.getY(),
        rect.getWidth(), rect.getHeight(), arc, arc);
  }

  /**
   * Adds padding to the rectangle by altering the rectangle.
   * 
   * @param rect The rectangle.
   * @param padding The padding to add.
   */
  public static void addPaddingInplace(final RoundRectangle2D rect, final double padding) {
    final double p2 = padding * 2;
    final double arc = padding / RADIUS_FACTOR;
    rect.setRoundRect(rect.getX() - padding, rect.getY() - padding,
        rect.getWidth() + p2, rect.getHeight() + p2,
        arc + rect.getArcWidth(), arc + rect.getArcHeight());
  }

  /**
   * Converts a rectangle to a round rectangle.
   * 
   * @param rect The rectangle.
   * @param radius The radius of the round rectangle.
   * @return The round rectangle.
   */
  public static RoundRectangle2D toRoundRectangle(
      final RectangularShape rect, final double radius) {
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
  public static Rectangle2D scaleCenter(final RectangularShape rect, final double scale) {
    final double w = rect.getWidth() * scale;
    final double h = rect.getHeight() * scale;
    return new Rectangle2D.Double(rect.getCenterX() - w * 0.5,
        rect.getCenterY() - h * 0.5, w, h);
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
  public static Rectangle2D fitInto(final RectangularShape rect,
      final double w, final double h) {
    final double s = fitIntoScale(rect, w, h);
    final double nw = w * s;
    final double nh = h * s;
    final double px = (rect.getWidth() - nw) * 0.5;
    final double py = (rect.getHeight() - nh) * 0.5;
    return new Rectangle2D.Double(rect.getX() + px, rect.getY() + py, nw, nh);
  }

  /**
   * Scales the given width and height so that they are maximally not larger
   * than the size of the given rectangle.
   * 
   * @param rect The rectangle.
   * @param w The width.
   * @param h The height.
   * @return The scale for the width and height.
   */
  public static double fitIntoScale(final RectangularShape rect,
      final double w, final double h) {
    final double rw = rect.getWidth();
    final double rh = rect.getHeight();
    final double hRatio = rh / h;
    final double verWidth = w * hRatio;
    return verWidth <= rw ? hRatio : rw / w;
  }

  /**
   * Scales a rectangle such that the rectangle fits into a pixel rectangle.
   * 
   * @param pixWidth The pixel rectangle width.
   * @param pixHeight The pixel rectangle height.
   * @param w The rectangle width.
   * @param h The rectangle height.
   * @param fit If set to <code>true</code> the rectangle will be completely
   *          visible. When set to <code>false</code> as much as possible from
   *          the rectangle will be visible without showing anything else.
   * @return The scaling to apply.
   */
  public static double fitIntoPixelScale(final int pixWidth, final int pixHeight,
      final double w, final double h, final boolean fit) {
    final double rw = pixWidth / w;
    final double rh = pixHeight / h;
    return fit ? Math.min(rw, rh) : Math.max(rw, rh);
  }

  /**
   * Creates a circle with the given radius.
   * 
   * @param x The x position.
   * @param y The y position.
   * @param r The radius.
   * @return The circle.
   */
  public static Ellipse2D createCircle(final double x, final double y, final double r) {
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
    final Path2D gp = new Path2D.Double();
    gp.moveTo(x1 + ortho.getX(), y1 + ortho.getY());
    gp.lineTo(x2 + ortho.getX(), y2 + ortho.getY());
    gp.lineTo(x2 - ortho.getX(), y2 - ortho.getY());
    gp.lineTo(x1 - ortho.getX(), y1 - ortho.getY());
    gp.closePath();
    return gp;
  }

  /**
   * Interpolates between two colors.
   * 
   * @param from The color to interpolate from.
   * @param to The color to interpolate to.
   * @param t The interpolation value from <code>0</code> to <code>1</code>.
   * @return The interpolated color.
   */
  public static Color interpolate(final Color from, final Color to, final double t) {
    if(Double.isNaN(t)) throw new IllegalArgumentException("NaN");
    if(t > 1 || t < 0) throw new IllegalArgumentException("" + t);
    final float[] fromRGBA = new float[4];
    final float[] toRGBA = new float[4];
    from.getRGBComponents(fromRGBA);
    to.getRGBComponents(toRGBA);
    final double r = fromRGBA[0] * (1 - t) + toRGBA[0] * t;
    final double g = fromRGBA[1] * (1 - t) + toRGBA[1] * t;
    final double b = fromRGBA[2] * (1 - t) + toRGBA[2] * t;
    final double a = fromRGBA[3] * (1 - t) + toRGBA[3] * t;
    return new Color((float) r, (float) g, (float) b, (float) a);
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
  public static Color hsbaColor(
      final double h, final double s, final double b, final double alpha) {
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
    final Composite comp = g.getComposite();
    if(comp instanceof AlphaComposite) {
      final AlphaComposite ac = (AlphaComposite) comp;
      if(ac.getRule() == AlphaComposite.SRC_OVER) {
        g.setComposite(ac.derive((float) (ac.getAlpha() * alpha)));
        return;
      }
    }
    System.err.println("Warning: cannot derive composite: " + comp);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
  }

  /** The zooming factor when detail lines disappear. */
  private static final double DISSAPPEAR = 1.2;

  /**
   * Draws a shape with a zoom dependent fading of the outline.
   * 
   * @param g The graphics context.
   * @param shape The shape to draw.
   * @param ctx The context.
   * @param border The border color.
   * @param fill The color used to fill and used when the zoom level is to
   *          small.
   */
  public static void drawShape(final Graphics2D g, final Shape shape,
      final KanvasContext ctx, final Color border, final Color fill) {
    drawShape(g, shape, ctx.toComponentLength(1), border, fill);
  }

  /**
   * Draws a shape with a zoom dependent fading of the outline.
   * 
   * @param g The graphics context.
   * @param shape The shape to draw.
   * @param zoom The zoom level.
   * @param border The border color.
   * @param fill The color used to fill and used when the zoom level is to
   *          small.
   */
  public static void drawShape(final Graphics2D g, final Shape shape,
      final double zoom, final Color border, final Color fill) {
    if(fill != null) {
      g.setColor(fill);
      g.fill(shape);
    }
    final double ratio = DISSAPPEAR / zoom;
    if(ratio >= 1) {
      if(fill == null) return;
      g.setColor(fill);
    } else {
      final Color f = fill != null ? fill : Color.WHITE;
      final double t = Math.log((Math.E - 1) * ratio + 1);
      g.setColor(PaintUtil.interpolate(border, f, t));
    }
    g.draw(shape);
  }

  /**
   * Fills a shape with a zoom dependent fading.
   * 
   * @param g The graphics context.
   * @param shape The shape.
   * @param ctx The context.
   * @param near The color used when being near.
   * @param far The color used when being far.
   */
  public static void fillShape(final Graphics2D g, final Shape shape,
      final KanvasContext ctx, final Color near, final Color far) {
    fillShape(g, shape, ctx.toComponentLength(1), near, far);
  }

  /**
   * Fills a shape with a zoom dependent fading.
   * 
   * @param g The graphics context.
   * @param shape The shape.
   * @param zoom The zoom level.
   * @param near The color used when being near.
   * @param far The color used when being far.
   */
  public static void fillShape(final Graphics2D g, final Shape shape,
      final double zoom, final Color near, final Color far) {
    final double ratio = DISSAPPEAR / zoom;
    if(ratio >= 1) {
      g.setColor(far);
    } else {
      final double t = Math.log((Math.E - 1) * ratio + 1);
      g.setColor(PaintUtil.interpolate(near, far, t));
    }
    g.fill(shape);
  }

  /**
   * Computes a color visible on the given background.
   * 
   * @param back The background color.
   * @return The color recommended for fonts.
   */
  public static Color getFontColor(final Color back) {
    final float[] rgb = back.getRGBColorComponents(null);
    if(0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2] > 0.5) return Color.BLACK;
    return Color.WHITE;
  }

}
