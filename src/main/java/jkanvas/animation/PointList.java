package jkanvas.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class PointList extends GenericPaintList<Ellipse2D> {

  protected static final int X_COORD = 0;

  protected static final int Y_COORD = 1;

  protected static final int SIZE = 2;

  protected static final int COLOR_FILL = 0;

  protected static final int COLOR_BORDER = 1;

  private Color defaultColor;

  private Color defaultBorder;

  public PointList(final int initialSize, final Color defaultColor,
      final Color defaultBorder) {
    super(3, 2, initialSize);
    this.defaultColor = defaultColor;
    this.defaultBorder = defaultBorder;
  }

  public void setDefaultColor(final Color defaultColor) {
    this.defaultColor = defaultColor;
  }

  public Color getDefaultColor() {
    return defaultColor;
  }

  public void setDefaultBorder(final Color defaultBorder) {
    this.defaultBorder = defaultBorder;
  }

  public Color getDefaultBorder() {
    return defaultBorder;
  }

  public int addPoint(final double x, final double y, final double size) {
    final int index = addIndex();
    set(X_COORD, index, x);
    set(Y_COORD, index, y);
    set(SIZE, index, size);
    setColor(COLOR_FILL, index, null);
    setColor(COLOR_BORDER, index, null);
    return index;
  }

  public void setPoint(final int index, final double x, final double y, final double size) {
    set(X_COORD, index, x);
    set(Y_COORD, index, y);
    set(SIZE, index, size);
  }

  public double getX(final int index) {
    return get(X_COORD, index);
  }

  public double getY(final int index) {
    return get(Y_COORD, index);
  }

  public void getPosition(final Point2D pos, final int index) {
    pos.setLocation(getX(index), getY(index));
  }

  public void setPosition(final int index, final double x, final double y) {
    set(X_COORD, index, x);
    set(Y_COORD, index, y);
  }

  public double getRadius(final int index) {
    return get(SIZE, index);
  }

  public void setRadius(final int index, final double radius) {
    set(SIZE, index, radius);
  }

  public void setColor(final int index, final Color color) {
    setColor(COLOR_FILL, index, color);
  }

  public Color getColor(final int index) {
    return getColor(COLOR_FILL, index);
  }

  public void setBorder(final int index, final Color color) {
    setColor(COLOR_BORDER, index, color);
  }

  public Color getBorder(final int index) {
    return getColor(COLOR_BORDER, index);
  }

  @Override
  protected Ellipse2D createDrawObject() {
    return new Ellipse2D.Double();
  }

  @Override
  protected void paint(final Graphics2D gfx, final Ellipse2D circle, final int index) {
    final double x = get(X_COORD, index);
    final double y = get(Y_COORD, index);
    final double s = get(SIZE, index);
    circle.setFrame(x - s, y - s, s * 2.0, s * 2.0);
    final Color fill = getColor(COLOR_FILL, index);
    if(fill != null || defaultColor != null) {
      gfx.setColor(fill != null ? fill : defaultColor);
      gfx.fill(circle);
    }
    final Color border = getColor(COLOR_BORDER, index);
    if(border != null || defaultBorder != null) {
      gfx.setColor(border != null ? border : defaultBorder);
      gfx.draw(circle);
    }
  }

}
