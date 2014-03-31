package jkanvas.painter.pod;

import jkanvas.painter.Renderpass;

public class AxisTitleRenderpass<T extends Renderpass> extends TitleRenderpass<T> {

  public AxisTitleRenderpass(final T pass, final double textHeight,
      final double space, final String min, final String max, final String title) {
    super(pass, textHeight, space, min, title, max);
  }

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

  public String getMin() {
    return getTitle(0);
  }

  public String getMax() {
    return getTitle(2);
  }

  public String getName() {
    return getTitle(1);
  }

  public void setMax(final String max) {
    super.setTitles(getMin(), getName(), max);
  }

  public void setMin(final String min) {
    super.setTitles(min, getName(), getMax());
  }

  public void setName(final String name) {
    super.setTitles(getMin(), name, getMax());
  }

  public void setValues(final String min, final String max, final String name) {
    super.setTitles(min, name, max);
  }

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
