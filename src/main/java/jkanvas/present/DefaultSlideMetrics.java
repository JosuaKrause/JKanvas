package jkanvas.present;

/**
 * Default slide metrics.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DefaultSlideMetrics extends SlideMetrics {

  @Override
  public double slideWidth() {
    return 800;
  }

  @Override
  public double slideHeight() {
    return 600;
  }

  @Override
  public double slideSpaceHorRatio() {
    return 0.1;
  }

  @Override
  public double lineHeightRatio() {
    return 1.0 / 14.0;
  }

  @Override
  public double verticalOffsetRatio() {
    return 1.0 / 14.0;
  }

  @Override
  public double lineOffsetRatio() {
    return 1.0 / 16.0;
  }

  @Override
  public double lineIndentRatio() {
    return 1.0 / 18.0;
  }

  @Override
  public double lineSpaceRatio() {
    return 1.0 / 4.0;
  }

}
