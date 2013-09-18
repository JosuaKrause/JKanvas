package jkanvas.present;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;

/**
 * A simple text object for a slide.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class TextRender implements SlideObject {

  /** A list bullet point. */
  public static final String BULLET = "\u2022";

  /** The vertical alignment behavior of the text. */
  private final VerticalSlideAlignment vAlign;
  /** The font of the text. */
  private Font font;
  /** The text. */
  private String text;
  /** The size of the text. */
  private double size;
  /** The horizontal alignment behavior of the text. */
  private HorizontalSlideAlignment hAlign;
  /** The line the text is in. */
  private int line;
  /** The indentation of the text. */
  private int indent;
  /** The width of the text. */
  private double width;
  /** The height of the text. */
  private double height;
  /** The vertical offset of the text. */
  private double voff;

  /**
   * Creates an empty text.
   * 
   * @param vAlign The vertical alignment.
   */
  public TextRender(final VerticalSlideAlignment vAlign) {
    this("", vAlign);
  }

  /**
   * Creates a text object.
   * 
   * @param text The initial text.
   * @param vAlign The vertical alignment.
   */
  public TextRender(final String text, final VerticalSlideAlignment vAlign) {
    this(text, vAlign, 0);
  }

  /**
   * Creates a text object.
   * 
   * @param text The initial text.
   * @param vAlign The vertical alignment.
   * @param indent The initial indent.
   */
  public TextRender(final String text,
      final VerticalSlideAlignment vAlign, final int indent) {
    this(text, vAlign, indent, HorizontalSlideAlignment.LEFT);
  }

  /**
   * Creates a text object.
   * 
   * @param text The initial text.
   * @param vAlign The vertical alignment.
   * @param indent The initial indent.
   * @param hAlign The initial horizontal alignment.
   */
  public TextRender(final String text, final VerticalSlideAlignment vAlign,
      final int indent, final HorizontalSlideAlignment hAlign) {
    this.hAlign = Objects.requireNonNull(hAlign);
    this.vAlign = Objects.requireNonNull(vAlign);
    this.text = Objects.requireNonNull(text);
    this.indent = indent;
    size = 0;
    line = Integer.MIN_VALUE;
    width = Double.NaN;
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

  /**
   * Getter.
   * 
   * @return The indentation of the text.
   */
  public int getIndent() {
    return indent;
  }

  /**
   * Setter.
   * 
   * @param hAlign Sets the horizontal alignment.
   */
  public void setHorizontalAlign(final HorizontalSlideAlignment hAlign) {
    this.hAlign = Objects.requireNonNull(hAlign);
  }

  /**
   * Getter.
   * 
   * @return The horizontal alignment.
   */
  public HorizontalSlideAlignment getHorizontalAlign() {
    return hAlign;
  }

  /**
   * Getter.
   * 
   * @return The vertical alignment.
   */
  public VerticalSlideAlignment getVerticalAlign() {
    return vAlign;
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
  public void configure(final Slide slide, final SlideMetrics metric) {
    if(line == Integer.MIN_VALUE) {
      switch(vAlign) {
        case TOP:
          line = slide.getCurrentTopLine();
          slide.incrementTopLine();
          break;
        case BOTTOM:
          line = slide.getCurrentBottomLine();
          slide.incrementBottomLine();
          break;
      }
    }
    size = metric.lineHeight();
  }

  @Override
  public void beforeDraw(final Graphics2D gfx) {
    if(Double.isNaN(width)) {
      if(font == null) {
        font = gfx.getFont().deriveFont((float) size);
      }
      final FontMetrics fm = gfx.getFontMetrics(font);
      width = fm.stringWidth(getText());
      voff = fm.getMaxAscent() + fm.getLeading();
      height = voff + fm.getMaxDescent();
    }
  }

  @Override
  public Point2D getOffset(final SlideMetrics metric) {
    return metric.getOffsetFor(indent, width, hAlign, line, vAlign);
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(0, 0, width, height);
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    g.setColor(Color.BLACK);
    g.setFont(font);
    g.translate(0, voff);
    g.drawString(text, 0, 0);
  }

}
