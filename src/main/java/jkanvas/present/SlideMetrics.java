package jkanvas.present;

import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

import jkanvas.io.json.JSONElement;

/**
 * Metrics for slides.
 * 
 * @see Slide
 * @see Presentation
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class SlideMetrics {

  /**
   * Horizontal alignments for objects in a slide.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public static enum HorizontalSlideAlignment {
    /** The object is aligned to the left side of the slide. */
    LEFT,
    /** The object is aligned to the center of the slide. */
    CENTER,
    /** The object is aligned to the right side of the slide. */
    RIGHT,
  } // HorizontalSlideAlignment

  /**
   * Vertical alignments for objects in a slide.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public static enum VerticalSlideAlignment {
    /** The object is aligned to the top of the slide. */
    TOP,
    /** The object is aligned in the center of the slide. */
    CENTER,
    /** The object is aligned to the bottom of the slide. */
    BOTTOM,
  } // VerticalSlideAlignment

  /**
   * Getter.
   * 
   * @return The width of the slide.
   */
  public abstract double slideWidth();

  /**
   * Getter.
   * 
   * @return The height of the slide.
   */
  public abstract double slideHeight();

  /**
   * Getter.
   * 
   * @return Horizontal space between slides as ratio of slide width.
   * @see #slideWidth()
   */
  public abstract double slideSpaceHorRatio();

  /**
   * Getter.
   * 
   * @return Horizontal space between slides.
   */
  public double slideSpaceHor() {
    return slideSpaceHorRatio() * slideWidth();
  }

  /**
   * Getter.
   * 
   * @return Line height as ratio of slide height.
   * @see #slideHeight()
   */
  public abstract double lineHeightRatio();

  /**
   * Getter.
   * 
   * @return The line height.
   */
  public double lineHeight() {
    return lineHeightRatio() * slideHeight();
  }

  /**
   * Getter.
   * 
   * @return The vertical offset as ratio of slide height.
   * @see #slideHeight()
   */
  public abstract double verticalOffsetRatio();

  /**
   * Getter.
   * 
   * @return The vertical offset.
   */
  public double verticalOffset() {
    return verticalOffsetRatio() * slideHeight();
  }

  /**
   * Getter.
   * 
   * @return Horizontal line offset as ratio of slide width.
   * @see #slideWidth()
   */
  public abstract double lineOffsetRatio();

  /**
   * Getter.
   * 
   * @return The horizontal line offset.
   */
  public double lineOffset() {
    return lineOffsetRatio() * slideWidth();
  }

  /**
   * Getter.
   * 
   * @return Horizontal line indent as ratio of slide width.
   * @see #slideWidth()
   */
  public abstract double lineIndentRatio();

  /**
   * Getter.
   * 
   * @return Horizontal line indent.
   */
  public double lineIndent() {
    return lineIndentRatio() * slideWidth();
  }

  /**
   * Getter.
   * 
   * @return Vertical space between lines as ratio of line height.
   * @see #lineHeight()
   */
  public abstract double lineSpaceRatio();

  /**
   * Getter.
   * 
   * @return Vertical space between lines.
   */
  public double lineSpace() {
    return lineSpaceRatio() * lineHeight();
  }

  /**
   * Getter.
   * 
   * @param bbox The rectangle in which the bounding box of the slide is stored.
   */
  public void getBoundingBox(final RectangularShape bbox) {
    bbox.setFrame(0, 0, slideWidth(), slideHeight());
  }

  /**
   * Computes the horizontal offset for the given configuration.
   * 
   * @param indents The indentation. This value is ignored when the alignment is
   *          set to {@link HorizontalSlideAlignment#CENTER}.
   * @param width The width of the object to position.
   * @param align The alignment.
   * @return The left offset of the given configuration.
   */
  public double getHorizontalOffsetFor(
      final int indents, final double width, final HorizontalSlideAlignment align) {
    switch(align) {
      case LEFT:
        return lineOffset() + indents * lineIndent();
      case RIGHT:
        return slideWidth() - width - lineOffset() - indents * lineIndent();
      case CENTER:
        return (slideWidth() - width) * 0.5;
      default:
        throw new NullPointerException("align");
    }
  }

  /**
   * Computes the vertical offset for the given configuration.
   * 
   * @param vOff The vertical offset of the object.
   * @param ownHeight The height of the object.
   * @param totalHeight The total height of all objects with the same alignment.
   * @param align The alignment.
   * @return The top offset of the given configuration.
   */
  public double getVerticalOffsetFor(final double vOff, final double ownHeight,
      final double totalHeight, final VerticalSlideAlignment align) {
    switch(align) {
      case TOP:
        return verticalOffset() + vOff;
      case BOTTOM:
        return slideHeight() - vOff - ownHeight;
      case CENTER:
        return (slideHeight() - totalHeight) * 0.5 + vOff;
      default:
        throw new NullPointerException("align");
    }
  }

  /**
   * Computes the offset of the given configuration.
   * 
   * @param indents The indentation.
   * @param width The width of the object.
   * @param hAlign The horizontal alignment.
   * @param vOff The vertical offset of the object.
   * @param ownHeight The height of the object.
   * @param totalHeight The total height of all objects with the same vertical
   *          alignment.
   * @param vAlign The vertical alignment.
   * @return The top left offset of the object.
   */
  public Point2D getOffsetFor(
      final int indents, final double width, final HorizontalSlideAlignment hAlign,
      final double vOff, final double ownHeight, final double totalHeight,
      final VerticalSlideAlignment vAlign) {
    return new Point2D.Double(getHorizontalOffsetFor(indents, width, hAlign),
        getVerticalOffsetFor(vOff, ownHeight, totalHeight, vAlign));
  }

  /**
   * Creates a slide metric from the given JSON element.
   * 
   * @param el The JSON element.
   * @param base The default values.
   * @return The slide metric.
   */
  public static SlideMetrics loadFromJSON(
      final JSONElement el, final SlideMetrics base) {
    return new JSONSlideMetrics(el, base);
  }

}
