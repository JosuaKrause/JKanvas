package jkanvas.adjacency;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.painter.PainterAdapter;

/**
 * Paints an adjacency matrix.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The content type.
 */
@Deprecated
public abstract class MatrixPainter<T> extends PainterAdapter {

  /** The refresh manager. */
  private final RefreshManager manager;
  /** The matrix. */
  private AdjacencyMatrix<T> matrix;
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
  @SuppressWarnings("deprecation")
  public MatrixPainter(final AdjacencyMatrix<T> matrix, final CellRealizer<T> cellDrawer,
      final RefreshManager manager) {
    this.manager = Objects.requireNonNull(manager);
    this.cellDrawer = Objects.requireNonNull(cellDrawer);
    setMatrix(matrix);
  }

  /**
   * Setter.
   * 
   * @param m The new matrix.
   */
  @SuppressWarnings("deprecation")
  public void setMatrix(final AdjacencyMatrix<T> m) {
    Objects.requireNonNull(m);
    if(matrix != null && matrix.supportsAutoRefreshing()) {
      matrix.setRefreshManager(null);
    }
    if(m.supportsAutoRefreshing()) {
      m.setRefreshManager(manager);
    }
    matrix = m;
    manager.refreshAll();
  }

  /**
   * Getter.
   * 
   * @return The matrix.
   */
  @SuppressWarnings("deprecation")
  public AdjacencyMatrix<T> getMatrix() {
    return matrix;
  }

  /**
   * Setter.
   * 
   * @param cellDrawer The cell realizer.
   */
  @SuppressWarnings("deprecation")
  public void setCellRealizer(final CellRealizer<T> cellDrawer) {
    this.cellDrawer = cellDrawer;
    manager.refreshAll();
  }

  /**
   * Getter.
   * 
   * @return The cell realizer.
   */
  @SuppressWarnings("deprecation")
  public CellRealizer<T> getCellDrawer() {
    return cellDrawer;
  }

  @Override
  @SuppressWarnings("deprecation")
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
  @SuppressWarnings("deprecation")
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
  @SuppressWarnings("deprecation")
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
  @SuppressWarnings("deprecation")
  public RefreshManager getRefreshManager() {
    return manager;
  }

}
