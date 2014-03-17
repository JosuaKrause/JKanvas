package jkanvas.animation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import jkanvas.table.LineMapper;

/**
 * A list of lines.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class LineList extends GenericPaintList<Line2D> {

  /** The The index for the first x coordinate. */
  protected static final int X_COORD_0 = 0;
  /** The The index for the first y coordinate. */
  protected static final int Y_COORD_0 = 1;
  /** The The index for the second x coordinate. */
  protected static final int X_COORD_1 = 2;
  /** The The index for the second y coordinate. */
  protected static final int Y_COORD_1 = 3;
  /** The The index for the alpha value. */
  protected static final int ALPHA = 4;
  /** The The index for the color. */
  protected static final int COLOR = 0;
  /** The default color. */
  private Color defaultColor;

  /**
   * Creates a line list.
   * 
   * @param initialSize The initial size.
   * @param defaultColor The default color or <code>null</code> if lines should
   *          be transparent by default.
   */
  public LineList(final int initialSize, final Color defaultColor) {
    super(5, 1, initialSize);
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

  /**
   * Adds a new line.
   * 
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @return The index of the newly created line.
   */
  public int addLine(final double x1, final double y1, final double x2, final double y2) {
    return addLine(x1, y1, x2, y2, 1.0);
  }

  /**
   * Adds a new line.
   * 
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @param alpha The alpha value of the line.
   * @return The index of the newly created line.
   */
  public int addLine(final double x1, final double y1,
      final double x2, final double y2, final double alpha) {
    if(alpha < 0 || alpha > 1) throw new IllegalArgumentException("" + alpha);
    final int index = addIndex();
    final int pos = getPosition(index);
    set(X_COORD_0, pos, x1);
    set(Y_COORD_0, pos, y1);
    set(X_COORD_1, pos, x2);
    set(Y_COORD_1, pos, y2);
    set(ALPHA, pos, alpha);
    final int cpos = getColorPosition(index);
    setColor(COLOR, cpos, null);
    return index;
  }

  /**
   * Setter.
   * 
   * @param index The index of the line.
   * @param alpha The alpha value of the line.
   */
  public void setAlpha(final int index, final double alpha) {
    ensureActive(index);
    if(alpha < 0 || alpha > 1) throw new IllegalArgumentException("" + alpha);
    final int pos = getPosition(index);
    set(ALPHA, pos, alpha);
  }

  /**
   * Getter.
   * 
   * @param index The index of the line.
   * @return The alpha value of the line.
   */
  public double getAlpha(final int index) {
    ensureActive(index);
    final int pos = getPosition(index);
    return get(ALPHA, pos);
  }

  /**
   * Sets the line at the given index.
   * 
   * @param index The index of the line.
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   */
  public void setLine(final int index, final double x1, final double y1,
      final double x2, final double y2) {
    ensureActive(index);
    final int pos = getPosition(index);
    set(X_COORD_0, pos, x1);
    set(Y_COORD_0, pos, y1);
    set(X_COORD_1, pos, x2);
    set(Y_COORD_1, pos, y2);
  }

  /**
   * Stores the line at the given index in the provided object.
   * 
   * @param line The line to store the information in.
   * @param index The index of the line.
   */
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
      final int pos, final int cpos, final Composite defaultComposite) {
    final double x1 = get(X_COORD_0, pos);
    final double y1 = get(Y_COORD_0, pos);
    final double x2 = get(X_COORD_1, pos);
    final double y2 = get(Y_COORD_1, pos);
    if(Double.isNaN(x1) || Double.isNaN(y1) ||
        Double.isNaN(x2) || Double.isNaN(y2)) return;
    final double alpha = get(ALPHA, pos);
    if(alpha <= 0) return;
    final Color color = getColor(COLOR, cpos);
    if(color != null || defaultColor != null) {
      gfx.setColor(color != null ? color : defaultColor);
      line.setLine(x1, y1, x2, y2);
      if(alpha >= 1) {
        gfx.setComposite(defaultComposite);
      } else {
        gfx.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, (float) alpha));
      }
      gfx.draw(line);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * If this method returns a different value than <code>false</code> optimizing
   * via {@link #optimize()} will result in wrong results for
   * {@link #hit(Point2D)} when using a {@link LineMapper}.
   */
  @Override
  protected boolean contains(final Point2D point, final Line2D obj,
      final int index, final int pos) {
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * If this method returns a different value than <code>false</code> optimizing
   * via {@link #optimize()} will result in wrong results for
   * {@link #hit(Point2D)} when using a {@link LineMapper}.
   */
  @Override
  protected boolean intersects(
      final Area area, final Line2D obj, final int index, final int pos) {
    return false;
  }

  /**
   * Reduces the number of lines by identifying similar lines and adding the
   * alpha values together.
   * <p>
   * Note that {@link #contains(Point2D, Line2D, int, int)} and
   * {@link #hit(Point2D)} can no longer be used with a {@link LineMapper} after
   * optimizing.
   */
  public void optimize() {
    final Comparator<Line2D> lineOrder = new Comparator<Line2D>() {

      @Override
      public int compare(final Line2D a, final Line2D b) {
        // TODO don't care for order of points
        final int cmpY1 = Double.compare(a.getY1(), b.getY1());
        if(cmpY1 != 0) return cmpY1;
        final int cmpY2 = Double.compare(a.getY2(), b.getY2());
        if(cmpY2 != 0) return cmpY2;
        final int cmpX1 = Double.compare(a.getX1(), b.getX1());
        if(cmpX1 != 0) return cmpX1;
        return Double.compare(a.getX2(), b.getX2());
      }

    };
    final int count = cardinality();
    if(visibleCardinality() != count) throw new IllegalStateException(
        "all lines must be visible");
    final SortedMap<Line2D, Double> lines = new TreeMap<>(lineOrder);
    for(final int i : actives()) {
      final Line2D line = new Line2D.Double() {

        @Override
        public boolean equals(final Object obj) {
          if(this == obj) return true;
          if(!(obj instanceof Line2D)) return false;
          return lineOrder.compare(this, (Line2D) obj) == 0;
        }

        @Override
        public int hashCode() {
          int res = 1;
          res += 31 * ((java.lang.Double) getY1()).hashCode();
          res += 31 * ((java.lang.Double) getY2()).hashCode();
          res += 31 * ((java.lang.Double) getX1()).hashCode();
          res += 31 * ((java.lang.Double) getX2()).hashCode();
          return res;
        }

      };
      getLine(line, i);
      Double alpha = lines.get(line);
      if(alpha == null) {
        alpha = 0.0;
      }
      lines.put(line, Math.min(alpha + getAlpha(i), 1));
    }
    if(count == lines.size()) return; // no need to change
    // System.out.println("before: " + count + " after: " + lines.size());
    clear();
    for(final Entry<Line2D, Double> e : lines.entrySet()) {
      final Line2D l = e.getKey();
      addLine(l.getX1(), l.getY1(), l.getX2(), l.getY2(), e.getValue());
    }
    trimToSize();
  }

}
