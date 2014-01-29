package jkanvas.present;

import jkanvas.io.json.JSONElement;

/**
 * JSON specified slide metrics.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class JSONSlideMetrics extends SlideMetrics {

  /**
   * Creates {@link SlideMetrics} for the given JSON element.
   * 
   * @param json The JSON element.
   * @param base The default values for the metrics.
   */
  JSONSlideMetrics(final JSONElement json, final SlideMetrics base) {
    json.expectObject();
    width = json.getDouble("width", base.slideWidth());
    height = json.getDouble("height", base.slideHeight());
    spaceHorRatio = json.getPercentage("slideSpace", base.slideSpaceHorRatio());
    lineHeightRatio = json.getPercentage("lineHeight", base.lineHeightRatio());
    vOffsetRatio = json.getPercentage("verticalOffset", base.verticalOffsetRatio());
    lineOffsetRatio = json.getPercentage("horizontalOffset", base.lineOffsetRatio());
    indentRatio = json.getPercentage("indent", base.lineIndentRatio());
    spaceRatio = json.getPercentage("lineSpace", base.lineSpaceRatio());
    spaceHor = super.slideSpaceHor();
    lineHeight = super.lineHeight();
    vOffset = super.verticalOffset();
    lineOffset = super.lineOffset();
    indent = super.lineIndent();
    space = super.lineSpace();
  }

  /** The cached width. */
  private final double width;

  @Override
  public double slideWidth() {
    return width;
  }

  /** The cached height. */
  private final double height;

  @Override
  public double slideHeight() {
    return height;
  }

  /** The cached slide space ratio. */
  private final double spaceHorRatio;

  @Override
  public double slideSpaceHorRatio() {
    return spaceHorRatio;
  }

  /** The cached slide space. */
  private final double spaceHor;

  @Override
  public double slideSpaceHor() {
    return spaceHor;
  }

  /** The cached line height ratio. */
  private final double lineHeightRatio;

  @Override
  public double lineHeightRatio() {
    return lineHeightRatio;
  }

  /** The cached line height. */
  private final double lineHeight;

  @Override
  public double lineHeight() {
    return lineHeight;
  }

  /** The cached vertical offset ratio. */
  private final double vOffsetRatio;

  @Override
  public double verticalOffsetRatio() {
    return vOffsetRatio;
  }

  /** The cached vertical offset. */
  private final double vOffset;

  @Override
  public double verticalOffset() {
    return vOffset;
  }

  /** The cached line offset ratio. */
  private final double lineOffsetRatio;

  @Override
  public double lineOffsetRatio() {
    return lineOffsetRatio;
  }

  /** The cached line offset. */
  private final double lineOffset;

  @Override
  public double lineOffset() {
    return lineOffset;
  }

  /** The cached line indent ratio. */
  private final double indentRatio;

  @Override
  public double lineIndentRatio() {
    return indentRatio;
  }

  /** The cached line indent. */
  private final double indent;

  @Override
  public double lineIndent() {
    return indent;
  }

  /** The cached line space ratio. */
  private final double spaceRatio;

  @Override
  public double lineSpaceRatio() {
    return spaceRatio;
  }

  /** The cached line space. */
  private final double space;

  @Override
  public double lineSpace() {
    return space;
  }

}
