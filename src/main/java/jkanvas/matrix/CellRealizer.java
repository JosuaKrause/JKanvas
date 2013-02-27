package jkanvas.matrix;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

/**
 * Realizes a cell of a matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <U> The content type.
 * @param <T> The matrix type.
 */
public interface CellRealizer<U, T extends QuadraticMatrix<U>> {

  /**
   * Draws a cell.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   * @param rect The cell rectangle.
   * @param matrix The quadratic matrix.
   * @param row The row of the cell.
   * @param col The column of the cell.
   * @param isSelected Whether this cell is selected. Note that this value may
   *          be <code>true</code> even when <code>hasSelection</code> is
   *          <code>false</code>. In this case the selection should be ignored.
   * @param hasSelection Whether any cell in the matrix is selected.
   */
  void drawCell(Graphics2D g, KanvasContext ctx, Rectangle2D rect, T matrix,
      int row, int col, boolean isSelected, boolean hasSelection);

}
