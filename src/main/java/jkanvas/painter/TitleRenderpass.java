package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;
import jkanvas.util.StringDrawer;
import jkanvas.util.StringDrawer.Orientation;

/**
 * Adds a title to the given renderpass.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class TitleRenderpass extends Renderpass {

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

  /** The render pass in a list for easier handling. */
  private final List<Renderpass> list;
  /** The decorated render pass. */
  private final Renderpass pass;
  /** The text height. */
  private final double textHeight;
  /** The space between render pass and text. */
  private final double space;
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
  public TitleRenderpass(final String title, final Renderpass pass,
      final double textHeight, final double space) {
    this.textHeight = textHeight;
    this.space = space;
    this.pass = pass;
    titles = new String[] { Objects.requireNonNull(title)};
    pos = Position.ABOVE;
    orientation = Orientation.HORIZONTAL;
    list = Collections.singletonList(pass);
    pass.setParent(this);
    pass.setOffset(0, space + textHeight);
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
        pass.setOffset(add, 0);
        break;
      case RIGHT:
      case BELOW:
        pass.setOffset(0, 0);
        break;
      case ABOVE:
        pass.setOffset(0, add);
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

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    final boolean hor;
    final Rectangle2D box = new Rectangle2D.Double();
    getBoundingBox(box);
    switch(pos) {
      case LEFT:
        box.setFrame(box.getX(), box.getY(), textHeight, box.getHeight());
        hor = false;
        break;
      case RIGHT:
        box.setFrame(box.getWidth() - textHeight, box.getY(),
            textHeight, box.getHeight());
        hor = false;
        break;
      case BELOW:
        box.setFrame(box.getX(), box.getHeight() - textHeight,
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
    RenderpassPainter.draw(list, g, ctx);
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
    final double w = (hor ? box.getWidth() : box.getHeight()) / titles.length;
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final String t : titles) {
      if(hor) {
        cur.setFrame(box.getX() + x, box.getY(), w, box.getHeight());
      } else {
        cur.setFrame(box.getX(), box.getY() + x, box.getWidth(), w);
      }
      StringDrawer.drawInto(g, t, cur, orientation);
      x += w;
    }
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox) {
    pass.getBoundingBox(bbox);
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

  @Override
  public boolean click(final Camera cam, final Point2D position, final MouseEvent e) {
    return RenderpassPainter.click(list, cam, position, e);
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D position, final MouseEvent e) {
    if(RenderpassPainter.doubleClick(list, cam, position, e)) return true;
    if(!USE_DOUBLE_CLICK_DEFAULT) return false;
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    cam.toView(this, AnimationTiming.SMOOTH, null, true);
    return true;
  }

  @Override
  public String getTooltip(final Point2D position) {
    return RenderpassPainter.getTooltip(list, position);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    return RenderpassPainter.moveMouse(list, cur);
  }

  /** The start position of the drag in the render pass coordinates. */
  private Point2D start = null;

  @Override
  public final boolean acceptDrag(final Point2D position, final MouseEvent e) {
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, position);
    final Rectangle2D bbox = new Rectangle2D.Double();
    pass.getBoundingBox(bbox);
    if(!bbox.contains(pos)) return false;
    if(!pass.acceptDrag(pos, e)) return false;
    start = pos;
    return true;
  }

  @Override
  public final void drag(final Point2D _, final Point2D cur,
      final double dx, final double dy) {
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, cur);
    pass.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _, final Point2D end,
      final double dx, final double dy) {
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(pass, end);
    pass.endDrag(start, pos, dx, dy);
  }

  @Override
  public boolean isChanging() {
    return pass.isChanging();
  }

  @Override
  public void processMessage(final String[] ids, final String msg) {
    super.processMessage(ids, msg);
    pass.processMessage(ids, msg);
  }

}
