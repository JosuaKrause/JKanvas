package jkanvas.matrix;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.painter.AbstractRenderpass;

/**
 * Paints a matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The matrix type.
 */
public class MatrixRenderpass<T extends QuadraticMatrix<?>> extends AbstractRenderpass {

  /** The refresh manager. */
  private final RefreshManager manager;
  /** The matrix. */
  private T matrix;
  /** The cell drawer. */
  private CellRealizer<T> cellDrawer;

  /**
   * Creates a matrix painter.
   * 
   * @param matrix The matrix.
   * @param cellDrawer The cell realizer.
   * @param manager The refresh manager that is notified each time something
   *          changes.
   */
  public MatrixRenderpass(final T matrix,
      final CellRealizer<T> cellDrawer, final RefreshManager manager) {
    this.manager = Objects.requireNonNull(manager);
    this.cellDrawer = Objects.requireNonNull(cellDrawer);
    setMatrix(matrix);
  }

  /**
   * Setter.
   * 
   * @param m The new matrix.
   */
  public void setMatrix(final T m) {
    Objects.requireNonNull(m);
    if(matrix != null && matrix instanceof MutableQuadraticMatrix) {
      ((MutableQuadraticMatrix<?>) matrix).setRefreshManager(null);
    }
    if(m instanceof MutableQuadraticMatrix) {
      ((MutableQuadraticMatrix<?>) m).setRefreshManager(manager);
    }
    matrix = m;
    manager.refreshAll();
  }

  /**
   * Getter.
   * 
   * @return The matrix.
   */
  public T getMatrix() {
    return matrix;
  }

  /**
   * Setter.
   * 
   * @param cellDrawer The cell realizer.
   */
  public void setCellRealizer(final CellRealizer<T> cellDrawer) {
    this.cellDrawer = cellDrawer;
    manager.refreshAll();
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
  protected boolean isSelected(@SuppressWarnings("unused") final int row,
      @SuppressWarnings("unused") final int col) {
    return false;
  }

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
  public void setBoundingBox(final Rectangle2D bbox) {
    throw new UnsupportedOperationException();
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

  /**
   * Getter.
   * 
   * @return The refresh manager.
   */
  public RefreshManager getRefreshManager() {
    return manager;
  }

}
