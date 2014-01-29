package jkanvas.present;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import jkanvas.KanvasContext;
import jkanvas.io.json.JSONElement;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;
import jkanvas.util.Resource;

/**
 * An image slide object.
 * 
 * @author Joschi <josua.krause@gmail.com>
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
    this(img, slide, vAlign, HorizontalSlideAlignment.CENTER, -1, -1);
  }

  /**
   * Creates an image slide object.
   * 
   * @param img The image.
   * @param slide The slide.
   * @param vAlign The vertical alignment.
   * @param hAlign The horizontal alignment.
   * @param width The requested width or -1 for automatic width.
   * @param height The requested height or -1 for automatic width.
   */
  public ImageRender(final Image img, final Slide slide,
      final VerticalSlideAlignment vAlign, final HorizontalSlideAlignment hAlign,
      final int width, final int height) {
    super(slide, hAlign, vAlign);
    Objects.requireNonNull(img);
    if(width < 0 && height < 0) {
      this.img = img;
    } else if(width == img.getWidth(null) && height == img.getHeight(null)) {
      this.img = img;
    } else {
      this.img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
    this.width = this.img.getWidth(null);
    this.height = this.img.getHeight(null);
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

  /**
   * Creates an image object from a JSON element.
   * 
   * @param el The element.
   * @param slide The slide.
   * @param hAlign The horizontal alignment.
   * @param vAlign The vertical alignment.
   * @return The image object.
   * @throws IOException I/O Exception.
   */
  public static ImageRender loadFromJSON(final JSONElement el, final Slide slide,
      final HorizontalSlideAlignment hAlign, final VerticalSlideAlignment vAlign)
      throws IOException {
    final String src = el.getString("src", null);
    final int w = el.getInt("width", -1);
    final int h = el.getInt("height", -1);
    final Resource r = Resource.getFor(src);
    final BufferedImage img = ImageIO.read(r.stream());
    return new ImageRender(img, slide, vAlign, hAlign, w, h);
  }

}
