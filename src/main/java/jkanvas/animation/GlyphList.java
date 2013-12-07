package jkanvas.animation;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import jkanvas.util.ShapeShifter;

/**
 * A list with different point glyphs.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class GlyphList extends PointList<Glyph> {

  /** Circle glyph. */
  public static final int TYPE_CIRCLE = 0;
  /** Rectangle glyph. */
  public static final int TYPE_RECT = 1;

  /**
   * Creates a glyph list.
   * 
   * @param initialSize The initial size.
   * @param defaultColor The default fill color.
   * @param defaultBorder The default border color.
   */
  public GlyphList(final int initialSize,
      final Color defaultColor, final Color defaultBorder) {
    super(initialSize, defaultColor, defaultBorder);
  }

  @Override
  protected Glyph createDrawObject() {
    return new Glyph();
  }

  @Override
  protected void setShape(final Glyph g, final int index,
      final double x, final double y, final double s) {
    g.setShape(getType(index), x, y, s);
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The type of the given index.
   */
  protected abstract int getType(int index);

}

/**
 * The glyph shape shifter.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
class Glyph extends ShapeShifter {

  /** The circle. */
  private final Ellipse2D circle = new Ellipse2D.Double();
  /** The rectangle. */
  private final Rectangle2D rect = new Rectangle2D.Double();

  /**
   * Setter.
   * 
   * @param type The type.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param s The size.
   */
  public void setShape(final int type, final double x, final double y, final double s) {
    switch(type) {
      case GlyphList.TYPE_CIRCLE:
        circle.setFrame(x - s, y - s, s * 2.0, s * 2.0);
        setActiveShape(circle);
        break;
      case GlyphList.TYPE_RECT:
        rect.setFrame(x - s, y - s, s * 2.0, s * 2.0);
        setActiveShape(rect);
        break;
      default:
        throw new IllegalArgumentException("illegal type: " + type);
    }
  }

}
