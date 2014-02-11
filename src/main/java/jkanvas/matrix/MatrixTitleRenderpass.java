package jkanvas.matrix;

import jkanvas.painter.pod.AbstractTitleRenderpass;
import jkanvas.painter.pod.Renderpod;

/**
 * Shows row and column names as titles.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The matrix render pass.
 */
public class MatrixTitleRenderpass<T extends MatrixRenderpass<? extends Matrix<?>>>
    extends AbstractTitleRenderpass<T> {

  /**
   * Creates a matrix title render pass.
   * 
   * @param rp The render pass.
   * @param textHeight The text height.
   * @param space The space.
   */
  public MatrixTitleRenderpass(final T rp, final double textHeight, final double space) {
    super(rp, textHeight, space);
  }

  /**
   * Creates a matrix title render pass.
   * 
   * @param wrap The parent render pod.
   * @param textHeight The text height.
   * @param space The space.
   */
  public MatrixTitleRenderpass(
      final Renderpod<T> wrap, final double textHeight, final double space) {
    super(wrap, textHeight, space);
  }

  /**
   * Getter.
   * 
   * @return Whether the title are row names.
   */
  protected boolean isRows() {
    switch(getPosition()) {
      case ABOVE:
      case BELOW:
        return false;
      case LEFT:
      case RIGHT:
        return true;
      default:
        throw new AssertionError();
    }
  }

  /**
   * Getter.
   * 
   * @return The matrix.
   */
  protected Matrix<?> getMatrix() {
    return unwrap().getMatrix();
  }

  @Override
  protected double getWidth(final double totalWidth, final int index) {
    final Matrix<?> m = getMatrix();
    return isRows() ? m.getHeight(index) : m.getWidth(index);
  }

  @Override
  protected double getTitleSpace(final int index) {
    return 0;
  }

  @Override
  public String getTitle(final int index) {
    final Matrix<?> m = getMatrix();
    return isRows() ? m.getRowName(index) : m.getColumnName(index);
  }

  @Override
  public int getTitleCount() {
    final Matrix<?> m = getMatrix();
    return isRows() ? m.rows() : m.cols();
  }

}
