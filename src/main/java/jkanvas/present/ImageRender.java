package jkanvas.present;

import java.awt.Graphics2D;
import java.awt.Image;

import jkanvas.KanvasContext;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

/**
 * An image slide object.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ImageRender extends SlideObject {

  /** The image. */
  private final Image img;
  /** The width of the image. */
  private final double width;
  /** The height of the image. */
  private final double height;
  /** The top offset of the image. */
  private double top;

  /**
   * Creates a centered image slide object.
   * 
   * @param img The image.
   * @param slide The slide.
   * @param vAlign The vertical alignment.
   */
  public ImageRender(final Image img, final Slide slide,
      final VerticalSlideAlignment vAlign) {
    this(img, slide, vAlign, HorizontalSlideAlignment.CENTER);
  }

  /**
   * Creates an image slide object.
   * 
   * @param img The image.
   * @param slide The slide.
   * @param vAlign The vertical alignment.
   * @param hAlign The horizontal alignment.
   */
  public ImageRender(final Image img, final Slide slide,
      final VerticalSlideAlignment vAlign, final HorizontalSlideAlignment hAlign) {
    super(slide, hAlign, vAlign);
    this.img = img;
    width = img.getWidth(null);
    height = img.getHeight(null);
    top = Double.NaN;
  }

  @Override
  public void beforeDraw(final Graphics2D gfx, final SlideMetrics metric) {
    if(Double.isNaN(top)) {
      final Slide s = getSlide();
      final VerticalSlideAlignment vAlign = getVerticalAlignment();
      top = s.getTotalHeight(vAlign);
      s.addHeight(getHeight(), vAlign);
    }
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    g.drawImage(img, 0, 0, null);
  }

  @Override
  public double getWidth() throws IllegalStateException {
    return width;
  }

  @Override
  public double getHeight() throws IllegalStateException {
    return height;
  }

  @Override
  public int getIndent() {
    return 0;
  }

  @Override
  public double getTop() throws IllegalStateException {
    if(Double.isNaN(top)) throw new IllegalStateException("top not initialized");
    return top;
  }

}
