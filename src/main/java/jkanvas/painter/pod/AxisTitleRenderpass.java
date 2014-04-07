package jkanvas.painter.pod;

import jkanvas.painter.Renderpass;

/**
 * Shows axis labels.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content render pass.
 */
public class AxisTitleRenderpass<T extends Renderpass> extends TitleRenderpass<T> {

  /**
   * Creates a new axis render pass.
   * 
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   * @param min The string for the minimal value.
   * @param max The string for the maximal value.
   * @param title The title of the axis.
   */
  public AxisTitleRenderpass(final T pass, final double textHeight,
      final double space, final String min, final String max, final String title) {
    super(pass, textHeight, space, min, title, max);
  }

  /**
   * Creates a new axis render pass.
   * 
   * @param pass The render pass.
   * @param textHeight The text height.
   * @param space The space.
   * @param min The string for the minimal value.
   * @param max The string for the maximal value.
   * @param title The title of the axis.
   */
  public AxisTitleRenderpass(final Renderpod<T> pass, final double textHeight,
      final double space, final String min, final String max, final String title) {
    super(pass, textHeight, space, min, title, max);
  }

  @Override
  public void setTitle(final String title) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTitles(final String... titles) {
    throw new UnsupportedOperationException();
  }

  /**
   * Getter.
   * 
   * @return The string for the minimal value.
   */
  public String getMin() {
    return getTitle(0);
  }

  /**
   * Getter.
   * 
   * @return The string for the maximal value.
   */
  public String getMax() {
    return getTitle(2);
  }

  /**
   * Getter.
   * 
   * @return The axis title.
   */
  public String getName() {
    return getTitle(1);
  }

  /**
   * Setter.
   * 
   * @param max The string for the maximal value.
   */
  public void setMax(final String max) {
    super.setTitles(getMin(), getName(), max);
  }

  /**
   * Setter.
   * 
   * @param min The string for the minimal value.
   */
  public void setMin(final String min) {
    super.setTitles(min, getName(), getMax());
  }

  /**
   * Setter.
   * 
   * @param name The title of the axis.
   */
  public void setName(final String name) {
    super.setTitles(getMin(), name, getMax());
  }

  /**
   * Setter.
   * 
   * @param min The string for the minimal value.
   * @param max The string for the maximal value.
   * @param name The title of the axis.
   */
  public void setValues(final String min, final String max, final String name) {
    super.setTitles(min, name, max);
  }

  /**
   * Getter.
   * 
   * @return Whether the text position is on the side.
   */
  protected boolean onSide() {
    return getPosition() == Position.LEFT || getPosition() == Position.RIGHT;
  }

  @Override
  public String getTitle(final int num) {
    if(onSide()) return super.getTitle(2 - num);
    return super.getTitle(num);
  }

  @Override
  public Alignment getAlignment(final int index) {
    if(index == 1) return Alignment.CENTER;
    if(onSide()) return index == 0 ? Alignment.RIGHT : Alignment.LEFT;
    return index == 0 ? Alignment.LEFT : Alignment.RIGHT;
  }

  @Override
  protected double getIndividualTextHeight(final int index) {
    return (index == 1 ? 1 : 0.75) * getTextHeight();
  }

}
