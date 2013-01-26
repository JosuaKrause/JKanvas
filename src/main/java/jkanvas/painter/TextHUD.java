package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.util.PaintUtil;

/**
 * A HUD that displays multiple lines of text. All lines are displayed in the
 * same color and an all enclosing background is added.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class TextHUD extends HUDRenderpassAdapter {

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
   * @param ctx The context.
   * @return The start position.
   */
  private Point2D getStartPosition(final KanvasContext ctx) {
    final Rectangle2D comp = ctx.getVisibleComponent();
    double x;
    switch(hpos) {
      case LEFT:
        x = comp.getMinX() + padding;
        break;
      case RIGHT:
        x = comp.getMaxX() - padding;
        break;
      default:
        throw new AssertionError();
    }
    double y;
    switch(vpos) {
      case TOP:
        y = comp.getMinY() + padding;
        break;
      case BOTTOM:
        y = comp.getMaxY() - padding;
        break;
      default:
        throw new AssertionError();
    }
    return new Point2D.Double(x, y);
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
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
    final double dirV = vpos == TOP ? 1 : -1;
    final double mulV = vpos == TOP ? 0 : 1;
    final double mulH = hpos == LEFT ? 0 : 1;
    final Point2D cur = getStartPosition(ctx);
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
      final StringDrawer sd = sds[i];
      if(sd == null) {
        continue;
      }
      sd.draw(cur,
          (hpos == LEFT ? StringDrawer.LEFT : StringDrawer.RIGHT),
          (vpos == TOP ? StringDrawer.TOP : StringDrawer.BOTTOM));
      cur.setLocation(cur.getX(), cur.getY() + sd.getHeight() * dirV);
    }
  }

}
