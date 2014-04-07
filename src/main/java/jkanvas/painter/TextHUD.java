package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;

/**
 * A HUD that displays multiple lines of text. All lines are displayed in the
 * same color and an all enclosing background is added.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class TextHUD extends HUDRenderpass {

  /** The padding of the text box. */
  public static final double PADDING = 5.0;

  /** The alpha value of the text box. */
  public static final double ALPHA = 0.5;

  /** The text color. */
  public static final Color TEXT = Color.WHITE;

  /** The text box color. */
  public static final Color BACK = Color.BLACK;

  /** Alignment on the left side of the component. */
  public static final int LEFT = -1;

  /** Alignment on the right side of the component. */
  public static final int RIGHT = -2;

  /** Alignment on the top of the component. */
  public static final int TOP = 1;

  /** Alignment on the bottom of the component. */
  public static final int BOTTOM = 2;

  /**
   * Whether the given alignment is valid.
   * 
   * @param align The alignment value.
   * @param horizontal Whether it should be a horizontal alignment value.
   * @return The alignment value.
   */
  private static int validAlignment(final int align, final boolean horizontal) {
    if(horizontal && align != LEFT && align != RIGHT) throw new IllegalArgumentException(
        "invalid horizontal alignment: " + align);
    if(!horizontal && align != TOP && align != BOTTOM) throw new IllegalArgumentException(
        "invalid vertical alignment: " + align);
    return align;
  }

  /** The text color. */
  private final Color text;
  /** The background color. */
  private final Color back;
  /** The alpha value of the background. */
  private final double alpha;
  /** The padding of the background. */
  private final double padding;
  /** The horizontal alignment. */
  private final int hpos;
  /** The vertical alignment. */
  private final int vpos;

  /**
   * Creates a text HUD.
   * 
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @see #LEFT
   * @see #RIGHT
   * @see #TOP
   * @see #BOTTOM
   */
  public TextHUD(final int hpos, final int vpos) {
    this(TEXT, BACK, ALPHA, PADDING, hpos, vpos);
  }

  /**
   * Creates a text HUD.
   * 
   * @param text The text color.
   * @param back The background color.
   * @param alpha The alpha value of the background color.
   * @param padding The padding of the background.
   * @param hpos The horizontal alignment.
   * @param vpos The vertical alignment.
   * @see #LEFT
   * @see #RIGHT
   * @see #TOP
   * @see #BOTTOM
   */
  public TextHUD(final Color text, final Color back, final double alpha,
      final double padding, final int hpos, final int vpos) {
    this.text = Objects.requireNonNull(text);
    Objects.requireNonNull(back);
    final float[] a = new float[1];
    this.back = PaintUtil.noAlpha(back, a);
    this.alpha = alpha * a[0];
    this.padding = padding;
    this.hpos = validAlignment(hpos, true);
    this.vpos = validAlignment(vpos, false);
  }

  /**
   * Getter.
   * 
   * @return The number of lines to display at most.
   */
  public abstract int lineCount();

  /**
   * Getter.
   * 
   * @param i The index of the line.
   * @return The line to display for the given index. If this method returns
   *         <code>null</code> the line is ignored.
   */
  public abstract String getLine(int i);

  /**
   * Calculates the start position.
   * 
   * @param visibleRect The visible rectangle in component coordinates.
   * @return The start position.
   */
  private Point2D getStartPosition(final RectangularShape visibleRect) {
    double x;
    switch(hpos) {
      case LEFT:
        x = visibleRect.getMinX() + padding;
        break;
      case RIGHT:
        x = visibleRect.getMaxX() - padding;
        break;
      default:
        throw new AssertionError();
    }
    double y;
    switch(vpos) {
      case TOP:
        y = visibleRect.getMinY() + padding;
        break;
      case BOTTOM:
        y = visibleRect.getMaxY() - padding;
        break;
      default:
        throw new AssertionError();
    }
    return new Point2D.Double(x, y);
  }

  @Override
  public void drawHUD(final Graphics2D g, final KanvasContext ctx) {
    draw(g, ctx.getVisibleComponent());
  }

  /**
   * Draws the text box.
   * 
   * @param gfx The graphics context.
   * @param visibleRect The visible rectangle in component coordinates.
   */
  protected void draw(final Graphics2D gfx, final RectangularShape visibleRect) {
    final int count = lineCount();
    if(count <= 0) return;
    final StringDrawer[] sds = new StringDrawer[count];
    double height = 0;
    double width = 0;
    for(int i = 0; i < sds.length; ++i) {
      final String line = getLine(i);
      if(line == null) {
        continue;
      }
      final StringDrawer sd = new StringDrawer(gfx, line);
      final double w = sd.getWidth();
      if(w > width) {
        width = w;
      }
      height += sd.getHeight();
      sds[i] = sd;
    }
    if(height <= 0 || width <= 0) return;
    final boolean isTop = vpos == TOP;
    final double dirV = isTop ? 1 : -1;
    final double mulV = isTop ? 0 : 1;
    final double mulH = hpos == LEFT ? 0 : 1;
    final Point2D cur = getStartPosition(visibleRect);
    final Rectangle2D box = new Rectangle2D.Double(cur.getX() - mulH * width,
        cur.getY() - mulV * height, width, height);
    if(alpha > 0) {
      final Graphics2D g = (Graphics2D) gfx.create();
      PaintUtil.setAlpha(g, alpha);
      g.setColor(back);
      g.fill(PaintUtil.toRoundRectangle(box, padding));
      g.dispose();
    }
    gfx.setColor(text);
    for(int i = 0; i < sds.length; ++i) {
      final int index = isTop ? i : sds.length - 1 - i;
      final StringDrawer sd = sds[index];
      if(sd == null) {
        continue;
      }
      drawLine(gfx, cur, sd, index,
          (hpos == LEFT ? StringDrawer.LEFT : StringDrawer.RIGHT),
          (vpos == TOP ? StringDrawer.TOP : StringDrawer.BOTTOM));
      cur.setLocation(cur.getX(), cur.getY() + sd.getHeight() * dirV);
    }
  }

  /**
   * Draws a text line.
   * 
   * @param gfx The graphics context.
   * @param cur The current position.
   * @param sd The string drawer.
   * @param index The current index.
   * @param hpos The horizontal position.
   * @param vpos The vertical position.
   */
  protected void drawLine(
      @SuppressWarnings("unused") final Graphics2D gfx, final Point2D cur,
      final StringDrawer sd, @SuppressWarnings("unused") final int index,
      final int hpos, final int vpos) {
    sd.draw(cur, hpos, vpos);
  }

}
