package jkanvas.adjacency;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.painter.PainterAdapter;

/**
 * Paints an adjacency matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
public abstract class MatrixPainter<T> extends PainterAdapter {

  /** The matrix. */
  private AdjacencyMatrix<T> matrix;
  /** The cell drawer. */
  private CellRealizer<T> cellDrawer;

  /**
   * Creates a matrix painter.
   * 
   * @param matrix The matrix.
   * @param cellDrawer The cell realizer.
   */
  public MatrixPainter(final AdjacencyMatrix<T> matrix, final CellRealizer<T> cellDrawer) {
    this.matrix = Objects.requireNonNull(matrix);
    this.cellDrawer = Objects.requireNonNull(cellDrawer);
  }

  /**
   * Setter.
   * 
   * @param m The new matrix.
   */
  public void setMatrix(final AdjacencyMatrix<T> m) {
    Objects.requireNonNull(m);
    m.inheritRefreshables(matrix);
    matrix = m;
    matrix.refreshAll();
  }

  /**
   * Getter.
   * 
   * @return The matrix.
   */
  public AdjacencyMatrix<T> getMatrix() {
    return matrix;
  }

  /**
   * Setter.
   * 
   * @param cellDrawer The cell realizer.
   */
  public void setCellRealizer(final CellRealizer<T> cellDrawer) {
    this.cellDrawer = cellDrawer;
    matrix.refreshAll();
  }

  /**
   * Getter.
   * 
   * @return The cell realizer.
   */
  public CellRealizer<T> getCellDrawer() {
    return cellDrawer;
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    final boolean hasSelection = hasSelection();
    double y = 0;
    for(int row = 0; row < matrix.size(); ++row) {
      double x = 0;
      final double h = matrix.getHeight(row);
      for(int col = 0; col < matrix.size(); ++col) {
        final double w = matrix.getWidth(col);
        final Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
        final boolean sel = isSelected(row, col);
        final Graphics2D g = (Graphics2D) gfx.create();
        cellDrawer.drawCell(g, ctx, rect, matrix, row, col, sel, hasSelection);
        g.dispose();
        x += w;
      }
      y += h;
    }
  }

  /**
   * Indicates if a cell is currently selected.
   * 
   * @param row The row.
   * @param col The column.
   * @return If the cell is selected.
   */
  protected abstract boolean isSelected(int row, int col);

  /**
   * Indicates whether any cell in the matrix is selected. This can be used to
   * temporarily deselect all cells by returning <code>false</code>.
   * 
   * @return Whether any cell is currently selected. Defaults to
   *         <code>true</code>.
   */
  protected boolean hasSelection() {
    return true;
  }

  /**
   * Finds the cell at the given position.
   * 
   * @param pos The position.
   * @return The cell at the position or <code>null</code> if there is no cell.
   */
  protected MatrixPosition pick(final Point2D pos) {
    int col = -1;
    double w = 0;
    for(int i = 0; i < matrix.size(); ++i) {
      if(w > pos.getX()) {
        break;
      }
      w += matrix.getWidth(i);
      ++col;
    }
    int row = -1;
    double h = 0;
    for(int i = 0; i < matrix.size(); ++i) {
      if(h > pos.getY()) {
        break;
      }
      h += matrix.getHeight(i);
      ++row;
    }
    final boolean missed = col < 0 || row < 0 || w <= pos.getX() || h <= pos.getY();
    return missed ? null : new MatrixPosition(row, col);
  }

  @Override
  public Rectangle2D getBoundingBox() {
    double w = 0;
    double h = 0;
    for(int i = 0; i < matrix.size(); ++i) {
      w += matrix.getWidth(i);
      h += matrix.getHeight(i);
    }
    if(w <= 0 || h <= 0) return null;
    return new Rectangle2D.Double(0, 0, w, h);
  }

}
