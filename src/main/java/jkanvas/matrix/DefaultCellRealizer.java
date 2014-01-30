package jkanvas.matrix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

/**
 * A default implementation for cell realizer. The color of a cell has to be
 * implemented.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <U> The content type.
 * @param <T> The matrix type.
 */
public abstract class DefaultCellRealizer<U, T extends Matrix<U>>
    implements CellRealizer<T> {

  @Override
  public void drawCell(final Graphics2D g, final KanvasContext ctx,
      final Rectangle2D rect, final T matrix, final int row, final int col,
      final boolean isSelected, final boolean hasSelection) {
    final U val = matrix.get(row, col);
    g.setColor(getColor(val, hasSelection && isSelected));
    g.fill(rect);
    g.setColor(getBorder());
    g.draw(rect);
  }

  /**
   * Determines the color for a given value.
   * 
   * @param value The value.
   * @param isSelected Whether the cell is selected.
   * @return The color of the cell.
   */
  protected abstract Color getColor(final U value, final boolean isSelected);

  /**
   * Getter.
   * 
   * @return The color of the border. The default implementation is
   *         {@link Color#BLACK}.
   */
  protected Color getBorder() {
    return Color.BLACK;
  }

}
