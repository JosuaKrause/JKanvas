package jkanvas.present;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.painter.AbstractRenderpass;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

/**
 * A slide.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Slide extends AbstractRenderpass {

  /** The metrics for the slide. */
  private SlideMetrics metric;
  /** The current line counted for the top. */
  private int currentTopLine;
  /** The current line counted for the bottom. */
  private int currentBottomLine;
  /** The current line counted for the center. */
  private int currentCenterLine;
  /** The slide content. */
  private final List<SlideObject> content;

  /** Creates an empty slide. */
  public Slide() {
    metric = null;
    currentTopLine = 0;
    currentBottomLine = 0;
    currentCenterLine = 0;
    content = new ArrayList<>();
  }

  /**
   * Getter.
   * 
   * @param align The alignment.
   * @return The line count for the given alignment.
   */
  public int getLineCount(final VerticalSlideAlignment align) {
    switch(align) {
      case BOTTOM:
        return currentBottomLine;
      case TOP:
        return currentTopLine;
      case CENTER:
        return currentCenterLine;
      default:
        throw new NullPointerException("align");
    }
  }

  /**
   * Increments the line counter for the given alignment.
   * 
   * @param align The alignment.
   */
  public void incrementLine(final VerticalSlideAlignment align) {
    switch(align) {
      case BOTTOM:
        ++currentBottomLine;
        break;
      case TOP:
        ++currentTopLine;
        break;
      case CENTER:
        ++currentCenterLine;
        break;
      default:
        throw new NullPointerException("align");
    }
  }

  /**
   * Getter.
   * 
   * @return Whether the slide has positioning already.
   */
  public boolean isPositioned() {
    return metric != null;
  }

  /** Ensures that elements on the slide can be positioned. */
  protected void checkPositioned() {
    if(!isPositioned()) throw new IllegalStateException("slide must be positioned first");
  }

  /**
   * Setter.
   * 
   * @param metric Sets the metrics for the slide.
   */
  public void setMetric(final SlideMetrics metric) {
    if(isPositioned() && metric != null) throw new IllegalStateException(
        "must remove metrics first");
    this.metric = metric;
  }

  /**
   * Adds a slide object.
   * 
   * @param obj The object to add.
   */
  void add(final SlideObject obj) {
    Objects.requireNonNull(obj);
    content.add(obj);
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D view = ctx.getVisibleCanvas();
    final Rectangle2D outer = getBoundingBox();
    if(!view.intersects(outer)) return;
    gfx.setColor(Color.BLACK);
    gfx.draw(outer);
    for(final SlideObject obj : content) {
      obj.beforeDraw(gfx, metric);
      final Point2D off = obj.getOffset(metric);
      final Rectangle2D bbox = getBoundingBox(obj, off);
      if(!view.intersects(bbox)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      g.setClip(bbox);
      final double dx = off.getX();
      final double dy = off.getY();
      g.translate(dx, dy);
      final KanvasContext c = ctx.translate(dx, dy);
      obj.draw(g, c);
      g.dispose();
    }
  }

  /**
   * Getter.
   * 
   * @param obj The object.
   * @param off The offset.
   * @return The bounding box of the given object with correct offset.
   */
  private static Rectangle2D getBoundingBox(final SlideObject obj, final Point2D off) {
    final Rectangle2D box = obj.getBoundingBox();
    return new Rectangle2D.Double(off.getX(), off.getY(), box.getWidth(), box.getHeight());
  }

  @Override
  public Rectangle2D getBoundingBox() {
    checkPositioned();
    return metric.getBoundingBox();
  }

}
