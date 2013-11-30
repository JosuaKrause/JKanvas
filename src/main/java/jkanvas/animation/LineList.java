package jkanvas.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class LineList extends GenericPaintList<Line2D> {

  protected static final int X_COORD_0 = 0;

  protected static final int Y_COORD_0 = 1;

  protected static final int X_COORD_1 = 2;

  protected static final int Y_COORD_1 = 3;

  protected static final int COLOR = 0;
  /** The default color. */
  private Color defaultColor;

  public LineList(final int initialSize, final Color defaultColor) {
    super(4, 1, initialSize);
    this.defaultColor = defaultColor;
  }

  /**
   * Setter.
   * 
   * @param defaultColor Sets the default color. <code>null</code> indicates
   *          invisible lines.
   */
  public void setDefaultColor(final Color defaultColor) {
    this.defaultColor = defaultColor;
  }

  /**
   * Getter.
   * 
   * @return The default color or <code>null</code> if the lines are invisible
   *         by default.
   */
  public Color getDefaultColor() {
    return defaultColor;
  }

  public int addLine(final double x1, final double y1, final double x2, final double y2) {
    final int index = addIndex();
    final int pos = getPosition(index);
    set(X_COORD_0, pos, x1);
    set(Y_COORD_0, pos, y1);
    set(X_COORD_1, pos, x2);
    set(Y_COORD_1, pos, y2);
    final int cpos = getColorPosition(index);
    setColor(COLOR, cpos, null);
    return index;
  }

  public void setLine(final int index, final double x1, final double y1,
      final double x2, final double y2) {
    ensureActive(index);
    final int pos = getPosition(index);
    set(X_COORD_0, pos, x1);
    set(Y_COORD_0, pos, y1);
    set(X_COORD_1, pos, x2);
    set(Y_COORD_1, pos, y2);
  }

  public void getLine(final Line2D line, final int index) {
    ensureActive(index);
    final int pos = getPosition(index);
    line.setLine(
        get(X_COORD_0, pos),
        get(Y_COORD_0, pos),
        get(X_COORD_1, pos),
        get(Y_COORD_1, pos));
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param color The color or <code>null</code> if the default should be used.
   */
  public void setColor(final int index, final Color color) {
    ensureActive(index);
    final int cpos = getColorPosition(index);
    setColor(COLOR, cpos, color);
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The color or <code>null</code> if the default color should be used.
   */
  public Color getColor(final int index) {
    ensureActive(index);
    final int cpos = getColorPosition(index);
    return getColor(COLOR, cpos);
  }

  @Override
  protected Line2D createDrawObject() {
    return new Line2D.Double();
  }

  @Override
  protected void paint(final Graphics2D gfx, final Line2D line, final int index,
      final int pos, final int cpos) {
    final double x1 = get(X_COORD_0, pos);
    final double y1 = get(Y_COORD_0, pos);
    final double x2 = get(X_COORD_1, pos);
    final double y2 = get(Y_COORD_1, pos);
    if(Double.isNaN(x1) || Double.isNaN(y1) ||
        Double.isNaN(x2) || Double.isNaN(y2)) return;
    final Color color = getColor(COLOR, cpos);
    if(color != null || defaultColor != null) {
      gfx.setColor(color != null ? color : defaultColor);
      line.setLine(x1, y1, x2, y2);
      gfx.draw(line);
    }
  }

  @Override
  protected boolean contains(final Point2D point, final Line2D obj, final int index,
      final int pos) {
    // FIXME lines are infinitesimal thin
    return false;
  }

}
