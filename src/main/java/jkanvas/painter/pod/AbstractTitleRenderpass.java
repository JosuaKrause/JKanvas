package jkanvas.painter.pod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;
import jkanvas.util.StringDrawer;
import jkanvas.util.StringDrawer.Orientation;

/**
 * An abstract title render pass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The wrapped render pass type.
 */
public abstract class AbstractTitleRenderpass<T extends Renderpass> extends Renderpod<T> {

  /**
   * The position of the titles.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public static enum Position {
    /** Above the render pass. */
    ABOVE,
    /** Left of the render pass. */
    LEFT,
    /** Below the render pass. */
    BELOW,
    /** Right of the render pass. */
    RIGHT,
    // EOD
    ;

  } // Position

  /**
   * The alignment of the titles.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public static enum Alignment {
    /** Text is aligned left w.r.t. drawing direction. */
    LEFT(StringDrawer.LEFT),
    /** Text is centered w.r.t. drawing direction. */
    CENTER(StringDrawer.CENTER_H),
    /** Text is aligned right w.r.t. drawing direction. */
    RIGHT(StringDrawer.RIGHT),
    // EOD
    ;

    /** The alignment value. */
    private final int alignX;

    /**
     * Creates an alignment.
     * 
     * @param alignX The alignment value from {@link StringDrawer}.
     */
    private Alignment(final int alignX) {
      this.alignX = alignX;
    }

    /**
     * Getter.
     * 
     * @return The alignment value.
     */
    public int getAlignment() {
      return alignX;
    }

  } // Alignment

  /** The text height. */
  private final double textHeight;
  /** The space between render pass and text. */
  private final double space;
  /** The position of the titles. */
  private Position pos;
  /** The orientation of the titles. */
  private Orientation orientation;
  /** The alignment of the titles. */
  private Alignment align;

  /**
   * Creates an abstract title render pass.
   * 
   * @param pass The wrapped render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public AbstractTitleRenderpass(
      final T pass, final double textHeight, final double space) {
    super(pass);
    this.textHeight = textHeight;
    this.space = space;
    pos = Position.ABOVE;
    orientation = Orientation.HORIZONTAL;
    align = Alignment.CENTER;
  }

  /**
   * Creates an abstract title render pass.
   * 
   * @param wrap The wrapped render pod.
   * @param textHeight The text height.
   * @param space The space.
   */
  public AbstractTitleRenderpass(
      final Renderpod<T> wrap, final double textHeight, final double space) {
    super(wrap);
    this.textHeight = textHeight;
    this.space = space;
    pos = Position.ABOVE;
    orientation = Orientation.HORIZONTAL;
    align = Alignment.CENTER;
  }

  /**
   * Setter.
   * 
   * @param align The alignment of the titles w.r.t. the drawing direction.
   */
  public void setAlignment(final Alignment align) {
    this.align = Objects.requireNonNull(align);
  }

  /**
   * Getter.
   * 
   * @param index The index of the title.
   * @return The alignment of the titles w.r.t. the drawing direction.
   */
  public Alignment getAlignment(@SuppressWarnings("unused") final int index) {
    return align;
  }

  /**
   * Setter.
   * 
   * @param pos The position of the titles.
   */
  public void setPosition(final Position pos) {
    this.pos = Objects.requireNonNull(pos);
    final double add = textHeight + space;
    switch(pos) {
      case LEFT:
        setChildOffset(add, 0);
        break;
      case RIGHT:
      case BELOW:
        setChildOffset(0, 0);
        break;
      case ABOVE:
        setChildOffset(0, add);
        break;
      default:
        throw new AssertionError();
    }
  }

  /**
   * Getter.
   * 
   * @return The position of the titles.
   */
  public Position getPosition() {
    return pos;
  }

  /**
   * Setter.
   * 
   * @param orientation The orientation of the titles.
   */
  public void setOrientation(final Orientation orientation) {
    this.orientation = Objects.requireNonNull(orientation);
  }

  /**
   * Getter.
   * 
   * @return The orientation of the titles.
   */
  public Orientation getOrientation() {
    return orientation;
  }

  /**
   * Getter.
   * 
   * @return The text height.
   */
  public double getTextHeight() {
    return textHeight;
  }

  /**
   * Getter.
   * 
   * @param index The index of the title.
   * @return The text height of the title.
   */
  protected double getIndividualTextHeight(@SuppressWarnings("unused") final int index) {
    return textHeight;
  }

  /**
   * Getter.
   * 
   * @return The space.
   */
  public double getSpace() {
    return space;
  }

  /**
   * Getter.
   * 
   * @return The number of titles.
   */
  public abstract int getTitleCount();

  /**
   * Getter.
   * 
   * @param index The index of the title.
   * @return The title.
   */
  public abstract String getTitle(int index);

  /**
   * Getter.
   * 
   * @param totalWidth The total width.
   * @param index The index of the title.
   * @return The width of the given title.
   */
  protected abstract double getWidth(double totalWidth, int index);

  /**
   * Getter.
   * 
   * @param index The index of the title.
   * @return The space between titles.
   */
  protected abstract double getTitleSpace(int index);

  /**
   * Draws the titles in the given rectangle.
   * 
   * @param g The graphics context.
   * @param box The box to draw in.
   * @param hor Whether the box is horizontally aligned.
   * @param view The currently visible portion of the canvas.
   */
  private void drawTexts(final Graphics2D g, final Rectangle2D box,
      final boolean hor, final Rectangle2D view) {
    double x = 0;
    final double totalW = (hor ? box.getWidth() : box.getHeight());
    final Rectangle2D cur = new Rectangle2D.Double();
    for(int i = 0; i < getTitleCount(); ++i) {
      final String t = getTitle(i);
      final double w = getWidth(totalW, i);
      final double h = getIndividualTextHeight(i);
      if(hor) {
        cur.setFrame(box.getX() + x, box.getY(), w, h);
      } else {
        cur.setFrame(box.getX(), box.getY() + x, h, w);
      }
      if(view.intersects(cur)) {
        StringDrawer.drawInto(g, t, cur, orientation, getAlignment(i).getAlignment());
      }
      x += w + getTitleSpace(i);
    }
  }

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    final boolean hor;
    final Rectangle2D box = new Rectangle2D.Double();
    getInnerBoundingBox(box);
    final double add = textHeight + space;
    switch(pos) {
      case LEFT:
        box.setFrame(box.getX(), box.getY(), textHeight, box.getHeight());
        hor = false;
        break;
      case RIGHT:
        box.setFrame(box.getX() + add + box.getWidth() - textHeight,
            box.getY(), textHeight, box.getHeight());
        hor = false;
        break;
      case BELOW:
        box.setFrame(box.getX(), box.getY() + add + box.getHeight() - textHeight,
            box.getWidth(), textHeight);
        hor = true;
        break;
      case ABOVE:
        box.setFrame(box.getX(), box.getY(), box.getWidth(), textHeight);
        hor = true;
        break;
      default:
        throw new AssertionError();
    }
    final Rectangle2D view = ctx.getVisibleCanvas();
    if(!view.intersects(box)) return;
    g.setColor(Color.BLACK);
    drawTexts(g, box, hor, view);
  }

  @Override
  protected void addOwnBox(final RectangularShape bbox) {
    final double add = textHeight + space;
    switch(pos) {
      case LEFT:
      case RIGHT:
        bbox.setFrame(bbox.getX(), bbox.getY(),
            bbox.getWidth() + add, bbox.getHeight());
        break;
      case BELOW:
      case ABOVE:
        bbox.setFrame(bbox.getX(), bbox.getY(),
            bbox.getWidth(), bbox.getHeight() + add);
        break;
      default:
        throw new AssertionError();
    }
  }

}
