package jkanvas.painter.pod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;
import jkanvas.util.StringDrawer;
import jkanvas.util.StringDrawer.Orientation;

/**
 * Adds a title to the given renderpass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The innermost wrapped type.
 */
public class TitleRenderpass<T extends Renderpass> extends Renderpod<T> {

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

  /** The text height. */
  private final double textHeight;
  /** The space between render pass and text. */
  private final double space;
  /** The space between title texts. */
  private double titleSpace;
  /** The title texts. */
  private String[] titles;
  /** The position of the titles. */
  private Position pos;
  /** The orientation of the titles. */
  private Orientation orientation;

  /**
   * Creates a title at the top for the given render pass.
   * 
   * @param title The initial title.
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public TitleRenderpass(final T pass, final String title,
      final double textHeight, final double space) {
    super(pass);
    this.textHeight = textHeight;
    this.space = space;
    titleSpace = 0;
    titles = new String[] { Objects.requireNonNull(title)};
    pos = Position.ABOVE;
    orientation = Orientation.HORIZONTAL;
    setChildOffset(0, space + textHeight);
  }

  /**
   * Creates a title at the top for the given render pass.
   * 
   * @param title The initial title.
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public TitleRenderpass(final Renderpod<T> pass,
      final String title, final double textHeight, final double space) {
    super(pass);
    this.textHeight = textHeight;
    this.space = space;
    titleSpace = 0;
    titles = new String[] { Objects.requireNonNull(title)};
    pos = Position.ABOVE;
    orientation = Orientation.HORIZONTAL;
    setChildOffset(0, space + textHeight);
  }

  /**
   * Setter.
   * 
   * @param title The title.
   */
  public void setTitle(final String title) {
    titles = new String[] { Objects.requireNonNull(title)};
  }

  /**
   * Setter.
   * 
   * @param titles The titles.
   */
  public void setTitles(final String... titles) {
    Objects.requireNonNull(titles);
    if(titles.length == 0) throw new IllegalArgumentException();
    for(final String s : titles) {
      Objects.requireNonNull(s);
    }
    this.titles = titles.clone();
  }

  /**
   * Getter.
   * 
   * @param num The index of the title.
   * @return The title.
   */
  public String getTitle(final int num) {
    return titles[num];
  }

  /**
   * Getter.
   * 
   * @return All titles.
   */
  public String[] getTitles() {
    return titles.clone();
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
  public int count() {
    return titles.length;
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
   * Setter.
   * 
   * @param titleSpace The space between titles.
   */
  public void setTitleSpace(final double titleSpace) {
    this.titleSpace = titleSpace;
  }

  /**
   * Getter.
   * 
   * @return The space between titles.
   */
  public double getTitleSpace() {
    return titleSpace;
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
        box.setFrame(box.getX() + add + box.getWidth() - textHeight, box.getY(),
            textHeight, box.getHeight());
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
    g.setColor(Color.BLACK);
    drawTexts(g, box, hor);
  }

  /**
   * Draws the titles in the given rectangle.
   * 
   * @param g The graphics context.
   * @param box The box to draw in.
   * @param hor Whether the box is horizontally aligned.
   */
  private void drawTexts(final Graphics2D g, final Rectangle2D box, final boolean hor) {
    double x = 0;
    final double totalW = (hor ? box.getWidth() : box.getHeight());
    final double w = (totalW - titleSpace * (titles.length - 1)) / titles.length;
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final String t : titles) {
      if(hor) {
        cur.setFrame(box.getX() + x, box.getY(), w, box.getHeight());
      } else {
        cur.setFrame(box.getX(), box.getY() + x, box.getWidth(), w);
      }
      StringDrawer.drawInto(g, t, cur, orientation);
      x += w + titleSpace;
    }
  }

  @Override
  protected void addOwnBox(final Rectangle2D bbox) {
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
