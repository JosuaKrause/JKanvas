package jkanvas.matrix;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.painter.Renderpass;
import jkanvas.painter.pod.AbstractTitleRenderpass.Alignment;
import jkanvas.painter.pod.AbstractTitleRenderpass.Position;
import jkanvas.painter.pod.BorderRenderpass;
import jkanvas.painter.pod.Renderpod;
import jkanvas.util.StringDrawer.Orientation;

/**
 * Paints a matrix.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The matrix type.
 */
public class MatrixRenderpass<T extends Matrix<?>> extends Renderpass {

  /** The refresh manager. */
  private RefreshManager manager;
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
    if(matrix != null) {
      matrix.setRefreshManager(null);
    }
    m.setRefreshManager(manager);
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
    final Rectangle2D view = ctx.getVisibleCanvas();
    final Rectangle2D rect = new Rectangle2D.Double();
    final boolean hasSelection = hasSelection();
    double y = 0;
    outer: for(int row = 0; row < matrix.rows(); ++row) {
      double x = 0;
      final double h = matrix.getHeight(row);
      inner: for(int col = 0; col < matrix.cols(); ++col) {
        final double w = matrix.getWidth(col);
        rect.setFrame(x, y, w, h);
        x += w;
        if(!view.intersects(rect)) {
          continue inner;
        }
        final boolean sel = isSelected(row, col);
        final Graphics2D g = (Graphics2D) gfx.create();
        cellDrawer.drawCell(g, ctx, rect, matrix, row, col, sel, hasSelection);
        g.dispose();
        if(x > view.getMaxX()) {
          // we will not encounter any more visible rectangles on this line
          break inner;
        }
      }
      y += h;
      if(y > view.getMaxY()) {
        // we will not encounter any more visible rectangles
        break outer;
      }
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
    for(int i = 0; i < matrix.cols(); ++i) {
      if(w > pos.getX()) {
        break;
      }
      w += matrix.getWidth(i);
      ++col;
    }
    int row = -1;
    double h = 0;
    for(int i = 0; i < matrix.rows(); ++i) {
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
  public void getBoundingBox(final RectangularShape bbox) {
    double h = 0;
    for(int i = 0; i < matrix.rows(); ++i) {
      h += matrix.getHeight(i);
    }
    double w = 0;
    for(int i = 0; i < matrix.cols(); ++i) {
      w += matrix.getWidth(i);
    }
    bbox.setFrame(0, 0, w, h);
  }

  /**
   * Setter.
   * 
   * @param manager The refresh manager.
   */
  protected void setRefreshManager(final RefreshManager manager) {
    this.manager = Objects.requireNonNull(manager);
    matrix.setRefreshManager(this.manager);
  }

  /**
   * Getter.
   * 
   * @return The refresh manager.
   */
  public RefreshManager getRefreshManager() {
    return manager;
  }

  /**
   * Creates a {@link MatrixRenderpass} with titles
   * 
   * @param <T> The matrix type.
   * @param rp The render pass to wrap.
   * @param textHeight The title text height.
   * @param space The space between title and matrix.
   * @return The render pod.
   */
  public static final <T extends Matrix<?>> Renderpod<MatrixRenderpass<T>>
      createTitledMatrixRenderpass(final MatrixRenderpass<T> rp,
          final double textHeight, final double space) {
    return createTitledMatrixRenderpass(rp, textHeight, space,
        Orientation.VERTICAL, Alignment.LEFT,
        Orientation.HORIZONTAL, Alignment.RIGHT);
  }

  /**
   * Creates a titled matrix render pass.
   * 
   * @param <T> The matrix type.
   * @param rp The render pass to wrap.
   * @param textHeight The title text height.
   * @param space The space between title and matrix.
   * @param upperO Upper title orientation.
   * @param upperA Upper title alignment.
   * @param sideO Side title orientation.
   * @param sideA Side title alignment.
   * @return The render pod.
   */
  public static final <T extends Matrix<?>> Renderpod<MatrixRenderpass<T>>
      createTitledMatrixRenderpass(final MatrixRenderpass<T> rp,
          final double textHeight, final double space,
          final Orientation upperO, final Alignment upperA,
          final Orientation sideO, final Alignment sideA) {
    final MatrixTitleRenderpass<MatrixRenderpass<T>> top =
        new MatrixTitleRenderpass<>(new BorderRenderpass<>(rp), textHeight, space);
    top.setPosition(Position.ABOVE);
    top.setOrientation(upperO);
    top.setAlignment(upperA);
    final MatrixTitleRenderpass<MatrixRenderpass<T>> left =
        new MatrixTitleRenderpass<>(top, textHeight, space);
    left.setPosition(Position.LEFT);
    left.setAlignment(sideA);
    left.setOrientation(sideO);
    return left;
  }

}
