package jkanvas.present;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.json.JSONElement;
import jkanvas.painter.AbstractRenderpass;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

/**
 * A slide.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Slide extends AbstractRenderpass {

  /** The metrics for the slide. */
  private final SlideMetrics metric;
  /** The current height of top objects. */
  private double currentTopHeight;
  /** The current height of bottom objects. */
  private double currentBottomHeight;
  /** The current height of center objects. */
  private double currentCenterHeight;
  /** The slide content. */
  private final List<SlideObject> content;

  /**
   * Creates an empty slide.
   * 
   * @param metric The metric for the slide.
   */
  public Slide(final SlideMetrics metric) {
    this.metric = metric;
    currentTopHeight = 0;
    currentBottomHeight = 0;
    currentCenterHeight = 0;
    content = new ArrayList<>();
  }

  /**
   * Parses a JSON element.
   * 
   * @param json The JSON element.
   */
  public void parseJSON(final JSONElement json) {
    json.expectObject();
    if(json.hasValue("top")) {
      final JSONElement top = json.getValue("top");
      top.expectArray();
      for(final JSONElement el : top) {
        getFor(el, VerticalSlideAlignment.TOP);
      }
    }
    if(json.hasValue("center")) {
      final JSONElement center = json.getValue("center");
      center.expectArray();
      for(final JSONElement el : center) {
        getFor(el, VerticalSlideAlignment.CENTER);
      }
    }
    if(json.hasValue("bottom")) {
      final JSONElement bottom = json.getValue("bottom");
      bottom.expectArray();
      for(final JSONElement el : bottom) {
        getFor(el, VerticalSlideAlignment.BOTTOM);
      }
    }
  }

  /**
   * Creates a slide object for the given JSON element.
   * 
   * @param el The JSON element.
   * @param vAlign The vertical alignment of the object.
   * @return The slide object. The object is already added to the slide.
   */
  private SlideObject getFor(final JSONElement el, final VerticalSlideAlignment vAlign) {
    if(el.isString()) return new TextRender(this, el.string(), vAlign);
    if(!el.isObject()) throw new IllegalArgumentException("el must be a string or object");
    final HorizontalSlideAlignment hAlign;
    final String ha = el.getString("align", "left");
    switch(ha) {
      case "left":
        hAlign = HorizontalSlideAlignment.LEFT;
        break;
      case "center":
        hAlign = HorizontalSlideAlignment.CENTER;
        break;
      case "right":
        hAlign = HorizontalSlideAlignment.RIGHT;
        break;
      default:
        throw new IllegalArgumentException("illegal hAlign: " + ha);
    }
    return fromJSONloader(el, vAlign, hAlign);
  }

  /**
   * Loads a slide object from a JSON loader.
   * 
   * @param el The element.
   * @param vAlign The vertical alignment.
   * @param hAlign The horizontal alignment.
   * @return The slide object.
   */
  private SlideObject fromJSONloader(final JSONElement el,
      final VerticalSlideAlignment vAlign, final HorizontalSlideAlignment hAlign) {
    final String type = el.getString("type", "");
    try {
      final Class<?> clz = Class.forName(type);
      final Class<SlideObject> so = SlideObject.class;
      if(!so.isAssignableFrom(clz)) throw new IllegalArgumentException(
          "class " + type + " must be a " + so.getName());
      final Method m = clz.getDeclaredMethod("loadFromJSON",
          JSONElement.class, Slide.class, HorizontalSlideAlignment.class,
          VerticalSlideAlignment.class);
      if(!clz.isAssignableFrom(m.getReturnType())) throw new IllegalArgumentException(
          "return type of method must be a " + clz.getName());
      return (SlideObject) m.invoke(null, el, this, hAlign, vAlign);
    } catch(final ClassNotFoundException | NoSuchMethodException | SecurityException
        | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Getter.
   * 
   * @param align The alignment.
   * @return The total height of the given alignment.
   */
  public double getTotalHeight(final VerticalSlideAlignment align) {
    switch(align) {
      case BOTTOM:
        return currentBottomHeight;
      case TOP:
        return currentTopHeight;
      case CENTER:
        return currentCenterHeight;
      default:
        throw new NullPointerException("align");
    }
  }

  /**
   * Adds height to the given alignment.
   * 
   * @param height The height without additional space.
   * @param align The alignment.
   */
  public void addHeight(final double height, final VerticalSlideAlignment align) {
    switch(align) {
      case BOTTOM:
        currentBottomHeight += height + metric.lineSpace();
        break;
      case TOP:
        currentTopHeight += height + metric.lineSpace();
        break;
      case CENTER:
        currentCenterHeight += height + metric.lineSpace();
        break;
      default:
        throw new NullPointerException("align");
    }
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
    }
    for(final SlideObject obj : content) {
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
    return metric.getBoundingBox();
  }

}
