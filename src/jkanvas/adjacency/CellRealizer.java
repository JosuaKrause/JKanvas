package jkanvas.adjacency;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

/**
 * Realizes a cell of an adjacency matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The type of matrix content.
 */
public interface CellRealizer<T> {

  /**
   * Draws a cell.
   * 
   * @param g The graphics context.
   * @param ctx The canvas context.
   * @param rect The cell rectangle.
   * @param matrix The adjacency matrix.
   * @param row The row of the cell.
   * @param col The column of the cell.
   * @param isSelected Whether this cell is selected. Note that this value may
   *          be <code>true</code> even when <code>hasSelection</code> is
   *          <code>false</code>. In this case the selection should be ignored.
   * @param hasSelection Whether any cell in the matrix is selected.
   */
  void drawCell(Graphics2D g, KanvasContext ctx, Rectangle2D rect,
      AdjacencyMatrix<T> matrix, int row, int col,
      boolean isSelected, boolean hasSelection);

}
