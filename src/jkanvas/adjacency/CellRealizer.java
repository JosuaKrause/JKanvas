package jkanvas.adjacency;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

public interface CellRealizer<T> {

  void drawCell(Graphics2D g, KanvasContext ctx, Rectangle2D rect,
      AdjacencyMatrix<T> matrix, int row, int col, boolean isSelected,
      boolean hasSelection);

}
