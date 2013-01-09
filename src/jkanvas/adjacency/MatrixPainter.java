package jkanvas.adjacency;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.painter.PainterAdapter;

public abstract class MatrixPainter<T> extends PainterAdapter {

  private final AdjacencyMatrix<T> matrix;
  private final CellRealizer<T> cellDrawer;

  public MatrixPainter(final AdjacencyMatrix<T> matrix, final CellRealizer<T> cellColor) {
    this.matrix = Objects.requireNonNull(matrix);
    this.cellDrawer = Objects.requireNonNull(cellColor);
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    double y = 0;
    for(int row = 0; row < matrix.size(); ++row) {
      double x = 0;
      final double h = matrix.getHeight(row);
      for(int col = 0; col < matrix.size(); ++col) {
        final double w = matrix.getWidth(col);
        final Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
        final boolean sel = isSelected(row, col);
        final Graphics2D g = (Graphics2D) gfx.create();
        cellDrawer.drawCell(g, ctx, rect, matrix, row, col, sel);
        g.dispose();
        x += w;
      }
      y += h;
    }
  }

  protected abstract boolean isSelected(int row, int col);

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
    return new Rectangle2D.Double(0, 0, w, h);
  }

}
