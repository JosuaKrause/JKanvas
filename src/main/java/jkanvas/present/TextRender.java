package jkanvas.present;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

/**
 * A simple text object for a slide.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class TextRender extends SlideObject {

  /** A list bullet point. */
  public static final String BULLET = "\u2022";

  /** The font of the text. */
  private Font font;
  /** The text. */
  private String text;
  /** The indentation of the text. */
  private int indent;
  /** The size of the text. */
  private double size;
  /** The width of the text. */
  private double width;
  /** The height of the text. */
  private double height;
  /** The vertical offset of the text. */
  private double vOff;
  /** The text offset. */
  private double tOff;

  /**
   * Creates an empty text.
   * 
   * @param owner The owner of the object.
   * @param vAlign The vertical alignment.
   */
  public TextRender(final Slide owner, final VerticalSlideAlignment vAlign) {
    this(owner, "", vAlign);
  }

  /**
   * Creates a text object.
   * 
   * @param owner The owner of the object.
   * @param text The initial text.
   * @param vAlign The vertical alignment.
   */
  public TextRender(final Slide owner, final String text,
      final VerticalSlideAlignment vAlign) {
    this(owner, text, vAlign, 0);
  }

  /**
   * Creates a text object.
   * 
   * @param owner The owner of the object.
   * @param text The initial text.
   * @param vAlign The vertical alignment.
   * @param indent The initial indent.
   */
  public TextRender(final Slide owner, final String text,
      final VerticalSlideAlignment vAlign, final int indent) {
    this(owner, text, vAlign, indent, HorizontalSlideAlignment.LEFT);
  }

  /**
   * Creates a text object.
   * 
   * @param owner The owner of the object.
   * @param text The initial text.
   * @param vAlign The vertical alignment.
   * @param indent The initial indent.
   * @param hAlign The initial horizontal alignment.
   */
  public TextRender(final Slide owner, final String text,
      final VerticalSlideAlignment vAlign,
      final int indent, final HorizontalSlideAlignment hAlign) {
    super(owner, hAlign, vAlign);
    this.text = Objects.requireNonNull(text);
    this.indent = indent;
    size = Double.NaN;
    width = Double.NaN;
    height = Double.NaN;
    vOff = Double.NaN;
    tOff = Double.NaN;
    font = null;
  }

  /**
   * Setter.
   * 
   * @param indent Sets the indentation of the text.
   */
  public void setIndent(final int indent) {
    this.indent = indent;
  }

  @Override
  public int getIndent() {
    return indent;
  }

  /**
   * Setter.
   * 
   * @param text Sets the text.
   */
  public void setText(final String text) {
    this.text = Objects.requireNonNull(text);
    width = Double.NaN;
  }

  /**
   * Getter.
   * 
   * @return The text.
   */
  public String getText() {
    return text;
  }

  @Override
  public void beforeDraw(final Graphics2D gfx, final SlideMetrics metric) {
    final double s = metric.lineHeight();
    if(s != size) {
      size = s;
      if(font == null) {
        font = gfx.getFont().deriveFont((float) size);
      }
      final FontMetrics fm = gfx.getFontMetrics(font);
      width = fm.stringWidth(getText());
      tOff = fm.getMaxAscent() + fm.getLeading();
      height = tOff + fm.getMaxDescent();
    }
    if(Double.isNaN(vOff)) {
      final Slide slide = getSlide();
      final VerticalSlideAlignment vAlign = getVerticalAlignment();
      vOff = slide.getTotalHeight(vAlign);
      slide.addHeight(s, vAlign);
    }
  }

  @Override
  public double getWidth() throws IllegalStateException {
    if(Double.isNaN(width)) throw new IllegalStateException("width not initialized");
    return width;
  }

  @Override
  public double getHeight() throws IllegalStateException {
    if(Double.isNaN(height)) throw new IllegalStateException("height not initialized");
    return height;
  }

  @Override
  public double getTop() throws IllegalStateException {
    if(Double.isNaN(vOff)) throw new IllegalStateException("top not initialized");
    return vOff;
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    g.setColor(Color.BLACK);
    g.setFont(font);
    g.translate(0, tOff);
    g.drawString(text, 0, 0);
  }

}
