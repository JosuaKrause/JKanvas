package jkanvas.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * A list of circular shaped points with filling and border color.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class PointList extends GenericPaintList<Ellipse2D> {

  /** The index for the x coordinate. */
  protected static final int X_COORD = 0;
  /** The index for the y coordinate. */
  protected static final int Y_COORD = 1;
  /** The index for the size. */
  protected static final int SIZE = 2;
  /** The index for the filling color. */
  protected static final int COLOR_FILL = 0;
  /** The index for the border color. */
  protected static final int COLOR_BORDER = 1;
  /** The default filling color. */
  private Color defaultColor;
  /** The default border color. */
  private Color defaultBorder;

  /**
   * Creates a point list with initial size.
   * 
   * @param initialSize The initial size.
   * @param defaultColor The default filling color.
   * @param defaultBorder The default border color.
   */
  public PointList(final int initialSize, final Color defaultColor,
      final Color defaultBorder) {
    super(3, 2, initialSize);
    this.defaultColor = defaultColor;
    this.defaultBorder = defaultBorder;
  }

  /**
   * Setter.
   * 
   * @param defaultColor Sets the default filling color. <code>null</code>
   *          indicates a invisible fill.
   */
  public void setDefaultColor(final Color defaultColor) {
    this.defaultColor = defaultColor;
  }

  /**
   * Getter.
   * 
   * @return The default filling color or <code>null</code> if the filling is
   *         not visible.
   */
  public Color getDefaultColor() {
    return defaultColor;
  }

  /**
   * Setter.
   * 
   * @param defaultBorder Sets the default border color. <code>null</code>
   *          indicates a invisible border.
   */
  public void setDefaultBorder(final Color defaultBorder) {
    this.defaultBorder = defaultBorder;
  }

  /**
   * Getter.
   * 
   * @return The default border color or <code>null</code> if the border is not
   *         visible.
   */
  public Color getDefaultBorder() {
    return defaultBorder;
  }

  /**
   * Adds a point.
   * 
   * @param x The initial x coordinate.
   * @param y The initial y coordinate.
   * @param size The initial size.
   * @return The index of the point.
   */
  public int addPoint(final double x, final double y, final double size) {
    final int index = addIndex();
    final int pos = getPosition(index);
    set(X_COORD, pos, x);
    set(Y_COORD, pos, y);
    set(SIZE, pos, size);
    final int cpos = getColorPosition(index);
    setColor(COLOR_FILL, cpos, null);
    setColor(COLOR_BORDER, cpos, null);
    return index;
  }

  /**
   * Sets the position of the given point.
   * 
   * @param index The index of the point.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param size The size.
   */
  public void setPoint(final int index, final double x, final double y, final double size) {
    ensureActive(index);
    final int pos = getPosition(index);
    set(X_COORD, pos, x);
    set(Y_COORD, pos, y);
    set(SIZE, pos, size);
  }

  /**
   * Getter.
   * 
   * @param index The index of the point.
   * @return The x coordinate.
   */
  public double getX(final int index) {
    ensureActive(index);
    final int pos = getPosition(index);
    return get(X_COORD, pos);
  }

  /**
   * Getter.
   * 
   * @param index The index of the point.
   * @return The y coordinate.
   */
  public double getY(final int index) {
    ensureActive(index);
    final int pos = getPosition(index);
    return get(Y_COORD, pos);
  }

  /**
   * Getter.
   * 
   * @param p The shape the position will be stored.
   * @param index The index of the point.
   */
  public void getPosition(final Point2D p, final int index) {
    ensureActive(index);
    final int pos = getPosition(index);
    p.setLocation(get(X_COORD, pos), get(Y_COORD, pos));
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  public void setPosition(final int index, final double x, final double y) {
    ensureActive(index);
    final int pos = getPosition(index);
    set(X_COORD, pos, x);
    set(Y_COORD, pos, y);
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The size.
   */
  public double getRadius(final int index) {
    ensureActive(index);
    final int pos = getPosition(index);
    return get(SIZE, pos);
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param radius The size.
   */
  public void setRadius(final int index, final double radius) {
    ensureActive(index);
    final int pos = getPosition(index);
    set(SIZE, pos, radius);
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param color The filling color or <code>null</code> if the default should
   *          be used.
   */
  public void setColor(final int index, final Color color) {
    ensureActive(index);
    final int cpos = getColorPosition(index);
    setColor(COLOR_FILL, cpos, color);
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The filling color or <code>null</code> if the default color should
   *         be used.
   */
  public Color getColor(final int index) {
    ensureActive(index);
    final int cpos = getColorPosition(index);
    return getColor(COLOR_FILL, cpos);
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param color The border color or <code>null</code> if the default should be
   *          used.
   */
  public void setBorder(final int index, final Color color) {
    ensureActive(index);
    final int cpos = getColorPosition(index);
    setColor(COLOR_BORDER, cpos, color);
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The border color or <code>null</code> if the default should be
   *         used.
   */
  public Color getBorder(final int index) {
    ensureActive(index);
    final int cpos = getColorPosition(index);
    return getColor(COLOR_BORDER, cpos);
  }

  @Override
  protected Ellipse2D createDrawObject() {
    return new Ellipse2D.Double();
  }

  @Override
  protected void paint(final Graphics2D gfx, final Ellipse2D circle,
      final int index, final int pos, final int cpos) {
    final double x = get(X_COORD, pos);
    final double y = get(Y_COORD, pos);
    final double s = get(SIZE, pos);
    if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(s)) return;
    circle.setFrame(x - s, y - s, s * 2.0, s * 2.0);
    final Color fill = getColor(COLOR_FILL, cpos);
    if(fill != null || defaultColor != null) {
      gfx.setColor(fill != null ? fill : defaultColor);
      gfx.fill(circle);
    }
    final Color border = getColor(COLOR_BORDER, cpos);
    if(border != null || defaultBorder != null) {
      gfx.setColor(border != null ? border : defaultBorder);
      gfx.draw(circle);
    }
  }

  @Override
  protected boolean contains(
      final Point2D point, final Ellipse2D circle, final int index, final int pos) {
    final double x = get(X_COORD, pos);
    final double y = get(Y_COORD, pos);
    final double s = get(SIZE, pos);
    if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(s)) return false;
    circle.setFrame(x - s, y - s, s * 2.0, s * 2.0);
    return circle.contains(point);
  }

}
